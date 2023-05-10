package flicka;

import org.javalite.activejdbc.*;
import org.jdesktop.application.*;

import core.*;



public class MultipleSimulationTask extends Task<Void, Void> {
	private Selector selector;
	private Model[] models;
	public MultipleSimulationTask(Model[] mods) {
		super(Alesia.getInstance());
		this.selector = new Selector(0, null);
		this.models = mods;
		// sel.horseSample = hs;
		selector.writePdistribution = false;
	}
	@Override
	protected Void doInBackground() throws Exception {
		Alesia.getInstance().openDB("flicka");
		selector.clearTables();
		// int max = models.length;
		int step = 0;
		for (Model rcd : models) {
			selector.date = rcd.getDate("redate");
			selector.race = rcd.getInteger("rerace");
			setProgress(++step);
			setMessage(selector.date + " " + selector.race);
			selector.select();
		}
		selector.printStats();
		Alesia.getMainPanel().signalFreshgen(StatisticsList.class);
		return null;
	}
}