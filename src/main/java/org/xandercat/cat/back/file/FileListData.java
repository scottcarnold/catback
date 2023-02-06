package org.xandercat.cat.back.file;

import java.io.Serializable;
import java.util.List;

/**
 * Class for storing information about the last backup for use on the next backup.
 * This information enables the backup process to skip scanning of the last backup
 * when doing a new backup.
 * 
 * @author Scott Arnold
 */
public class FileListData implements Serializable {

	private static final long serialVersionUID = 2013080301L;

	private List<BackupFile> backupFiles;
	private long backupSize;
	
	public FileListData(List<BackupFile> backupFiles, long backupSize) {
		this.backupFiles = backupFiles;
		this.backupSize = backupSize;
	}
	public List<BackupFile> getBackupFiles() {
		return backupFiles;
	}
	public long getBackupSize() {
		return backupSize;
	}
}
