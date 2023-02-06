package org.xandercat.cat.back.engine;

/**
 * BackupEngineListener can be implemented by any class that wishes to be notified of backup
 * engine completion.
 * 
 * @author Scott C Arnold
 */
public interface BackupEngineListener {
	
	public void backupEngineComplete(String backupId, boolean resolutionRequired, boolean copyCancelled);
}
