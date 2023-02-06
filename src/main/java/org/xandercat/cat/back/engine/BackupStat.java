package org.xandercat.cat.back.engine;

import java.io.File;
import java.io.Serializable;
import java.util.Date;

/**
 * Snapshot statistics for an executed backup.
 * 
 * @author Scott Arnold
 */
public class BackupStat implements Serializable, Comparable<BackupStat> {

	private static final long serialVersionUID = 2010081301L;
	
	private String backupId;
	private Date dateStarted;
	private Date dateFinished;
	private BackupStatus backupStatus;
	private long backupSize;
	private int totalFiles;
	private int filesMoved;
	private int filesCopied;
	private File baseBackupDirectory;
	private File incrementalBackupDirectory;
	private long incrementalBackupSize;
	
	public BackupStat() {
	}
	
	public BackupStat(String backupId) {
		this();
		this.backupId = backupId;
	}

	public String getBackupId() {
		return backupId;
	}

	public void setBackupId(String backupId) {
		this.backupId = backupId;
	}

	public BackupStatus getBackupStatus() {
		return backupStatus;
	}

	public void setBackupStatus(BackupStatus backupStatus) {
		this.backupStatus = backupStatus;
	}

	public Date getDateStarted() {
		return dateStarted;
	}

	public void setDateStarted(Date dateStarted) {
		this.dateStarted = dateStarted;
	}

	public Date getDateFinished() {
		return dateFinished;
	}

	public void setDateFinished(Date dateFinished) {
		this.dateFinished = dateFinished;
	}

	public long getBackupSize() {
		return backupSize;
	}

	public void setBackupSize(long backupSize) {
		this.backupSize = backupSize;
	}

	public int getTotalFiles() {
		return totalFiles;
	}

	public void setTotalFiles(int totalFiles) {
		this.totalFiles = totalFiles;
	}

	public int getFilesMoved() {
		return filesMoved;
	}

	public void setFilesMoved(int filesMoved) {
		this.filesMoved = filesMoved;
	}

	public int getFilesCopied() {
		return filesCopied;
	}

	public void setFilesCopied(int filesCopied) {
		this.filesCopied = filesCopied;
	}

	public File getBaseBackupDirectory() {
		return baseBackupDirectory;
	}

	public void setBaseBackupDirectory(File baseBackupDirectory) {
		this.baseBackupDirectory = baseBackupDirectory;
	}

	public File getIncrementalBackupDirectory() {
		return incrementalBackupDirectory;
	}

	public void setIncrementalBackupDirectory(File incrementalBackupDirectory) {
		this.incrementalBackupDirectory = incrementalBackupDirectory;
	}

	public long getIncrementalBackupSize() {
		return incrementalBackupSize;
	}

	public void setIncrementalBackupSize(long incrementalBackupSize) {
		this.incrementalBackupSize = incrementalBackupSize;
	}

	@Override
	public int compareTo(BackupStat o) {
		return dateStarted.compareTo(o.dateStarted);
	}
}
