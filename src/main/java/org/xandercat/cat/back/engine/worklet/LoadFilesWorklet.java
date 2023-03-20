package org.xandercat.cat.back.engine.worklet;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.xandercat.cat.back.engine.BackupEngine;
import org.xandercat.cat.back.file.BackupFile;
import org.xandercat.swing.tree.CheckboxFileTree;
import org.xandercat.swing.util.PlatformTool;

/**
 * Worklet for loading a list of BackupFile.
 * 
 * @author Scott Arnold
 */
public abstract class LoadFilesWorklet<T extends Collection<BackupFile>> extends BackupEngineWorklet<List<BackupFile>> {
	
	protected T backupFiles;
	protected File backupDirectory;
	protected CheckboxFileTree excludedTree;
	private volatile long filesSize;
	private volatile int filesCount;
	
	public LoadFilesWorklet(BackupEngine backupEngine, CheckboxFileTree excludedTree, T backupFiles, File backupDirectory) {
		super(backupEngine);
		this.backupFiles = backupFiles;
		this.excludedTree = excludedTree;
		this.backupDirectory = backupDirectory;
	}
	
	protected boolean isExcluded(File file) {
		return excludedTree.isChecked(file);
	}
	
	protected void loadFile(File file, BackupFile.Type type) {
		backupFiles.add(new BackupFile(file, type, backupDirectory));
		filesSize += file.length();
		if (!file.isDirectory()) {
			filesCount++;
		}
		publish("Inspecting " + file.getName());
		advanceProgress(1);
	}
	
	protected void loadFilesForDirectory(File directory, BackupFile.Type type) {
		if (!isCancelled()) {
			File[] dirFiles = directory.listFiles(PlatformTool.FILE_FILTER);
			if (dirFiles != null) {
				for (File file : dirFiles) {
					if (isExcluded(file)) {
						continue;
					}
					if (file.isDirectory()) {
						loadFilesForDirectory(file, type);
					} 
					loadFile(file, type);
				}
			}
		}
	}
	
	public long getFilesSize() {
		return filesSize;
	}
	
	public int getFilesCount() {
		return filesCount;
	}
}
