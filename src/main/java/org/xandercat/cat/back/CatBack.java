package org.xandercat.cat.back;

import java.io.IOException;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xandercat.cat.back.swing.frame.CatBackFrame;
import org.xandercat.cat.back.swing.frame.ImmediateFileBackupFrame;
import org.xandercat.swing.file.FileManager;
import org.xandercat.swing.log.LoggingConfigurer;
import org.xandercat.swing.util.ArgumentProcessor;
import org.xandercat.swing.util.PlatformTool;


/**
 * CatBack is a light weight backup program for backing up user files.  While there is a fair amount
 * of backup software already out there, CatBack differentiates itself in the following ways:
 * 
 * 1)  Simple and easy to use.  Complex features simply aren't worth the trouble.
 * 2)  Store backup files in a simple easy to navigate directory structure.  Restoring files is 
 *     not dependent on the backup software.
 * 3)  Easy to install.  No complicated install process, no databases, no hassle.
 * 4)  Works on virtually all platforms.
 * 5)  Small footprint.  No unnecessary dependencies. 
 * 
 * This application is essentially just a fancy file copy utility capable of doing file comparisons
 * for incremental file backup.  It exists in a niche somewhere between fully featured backup applications
 * and just using a shell script for copying your important folders.  
 */
public class CatBack {

	private static final String APPLICATION_NAME = "CatBack";
	private static final String APPLICATION_VERSION = "1.5";
	
	public static final String LOG_KEY = "-l";
	public static final String IMMEDIATE_BACKUP_KEY = "-b";
	public static final String DRY_RUN_KEY = "-dryrun";
	public static final String DRY_RUN_SPEED_KEY = "-speed";
	
	private static final Logger log = LogManager.getLogger(CatBack.class);
	
	public static void main(String[] args) {
		
		// process command-line arguments
		ArgumentProcessor argumentProcessor = new ArgumentProcessor();
		argumentProcessor.addValidSwitchValuePair(IMMEDIATE_BACKUP_KEY, "<filename>", "Backup the given Backup Profile", null);
		LoggingConfigurer.addArgumentProcessorSwitchValuePair(argumentProcessor, LOG_KEY);
		argumentProcessor.addValidSwitch(DRY_RUN_KEY, "Execute backups as simulations without actually updating the file systems.");
		argumentProcessor.addValidSwitchValuePair(DRY_RUN_SPEED_KEY, "<number>", "Speed factor that impacts simulated file copy time.", "^[\\d]*$");
		argumentProcessor.process(args);
		if (LoggingConfigurer.configureLogging(argumentProcessor, LOG_KEY, Level.INFO, LoggingConfigurer.Target.FILE, "catback.log")) {
			log.info("Logging configuration updated.  Application will need to be restarted for changes to take effect.");
		} 
		PlatformTool.setApplicationNameOnMac(APPLICATION_NAME);
		final String backupFilename = argumentProcessor.getValueForSwitch(IMMEDIATE_BACKUP_KEY);
		final boolean dryRun = argumentProcessor.isSwitchPresent(DRY_RUN_KEY);
		String dryRunSpeedFactorValue = argumentProcessor.getValueForSwitch(DRY_RUN_SPEED_KEY);
		final Long dryRunSpeedFactor;
		if (dryRunSpeedFactorValue != null && dryRunSpeedFactorValue.length() > 0) {
			dryRunSpeedFactor = Long.valueOf(dryRunSpeedFactorValue);
		} else {
			dryRunSpeedFactor = null;
		}

		// load application settings
		Object settingsObject = null;
		final CatBackSettings settings;
		try {
			settingsObject = FileManager.loadObject(CatBackSettings.SETTINGS_FILE, CatBackSettings.class);
		} catch (IOException ioe) {
			log.warn("Unable to load catback settings.", ioe);
		}
		if (settingsObject != null && settingsObject instanceof CatBackSettings) {
			settings = (CatBackSettings) settingsObject;
			log.info("Settings loaded.");
		} else {
			settings = new CatBackSettings();
			if (PlatformTool.isWindows()) {
				// set default Windows L&F to Nimbus
				for (LookAndFeelInfo lafInfo : UIManager.getInstalledLookAndFeels()) {
					if (lafInfo.getName().toLowerCase().contains("nimbus")) {
						settings.setLookAndFeelName(lafInfo.getName());
						break;
					}
				}
			}
			log.info("Using default application settings.");
		}
		
		// set Look and Feel
		if (settings.getLookAndFeelName() != null) {
			for (LookAndFeelInfo lafInfo : UIManager.getInstalledLookAndFeels()) {
				if (settings.getLookAndFeelName().equals(lafInfo.getName())) {
					try {
						UIManager.setLookAndFeel(lafInfo.getClassName());
					} catch (Exception e) {
						log.error("Unable to set up Look and Feel " + lafInfo.getName(), e);
					}
					break;
				}
			}
		}
		
		// launch application
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (backupFilename == null) {
					launchUI(settings, dryRun, dryRunSpeedFactor);
				} else {
					launchImmediateBackup(backupFilename);
				}
			}
		}); 
	}

	private static void launchUI(CatBackSettings settings, boolean dryRun, Long dryRunSpeedFactor) {
		CatBackFrame ui = new CatBackFrame(APPLICATION_NAME, APPLICATION_VERSION, settings);
		if (dryRun) { 
			ui.setDryRun(dryRun, dryRunSpeedFactor);
		}
		ui.setVisible(true);
	}
	
	public static void launchImmediateBackup(String backupProfileFilename) {
		ImmediateFileBackupFrame ui = new ImmediateFileBackupFrame(APPLICATION_NAME, APPLICATION_VERSION, backupProfileFilename);
		ui.setVisible(true);
	}
}
