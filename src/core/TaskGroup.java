package core;

import java.awt.*;
import java.beans.*;
import java.util.*;
import java.util.List;

import org.javalite.activejdbc.*;
import org.jdesktop.application.*;

import com.alee.laf.panel.*;

import datasource.*;
import hero.ozsoft.*;

/**
 * encapsulate a group of task that count as one
 * 
 * @author terry
 *
 */
public class TaskGroup extends Task<Void, Void> implements PropertyChangeListener {
	private List<Task<?, ?>> tasks;

	public static final String PARTIAL_RESULT_REQUEST = "PARTIAL_RESULT_REQUEST";

	/** cont the number of active task that need a partial result */
	protected int partialResult;

	public TaskGroup() {
		super(Alesia.getInstance());
		this.tasks = new ArrayList<>();
		setInputBlocker(new TaskDialog(this));
	}

	public void addTable(Task<?, ?> task) {
		tasks.add(task);
		task.addPropertyChangeListener(this);
	}

	public WebPanel getTaskPanel() {
		WebPanel panel = new WebPanel(new GridLayout(tasks.size(), 1));
		for (Task<?, ?> task : tasks) {
			TTaskMonitor monitor = new TTaskMonitor(task);
			panel.add(monitor.getTaskPanel());
		}
		return panel;
	}

	@Override
	protected void cancelled() {
		tasks.forEach(t -> t.cancel(true));
	}

	@Override
	protected Void doInBackground() throws Exception {
		Alesia.openDB();
		long done = 0;
		tasks.forEach(t -> Alesia.getTaskManager().getTaskService().execute(t));
		while (done < tasks.size()) {
			Thread.sleep(1000);

			// the number of done task
			done = tasks.stream().filter(t -> t.isCancelled() || t.isDone()).count();

			long active = tasks.size() - done;

			// if all active task requested partial result, perfomt it
			if (partialResult >= active) {
				processPartialResult();
				partialResult = 0;
				tasks.forEach(t -> ((Table) t).resetBankRollCounter());
			}

		}
		// sumarize pendig result. this happen whit the job finish but partial result
		// are no precesed
		processPartialResult();
		return null;
	}

	public static void processPartialResult() {
		// list of the unprocessed records
		List<String> processed = new ArrayList<>();
		LazyList<SimulationResult> sourceList = SimulationResult.find("tableId > ?", -1);
		for (SimulationResult source : sourceList) {
			String variables = source.getString("variables");
			if (processed.contains(variables))
				continue;
			// retrive all records with the same variable value (processed included)
			LazyList<SimulationResult> sameVariables = SimulationResult.find("variables = ?", variables);
			SimulationResult summe = new SimulationResult();
			summe.setInteger("simulation_parameter_id", 1);
			summe.setInteger("tableId", -1);
			summe.setString("trooper", "*");
			summe.setString("variables", variables);
			summe.setInteger("hands", 0);
			summe.setString("status", SimulationResult.ACTIVE);
			summe.setInteger("tables", 0);
			summe.setInteger("wins", 0);
			summe.setDouble("ratio", 0D);
			for (SimulationResult sameVariable : sameVariables) {
				int hands = summe.getInteger("hands") + sameVariable.getInteger("hands");
				summe.set("hands", hands);

				int tables = summe.getInteger("tables") + 1;
				summe.set("tables", tables);

				int wins = summe.getInteger("wins") + sameVariable.getInteger("wins");
				summe.set("wins", wins);
			}
			double ratio = summe.getDouble("wins") / summe.getDouble("hands");
			summe.set("ratio", ratio);
			SimulationResult.delete("variables = ?", variables);
			processed.add(variables);

			// check the tournament status of the player
			summe.save();
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String propertyName = evt.getPropertyName();

		if (PARTIAL_RESULT_REQUEST.equals(propertyName)) {
			partialResult++;
		}
	}
}
