package org.xandercat.cat.back.file;

import java.io.File;
import java.io.Serializable;

/**
 * BackupFile contains information on a File that allows a source file to be compared against
 * a backup file.
 * 
 * @author Scott C Arnold
 */
public class BackupFile implements Comparable<BackupFile>, Serializable {

	private static final long serialVersionUID = 2013072001L;

	public static final long LAST_MOD_PRECISION = 1000000;  //upped to 1000k from 1k due to thumb drive behavior
			
	private File file;
	private Type type;
	private String criticalPath;
	
	public enum Type {
		SOURCE, DESTINATION;
	}
	
	public BackupFile(File file, Type type, File latestBackupDirectory) {
		this.file = file;
		this.type = type;
		if (type == Type.SOURCE) {
			this.criticalPath = BackupPathGenerator.generateCriticalPath(file);
		} else if (type == Type.DESTINATION) {
			int beginIndex = latestBackupDirectory.getAbsolutePath().length() + File.separator.length();
			this.criticalPath = file.getAbsolutePath().substring(beginIndex); 
		}
	}
	
	public File getFile() {
		return file;
	}

	public Type getType() {
		return type;
	}

	public String getCriticalPath() {
		return criticalPath;
	}
	
	public int compareTo(BackupFile other) {
		return (other == null)? -1 : criticalPath.compareTo(other.criticalPath);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof BackupFile)) {
			return false;
		}
		return criticalPath.equals(((BackupFile) obj).criticalPath);
	}

	@Override
	public int hashCode() {
		return criticalPath.hashCode();
	}
	
	public boolean isFileEquivalent(BackupFile other) {
		if (!criticalPath.equals(other.criticalPath)) {
			return false;
		}
		return (file.isDirectory() || 
				(file.length() == other.file.length() && 
						(file.lastModified() / LAST_MOD_PRECISION == other.file.lastModified() / LAST_MOD_PRECISION)));
//		if (!fe || (file.getName() != null && file.getName().equals("BackupFile.java"))) {
//			System.out.println("cp1:" + criticalPath);
//			System.out.println("cp2:" + other.criticalPath);
//			System.out.println("l1:" + file.length());
//			System.out.println("l2:" + other.file.length());
//			System.out.println("lm1:" + file.lastModified());
//			System.out.println("lm2:" + other.file.lastModified());
//			System.out.println("lmc1:" + file.lastModified() / LAST_MOD_PRECISION);
//			System.out.println("lmc2:" + other.file.lastModified() / LAST_MOD_PRECISION);
//		}
//		return fe;
	}
	
	public boolean isParent(BackupFile other) {
		return other.criticalPath.startsWith(criticalPath)
			&& other.criticalPath.length() > criticalPath.length()
			&& other.criticalPath.charAt(criticalPath.length()) == File.separatorChar;
	}
	
	@Override
	public String toString() {
		return criticalPath + " [" + type.toString() + "->" + file.getAbsolutePath() + "]";
	}
	
	public File toGenerationFile(File generationBackupDirectory) {
		return new File(generationBackupDirectory.getAbsolutePath() + File.separator + criticalPath);
	}
}
