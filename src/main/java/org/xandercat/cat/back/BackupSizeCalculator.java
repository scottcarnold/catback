package org.xandercat.cat.back;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.SwingUtilities;

import org.xandercat.swing.file.FilesSize;
import org.xandercat.swing.file.FilesSizeCalculator;
import org.xandercat.swing.file.FilesSizeHandler;
import org.xandercat.swing.tree.CheckboxFileTree;
import org.xandercat.swing.tree.CheckboxFileTreeListener;
import org.xandercat.swing.worker.SwingWorkerUtil;

/**
 * Class for calculating the size in bytes of all files included in an "included"
 * CheckboxFileTree, less the size of any files excluded in an "excluded" CheckboxFileTree.
 * 
 * @author Scott Arnold
 */
public class BackupSizeCalculator implements CheckboxFileTreeListener, FilesSizeHandler {

	private CheckboxFileTree includedTree;
	private CheckboxFileTree excludedTree;
	private FilesSizeCalculator includedCalculator;
	private FilesSizeCalculator excludedCalculator;
	private FilesSize includedSize = new FilesSize();
	private FilesSize excludedSize = new FilesSize();
	private FilesSize totalSize;
	private volatile boolean calculating = true;
	private List<BackupSizeListener> backupSizeListeners = new ArrayList<BackupSizeListener>();
	private ReentrantLock sizeLock = new ReentrantLock();
	
	public BackupSizeCalculator(CheckboxFileTree includedTree, CheckboxFileTree excludedTree) {
		this.includedTree = includedTree;
		this.excludedTree = excludedTree;
		includedTree.addCheckboxFileTreeListener(this);
		excludedTree.addCheckboxFileTreeListener(this);
		
		// fire off first calculation
		filesChanged(this.includedTree, this.includedTree.getCheckedFiles(), new ArrayList<File>());
	}
	
	public void addBackupSizeListener(BackupSizeListener listener) {
		this.backupSizeListeners.add(listener);
	}
	
	public void removeBackupSizeListener(BackupSizeListener listener) {
		this.backupSizeListeners.remove(listener);
	}

	/**
	 * Cancels any currently running size calculation.
	 */
	public void cancel() {
		this.sizeLock.lock();
		if (includedCalculator != null && !includedCalculator.isDone()) {
			includedCalculator.cancel(true);
		}
		if (excludedCalculator != null && !excludedCalculator.isDone()) {
			excludedCalculator.cancel(true);
		}
		this.totalSize = null;
		this.excludedSize = null;
		this.sizeLock.unlock();
	}
	
	/**
	 * Returns whether or not the size is currently being calculated.
	 * 
	 * @return 		whether or not the size is currently being calculated
	 */
	public boolean isCalculating() {
		return this.calculating;
	}
	
	/**
	 * Returns the total size of the backup, or null if the total size is currently
	 * being calculated.
	 * 
	 * @return		total size of backup, or null if not computed yet
	 */
	public FilesSize getTotalSize() {
		FilesSize totalSize = null;
		this.sizeLock.lock();
		try {
			if (!this.calculating && this.totalSize != null) {
				totalSize = this.totalSize.clone();
			}
		} finally {
			this.sizeLock.unlock();
		}
		return totalSize;
	}
	
