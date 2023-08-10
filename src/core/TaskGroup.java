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
		long done = 0;
		tasks.forEach(t -> Alesia.getTaskManager().getTaskService().execute(t));
		while (done < tasks.size()) {
			Thread.sleep(1000);

			// check bankRollPause
			if (bankRollPause >= tasks.size())
				performSummarization();

			// check all done tasks
			done = tasks.stream().filter(t -> t.isCancelled() || t.isDone()).count();
		}
		// sumarize the rest
		performSummarization();
		return null;
	}

	private void performSummarization() {
		// list of the unprocessed records
		LazyList<SimulationResult> sourceList = SimulationResult.find("tableId != -1");
		for (SimulationResult source : sourceList) {
			// retrive all records with the same variable value (processed included)
			LazyList<SimulationResult> results2 = SimulationResult.find("variables = ?", source.getString("variables"));
			// for only 1 element only mark it. form more as 1 elements, perform sum
			if (!results2.isEmpty()) {
				boolean mark = results2.size() == 1;
				SimulationResult summe = new SimulationResult();
				summe.copyFrom(source);
				for (SimulationResult result2 : results2) {
					summe.setInteger("simulation_parameter_id", 1);
					summe.setInteger("tableId", -1);
					summe.setString("trooper", "*");

					int hand = mark ? summe.getInteger("hands")
							: summe.getInteger("hands") + result2.getInteger("hands");
					summe.set("hands", hand);

					int wins = mark ? summe.getInteger("wins") : summe.getInteger("wins") + result2.getInteger("wins");
					summe.set("wins", wins);

					int ratio = mark ? summe.getInteger("ratio")
							: summe.getInteger("ratio") + result2.getInteger("ratio");
					summe.set("ratio", ratio);
				}
				summe.save();
				SimulationResult.de
				source.delete();
			}
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String propertyName = evt.getPropertyName();

		if (Table.BANKROLL_PAUSE.equals(propertyName)) {
			bankRollPause++;
		}
	}
}
