package org.xandercat.cat.back.engine.worklet;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xandercat.cat.back.engine.BackupEngine;
import org.xandercat.cat.back.file.BackupPathGenerator;
import org.xandercat.swing.file.FileCopier;
import org.xandercat.swing.file.FileCopyListener;
import org.xandercat.swing.file.FileCopyProgressListener;
import org.xandercat.swing.file.icon.FileIconCache;
import org.xandercat.swing.frame.FileCopyProcessFrame;

public class CopyFiles extends BackupEngineWorklet<Boolean> implements FileCopyListener, FileCopyProgressListener {

	private static final Logger log = LogManager.getLogger(CopyFiles.class);
	
	private List<File> filesToCopy;
	private long bytesToCopy;
	private long lastBytesCopied;
	private File backupDirectory;
	private FileIconCache fileIconCache;
	private boolean alwaysLeaveCopyWindowOpen;
	private CountDownLatch countDownLatch;
	private volatile int filesCopied;
	private volatile long filesSize;
	private FileCopyProcessFrame copyFrame;
	private boolean resolutionRequired;
	private boolean copyCancelled;
	private boolean copyComplete;
	private int errorsUntilHalt;
	private boolean dryRun;
	private Long speedFactor;
	private String dryRunPrefix = "";
	
	public CopyFiles(BackupEngine backupEngine, List<File> filesToCopy, long bytesToCopy, 
			File backupDirectory, FileIconCache fileIconCache, int errorsUntilHalt) {
		super(backupEngine);
		this.filesToCopy = filesToCopy;
		this.bytesToCopy = bytesToCopy;
		this.backupDirectory = backupDirectory;
		this.fileIconCache = fileIconCache;
		this.errorsUntilHalt = errorsUntilHalt;
	}

	public void enableDryRun(String dryRunPrefix, Long speedFactor) {
		this.dryRun = true;
		this.dryRunPrefix = dryRunPrefix;
		this.speedFactor = speedFactor;
	}
	
	@Override
	public String getTitle() {
		return "Backing up files";
	}

	public void setAlwaysLeaveCopyWindowOpen(boolean alwaysLeaveCopyWindowOpen) {
		this.alwaysLeaveCopyWindowOpen = alwaysLeaveCopyWindowOpen;
	}
	
	@Override
	public Boolean execute() throws Exception {
		log.debug(dryRunPrefix + "Backing up " + filesToCopy.size() + " files to main backup directory...");
		BackupPathGenerator pathGenerator = new BackupPathGenerator(backupDirectory);
		this.copyFrame = new FileCopyProcessFrame(
				filesToCopy, fileIconCache, pathGenerator, true, !alwaysLeaveCopyWindowOpen, errorsUntilHalt);
		this.copyFrame.setLogCopiedFilesWithoutAbsolutePaths(true); // cuts down on amount of text being fed to scroll pane
		this.copyFrame.addFileCopyListener(this);
		this.copyFrame.addFileCopyProgressListener(this);
		if (dryRun) {
			if (speedFactor == null) {
				this.copyFrame.enableTestMode();
			} else {
				this.copyFrame.enableTestMode(speedFactor.longValue());
			}
		}
		this.countDownLatch = new CountDownLatch(1); 
		this.copyFrame.copy();
		try {
			this.countDownLatch.await();
		} catch (InterruptedException ie) {
			// nothing to do here
		}
		Boolean returnVal = Boolean.valueOf(copyFrame.isHaltedDueToErrors());
		this.filesToCopy = null;
		this.copyFrame = null;
		return returnVal;
	}

	@Override
	public long getProgressMaximum() {
		return bytesToCopy;
	}

	public int getFilesCopied() {
		return filesCopied;
	}
	
	public long getFilesSize() {
		return filesSize;
	}
	
	public boolean isResolutionRequired() {
		return resolutionRequired;
	}

	public boolean isCopyCancelled() {
		return copyCancelled;
	}

	public boolean isCopyComplete() {
		return copyComplete;
	}
	
	public boolean cancelCopy() {
		if (copyFrame != null) {
			this.copyCancelled = copyFrame.cancelCopyInProgress();
		}
		return this.copyCancelled;
	}
	
	public void fileCopying(File from, File to, long bytesCopied, boolean copyComplete) {
		advanceProgress(bytesCopied - lastBytesCopied);
		lastBytesCopied = copyComplete? 0 : bytesCopied;
	}

	public void copyComplete(boolean resolutionRequired, boolean copyCancelled) {
		this.resolutionRequired = resolutionRequired;
		this.copyCancelled = copyCancelled;
		this.copyComplete = true;
		this.countDownLatch.countDown();	
	}

	public void fileCopied(File from, File to, boolean isDirectory, FileCopier.CopyResult result) {
		if (!isDirectory) {
			filesCopied++;	
		}
		filesSize += from.length();
	}

	public void fileCopying(File from, File to, boolean isDirectory) {
		publish(dryRunPrefix + "Copying " + from.getName());
	}
}
