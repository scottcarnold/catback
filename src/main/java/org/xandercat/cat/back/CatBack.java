package org.xandercat.cat.back;

import java.io.IOException;
import java.util.Properties;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
//import org.xandercat.cat.back.CatBackSettings;
//import org.xandercat.cat.back.ui.CatBackFrame;
//import org.xandercat.cat.back.ui.ImmediateFileBackupFrame;
//import org.xandercat.common.ui.file.FileManager;
//import org.xandercat.common.ui.file.FilesSizeMonitor;
//import org.xandercat.common.util.ArgumentProcessor;
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
	public static final String FILES_SIZE_MONITOR_KEY = "-monitor";
	
	private static final Logger log = LogManager.getLogger(CatBack.class);
	
	public static void main(String[] args) {
		
		// process command-line arguments
//		ArgumentProcessor argumentProcessor = new ArgumentProcessor();
//		argumentProcessor.addValidSwitchValuePair(IMMEDIATE_BACKUP_KEY, "<filename>", "Backup the given Backup Profile", null);
//		argumentProcessor.addValidSwitch(FILES_SIZE_MONITOR_KEY, "Activates a performance monitor window");
//		LoggingUtil.addArgumentProcessorSwitchValuePair(argumentProcessor, LOG_KEY);
//		argumentProcessor.process(args);
//		Properties defaultProperties = LoggingUtil.getBasicFileLoggingProperties(
//				"catback.log", Level.INFO, "CAT", "org.xandercat");
//		LoggingUtil.configureLogging(argumentProcessor, LOG_KEY, LoggingUtil.PROPERTIES, defaultProperties);
//		log.debug("Logging configured.");
		PlatformTool.setApplicationNameOnMac(APPLICATION_NAME);
//		final String backupFilename = argumentProcessor.getValueForSwitch(IMMEDIATE_BACKUP_KEY);
//		final boolean normalMode = backupFilename == null;
//		final boolean activateMonitor = argumentProcessor.isSwitchPresent(FILES_SIZE_MONITOR_KEY);

//		// load application settings
//		Object settingsObject = null;
//		CatBackSettings settings = null;
//		try {
//			settingsObject = FileManager.loadObject(CatBackSettings.SETTINGS_FILE, CatBackSettings.class);
//		} catch (IOException ioe) {
//			log.warn("Unable to load catback settings.", ioe);
//		}
//		if (settingsObject != null && settingsObject instanceof CatBackSettings) {
//			settings = (CatBackSettings) settingsObject;
//			log.info("Settings loaded.");
//		} else {
//			settings = new CatBackSettings();
//			if (PlatformTool.isWindows()) {
//				// set default Windows L&F to Nimbus
//				for (LookAndFeelInfo lafInfo : UIManager.getInstalledLookAndFeels()) {
//					if (lafInfo.getName().toLowerCase().contains("nimbus")) {
//						settings.setLookAndFeelName(lafInfo.getName());
//						break;
//					}
//				}
//			}
//			log.info("Using default application settings.");
//		}
//		final CatBackSettings finalSettings = settings;
//		
//		// set Look and Feel
//		if (normalMode && finalSettings.getLookAndFeelName() != null) {
//			for (LookAndFeelInfo lafInfo : UIManager.getInstalledLookAndFeels()) {
//				if (finalSettings.getLookAndFeelName().equals(lafInfo.getName())) {
//					try {
//						UIManager.setLookAndFeel(lafInfo.getClassName());
//					} catch (Exception e) {
//						log.error("Unable to set up Look and Feel " + lafInfo.getName(), e);
//					}
//					break;
//				}
//			}
//		}
//		
//		// launch application
//		SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
//				if (normalMode) {
//					launchUI(activateMonitor, finalSettings);
//				} else {
//					launchImmediateBackup(backupFilename);
//				}
//			}
//		}); 
	}

//	private static void launchUI(boolean activateMonitor, CatBackSettings settings) {
//		if (activateMonitor) {
//			FilesSizeMonitor monitor = new FilesSizeMonitor();
//			monitor.setVisible(true);
//		}
//		CatBackFrame ui = new CatBackFrame(settings);
//		ui.setVisible(true);
//	}
//	
//	public static void launchImmediateBackup(String backupProfileFilename) {
//		ImmediateFileBackupFrame ui = new ImmediateFileBackupFrame(backupProfileFilename);
//		ui.setVisible(true);
//	}
}
