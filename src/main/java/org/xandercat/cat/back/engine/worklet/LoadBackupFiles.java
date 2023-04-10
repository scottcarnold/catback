package org.xandercat.cat.back.engine.worklet;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xandercat.cat.back.engine.BackupEngine;
import org.xandercat.cat.back.engine.BackupStat;
import org.xandercat.cat.back.file.BackupFile;
import org.xandercat.swing.tree.CheckboxFileTree;

public class LoadBackupFiles extends LoadFilesWorklet<List<BackupFile>> {

	private static final Logger log = LogManager.getLogger(LoadBackupFiles.class);
	
	private long totalFiles;
	
	public LoadBackupFiles(BackupEngine backupEngine, CheckboxFileTree excludedTree, File backupDirectory, BackupStat statLastBackup) {
		super(backupEngine, excludedTree, new ArrayList<BackupFile>(), backupDirectory);
		this.totalFiles = (statLastBackup == null)? 0 : statLastBackup.getTotalFiles();
	}

	@Override
	public String getTitle() {
		return "Inspecting last backup";
	}

	@Override
	public void enableDryRun(String dryRunPrefix, Long speedFactor) {
		// no action required
	}

	@Override
	public List<BackupFile> execute() throws Exception {
		log.debug("Loading previous files list (est. " + totalFiles + ")...");
		List<BackupFile> previousFiles = backupFiles;
		loadFilesForDirectory(backupDirectory, BackupFile.Type.DESTINATION);
		if (isCancelled()) {
			return new ArrayList<BackupFile>();
		}
		Collections.sort(previousFiles);
		log.debug("Total previous files: " + previousFiles.size());
		return previousFiles;
	}

	@Override
	public long getProgressMaximum() {
		return -1;
	}
}