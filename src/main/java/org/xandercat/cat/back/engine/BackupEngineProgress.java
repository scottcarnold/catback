package org.xandercat.cat.back.engine;

public class BackupEngineProgress {

	private boolean progressUpdate;
	private long progress;
	private long progressMaximum;
	private String heading;
	private String message;
	
	public BackupEngineProgress(String heading, String message) {
		this.heading = heading;
		this.message = message;
	}
	
	public BackupEngineProgress(long progress, long progressMaximum) {
		this.progress = progress;
		this.progressMaximum = progressMaximum;
		this.progressUpdate = true;
	}

	public boolean isProgressUpdate() {
		return progressUpdate;
	}

	public long getProgress() {
		return progress;
	}

	public long getProgressMaximum() {
		return progressMaximum;
	}

	public String getHeading() {
		return heading;
	}

	public String getMessage() {
		return message;
	}
}
