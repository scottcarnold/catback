package org.xandercat.cat.back.engine.worklet;

import org.xandercat.cat.back.engine.BackupEngine;
import org.xandercat.swing.worker.SwingWorklet;

public abstract class BackupEngineWorklet<T> extends SwingWorklet<T, String> {

	protected long progress;
	
	public BackupEngineWorklet(BackupEngine backupEngine) {
		super(backupEngine);
	}

	public abstract String getTitle();
	
	public abstract void enableDryRun(String dryRunPrefix, Long speedFactor);
	
	protected void advanceProgress(long progressBy) {
		this.progress += progressBy;
		setProgress(this.progress);
	}
}
