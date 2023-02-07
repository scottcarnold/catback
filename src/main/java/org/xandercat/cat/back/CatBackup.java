package org.xandercat.cat.back;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.xandercat.common.util.ByteSize;
import org.xandercat.common.util.TimeDuration;
import org.xandercat.common.util.file.FileUtil;

/**
 * Class for storing all information about a backup.  This class represents the files
 * managed by the CatBack application.
 * 
 * This class is for version 1.3/1.4 of CatBack.  It exists here to support upgrading 
 * backups from the older versions.  For CatBack 1.5, the replacement class is CatBackup15.
 * 
 * @author Scott Arnold
 */
public class CatBackup implements Serializable {
	
	private static final long serialVersionUID = 2010081301L;
	
	private String id;
	
	private String name;
	private File backupDirectory;
	private List<File> includedFiles = new ArrayList<File>();			// list of all files, including directories
	private List<File> includedDirectories = new ArrayList<File>();		// list of directories only
	private List<File> excludedFiles = new ArrayList<File>();			// list of all excluded files, including directories
	private List<File> excludedDirectories = new ArrayList<File>();		// list of excluded directories only
	private boolean showFilesBeforeMoveCopy;
	private boolean alwaysLeaveCopyWindowOpen;
	private boolean limitIncrementalBackups;
	private TimeDuration keepAtLeastTime;
	private TimeDuration keepNoMoreThanTime;
	private ByteSize keepNoMoreThanBytes;
	private boolean scanLastBackup;
	private Integer errorsUntilBackupHalt = Integer.valueOf(10);
	
	public CatBackup() {
		this(UUID.randomUUID().toString());	
	}
	
	public CatBackup(String id) {
		this.id = id;
		this.keepAtLeastTime = new TimeDuration(1, TimeDuration.Unit.MONTH);
		this.keepNoMoreThanTime = new TimeDuration(1, TimeDuration.Unit.YEAR);
		this.keepNoMoreThanBytes = new ByteSize(25, FileUtil.BinaryPrefix.GiB);		
	}
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public File getBackupDirectory() {
		return backupDirectory;
	}

	public String getBackupDirectoryPath() {
		return (backupDirectory == null)? null : backupDirectory.getAbsolutePath();
	}
	
	public void setBackupDirectory(File backupDirectory) {
		this.backupDirectory = backupDirectory;
	}

	public void setBackupDirectoryPath(String path) {
		this.backupDirectory = (path == null || path.trim().length() == 0)? null : new File(path);
	}

	public boolean isShowFilesBeforeMoveCopy() {
		return showFilesBeforeMoveCopy;
	}

	public void setShowFilesBeforeMoveCopy(boolean showFilesBeforeMoveCopy) {
		this.showFilesBeforeMoveCopy = showFilesBeforeMoveCopy;
	}

	public boolean isAlwaysLeaveCopyWindowOpen() {
		return alwaysLeaveCopyWindowOpen;
	}

	public void setAlwaysLeaveCopyWindowOpen(boolean alwaysLeaveCopyWindowOpen) {
		this.alwaysLeaveCopyWindowOpen = alwaysLeaveCopyWindowOpen;
	}

	public boolean isLimitIncrementalBackups() {
		return limitIncrementalBackups;
	}

	public void setLimitIncrementalBackups(boolean limitIncrementalBackups) {
		this.limitIncrementalBackups = limitIncrementalBackups;
	}

	public TimeDuration getKeepAtLeastTime() {
		return keepAtLeastTime;
	}

	public void setKeepAtLeastTime(TimeDuration keepAtLeastTime) {
		this.keepAtLeastTime = keepAtLeastTime;
	}

	public TimeDuration getKeepNoMoreThanTime() {
		return keepNoMoreThanTime;
	}

	public void setKeepNoMoreThanTime(TimeDuration keepNoMoreThanTime) {
		this.keepNoMoreThanTime = keepNoMoreThanTime;
	}

	public ByteSize getKeepNoMoreThanBytes() {
		return keepNoMoreThanBytes;
	}

	public void setKeepNoMoreThanBytes(ByteSize keepNoMoreThanBytes) {
		this.keepNoMoreThanBytes = keepNoMoreThanBytes;
	}

	public boolean isScanLastBackup() {
		return scanLastBackup;
	}

	public void setScanLastBackup(boolean scanLastBackup) {
		this.scanLastBackup = scanLastBackup;
	}

	public Integer getErrorsUntilBackupHalt() {
		return errorsUntilBackupHalt;
	}

	public void setErrorsUntilBackupHalt(Integer errorsUntilBackupHalt) {
		this.errorsUntilBackupHalt = errorsUntilBackupHalt;
	}

	public List<File> getIncludedFiles() {
		List<File> filesCopy = new ArrayList<File>();
		filesCopy.addAll(includedFiles);
		return filesCopy;
	}
	
	public List<File> getExcludedFiles() {
		List<File> excludedFilesCopy = new ArrayList<File>();
		excludedFilesCopy.addAll(excludedFiles);
		return excludedFilesCopy;
	}
	
	public List<File> getIncludedDirectories() {
		List<File> dirsCopy = new ArrayList<File>();
		dirsCopy.addAll(includedDirectories);
		return dirsCopy;
	}
	
	public List<File> getExcludedDirectories() {
		List<File> dirsCopy = new ArrayList<File>();
		dirsCopy.addAll(excludedDirectories);
		return dirsCopy;
	}
	
	public boolean wasDirectory(File file) {
		if (includedFiles.contains(file)) {
			return includedDirectories.contains(file);
		} else if (excludedFiles.contains(file)) {
			return excludedDirectories.contains(file);
		} else {
			throw new IllegalArgumentException("Only files that are part of the backup profile can be tested.");
		}
	}
	
	public void setIncludedFiles(List<File> files) {
		this.includedFiles.clear();
		this.includedDirectories.clear();
		this.includedFiles.addAll(files);
		for (File file : this.includedFiles) {
			if (file.isDirectory()) {
				this.includedDirectories.add(file);
			}
		}
	}
	
	public void setExcludedFiles(List<File> files) {
		this.excludedFiles.clear();
		this.excludedDirectories.clear();
		this.excludedFiles.addAll(files);
		for (File file : this.excludedFiles) {
			if (file.isDirectory()) {
				this.excludedDirectories.add(file);
			}
		}
	}

	public void filesChanged(boolean includedFilesChanged, List<File> addedFiles, List<File> removedFiles) {
		List<File> files = null;
		List<File> directories = null;
		if (includedFilesChanged) {
			files = this.includedFiles;
			directories = this.includedDirectories;
		} else {
			files = this.excludedFiles;
			directories = this.excludedDirectories;
		}
		for (File file : removedFiles) {
			files.remove(file);
			directories.remove(file);
		}
		for (File file : addedFiles) {
			Boolean isDirectory = FileUtil.isDirectory(file);
			if (isDirectory != null) {
				// only add files if we can tell if they were a directory or not
				// note that this means any missing files checked in the tree will be ignored
				files.add(file);
				if (isDirectory.booleanValue()) {
					directories.add(file);
				}
			}
		}
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		// errorsUntilBackupHalt was added in v1.4; need to initialize it for pre-1.4 loads
		if (errorsUntilBackupHalt == null) {
			errorsUntilBackupHalt = Integer.valueOf(10);
		}
	}
}
