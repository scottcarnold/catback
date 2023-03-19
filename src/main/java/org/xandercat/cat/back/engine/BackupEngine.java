package org.xandercat.cat.back.engine;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xandercat.cat.back.CatBackup16;
import org.xandercat.cat.back.engine.worklet.ApplyIncrementalBackupLimits;
import org.xandercat.cat.back.engine.worklet.BackupEngineWorklet;
import org.xandercat.cat.back.engine.worklet.CompareFiles;
import org.xandercat.cat.back.engine.worklet.CopyFiles;
import org.xandercat.cat.back.engine.worklet.LoadBackupFiles;
import org.xandercat.cat.back.engine.worklet.LoadCurrentFiles;
import org.xandercat.cat.back.engine.worklet.MoveFiles;
import org.xandercat.cat.back.file.BackupFile;
import org.xandercat.cat.back.file.BackupPathGenerator;
import org.xandercat.cat.back.file.FileListData;
import org.xandercat.swing.app.ApplicationFrame;
import org.xandercat.swing.app.CloseListener;
import org.xandercat.swing.datetime.TimeDuration;
import org.xandercat.swing.dialog.ProgressMonitor;
import org.xandercat.swing.file.FileManager;
import org.xandercat.swing.file.icon.FileIconCache;
import org.xandercat.swing.tree.CheckboxFileTree;
import org.xandercat.swing.worker.SwingWorkletManager;

public class BackupEngine extends SwingWorker<Void, BackupEngineProgress> implements SwingWorkletManager<String>, CloseListener {

	private static final Logger log = LogManager.getLogger(BackupEngine.class);
	private static final String DRY_RUN_PREFIX = "[DRY RUN / SIMULATED] ";
	
	public static final String LATEST_BACKUP_DIR_NAME = "latest";
	public static final String LATEST_FILE_LIST_FILE_NAME = ".catback_filelist";
	public static final String INCREMENTAL_SIZE_FILE_NAME = ".catback_isize";
	public static final String INCREMENTAL_BACKUP_DIR_NAME_DATE_PATTERN = "yyyyMMdd";
	public static final String INCREMENTAL_BACKUP_DIR_NAME_REGEX_PATTERN = "[\\d]{8}|[\\d]{8}[-][\\d]{1,4}";
	public static final String STATS_FILENAME = "backup.txt";
	
	private ApplicationFrame parent;
	private String backupId;
	private String backupName;
	private List<BackupEngineListener> listeners;
	private FileIconCache fileIconCache;
	private CheckboxFileTree excludedTree;
	private Set<File> currentFilesAndDirectories;
	private File baseBackupDirectory;			// base backup directory
	private File backupDirectory;				// backup directory where latest backup is stored
	private File incrementalBackupDirectory;	// backup directory where incremental backup is stored
	private File latestFileListFile;            // file that stores the List<BackupFile> for files in latest dir
	private boolean showMoveCopyDialog;
	private boolean limitIncrementalBackups;
	private TimeDuration keepAtLeastTime;
	private TimeDuration keepNoMoreThanTime;
	private long keepNoMoreThanBytes;
	private boolean leaveCopyWindowOpen;
	private boolean resolutionRequired;
	private boolean copyCancelled;
	private boolean scanLastBackup;
	private int errorsUntilHalt;
	private ProgressMonitor progressMonitor;
	private boolean runQuiet;
	private BackupStats stats;
	private BackupStat stat = new BackupStat();
	private LoadBackupFiles loadBackupFiles;
	private MoveFiles moveFiles;
	private CopyFiles copyFiles;
	private long backupSize;  // used to hold size of backup to be saved in backup stat when done
	private boolean dryRun;
	private Long speedFactor;
	private boolean active;
	
