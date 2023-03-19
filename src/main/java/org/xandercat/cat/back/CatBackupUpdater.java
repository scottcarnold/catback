package org.xandercat.cat.back;

import java.io.File;
import java.util.List;

import org.xandercat.swing.tree.CheckboxFileTree;
import org.xandercat.swing.tree.CheckboxFileTreeListener;

/**
 * This class handles updating the files in the backup any time one of the checkbox file trees
 * is changed.  The backup profile cannot handle this itself without holding onto transient references
 * to the checkbox file trees, and the FileManager does not work properly if the managed objects have
 * transient references.  //TODO: Elaborate on problem with transient references
 * 
 * @author Scott Arnold
 */
public class CatBackupUpdater implements CheckboxFileTreeListener {

	private CatBackup16 backup;
	private CheckboxFileTree includedFilesTree;
	private CheckboxFileTree excludedFilesTree;
	
	public CatBackupUpdater(CatBackup16 backup, CheckboxFileTree includedFilesTree, CheckboxFileTree excludedFilesTree) {
		this.backup = backup;
		this.includedFilesTree = includedFilesTree;
		this.excludedFilesTree = excludedFilesTree;
		this.includedFilesTree.addCheckboxFileTreeListener(this);
		this.excludedFilesTree.addCheckboxFileTreeListener(this);
	}

	public void filesChanged(CheckboxFileTree source, List<File> addedFiles, List<File> removedFiles) {
		backup.filesChanged(source == includedFilesTree, addedFiles, removedFiles);
	}
	
	public void destroy() {
		this.includedFilesTree.removeCheckboxFileTreeListener(this);
		this.excludedFilesTree.removeCheckboxFileTreeListener(this);
		this.includedFilesTree = null;
		this.excludedFilesTree = null;
		this.backup = null;
	}
}
