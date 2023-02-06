package org.xandercat.cat.back.engine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xandercat.cat.back.CatBackSettings;
import org.xandercat.swing.file.FileManager;

/**
 * Class for managing backup statistics.
 * 
 * @author Scott Arnold
 */
public class BackupStats {

	private List<BackupStat> backupStats;
	
	/**
	 * Constructs a new BackupStats object.  Use this constructor when starting a new
	 * set of backup statistics. 
	 */
	public BackupStats() {	
	}
	
	/**
	 * Constructs a new BackupStats object.  On construction, backup stats are loaded from 
	 * the given backup directory, if available.
	 * 
	 * @param baseBackupDirectory
	 * 
	 * @throws IOException
	 */
	public BackupStats(File baseBackupDirectory) throws IOException {
		loadStats(baseBackupDirectory);
	}
	
	/**
	 * Returns the latest backup statistic, or null if none are available.
	 * 
	 * @return		latest backup statistic, or null if none are available
	 */
	public synchronized BackupStat getLatestStat() {
		if (backupStats == null || backupStats.size() == 0) {
			return null;
		} else {
			return backupStats.get(backupStats.size()-1);
		}
	}
	
	/**
	 * Returns the latest backup statistic that has the given backup status. 
	 * If no statistic exists with the given status, null is returned.
	 * 
	 * @param status		status to match
	 * 
	 * @return				latest backup statistic with the given status
	 */
	public synchronized BackupStat getLatestStat(BackupStatus status) {
		if (backupStats == null) {
			return null;
		}
		for (int i=backupStats.size()-1; i>=0; i--) {
			BackupStat stat = backupStats.get(i);
			if (stat.getBackupStatus() == status) {
				return stat;
			}
		}
		return null;
	}
	
	/**
	 * Add the given backup statistic to the list of backup statistics.  
	 * 
	 * @param backupStat
	 * 
	 * @throws IOException
	 */
	public synchronized void addBackupStat(BackupStat backupStat) {
		if (this.backupStats == null) {
			this.backupStats = new ArrayList<BackupStat>();
		}
		this.backupStats.add(backupStat);
	}
	
	public synchronized List<BackupStat> getBackupStats() {
		List<BackupStat> stats = new ArrayList<BackupStat>();
		if (this.backupStats != null) {
			stats.addAll(this.backupStats);
		}
		return stats;
	}
	
	@SuppressWarnings("unchecked")
	private void loadStats(File baseBackupDirectory) throws IOException {
		this.backupStats = null;
		if (baseBackupDirectory != null) {
			File backupStatsFile = new File(baseBackupDirectory, CatBackSettings.BACKUP_STATS_FILE_NAME);
			if (backupStatsFile.exists()) {
				this.backupStats = (List<BackupStat>) FileManager.loadObject(backupStatsFile, ArrayList.class);
			}
		}
	}
	
	/**
	 * Save the current backup statistics to the given base backup location.
	 * 
	 * @param baseBackupDirectory		backup location
	 * 
	 * @throws IOException
	 */
	public synchronized void saveStats(File baseBackupDirectory) throws IOException {
		if (this.backupStats != null) {
			File backupStatsFile = new File(baseBackupDirectory, CatBackSettings.BACKUP_STATS_FILE_NAME);
			FileManager.saveObject(backupStatsFile, this.backupStats);
		}
	}
}
