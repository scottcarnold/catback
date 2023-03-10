package org.xandercat.cat.back.engine.worklet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xandercat.cat.back.engine.BackupEngine;
import org.xandercat.cat.back.file.BackupFile;
import org.xandercat.swing.file.DirectorySizeCache;
import org.xandercat.swing.file.FileManager;
import org.xandercat.swing.file.FilesSize;

public class MoveFiles extends BackupEngineWorklet<Void> {

	private static final Logger log = LogManager.getLogger(MoveFiles.class);
	
	private List<BackupFile> filesToMove;
	private List<File> filesToCopy;
	private File incrementalBackupDirectory;
	private volatile int filesMoved;
	private volatile long filesSize;
	private boolean dryRun;
	private String dryRunPrefix = "";
	
	public MoveFiles(BackupEngine backupEngine, List<BackupFile> filesToMove, List<File> filesToCopy, File incrementalBackupDirectory) {
		super(backupEngine);
		this.filesToMove = filesToMove;
		this.filesToCopy = filesToCopy;
		this.incrementalBackupDirectory = incrementalBackupDirectory;
	}

	@Override
	public String getTitle() {
		return "Moving old files";
	}

	@Override
	public void enableDryRun(String dryRunPrefix, Long speedFactor) {
		this.dryRun = true;
		this.dryRunPrefix = dryRunPrefix;
	}

	@Override
	public Void execute() throws Exception {
		log.info(dryRunPrefix + "Moving changed and deleted files to incremental backup directory...");
		if (dryRun) {
			log.info(dryRunPrefix + "Incremental backup directory to create: " + incrementalBackupDirectory.getAbsolutePath());
		} else {
			incrementalBackupDirectory.mkdir();
		}
		List<BackupFile> moveFailures = new ArrayList<BackupFile>();
		List<BackupFile> directoriesMoved = new ArrayList<BackupFile>();
		for (BackupFile latestFile : filesToMove) {
			if (isCancelled()) {
				break;
			}
			if (movedByDirectory(latestFile, directoriesMoved)) {
				log.debug("Parent already moved; skipping file " + latestFile.toString());
				continue;
			}
			try {
				File generationFile = latestFile.toGenerationFile(incrementalBackupDirectory);
				log.debug("Renaming " + latestFile.getFile().getAbsolutePath() + " to " + generationFile.getAbsolutePath());
				publish(dryRunPrefix + "Moving " + generationFile.getName());
				if (!dryRun) {
					generationFile.getParentFile().mkdirs();
					if (!latestFile.getFile().renameTo(generationFile)) {
						moveFailures.add(latestFile);
						log.warn("Unable to move file " + latestFile.toString());
					} else {
						if (generationFile.isDirectory()) {
							directoriesMoved.add(latestFile);
							FilesSize size = DirectorySizeCache.getInstance().loadDirectorySize(generationFile);
							filesMoved += size.getFiles();
							filesSize += size.getBytes();
						} else {
							filesMoved++;
							filesSize += generationFile.length();
						}
						// remove any directory that is empty and is not itself to be backed up
						File parent = latestFile.getFile().getParentFile();
						while (parent != null && parent.listFiles() == null) {
							if (!filesToCopy.contains(parent)) {	// potentially expensive call, but shouldn't happen often
								try {
									if (!parent.delete()) {
										log.warn("Unable to remove empty parent directory " + parent.getAbsolutePath());
									}
								} catch (Exception e) {
									log.warn("Unable to remove empty parent directory " + parent.getAbsolutePath(), e);
								}
							}
							parent = parent.getParentFile();
						}
					}
				}
			} catch (Exception e) {
				log.warn("Unable to move file " + latestFile.toString(), e);
				moveFailures.add(latestFile);
			}
			advanceProgress(1);
		}
		if (moveFailures.size() > 0) {
			publish(moveFailures.size() + " files could not be moved; you will have the option to either overwrite them or cancel backing up the newer versions.");
			log.warn(moveFailures.size() + " files could not be moved");
		}
		
		// cache the size of the incremental directory
		File sizeFile = new File(incrementalBackupDirectory, BackupEngine.INCREMENTAL_SIZE_FILE_NAME);
		try {
			FileManager.saveObject(sizeFile, Long.valueOf(filesSize));
		} catch (Exception e) {
			log.warn("Unable to save incremental backup size to file: " + sizeFile.getAbsolutePath());
		}
		
		filesToMove = null;
		filesToCopy = null;
		return null;
	}
	
	/**
	 * Returns whether or not the given backup file has already been moved due to the move
	 * of a parent directory.  Note that this method requires that backup files be ordered
	 * such that parent directories will be processed prior to their children.
	 * 
	 * @param backupFile			backup file to test
	 * @param directoriesMoved		directories already moved
	 * 
	 * @return		whether or not a backup file has already been moved
	 */
	private boolean movedByDirectory(BackupFile backupFile, List<BackupFile> directoriesMoved) {
		for (BackupFile directoryMoved : directoriesMoved) {
			if (directoryMoved.isParent(backupFile)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public long getProgressMaximum() {
		return filesToMove.size();
	}
	
	public int getFilesMoved() {
		return filesMoved;
	}
	
	public long getFilesSize() {
		return filesSize;
	}
}

