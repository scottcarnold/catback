package org.xandercat.cat.back;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.xandercat.cat.back.swing.zenput.annotation.ValidateTimeDuration;
import org.xandercat.swing.datetime.TimeDuration;
import org.xandercat.swing.file.BinaryPrefix;
import org.xandercat.swing.file.ByteSize;
import org.xandercat.swing.util.FileUtil;
import org.xandercat.swing.zenput.annotation.ControlEquals;
import org.xandercat.swing.zenput.annotation.InputField;
import org.xandercat.swing.zenput.annotation.ValidateFile;
import org.xandercat.swing.zenput.annotation.ValidateFile.Mode;
import org.xandercat.swing.zenput.annotation.ValidateInteger;
import org.xandercat.swing.zenput.annotation.ValidateRequired;

/**
 * Class for storing all information about a backup.  This class represents the files
 * managed by the CatBack application.
 * 
 * To make it easier to support loading old backups on newer versions of CatBack, from 
 * version 1.5 onward, the lowest compatible version will be appended to the backup file name
 * (if 1.6 were released with no changes to the backup class, the new version may continue
 * to use CatBackup15; if breaking changes are introduced, a new class CatBackup16 would be
 * created.)
 * 
 * @author Scott Arnold
 */
public class CatBackup15 implements Serializable {
	
	private static final long serialVersionUID = 2023020601L;
	
	private String id;
	
	@InputField(title="Backup Name")
	@ValidateRequired
	private String name;
	
	@InputField(title="Backup Directory")
	@ValidateRequired
	@ValidateFile(mode=Mode.DIRECTORIES_ONLY, exists=true)
	private File backupDirectory;
	
	private List<File> includedFiles = new ArrayList<File>();			// list of all files, including directories
	private List<File> includedDirectories = new ArrayList<File>();		// list of directories only
	private List<File> excludedFiles = new ArrayList<File>();			// list of all excluded files, including directories
	private List<File> excludedDirectories = new ArrayList<File>();		// list of excluded directories only
	
	@InputField(title="Show Files Before Move/Copy")
	private boolean showFilesBeforeMoveCopy;
	
	@InputField(title="Always Leave Copy Window Open")
	private boolean alwaysLeaveCopyWindowOpen;
	
	@InputField(title="Limit Incremental Backups")
	private boolean limitIncrementalBackups;
	
	@InputField(title="Keep For At Least (Time)")
	@ControlEquals(dependencyOn="limitIncrementalBackups", valueType=Boolean.class, stringValue="true")
	@ValidateRequired
	@ValidateTimeDuration
	private TimeDuration keepAtLeastTime;
	
	@InputField(title="Keep No More Than (Time)")
	@ControlEquals(dependencyOn="limitIncrementalBackups", valueType=Boolean.class, stringValue="true")
	@ValidateRequired
	@ValidateTimeDuration
	private TimeDuration keepNoMoreThanTime;
	
	@InputField(title="Keep No More Than (Size)")
	@ControlEquals(dependencyOn="limitIncrementalBackups", valueType=Boolean.class, stringValue="true")
	@ValidateRequired
	private ByteSize keepNoMoreThanBytes;
	
	@InputField(title="Perform Full Scan of Previous Backup")
	private boolean scanLastBackup;
	
	@InputField(title="Errors Until Backup Halt")
	@ValidateInteger(min=1)
	@ValidateRequired
	private Integer errorsUntilBackupHalt = Integer.valueOf(10);
	
	public CatBackup15() {
		this(UUID.randomUUID().toString());	
	}
	
	public CatBackup15(String id) {
		this.id = id;
		this.keepAtLeastTime = new TimeDuration(1, TimeDuration.Unit.MONTH);
		this.keepNoMoreThanTime = new TimeDuration(1, TimeDuration.Unit.YEAR);
		this.keepNoMoreThanBytes = new ByteSize(25, BinaryPrefix.GiB);		
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
		return Collections.unmodifiableList(includedFiles);
	}
	
	public List<File> getExcludedFiles() {
		return Collections.unmodifiableList(excludedFiles);
	}
	
	public List<File> getIncludedDirectories() {
		return Collections.unmodifiableList(includedDirectories);
	}
	
	public List<File> getExcludedDirectories() {
		return Collections.unmodifiableList(excludedDirectories);
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
	
	/**
	 * Sets what files are included in this backup.  filesRepresentingDirectories should be a subset of files,
	 * but this is not checked; not adhering to this rule could have unpredictable results.  filesRepresentingDirectories
	 * can be left null, in which case each file will be tested to determine if it is a directory or not, though if
	 * the file does not exist, it will simply assume it was not a directory.
	 * 
	 * @param files                           files included in the backup (can contain files and directories)
	 * @param filesRepresentingDirectories    subset of files that represent directories (if provided, files will not have to be tested)
	 */
	public void setIncludedFiles(List<File> files, List<File> filesRepresentingDirectories) {
		this.includedFiles.clear();
		this.includedDirectories.clear();
		this.includedFiles.addAll(files);
		if (filesRepresentingDirectories != null) {
			this.includedDirectories.addAll(filesRepresentingDirectories);
		} else {
			for (File file : this.includedFiles) {
				if (file.isDirectory()) {
					this.includedDirectories.add(file);
				}
			}
		}
	}
	
	/**
	 * Sets what files are marked excluded in this backup.  filesRepresentingDirectories should be a subset of files,
	 * but this is not checked; not adhering to this rule could have unpredictable results.  filesRepresentingDirectories
	 * can be left null, in which case each file will be tested to determine if it is a directory or not, though if
	 * the file does not exist, it will simply assume it was not a directory.
	 * 
	 * @param files                           files excluded in the backup (can contain files and directories)
	 * @param filesRepresentingDirectories    subset of files that represent directories (if provided, files will not have to be tested)
	 */
	public void setExcludedFiles(List<File> files, List<File> filesRepresentingDirectories) {
		this.excludedFiles.clear();
		this.excludedDirectories.clear();
		this.excludedFiles.addAll(files);
		if (filesRepresentingDirectories != null) {
			this.excludedDirectories.addAll(filesRepresentingDirectories);
		} else {
			for (File file : this.excludedFiles) {
				if (file.isDirectory()) {
					this.excludedDirectories.add(file);
				}
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
