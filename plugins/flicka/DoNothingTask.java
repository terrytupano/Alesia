package plugins.flicka;

import org.jdesktop.application.*;

import core.*;

public class DoNothingTask extends Task<Void, Void> {
	
	DoNothingTask() {
		super(Alesia.getInstance());
		setUserCanCancel(true);
	}

	@Override
	protected Void doInBackground() throws InterruptedException {
		for (int i = 0; i < 50; i++) {
			setMessage("Working... [" + i + "]");
			Thread.sleep(150L);
			setProgress(i, 0, 49);
		}
		Thread.sleep(150L);
		return null;
	}

	@Override
	protected void succeeded(Void ignored) {
		setMessage("Done");
	}

	@Override
	protected void cancelled() {
		setMessage("Canceled");
	}

}
