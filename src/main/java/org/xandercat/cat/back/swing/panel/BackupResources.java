package org.xandercat.cat.back.swing.panel;

import org.xandercat.cat.back.BackupSizeCalculator;
import org.xandercat.cat.back.CatBackup15;
import org.xandercat.cat.back.engine.BackupStats;
import org.xandercat.swing.file.FileManager;
import org.xandercat.swing.tree.CheckboxFileTree;
import org.xandercat.swing.zenput.processor.InputProcessor;

public class BackupResources {

	private FileManager<CatBackup15> fileManager;
	private InputProcessor inputProcessor;
	private CheckboxFileTree includedTree;
	private CheckboxFileTree excludedTree;
	private BackupSizeCalculator backupSizeCalculator;
	private BackupStats backupStats;
	
	public FileManager<CatBackup15> getFileManager() {
		return fileManager;
	}
	public void setFileManager(FileManager<CatBackup15> fileManager) {
		this.fileManager = fileManager;
	}
	public InputProcessor getInputProcessor() {
		return inputProcessor;
	}
	public void setInputProcessor(InputProcessor inputProcessor) {
		this.inputProcessor = inputProcessor;
	}
	public CheckboxFileTree getIncludedTree() {
		return includedTree;
	}
	public void setIncludedTree(CheckboxFileTree includedTree) {
		this.includedTree = includedTree;
	}
	public CheckboxFileTree getExcludedTree() {
		return excludedTree;
	}
	public void setExcludedTree(CheckboxFileTree excludedTree) {
		this.excludedTree = excludedTree;
	}
	public BackupSizeCalculator getBackupSizeCalculator() {
		return backupSizeCalculator;
	}
	public void setBackupSizeCalculator(BackupSizeCalculator backupSizeCalculator) {
		this.backupSizeCalculator = backupSizeCalculator;
	}
	public BackupStats getBackupStats() {
		return backupStats;
	}
	public void setBackupStats(BackupStats backupStats) {
		this.backupStats = backupStats;
	}
}
