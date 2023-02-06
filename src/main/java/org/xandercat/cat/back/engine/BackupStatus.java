package org.xandercat.cat.back.engine;

/**
 * Status for the final result of a started backup.
 * <ul>
 * <li>COMPLETED:  Backup fully completed.</li>
 * <li>CANCELLED_BEFORE:  Backup cancelled before any changes to the backup location had started.</li>
 * <li>CANCELLED_DURING:  Backup cancelled while changes to the backup location were in progress.</li>
 * <li>ERROR:  Backup failed due to some exception condition.</li>
 * </ul>
 * 
 * @author Scott Arnold
 */
public enum BackupStatus {
	COMPLETED("Completed"), 
	CANCELLED_BEFORE("Cancelled"), 
	CANCELLED_DURING("Incomplete"), 
	ERROR("Error");
	
	private String displayString;
	
	private BackupStatus(String displayString) {
		this.displayString = displayString;
	}
	
	@Override
	public String toString() {
		return displayString;
	}
}
