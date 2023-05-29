package core;

import java.awt.*;
import java.util.*;
import java.util.List;

import org.jdesktop.application.*;

import com.alee.laf.panel.*;

import hero.ozsoft.*;

/**
 * encapsulate a group of task that count as one
 * 
 * @author terry
 *
 */
public class TaskGroup extends Task<Void, Void> {
	private List<Table> tables;

	public TaskGroup() {
		super(Alesia.getInstance());
		this.tables = new ArrayList<>();
		setInputBlocker(new TaskDialog(this));
	}

	public void addTable(Table table) {
		tables.add(table);
//		Alesia.getTaskManager().getTaskService().execute(table);
	}

	public WebPanel getTaskPanel() {
		WebPanel panel = new WebPanel(new GridLayout(tables.size(), 1));
		tables.forEach(t -> panel.add(t.getTaskMonitor().getTaskPanel()));
		return panel;
	}

	public void pause(boolean pause) {
		tables.forEach(t -> t.pause(Table.PAUSE_TASK));
	}

	@Override
	protected void cancelled() {
		tables.forEach(t -> t.cancel(true));
//		getTaskService().shutdown();
	}

	@Override
	protected Void doInBackground() throws Exception {
		long done = 0;
		tables.forEach(t -> Alesia.getTaskManager().getTaskService().execute(t));
		while (done < tables.size()) {
			Thread.sleep(1000);
			done = tables.stream().filter(t -> t.isCancelled() || t.isDone()).count();
		}
		return null;
	}
}
