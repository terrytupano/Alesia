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
	private int bankRollPause;

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
		// getTaskService().shutdown();
	}

	@Override
	protected Void doInBackground() throws Exception {
		Alesia.openDB();
		long done = 0;
		tasks.forEach(t -> Alesia.getTaskManager().getTaskService().execute(t));
		while (done < tasks.size()) {
			Thread.sleep(1000);

			// check all done tasks
			done = tasks.stream().filter(t -> t.isCancelled() || t.isDone()).count();

			long active = tasks.size() - done;

			// check bankRollPause
			if (bankRollPause >= active) {
				performSummarization();
				tasks.forEach(t -> ((Table) t).resetBankRollCounter());
			}

		}
		// sumarize the rest
		performSummarization();
		return null;
	}

	private void performSummarization() {
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
			summe.set("hands", 0);
			summe.set("wins", 0);
			for (SimulationResult sameVariable : sameVariables) {
				int hands = summe.getInteger("hands") + sameVariable.getInteger("hands");
				summe.set("hands", hands);

				int wins = summe.getInteger("wins") + sameVariable.getInteger("wins");
				summe.set("wins", wins);

			}
			SimulationResult.delete("variables = ?", variables);
			processed.add(variables);
			summe.save();
		}
		bankRollPause = 0;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String propertyName = evt.getPropertyName();

		if (Table.BANKROLL_PAUSE.equals(propertyName)) {
			bankRollPause++;
		}
	}
}
