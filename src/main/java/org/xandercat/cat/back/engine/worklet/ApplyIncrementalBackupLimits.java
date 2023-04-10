package org.xandercat.cat.back.engine.worklet;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xandercat.cat.back.engine.BackupEngine;
import org.xandercat.swing.datetime.TimeDuration;
import org.xandercat.swing.file.DirectorySizeCache;
import org.xandercat.swing.file.FileManager;
import org.xandercat.swing.file.FilesSize;
import org.xandercat.swing.util.FileUtil;

public class ApplyIncrementalBackupLimits extends BackupEngineWorklet<Void> {

	private static final Logger log = LogManager.getLogger(ApplyIncrementalBackupLimits.class);
	
	private File baseBackupDirectory;
	private TimeDuration keepAtLeastTime;
	private TimeDuration keepNoMoreThanTime;
	private long keepNoMoreThanBytes;
	private long progressMaximum;
	private long bytesToMove;
	private boolean dryRun;
	private String dryRunPrefix = "";
	
	public ApplyIncrementalBackupLimits(BackupEngine backupEngine, File baseBackupDirectory, TimeDuration keepAtLeastTime, TimeDuration keepNoMoreThanTime, long keepNoMoreThanBytes, long bytesToMove) {
		super(backupEngine);
		this.baseBackupDirectory = baseBackupDirectory;
		this.keepAtLeastTime = keepAtLeastTime;
		this.keepNoMoreThanTime = keepNoMoreThanTime;
		this.keepNoMoreThanBytes = keepNoMoreThanBytes;
		this.bytesToMove = bytesToMove;
	}

	@Override
	public String getTitle() {
		return "Removing expired incremental backups";
	}

	@Override
	public void enableDryRun(String dryRunPrefix, Long speedFactor) {
		this.dryRun = true;
		this.dryRunPrefix = dryRunPrefix;
	}

	@Override
	public Void execute() throws Exception {
		log.debug(dryRunPrefix + "Applying incremental backup limits...");
		FileFilter filter = new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory() 
					&& file.getName().matches(BackupEngine.INCREMENTAL_BACKUP_DIR_NAME_REGEX_PATTERN);
			}
		};
		List<String> backupDateStrings = new ArrayList<String>();
		Map<String, Long> backupSizes = new HashMap<String, Long>();
		Map<String, File> backupFiles = new HashMap<String, File>();
		for (File incrementalDirectory : baseBackupDirectory.listFiles(filter)) {
			log.debug("Incremental backup directory found: " + incrementalDirectory.getName());
			StringBuilder sb = new StringBuilder();
			sb.append(incrementalDirectory.getName().substring(0, 8));
			if (incrementalDirectory.getName().length() > 8) {
				String seq = incrementalDirectory.getName().substring(9);
				for (int i=seq.length(); i<4; i++) {
					sb.append("0");
				}
				sb.append(seq);
			} else {
				sb.append("0000");
			}
			String backupDateString = sb.toString();
			backupDateStrings.add(backupDateString);
			backupSizes.put(backupDateString, Long.valueOf(loadIncrementalBackupSize(incrementalDirectory)));
			backupFiles.put(backupDateString, incrementalDirectory);
		}
		Collections.sort(backupDateStrings);
		Collections.reverse(backupDateStrings);
		SimpleDateFormat dateFormat = new SimpleDateFormat(BackupEngine.INCREMENTAL_BACKUP_DIR_NAME_DATE_PATTERN);
		long cumulativeSize = this.bytesToMove;
		Calendar keepAtLeastCalendar = this.keepAtLeastTime.getPastCalendar();
		log.debug("Keep At Least Calendar: " + dateFormat.format(keepAtLeastCalendar.getTime()));
		Calendar keepNoMoreThanCalendar = this.keepNoMoreThanTime.getPastCalendar();
		log.debug("Keep No More Than Calendar: " + dateFormat.format(keepNoMoreThanCalendar.getTime()));
		List<File> deleteDirectoryList = new ArrayList<File>();
		for (String backupDateString : backupDateStrings) {
			try {
				log.debug("Processing incremental backup datestring " + backupDateString);
				Calendar backupCalendar = Calendar.getInstance();
				backupCalendar.setTime(dateFormat.parse(backupDateString.substring(0, 8)));
				cumulativeSize += backupSizes.get(backupDateString).longValue();
				File directory = backupFiles.get(backupDateString);
				if (backupCalendar.before(keepAtLeastCalendar)) {
					if (cumulativeSize > keepNoMoreThanBytes || backupCalendar.before(keepNoMoreThanCalendar)) {
						if (backupCalendar.before(keepNoMoreThanCalendar)) {
							log.info("Incremental backup directory to be deleted (over maximum age): " + directory.getName());
						} else {
							log.info("Incremental backup directory to be deleted (incremental backups exceed maximum size): " + directory.getName());
						}
						deleteDirectoryList.add(directory);
					} else {
						log.info("Keeping incremental backup (size and date within set limits): " + directory.getName());
					}
				} else {
					log.info("Keeping incremental backup (under minimum age): " + directory.getName());
				}
			} catch (ParseException pe) {
				log.error("Unable to process directory date: " + backupDateString, pe);
			}
		}
		if (deleteDirectoryList.size() > 0 && !isCancelled()) {
			this.progressMaximum = deleteDirectoryList.size();
			for (File directory : deleteDirectoryList) {
				if (isCancelled()) {
					break;
				}
				if (dryRun) {
					publish(dryRunPrefix + "Removing expired incremental backup " + directory.getAbsolutePath());
					Thread.sleep(1000);
				} else {
					publish("Removing expired incremental backup " + directory.getAbsolutePath());
					FileUtil.delete(directory);
				}
				advanceProgress(1);
			}
		}
		return null;
	}

	private long loadIncrementalBackupSize(File incrementalBackupDirectory) {
		long backupSize = 0;
		File sizeFile = new File(incrementalBackupDirectory, BackupEngine.INCREMENTAL_SIZE_FILE_NAME);
		if (sizeFile.exists()) {
			try {
				Long loadedSize = FileManager.loadObject(sizeFile, Long.class);
				if (loadedSize != null && loadedSize.longValue() > 0) {
					backupSize = loadedSize.longValue();
				}
			} catch (IOException e) {
				// nothing to do here, size will be loaded by directory size cache instead
			}
		}
		if (backupSize == 0) {
			// fallback to directory size cache
			log.info("No stat file found for incremental directory " + incrementalBackupDirectory.getName());
			log.info("Getting incremental backup size using Directory Size Cache...");
			FilesSize filesSize = DirectorySizeCache.getInstance().loadDirectorySize(incrementalBackupDirectory);
			backupSize = filesSize.getBytes();
			if (!dryRun) {
				try {
					FileManager.saveObject(sizeFile, Long.valueOf(backupSize));
				} catch (IOException e) {
					log.warn("Unable to save incremental backup file size to incremental backup folder " + incrementalBackupDirectory.getAbsolutePath());
				}
			}
		}
		log.debug("Incremental backup " + incrementalBackupDirectory.getName() + " size = " + backupSize);
		return backupSize;
	}
	
	@Override
	public long getProgressMaximum() {
		return progressMaximum;
	}
}
