package core;

import java.awt.*;
import java.util.*;
import java.util.List;

import org.jdesktop.application.*;

import com.alee.extended.layout.*;
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
	private WebPanel taskPanel;

	public TaskGroup() {
		super(Alesia.getInstance());
		this.tables = new ArrayList<>();
		this.taskPanel= new WebPanel(new VerticalFlowLayout(VerticalFlowLayout.MIDDLE, 0, 0, true, false));
//		vertical.add(titleLabel);
//		vertical.add(descriptionLabel);		
		setInputBlocker(new TaskDialog(this));
	}

	public void addTable(Table table) {
		tables.add(table);
		taskPanel.add(table.getTaskMonitor().getTaskPanel());
		Alesia.getTaskManager().getTaskService().execute(table);
	}

	public WebPanel getTaskPanel() {
		return taskPanel;
	}

	public void pause(boolean pause) {
		tables.forEach(t -> t.pause(pause));
	}

	@Override
	protected void cancelled() {
		tables.forEach(t -> t.cancel(false));
	}

	@Override
	protected Void doInBackground() throws Exception {
		boolean allDone = false;
		while (!allDone) {
			Thread.sleep(1000);
			for (Table table : tables) {
				allDone = table.isCancelled() || table.isDone() ? true : allDone;
			}
		}
		return null;
	}
}