	public BackupEngine(ApplicationFrame parent, 
			CatBackup16 backup, 
			FileIconCache fileIconCache, 
			CheckboxFileTree excludedTree,
			BackupStats stats) {
		this.parent = parent;
		this.stats = stats;
		this.backupId = backup.getId();
		this.backupName = backup.getName();
		this.errorsUntilHalt = backup.getErrorsUntilBackupHalt().intValue();
		this.fileIconCache = fileIconCache;
		this.excludedTree = excludedTree;
		this.currentFilesAndDirectories = backup.getIncludedFiles();
		parent.addCloseListener(this);
		this.baseBackupDirectory = backup.getBackupDirectory();
		this.backupDirectory = new File(baseBackupDirectory.getAbsolutePath() + File.separator + LATEST_BACKUP_DIR_NAME);
		this.latestFileListFile = new File(this.baseBackupDirectory, LATEST_FILE_LIST_FILE_NAME);
		SimpleDateFormat incDirFormatter = new SimpleDateFormat(INCREMENTAL_BACKUP_DIR_NAME_DATE_PATTERN);
		String incDir = incDirFormatter.format(new java.util.Date());
		this.incrementalBackupDirectory = new File(baseBackupDirectory.getAbsolutePath() + File.separator + incDir);
		int i = 0;
		while(incrementalBackupDirectory.exists()) {
			i++;
			this.incrementalBackupDirectory = new File(baseBackupDirectory.getAbsolutePath() + File.separator + incDir + "-" + i);			
		}
		this.showMoveCopyDialog = backup.isShowFilesBeforeMoveCopy();
		this.limitIncrementalBackups = backup.isLimitIncrementalBackups();
		this.keepAtLeastTime = backup.getKeepAtLeastTime();
		this.keepNoMoreThanTime = backup.getKeepNoMoreThanTime();
		this.keepNoMoreThanBytes = backup.getKeepNoMoreThanBytes().getBytes();
		this.leaveCopyWindowOpen = backup.isAlwaysLeaveCopyWindowOpen();
		this.scanLastBackup = backup.isScanLastBackup();
	}
	
	public void addBackupEngineListener(BackupEngineListener listener) {
		if (listeners == null) {
			listeners = new ArrayList<BackupEngineListener>();
		}
		listeners.add(listener);
	}
	
	public void removeBackupEngineListener(BackupEngineListener listener) {
		if (listeners != null) {
			listeners.remove(listener);
		}
	}
	
	public boolean isRunQuiet() {
		return runQuiet;
	}

	public void setRunQuiet(boolean runQuiet) {
		this.runQuiet = runQuiet;
	}

	public boolean isDryRun() {
		return dryRun;
	}
	
	public void setDryRun(boolean dryRun) {
		if (active) {
			throw new UnsupportedOperationException("DryRun cannot be changed while backup engine is active.");
		}
		this.dryRun = dryRun;
	}
	
