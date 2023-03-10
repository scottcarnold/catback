package org.xandercat.cat.back.engine;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;

import org.xandercat.cat.back.CatBackup15;
import org.xandercat.cat.back.swing.frame.CatBackFrame;
import org.xandercat.swing.file.FileManager;
import org.xandercat.swing.file.FileManagerListener;
import org.xandercat.swing.file.icon.FileIconCache;
import org.xandercat.swing.tree.CheckboxFileTree;
import org.xandercat.swing.util.ResourceManager;
import org.xandercat.swing.worker.SwingWorkerUtil;
import org.xandercat.swing.zenput.error.ValidationException;
import org.xandercat.swing.zenput.processor.InputProcessor;
import org.xandercat.swing.zenput.util.ValidationErrorUtil;

/**
 * Manager for controlling the launch of backups.
 * 
 * @author Scott Arnold
 */
public class BackupEngineManager implements FileManagerListener<CatBackup15>{

	/**
	 * Action for beginning the backup process.  Action is enabled if a backup is open and valid
	 * and a backup started by this action is not already in progress.
	 * 
	 * @author Scott Arnold
	 */
	private class BeginBackupAction extends AbstractAction implements BackupEngineListener {

		private static final long serialVersionUID = 2010080901L;
		
		public BeginBackupAction() {
			super("Begin Backup");			
			updateEnabled();
		}
		
		private void updateEnabled() {
			CatBackup15 backup = backupFileManager.getObject();
			setEnabled(backup != null 
					&& inputProcessor != null 
					&& !executingBackupIDs.contains(backup.getId()));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			CatBackup15 backup = backupFileManager.getObject();
			List<ValidationException> validationExceptions = inputProcessor.getErrors();
			if (validationExceptions == null || validationExceptions.size() == 0) {
				executingBackupIDs.add(backup.getId());
				updateEnabled();
				FileIconCache fileIconCache = ResourceManager.getInstance().getResource(FileIconCache.class);
				BackupEngine backupEngine = new BackupEngine(catBackFrame, backup, fileIconCache, excludedTree, stats);
				backupEngine.setDryRun(BackupEngineManager.this.dryRun);
				backupEngine.setDryRunSpeedFactor(speedFactor);
				backupEngine.addBackupEngineListener(this);
				for (BackupEngineListener listener : backupEngineListeners) {
					backupEngine.addBackupEngineListener(listener);
				}
				SwingWorkerUtil.execute(backupEngine);
			} else {
				ValidationErrorUtil.showMessageDialog(catBackFrame, 
						inputProcessor, 
						validationExceptions,
						"The following errors must be corrected before a backup can be started:");
			}
		}

		@Override
		public void backupEngineComplete(String backupId, boolean resolutionRequired, boolean copyCancelled) {
			executingBackupIDs.remove(backupId);
			updateEnabled();
		}
	}
	
	private final CatBackFrame catBackFrame;
	private final FileManager<CatBackup15> backupFileManager;
	private final Set<String> executingBackupIDs = new HashSet<String>();
	private InputProcessor inputProcessor;
	private CheckboxFileTree excludedTree;
	private BackupStats stats;
	private BeginBackupAction beginBackupAction;
	private final Set<BackupEngineListener> backupEngineListeners = new HashSet<BackupEngineListener>();
	private boolean dryRun;
	private Long speedFactor;
	
	public BackupEngineManager(CatBackFrame catBackFrame, FileManager<CatBackup15> backupFileManager) {
		this.catBackFrame = catBackFrame;
		this.backupFileManager = backupFileManager;
		this.backupFileManager.addFileManagerListener(this);
		this.beginBackupAction = new BeginBackupAction();		
	}
	
	public void addBackupEngineListener(BackupEngineListener listener) {
		backupEngineListeners.add(listener);
	}
	
	public void removeBackupEngineListener(BackupEngineListener listener) {
		backupEngineListeners.remove(listener);
	}
	
	public boolean isDryRun() {
		return dryRun;
	}

	/**
	 * Sets the backup system "dry run" mode.  When in dry run mode, any launched backups will be simulated.
	 * The backup engine will go through many of the normal steps in executing a backup, but no actual changes to 
	 * the file system will take place.
	 * 
	 * @param dryRun  when true, backups are simulated.
	 */
	public void setDryRun(boolean dryRun) {
		this.dryRun = dryRun;
	}

	public void setDryRunSpeedFactor(Long speedFactor) {
		this.speedFactor = speedFactor;
	}
	
	/**
	 * Returns an action that can be used to initiate backup.
	 * 
	 * @return		Action that can be used to initiate backup.
	 */
	public AbstractAction getBeginBackupAction() {
		return beginBackupAction;
	}
	
	/**
	 * Initializes the BackupEngineManager for a new backup.
	 * 
	 * @param inputProcessor	InputProcessor for backup input fields
	 * @param backupStats		Backup statistics for backup
	 */
	public void initializeForBackup(InputProcessor inputProcessor, CheckboxFileTree excludedTree, BackupStats backupStats) {
		this.inputProcessor = inputProcessor;
		this.excludedTree = excludedTree;
		this.stats = backupStats;
		beginBackupAction.updateEnabled();
	}
	
	@Override
	public void afterOpen(String key) {
		// no action required
	}

	@Override
	public void afterClose() {
		this.inputProcessor = null;
		this.excludedTree = null;
		beginBackupAction.updateEnabled();
	}

	@Override
	public void beforeSaveOrClose(CatBackup15 toSave) {
		// no action required	
	}

	@Override
	public void filePathChange(String newAbsolutePath) {
		// no action required
	}	
}
