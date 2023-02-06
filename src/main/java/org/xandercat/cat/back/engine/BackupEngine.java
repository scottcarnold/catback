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

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xandercat.cat.back.CatBackup;
import org.xandercat.cat.back.engine.worklet.ApplyIncrementalBackupLimits;
import org.xandercat.cat.back.engine.worklet.BackupEngineWorklet;
import org.xandercat.cat.back.engine.worklet.CompareFiles;
import org.xandercat.cat.back.engine.worklet.CopyFiles;
import org.xandercat.cat.back.engine.worklet.LoadBackupFiles;
import org.xandercat.cat.back.engine.worklet.LoadCurrentFiles;
import org.xandercat.cat.back.engine.worklet.MoveFiles;
import org.xandercat.cat.back.file.BackupFile;
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
	
	public static final String LATEST_BACKUP_DIR_NAME = "latest";
	public static final String LATEST_FILE_LIST_FILE_NAME = ".cb_filelist";
	public static final String INCREMENTAL_SIZE_FILE_NAME = ".cb_size";
	public static final String INCREMENTAL_BACKUP_DIR_NAME_DATE_PATTERN = "yyyyMMdd";
	public static final String INCREMENTAL_BACKUP_DIR_NAME_REGEX_PATTERN = "[\\d]{8}|[\\d]{8}[-][\\d]{1,4}";
	public static final String STATS_FILENAME = "backup.txt";
	
	private ApplicationFrame parent;
	private String backupId;
	private String backupName;
	private List<BackupEngineListener> listeners;
	private FileIconCache fileIconCache;
	private CheckboxFileTree excludedTree;
	private List<File> currentFilesAndDirectories;
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
	
	public BackupEngine(ApplicationFrame parent, 
			CatBackup backup, 
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

	@SuppressWarnings("unchecked")
	@Override
	protected Void doInBackground() throws Exception {
		
		try {
		
			this.stat.setBackupId(backupId);
			this.stat.setBaseBackupDirectory(baseBackupDirectory);
			this.stat.setDateStarted(new Date());
			this.stat.setIncrementalBackupDirectory(incrementalBackupDirectory);
			
			LoadCurrentFiles loadCurrentFiles = new LoadCurrentFiles(this, excludedTree, currentFilesAndDirectories, backupDirectory);
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
			if (scanLastBackup || backupFiles == null) {
				this.loadBackupFiles = new LoadBackupFiles(this, excludedTree, backupDirectory, stats.getLatestStat());
				publishStep(2, loadBackupFiles);
				backupFiles = loadBackupFiles.execute();
				this.backupSize = loadBackupFiles.getFilesSize();
			}
			
			CompareFiles compareFiles = new CompareFiles(this, currentFiles, backupFiles);
			publishStep(3, compareFiles);
			if (showMoveCopyDialog && !runQuiet) {
				compareFiles.enableShowMoveCopyDialog(parent, fileIconCache);
			}
			if (!compareFiles.execute().booleanValue()) {
				this.stat.setBackupStatus(BackupStatus.CANCELLED_BEFORE);
				cancel(true);	// TODO: Should this be done on the EDT?
				return null;
			}
			long bytesToMove = compareFiles.getBytesToMove();
			long bytesToCopy = compareFiles.getBytesToCopy();
			List<BackupFile> filesToMove = compareFiles.getFilesToMove();
			List<File> filesToCopy = compareFiles.getFilesToCopy();
			
			if (limitIncrementalBackups) {
				ApplyIncrementalBackupLimits applyBackupLimits = new ApplyIncrementalBackupLimits(this, 
						baseBackupDirectory, keepAtLeastTime, keepNoMoreThanTime, keepNoMoreThanBytes, bytesToMove);
				publishStep(4, applyBackupLimits);
				applyBackupLimits.execute();
			}
			
			latestFileListFile.delete();  // file contents no longer valid once move/copy steps start
			
			if (filesToMove.size() > 0) {
				this.moveFiles = new MoveFiles(this, filesToMove, filesToCopy, incrementalBackupDirectory);
				publishStep(5, moveFiles);
				moveFiles.execute();
				this.backupSize -= moveFiles.getFilesSize();
			}
			
			if (filesToCopy.size() > 0) {
				this.copyFiles = new CopyFiles(this, filesToCopy, bytesToCopy, 
						backupDirectory, fileIconCache, errorsUntilHalt);
				this.copyFiles.setAlwaysLeaveCopyWindowOpen(this.leaveCopyWindowOpen);
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
				FileManager.saveObject(latestFileListFile, new FileListData(backupFiles, backupSize));
			}
			
		} catch (Exception e) {
			log.error("Backup did not complete normally.", e);
			this.stat.setBackupStatus(BackupStatus.ERROR);
		}
		
		return null;
	}

	private void publishStep(int stepNumber, BackupEngineWorklet<?> worklet) {
		publish(new BackupEngineProgress("Step " + stepNumber + "/6: " + worklet.getTitle() + "...", null));
	}
	
	@Override
	protected void done() {
		parent.removeCloseListener(this);
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
			this.stat.setFilesCopied(copyFiles.getFilesCopied());
		}
		try {
			stats.addBackupStat(stat);
			stats.saveStats(baseBackupDirectory);
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
		if (!runQuiet) {
			JOptionPane.showMessageDialog(parent, backupName + "\n" + backupStatus + "\nOlder files moved: " + this.stat.getFilesMoved() + "\nNewer files copied: " + this.stat.getFilesCopied());
		}
		if (listeners != null) {
			for (BackupEngineListener listener : listeners) {
				listener.backupEngineComplete(backupId, resolutionRequired, copyCancelled);
			}
			listeners = null;
		}
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