	public void setDryRunSpeedFactor(Long speedFactor) {
		this.speedFactor = speedFactor;
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		this.active = true;
		try {
		
			this.stat.setBackupId(backupId);
			this.stat.setBaseBackupDirectory(baseBackupDirectory);
			this.stat.setDateStarted(new Date());
			this.stat.setIncrementalBackupDirectory(incrementalBackupDirectory);
			
			LoadCurrentFiles loadCurrentFiles = new LoadCurrentFiles(this, excludedTree, currentFilesAndDirectories, backupDirectory);
			if (dryRun) {
				loadCurrentFiles.enableDryRun(DRY_RUN_PREFIX, speedFactor);
			}
			publishStep(1, loadCurrentFiles);
			List<BackupFile> currentFiles = loadCurrentFiles.execute();		
			this.stat.setTotalFiles(loadCurrentFiles.getFilesCount());
			
			List<BackupFile> backupFiles = null;
			if (!scanLastBackup) {
				try {
					FileListData fileListData = FileManager.loadObject(latestFileListFile, FileListData.class);
					backupFiles = fileListData.getBackupFiles();
					this.backupSize = fileListData.getBackupSize();
				} catch (Exception e) {
					log.warn("Unable to load latest file list from " + latestFileListFile.getAbsolutePath());
					backupFiles = null;
				}
			}
			if (isCancelled()) {
				return null;
			}
			if (scanLastBackup || backupFiles == null) {
				this.loadBackupFiles = new LoadBackupFiles(this, excludedTree, backupDirectory, stats.getLatestStat());
				if (dryRun) {
					loadBackupFiles.enableDryRun(DRY_RUN_PREFIX, speedFactor);
				}
				publishStep(2, loadBackupFiles);
				backupFiles = loadBackupFiles.execute();
				this.backupSize = loadBackupFiles.getFilesSize();
			}
			if (isCancelled()) {
				return null;
			}			
			CompareFiles compareFiles = new CompareFiles(this, currentFiles, backupFiles);
			if (dryRun) {
				loadCurrentFiles.enableDryRun(DRY_RUN_PREFIX, speedFactor);
			}
			publishStep(3, compareFiles);
			if (showMoveCopyDialog && !runQuiet) {
				compareFiles.enableShowMoveCopyDialog(parent, fileIconCache);
			}
			if (!compareFiles.execute().booleanValue()) {
				this.stat.setBackupStatus(BackupStatus.CANCELLED_BEFORE);
				cancel(true);
				return null;
			}
			if (isCancelled()) {
				return null;
			}
			long bytesToMove = compareFiles.getBytesToMove();
			long bytesToCopy = compareFiles.getBytesToCopy();
			List<BackupFile> filesToMove = compareFiles.getFilesToMove();
			List<File> filesToCopy = compareFiles.getFilesToCopy();
			
			if (limitIncrementalBackups) {
				ApplyIncrementalBackupLimits applyBackupLimits = new ApplyIncrementalBackupLimits(this, 
						baseBackupDirectory, keepAtLeastTime, keepNoMoreThanTime, keepNoMoreThanBytes, bytesToMove);
				if (dryRun) {
					applyBackupLimits.enableDryRun(DRY_RUN_PREFIX, speedFactor);
				}
				publishStep(4, applyBackupLimits);
				applyBackupLimits.execute();
			}
			if (isCancelled()) {
				return null;
			}			
			if (!dryRun) {
				latestFileListFile.delete();  // file contents no longer valid once move/copy steps start
			}
			if (filesToMove.size() > 0) {
				this.moveFiles = new MoveFiles(this, filesToMove, filesToCopy, incrementalBackupDirectory);
				if (dryRun) {
					moveFiles.enableDryRun(DRY_RUN_PREFIX, speedFactor);
				}
				publishStep(5, moveFiles);
				moveFiles.execute();
				this.backupSize -= moveFiles.getFilesSize();
			}
			if (isCancelled()) {
				return null;
			}
			if (filesToCopy.size() > 0) {
				this.copyFiles = new CopyFiles(this, filesToCopy, bytesToCopy, 
						backupDirectory, fileIconCache, errorsUntilHalt);
				this.copyFiles.setAlwaysLeaveCopyWindowOpen(this.leaveCopyWindowOpen);
				if (dryRun) {
					this.copyFiles.enableDryRun(DRY_RUN_PREFIX, speedFactor);
				}
				publishStep(6, copyFiles);
				boolean haltedDueToErrors = copyFiles.execute();
				this.resolutionRequired = copyFiles.isResolutionRequired();
				this.copyCancelled = copyFiles.isCopyCancelled() || haltedDueToErrors;
				if (!copyCancelled) {
					this.backupSize += copyFiles.getFilesSize();
				}
			}
		
			// finally, save list of backup files to latest directory; 
			// this allows step 2 to be bypassed on next backup if scan last backup flag is off
			if (!isCancelled() && !copyCancelled) {
				publish(new BackupEngineProgress("Finishing", "Saving backup file list"));
				backupFiles.removeAll(filesToMove);
				for (File file : filesToCopy) {
					File gfile = new File(backupDirectory.getAbsolutePath() + File.separator + BackupPathGenerator.generateCriticalPath(file));
					backupFiles.add(new BackupFile(gfile, BackupFile.Type.DESTINATION, backupDirectory));
				}
				Collections.sort(backupFiles);
				if (!dryRun) {
					FileManager.saveObject(latestFileListFile, new FileListData(backupFiles, backupSize));
				}
			}
			
		} catch (Exception e) {
			log.error("Backup did not complete normally.", e);
			this.stat.setBackupStatus(BackupStatus.ERROR);
		}
		
		return null;
	}

	private void publishStep(int stepNumber, BackupEngineWorklet<?> worklet) {
		String dryRunPrefix = dryRun? DRY_RUN_PREFIX : "";
		publish(new BackupEngineProgress(dryRunPrefix + "Step " + stepNumber + "/6: " + worklet.getTitle() + "...", null));
	}
	