	/**
	 * Returns the size of files excluded from backup, or null if being calculated.
	 * 
	 * @return		size of files excluded from backup, or null if not computed yet
	 */
	public FilesSize getExcludedSize() {
		FilesSize xSize = null;
		this.sizeLock.lock();
		try {
			if (!this.calculating && this.excludedSize != null) {
				xSize = this.excludedSize.clone();
			}
		} finally {
			this.sizeLock.unlock();
		}
		return xSize;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void filesChanged(CheckboxFileTree tree, List<File> addedFiles, List<File> removedFiles) {
		boolean runExcludedCalc = false;
		boolean runCalc = false;
		this.sizeLock.lock();
		try {
			if (tree == this.includedTree) {
				if (includedCalculator == null || includedCalculator.isDone()) {
					includedCalculator = new FilesSizeCalculator(includedSize, addedFiles, removedFiles, this);
				} else {
					includedCalculator.cancel(true);
					includedCalculator = new FilesSizeCalculator(includedCalculator, addedFiles, removedFiles);
				}
				runCalc = true;
				runExcludedCalc = isOverlap(this.excludedTree, addedFiles, removedFiles);
			} else if (tree == this.excludedTree) {
				runExcludedCalc = isOverlap(this.includedTree, addedFiles, removedFiles);
			}
			if (runExcludedCalc) {
				List<File> excludedFiles = new ArrayList<File>();
				for (File file : this.excludedTree.getCheckedFiles()) {
					if (this.includedTree.isChecked(file)) {
						excludedFiles.add(file);
					} else if (this.includedTree.isDescendantChecked(file)) {
						excludedFiles.addAll(this.includedTree.getCheckedDescendantFiles(file));
					}
				}
				this.excludedSize = new FilesSize();
				this.excludedCalculator = new FilesSizeCalculator(this.excludedSize, excludedFiles, new ArrayList<File>(), this);
			}
			this.calculating = runCalc || runExcludedCalc;
		} finally { 
			this.sizeLock.unlock();
		}
		if (runCalc || runExcludedCalc) {
			fireBackupSizeCalculating();
			if (runCalc) {
				SwingWorkerUtil.execute(this.includedCalculator);
			}
			if (runExcludedCalc) {
				SwingWorkerUtil.execute(this.excludedCalculator);
			}
		}
	}
	
	private boolean isOverlap(CheckboxFileTree tree, List<File>... fileLists) {
		for (List<File> files : fileLists) {
			for (File file : files) {
				if (tree.isChecked(file) || tree.isDescendantChecked(file)) {
					return true;
				}
			}
		}
		return false;
	}
	
	protected void fireBackupSizeCalculating() {
		if (SwingUtilities.isEventDispatchThread()) {
			internalFireBackupSizeCalculating();
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					internalFireBackupSizeCalculating();
				}
			});
		}
	}
	
	private void internalFireBackupSizeCalculating() {
		for (BackupSizeListener listener : this.backupSizeListeners) {
			listener.backupSizeCalculating();
		}
	}

	protected void fireBackupSizeChange(final FilesSize totalSize, final FilesSize excludedSize) {
		if (SwingUtilities.isEventDispatchThread()) {
			internalFireBackupSizeChange(totalSize, excludedSize);
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					internalFireBackupSizeChange(totalSize, excludedSize);
				}
			});
		}
	}
	
	private void internalFireBackupSizeChange(FilesSize totalSize, FilesSize excludedSize) {
		for (BackupSizeListener listener : this.backupSizeListeners) {
			listener.backupSizeChange(totalSize, excludedSize);
		}
	}
	
	@Override
	public void handleDirectoryProcessing(List<File> directories) {
		// nothing needs to be done here
	}

	@Override
	public void handleFilesSize(FilesSizeCalculator calculator,	FilesSize size) {
		FilesSize tSize = null;
		FilesSize xSize = null;
		this.sizeLock.lock();
		try {
			if (calculator == this.includedCalculator) {
				this.includedSize = size.clone();
			} else if (calculator == this.excludedCalculator) {
				this.excludedSize = size.clone();
			}
			if (this.includedCalculator.isDone() && (this.excludedCalculator == null || this.excludedCalculator.isDone())) {
				this.totalSize = this.includedSize.clone();
				this.totalSize.remove(this.excludedSize);
				tSize = this.totalSize.clone();
				xSize = this.excludedSize.clone();
				this.calculating = false;
			}
		} finally {
			this.sizeLock.unlock();
		}
		if (tSize != null) {
			fireBackupSizeChange(tSize, xSize);
		}
	}

	@Override
	public void handleFilesSizeInterrupted() {
		// nothing needs to be done here
	}
}
