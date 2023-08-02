package core;

import java.awt.*;
import java.util.*;
import java.util.List;

import org.jdesktop.application.*;

import com.alee.laf.panel.*;

/**
 * encapsulate a group of task that count as one
 * 
 * @author terry
 *
 */
public class TaskGroup extends Task<Void, Void> {
	private List<Task<?, ?>> tasks;

	public TaskGroup() {
		super(Alesia.getInstance());
		this.tasks = new ArrayList<>();
		setInputBlocker(new TaskDialog(this));
	}

	public void addTable(Task<?, ?> task) {
		tasks.add(task);
//		Alesia.getTaskManager().getTaskService().execute(table);
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
//		getTaskService().shutdown();
	}

	@Override
	protected Void doInBackground() throws Exception {
		long done = 0;
		tasks.forEach(t -> Alesia.getTaskManager().getTaskService().execute(t));
		while (done < tasks.size()) {
			Thread.sleep(1000);
			done = tasks.stream().filter(t -> t.isCancelled() || t.isDone()).count();
		}
		return null;
	}
}