	@Override
	protected void done() {
		parent.removeCloseListener(this);
		this.active = false;
		new Thread(() -> {
			// performed in separate thread in case it needs to wait on completion of anything
			this.stat.setDateFinished(new Date());
			this.stat.setBackupSize(this.backupSize);
			if (this.stat.getBackupStatus() == null) {
				this.stat.setBackupStatus(isCancelled()? BackupStatus.CANCELLED_DURING : BackupStatus.COMPLETED);
			}
			if (this.moveFiles != null) {
				this.stat.setFilesMoved(moveFiles.getFilesMoved());
				this.stat.setIncrementalBackupSize(moveFiles.getFilesSize());
			}
			if (this.copyFiles != null) {
				while (!copyFiles.isCopyComplete()) { // need to wait on CopyFiles worklet to fully complete
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
				}
				this.stat.setFilesCopied(copyFiles.getFilesCopied());
			}
			try {
				if (dryRun) {
					log.info("Stats for dry run (will not be saved): " + stat.toString());
				} else {
					stats.addBackupStat(stat);
					stats.saveStats(baseBackupDirectory);
				}
			} catch (IOException ioe) {
				log.error("Unable to save backup stats.", ioe);
			}
			if (progressMonitor != null) {
				progressMonitor.setVisible(false);
			}
			String backupStatus = copyCancelled? "Backup cancelled.  Some files were not backed up." : "Backup complete.";
			if (this.stat.getBackupStatus() == BackupStatus.ERROR) {
				backupStatus = "Backup process encountered an error.  See the log for details.";
			}
			if (dryRun) {
				backupStatus = DRY_RUN_PREFIX + backupStatus;
			}
			final String backupStatusString = backupStatus;
			SwingUtilities.invokeLater(() -> {
				if (!runQuiet) {
					JOptionPane.showMessageDialog(parent, backupName + "\n" + backupStatusString + "\nOlder files moved: " + this.stat.getFilesMoved() + "\nNewer files copied: " + this.stat.getFilesCopied());
				}
				if (listeners != null) {
					for (BackupEngineListener listener : listeners) {
						listener.backupEngineComplete(backupId, resolutionRequired, copyCancelled);
					}
					listeners = null;
				}					
			});
		}).start();
	}

	@Override
	protected void process(List<BackupEngineProgress> list) {
		if (runQuiet) {
			return;
		}
		String heading = null;
		String message = null;
		long progress = -1;
		long progressMaximum = -1;
		for (BackupEngineProgress p : list) {
			if (p.getHeading() != null) {
				heading = p.getHeading();
			}
			if (p.getMessage() != null) {
				message = p.getMessage();
			}
			if (p.isProgressUpdate()) {
				progress = p.getProgress();
				progressMaximum = p.getProgressMaximum();
			}
		}
		if (progressMonitor == null) {
			progressMonitor = new ProgressMonitor(parent, "Backup Progress", heading, 0L, progressMaximum);
			progressMonitor.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					// need to force cancel the file copier if it's running and not already cancelled
					if (copyFiles != null && !copyCancelled && !copyFiles.isCopyComplete()) {
						copyCancelled = copyFiles.cancelCopy();
					}
					cancel(true);
				}
			});
			progressMonitor.setAnimated(true);
			progressMonitor.setVisible(true);
		}
		if (heading != null) {
			progressMonitor.setHeading(heading);
		}
		if (message != null) {
			progressMonitor.setMessage(message);
		}
		if (progress >= 0) {
			progressMonitor.setMaximum(progressMaximum);
			progressMonitor.setProgress(progress);
			log.debug("Progress " + progress + " / " + progressMaximum);
		}
	}

	public void workletProgress(long progress, long progressMaximum) {
		publish(new BackupEngineProgress(progress, progressMaximum));
	}

	public void workletPublish(String message) {
		publish(new BackupEngineProgress(null, message));
	}

	public boolean closeAction(WindowEvent event) {
		JOptionPane.showMessageDialog(parent, 
				"A backup is currently in progress.\nThe backup must be cancelled or allowed to complete before the application can be closed.",
				"Backup In Progress",
				JOptionPane.WARNING_MESSAGE);
		return false;
	}
}
