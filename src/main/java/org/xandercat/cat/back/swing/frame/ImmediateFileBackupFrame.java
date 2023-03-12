package org.xandercat.cat.back.swing.frame;

import java.awt.FlowLayout;
import java.io.File;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
	private static final Logger log = LogManager.getLogger(ImmediateFileBackupFrame.class);
	private boolean dryRun;
	private Long dryRunSpeedFactor;
	
	private String backupProfileFilename;
	
	public ImmediateFileBackupFrame(String applicationName, String applicationVersion, String backupProfileFilename) {
		super(applicationName, applicationVersion);
		this.backupProfileFilename = backupProfileFilename;
		JPanel panel = new JPanel(new FlowLayout());
		JLabel label = new JLabel("Running backup: " + backupProfileFilename);
		panel.add(label);
		setContentPane(panel);
		pack();
		setLocation(0, 0);
	}
	
	public void setDryRun(boolean dryRun, Long dryRunSpeedFactor) {
		this.dryRun = dryRun;
		this.dryRunSpeedFactor = dryRunSpeedFactor;
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
				BackupStats stats = new BackupStats(backup.getBackupDirectory());
				BackupEngine backupEngine = new BackupEngine(this, backup, fileIconCache, excludedTree, stats);
				backupEngine.addBackupEngineListener(this);
				backupEngine.setDryRun(dryRun);
				backupEngine.setDryRunSpeedFactor(dryRunSpeedFactor);
				backupEngine.setRunQuiet(true);
				log.info("Launching immediate backup: " + backupProfileFilename);
				SwingWorkerUtil.execute(backupEngine);
			} catch (Exception e) {
				log.error("Backup could not be completed.", e);
				JOptionPane.showMessageDialog(this, 
						"Unable to perform backup.\n" + e.getMessage(), 
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
