package org.xandercat.cat.back.engine.worklet;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xandercat.cat.back.engine.BackupEngine;
import org.xandercat.cat.back.file.BackupFile;
import org.xandercat.swing.file.DirectorySizeCache;
import org.xandercat.swing.file.FilesSize;
import org.xandercat.swing.tree.CheckboxFileTree;
import org.xandercat.swing.util.PlatformTool;

public class LoadCurrentFiles extends LoadFilesWorklet<Set<BackupFile>> {

	private static final Logger log = LogManager.getLogger(LoadCurrentFiles.class);
	
	private List<File> files;
	private long totalFiles = -1;
	private boolean directorySizeLoadStarted;
	
	public LoadCurrentFiles(BackupEngine backupEngine, CheckboxFileTree excludedTree, List<File> files, File backupDirectory) {
		super(backupEngine, excludedTree, new HashSet<BackupFile>(), backupDirectory);
		this.files = files;
	}

	@Override
	public String getTitle() {
		return "Inspecting files";
	}
	
	@Override
	public void enableDryRun(String dryRunPrefix) {
		// no action required
	}

	@Override
	public List<BackupFile> execute() throws Exception {
		log.info("Loading current files list...");
		Set<BackupFile> currentFilesSet = backupFiles;	// start out using set for performance
		for (File file : files) {
			if (isCancelled()) {
				return new ArrayList<BackupFile>();
			}
			if (isExcluded(file)) {
				continue;
			}
			publish("Inspecting " + file.getName());
			if (file.isDirectory()) {
				loadFilesForDirectory(file, BackupFile.Type.SOURCE);
			} 
			loadFile(file, BackupFile.Type.SOURCE);
			File parent = file.getParentFile();
			while (parent != null) {
				BackupFile parentBackupFile = new BackupFile(parent, BackupFile.Type.SOURCE, backupDirectory);
				if (!currentFilesSet.contains(parentBackupFile)) {
					currentFilesSet.add(parentBackupFile);
				}
				parent = parent.getParentFile();
			}
		}
		if (PlatformTool.isMac()) {		// remove Mac root "/"
			log.debug("Platform is Mac; attempting to remove Mac root from current files list (list size " + currentFilesSet.size() + ")");
			BackupFile macRoot = new BackupFile(new File("/"), BackupFile.Type.SOURCE, backupDirectory);
			if (currentFilesSet.remove(macRoot)) {
				log.debug("Mac root removed");
			} else {
				log.warn("Unable to remove Mac root file; Mac root not found in list of current files");
			}
		}
		// transfer set of files into list so it can be sorted
		// we use an iterator to avoid having potentially two enormous collections at once
		List<BackupFile> currentFiles = new ArrayList<BackupFile>();
		for (Iterator<BackupFile> iter = currentFilesSet.iterator(); iter.hasNext();) {
			if (isCancelled()) {
				return new ArrayList<BackupFile>();
			}
			BackupFile backupFile = iter.next();
			currentFiles.add(backupFile);
			iter.remove();
		}
		currentFilesSet = null;
		Collections.sort(currentFiles);
		log.debug("Total current files/directories: " + currentFiles.size());		
		return currentFiles;
	}

	@Override
	public long getProgressMaximum() {
		if (totalFiles <= 0) {
			FilesSize filesSize = DirectorySizeCache.getInstance().getDirectorySize(backupDirectory);
			if (filesSize != null) {
				totalFiles = filesSize.getFiles() + filesSize.getDirectories();
			} else if (!directorySizeLoadStarted) {
				directorySizeLoadStarted = true;
				DirectorySizeCache.getInstance().loadDirectorySizeAnsync(backupDirectory);
			}
		} 
		return totalFiles;
	}
}
