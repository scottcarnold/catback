package org.xandercat.cat.back;

import org.xandercat.swing.file.FilesSize;

/**
 * Interface to be implemented by any class that needs to be informed whenever the
 * total size of a CatBack backup changes.
 * 
 * @author Scott Arnold
 */
public interface BackupSizeListener {

	/**
	 * Method called when the size of the backup is being calculated.
	 */
	public void backupSizeCalculating();
	
	/**
	 * Method called after backup size has been calculated.  Backup size is the size
	 * of included files minus the size of excluded files.  
	 * 
	 * @param backupSize		size of all included files minus size of any excluded files
	 * @param excludedSize		size of excluded files
	 */
	public void backupSizeChange(FilesSize backupSize, FilesSize excludedSize);
}
