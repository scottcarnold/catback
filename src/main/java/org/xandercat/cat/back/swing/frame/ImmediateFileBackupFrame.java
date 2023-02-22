package org.xandercat.cat.back.swing.frame;

import java.io.File;
import java.util.List;

import javax.swing.JOptionPane;

import org.xandercat.cat.back.CatBackup15;
import org.xandercat.cat.back.engine.BackupEngine;
import org.xandercat.cat.back.engine.BackupEngineListener;
import org.xandercat.cat.back.engine.BackupStats;
import org.xandercat.swing.app.ApplicationFrame;
import org.xandercat.swing.file.FileManager;
import org.xandercat.swing.file.icon.FileIconCache;
import org.xandercat.swing.file.icon.FileIconSet;
import org.xandercat.swing.file.icon.FileIconSetFactory;
import org.xandercat.swing.tree.CheckboxFileTree;
import org.xandercat.swing.tree.CheckboxFileTreeFactory;
import org.xandercat.swing.worker.SwingWorkerUtil;
import org.xandercat.swing.zenput.processor.Processor;
import org.xandercat.swing.zenput.processor.SourceProcessor;

/**
 * ImmediateFileBackupFrame performs an immediate backup on the specified backup profile.
 * If there are no errors during the backup process, it will then exit.
 * 
 * @author Scott C Arnold
 */
public class ImmediateFileBackupFrame extends ApplicationFrame implements BackupEngineListener {

	private static final long serialVersionUID = 2009032701L;
	
	private String backupProfileFilename;
	
	public ImmediateFileBackupFrame(String applicationName, String applicationVersion, String backupProfileFilename) {
		super(applicationName, applicationVersion);
		this.backupProfileFilename = backupProfileFilename;
		setSize(300, 50);
		setLocation(0, 0);
	}
	
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			try {
				CatBackup15 backup = (CatBackup15) FileManager.loadObject(backupProfileFilename, CatBackup15.class);
				Processor processor = new SourceProcessor(backup);
				if (!processor.validate()) {
					throw new IllegalArgumentException("Backup settings are invalid.");
				}
				FileIconSet fileIconSet = FileIconSetFactory.buildIconSet(FileIconSetFactory.GLAZE);
				FileIconCache fileIconCache = new FileIconCache(fileIconSet);
				CheckboxFileTree excludedTree = CheckboxFileTreeFactory.createCheckboxFileTree(false, false, fileIconCache);
				List<File> excludedFiles = backup.getExcludedFiles();
				if (excludedFiles != null) {
					for (File bfile : excludedFiles) {
						excludedTree.selectAddFile(bfile, backup.wasDirectory(bfile), true);
					}
				}			
				BackupStats stats = null;
				try {
					stats = new BackupStats(backup.getBackupDirectory());
				} catch (Exception e) {
					//TODO: Might want to do something about the error here
					stats = new BackupStats();
				}
				BackupEngine backupEngine = new BackupEngine(this, backup, fileIconCache, excludedTree, stats);
				backupEngine.addBackupEngineListener(this);
				backupEngine.setRunQuiet(true);
				SwingWorkerUtil.execute(backupEngine);
			} catch (Exception e) {
				final String message = e.getMessage();
				System.out.println("Unable to complete backup.\n" + message);
				JOptionPane.showMessageDialog(this, 
						"Unable to perform backup.\n" + message, 
						"Backup Error", JOptionPane.ERROR_MESSAGE);
				backupEngineComplete(null, false, false);
			}
		}
	}
	
	public void backupEngineComplete(String backupId, boolean resolutionRequired, boolean copyCancelled) {
		if (resolutionRequired) {
			JOptionPane.showMessageDialog(this, 
					"Backup did not fully complete.  Some files require resolution.\nCheck the file copy frame for more details.", 
					"Backup Incomplete", JOptionPane.WARNING_MESSAGE);			
		} else {
			closeApplication();
		}
	}
}
