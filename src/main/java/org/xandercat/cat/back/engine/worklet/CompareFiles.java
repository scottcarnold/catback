package org.xandercat.cat.back.engine.worklet;

import java.awt.Frame;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xandercat.cat.back.engine.BackupEngine;
import org.xandercat.cat.back.file.BackupFile;
import org.xandercat.cat.back.swing.dialog.MoveCopyDialog;
import org.xandercat.swing.file.icon.FileIconCache;
import org.xandercat.swing.table.FileData;

public class CompareFiles extends BackupEngineWorklet<Boolean> {

	private static final Logger log = LogManager.getLogger(CompareFiles.class);
	
	private List<BackupFile> currentFiles;
	private List<BackupFile> previousFiles;
	private long progressMaximum;
	private long bytesToCopy;
	private long bytesToMove;
	private boolean showMoveCopyDialog;
	private volatile Boolean proceedToNextStep = Boolean.TRUE;
	private FileIconCache fileIconCache;
	private Frame parent;
	private List<File> filesToCopy = new ArrayList<File>();
	private List<BackupFile> filesToMove = new ArrayList<BackupFile>();
	
	public CompareFiles(BackupEngine backupEngine, List<BackupFile> currentFiles, List<BackupFile> previousFiles) {
		super(backupEngine);
		this.currentFiles = currentFiles;
		this.previousFiles = previousFiles;
		this.progressMaximum = currentFiles.size() + previousFiles.size();
	}

	@Override
	public String getTitle() {
		return "Comparing files";
	}

	public void enableShowMoveCopyDialog(Frame parent, FileIconCache fileIconCache) {
		this.showMoveCopyDialog = true;
		this.parent = parent;
		this.fileIconCache = fileIconCache;
	}
	
	@Override
	public Boolean execute() throws Exception {
		log.info("Comparing previous list to current list...");
		Iterator<BackupFile> previousFilesIter = previousFiles.iterator();
		Iterator<BackupFile> currentFilesIter = currentFiles.iterator();
		BackupFile previousFile = previousFilesIter.hasNext()? previousFilesIter.next() : null;
		BackupFile currentFile = currentFilesIter.hasNext()? currentFilesIter.next() : null;
		final List<FileData> moveFileData = showMoveCopyDialog? new ArrayList<FileData>() : null;
		final List<FileData> copyFileData = showMoveCopyDialog? new ArrayList<FileData>() : null;
		while (!isCancelled() && (previousFile != null || currentFile != null)) {
			if (currentFile != null) {
				publish("Comparing " + currentFile.getFile().getName());
			}
			//log.debug("------- compare interation -------");
			//log.debug("Previous file is " + ((previousFile == null)? "null" : previousFile.toString()));
			//log.debug("Current file is  " + ((currentFile == null)? "null" : currentFile.toString()));
			if (previousFile == null || previousFile.compareTo(currentFile) > 0) {
				//log.debug("Previous is greater than current (or previous is null), moving current to copy list");
				filesToCopy.add(currentFile.getFile());
				bytesToCopy += currentFile.getFile().length();
				if (showMoveCopyDialog) {
					copyFileData.add(new FileData(currentFile.getFile()));
				}
				currentFile = currentFilesIter.hasNext()? currentFilesIter.next() : null;
				advanceProgress(1);
			} else if (currentFile == null || previousFile.compareTo(currentFile) < 0) {
				//log.debug("Previous is less than current (or current is null), moving previous to generation list");
				filesToMove.add(previousFile);
				bytesToMove += previousFile.getFile().length();
				if (showMoveCopyDialog) {
					moveFileData.add(new FileData(previousFile.getFile()));
				}
				previousFile = previousFilesIter.hasNext()? previousFilesIter.next() : null; 
				advanceProgress(1);
			} else if (!previousFile.isFileEquivalent(currentFile)) {
				//log.debug("Previous == current; file is changed, moving previous to generation list and current to copy list");
				filesToMove.add(previousFile);
				filesToCopy.add(currentFile.getFile());
				bytesToCopy += currentFile.getFile().length();
				bytesToMove += previousFile.getFile().length();
				if (showMoveCopyDialog) {
					copyFileData.add(new FileData(currentFile.getFile()));
					moveFileData.add(new FileData(previousFile.getFile()));
				}
				previousFile = previousFilesIter.hasNext()? previousFilesIter.next() : null; 
				currentFile = currentFilesIter.hasNext()? currentFilesIter.next() : null;	
				advanceProgress(2);
			} else {
				//log.debug("Previous == current; file is unchanged, file will be skipped");
				previousFile = previousFilesIter.hasNext()? previousFilesIter.next() : null; 
				currentFile = currentFilesIter.hasNext()? currentFilesIter.next() : null;
				advanceProgress(2);
			}
		}
		if (showMoveCopyDialog && !isCancelled()) {
			publish("Waiting to proceed...");
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					MoveCopyDialog moveCopyDialog = new MoveCopyDialog(parent, fileIconCache);
					moveCopyDialog.setFilesToCopy(copyFileData);
					moveCopyDialog.setFilesToMove(moveFileData);
					int result = moveCopyDialog.showDialog();
					if (result == MoveCopyDialog.CANCEL_OPTION) {
						log.info("Backup cancelled by user.");
						proceedToNextStep = Boolean.FALSE;
					}					
				}
			});		
		}
		return proceedToNextStep;
	}

	@Override
	public long getProgressMaximum() {
		return progressMaximum;
	}

	public long getBytesToCopy() {
		return bytesToCopy;
	}

	public long getBytesToMove() {
		return bytesToMove;
	}

	public List<File> getFilesToCopy() {
		return filesToCopy;
	}

	public List<BackupFile> getFilesToMove() {
		return filesToMove;
	}
}
