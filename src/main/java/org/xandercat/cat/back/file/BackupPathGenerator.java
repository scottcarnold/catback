package org.xandercat.cat.back.file;

import java.io.File;

/**
 * BackupPathGenerator generates paths to be used for files that will be backed up.  
 * 
 * @author Scott C Arnold
 */
public class BackupPathGenerator implements FileCopierPathGenerator {

	private File backupDirectory;
	
	/**
	 * Build and return the path within a backup directory that would be used to store the given file.
	 * 
	 * @param file		the file to generate a critical path for
	 * 
	 * @return			critical path for the given file
	 */
	public static String generateCriticalPath(File file) {
		String criticalPath = file.getAbsolutePath();
		if (criticalPath.indexOf(":") == 1) {
			criticalPath = criticalPath.charAt(0) + criticalPath.substring(2);
		} else if (criticalPath.startsWith(File.separator)) {
			// TODO: Test this on a Mac (this is expected to happen on Mac local drive
			criticalPath = criticalPath.substring(File.separator.length());
		}
		if (criticalPath.endsWith(File.separator)) {
			// this happens on Windows root files
			criticalPath = criticalPath.substring(0, criticalPath.length() - File.separator.length());
		}		
		return criticalPath;
	}
	
	public BackupPathGenerator(File backupDirectory) {
		this.backupDirectory = backupDirectory;
	}
	
	public String generateDestinationPath(File file) {
		return backupDirectory.getAbsolutePath() + File.separator + generateCriticalPath(file);
	}
}
