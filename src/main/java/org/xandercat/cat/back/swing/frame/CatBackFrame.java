package org.xandercat.cat.back.swing.frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xandercat.cat.back.BackupSizeCalculator;
import org.xandercat.cat.back.CatBackSettings;
import org.xandercat.cat.back.CatBackup15;
import org.xandercat.cat.back.CatBackupTreeStates;
import org.xandercat.cat.back.CatBackupUpdater;
import org.xandercat.cat.back.engine.BackupEngineManager;
import org.xandercat.cat.back.engine.BackupStats;
import org.xandercat.cat.back.importer.OldBackupImporter;
import org.xandercat.cat.back.media.Icons;
import org.xandercat.cat.back.media.Images;
import org.xandercat.cat.back.media.UserGuide;
import org.xandercat.cat.back.swing.dialog.BackupHistoryDialog;
import org.xandercat.cat.back.swing.panel.BackupResources;
import org.xandercat.cat.back.swing.panel.CatBackPanelHandler;
import org.xandercat.cat.back.swing.panel.CatBackPanelHandlerListCellRenderer;
import org.xandercat.cat.back.swing.panel.CheckboxFileTreePanel;
import org.xandercat.cat.back.swing.panel.EmptyPanel;
import org.xandercat.cat.back.swing.panel.NameLocationPanel;
import org.xandercat.cat.back.swing.panel.SettingsPanel;
import org.xandercat.cat.back.swing.panel.SummaryPanel;
import org.xandercat.cat.back.swing.tree.CatBackFileTreeCellRenderer;
import org.xandercat.cat.back.swing.zenput.validator.TimeDurationValidator;
import org.xandercat.swing.app.ApplicationFrame;
import org.xandercat.swing.component.ButtonFactory;
import org.xandercat.swing.dialog.AboutDialog;
import org.xandercat.swing.file.FileManager;
import org.xandercat.swing.file.FileManagerListener;
import org.xandercat.swing.file.SaveOnCloseAction;
import org.xandercat.swing.file.SaveOnCloseObjectFinalizer;
import org.xandercat.swing.file.icon.FileIconCache;
import org.xandercat.swing.file.icon.FileIconSet;
import org.xandercat.swing.file.icon.FileIconSetFactory;
import org.xandercat.swing.label.VersionLabel;
import org.xandercat.swing.laf.LookAndFeelSelectionDialog;
import org.xandercat.swing.log.LoggingConfigurer;
import org.xandercat.swing.menu.RecentlyLoadedActionEvent;
import org.xandercat.swing.menu.RecentlyLoadedActionListener;
import org.xandercat.swing.menu.RecentlyLoadedFilesManager;
import org.xandercat.swing.panel.DesignerPanel;
import org.xandercat.swing.tree.CheckboxFileTree;
import org.xandercat.swing.tree.CheckboxFileTreeFactory;
import org.xandercat.swing.tree.CheckboxTreeCellEditor;
import org.xandercat.swing.tree.CheckboxTreeCellRenderer;
import org.xandercat.swing.tree.TreeState;
import org.xandercat.swing.util.PlatformTool;
import org.xandercat.swing.util.ResourceManager;
import org.xandercat.swing.zenput.error.ZenputException;
import org.xandercat.swing.zenput.marker.MarkerFactory;
import org.xandercat.swing.zenput.processor.CommitMode;
import org.xandercat.swing.zenput.processor.InputProcessor;
import org.xandercat.swing.zenput.processor.Processor;
import org.xandercat.swing.zenput.processor.SourceProcessor;

/**
 * Main frame for the CatBack application.
 * 
 * The following object types are maintained in the ResourceManager:
 * <ul>
 * <li>FileIconCache - registered at all times</li>
 * </ul>
 * 
 * @author Scott Arnold
 */
public class CatBackFrame extends ApplicationFrame implements 
		FileManagerListener<CatBackup15>, 
		RecentlyLoadedActionListener,
		ListSelectionListener {

	private static final long serialVersionUID = 2010072301L;
	private static final Logger log = LogManager.getLogger(CatBackFrame.class);
	
	// management
	private CatBackSettings settings;
	private FileManager<CatBackup15> fileManager;
	private RecentlyLoadedFilesManager recentlyLoadedFilesManager;
	private CheckboxFileTree includedTree;
	private CheckboxFileTree excludedTree;
	private CatBackupUpdater catBackupUpdater;
	private InputProcessor inputProcessor;
	private BackupSizeCalculator backupSizeCalculator;
	private BackupStats backupStats;
	
	// windows
	private AboutDialog aboutDialog;
	private LookAndFeelSelectionDialog lafSelectionDialog;
	private CatBackPanelHandler previousPanelHandler;
	private CatBackPanelHandler emptyPanelHandler;
	private List<CatBackPanelHandler> catBackPanelHandlers;
	
	// UI components for this window
	private JList<CatBackPanelHandler> catBackPanelList;
	private DesignerPanel listPanel;
	private JPanel mainPanel;
	private JPanel catBackNavPanel;
	private JScrollPane catBackPanelScrollPane;
	private JComponent currentCatBackPanel;
	private JButton navPreviousButton;
	private JButton navNextButton;
	private JPanel navLabelPanel;
	private JLabel navLabel;
	
	/**
	 * Constructs a new CatBack main application frame.
	 * 
	 * The CatBack settings file is passed in as an argument as it will
	 * have already been loaded in order to set up the look and feel.
	 * 
	 * @param settings		CatBack settings file
	 */
	public CatBackFrame(String applicationName, String applicationVersion, CatBackSettings settings) {
		super(applicationName, applicationVersion);
		setSplashStatus(applicationName + " " + applicationVersion + " Loading...");
		PlatformTool.useScreenMenuBarOnMac();
		this.settings = settings;

		// set the window icon for Windows platform
		if (PlatformTool.isWindows()) {
			setIconImage(Icons.CATBACK_ICON.getImage());
		}
		
		// save settings on close
		SaveOnCloseObjectFinalizer<CatBackSettings> settingsFinalizer = new SaveOnCloseObjectFinalizer<CatBackSettings>() {
			@Override
			public void finalizeObjectState(CatBackSettings settings) {
				settings.getFrameState().store(CatBackFrame.this);
			}
		};
		addCloseListener(new SaveOnCloseAction<CatBackSettings>(CatBackSettings.SETTINGS_FILE, settings, settingsFinalizer));

		// create file manager and recently loaded files manager
		this.fileManager = new FileManager<CatBackup15>(this, "catback", "Backup");
		this.fileManager.setClassChecked(CatBackup15.class);
		this.fileManager.setImporter(new OldBackupImporter());
		this.recentlyLoadedFilesManager = new RecentlyLoadedFilesManager(settings.getRecentlyLoadedFiles(), CatBackSettings.MAX_RECENTLY_LOADED_FILES);
		this.recentlyLoadedFilesManager.addRecentlyLoadedActionListener(this);
		this.fileManager.setRecentlyLoadedFilesManager(this.recentlyLoadedFilesManager);
		this.fileManager.addFileManagerListener(this);	// in order to store tree states before save
		this.fileManager.setNewFileDescription("new backup");
		
		// create About dialog
		ImageIcon aboutImageIcon = new ImageIcon(Images.getImage(Images.CATBACK));
		InputStream aboutMarkdownIS = getClass().getResourceAsStream("/RELEASE_NOTES.md");
		this.aboutDialog = new AboutDialog(this);
		this.aboutDialog.setImageIcon(aboutImageIcon);
		this.aboutDialog.addMarkdownContent(aboutMarkdownIS, "background-color: #F0F0F0; padding-left: 10px; padding-right: 10px");
		this.aboutDialog.build();
		
		// create icon cache
		FileIconSet fileIconSet = FileIconSetFactory.buildIconSet(FileIconSetFactory.GLAZE);
		ResourceManager.getInstance().register(new FileIconCache(fileIconSet));
		
		// create Backup Engine Manager
		// needs to be done after creating icon cache but before creating CatBack panel resources
		BackupEngineManager bem = new BackupEngineManager(this, fileManager);
		ResourceManager.getInstance().register(bem);
		
		// create Look and Feel dialog
		this.lafSelectionDialog = new LookAndFeelSelectionDialog(this);
		
		// create CatBack panel resources
		this.emptyPanelHandler = new CatBackPanelHandler(new EmptyPanel(this));
		this.emptyPanelHandler.setNavBackAvailable(false);
		this.emptyPanelHandler.setNavForwardAvailable(false);
		this.catBackPanelHandlers = new ArrayList<CatBackPanelHandler>();
		CatBackPanelHandler cbpHandler = new CatBackPanelHandler(new SummaryPanel(this, this.fileManager));
		cbpHandler.setNavForwardText("Configure");
		cbpHandler.setNavBackAvailable(false);
		this.catBackPanelHandlers.add(cbpHandler);
		cbpHandler = new CatBackPanelHandler(new NameLocationPanel());
		cbpHandler.setNavBackAvailable(false);
		this.catBackPanelHandlers.add(cbpHandler);
		cbpHandler = new CatBackPanelHandler(new SettingsPanel());
		this.catBackPanelHandlers.add(cbpHandler);
		cbpHandler = new CatBackPanelHandler(new CheckboxFileTreePanel(CheckboxFileTreePanel.INCLUDED));
		this.catBackPanelHandlers.add(cbpHandler);
		cbpHandler = new CatBackPanelHandler(new CheckboxFileTreePanel(CheckboxFileTreePanel.EXCLUDED));
		cbpHandler.setNavForwardText("Summary");
		this.catBackPanelHandlers.add(cbpHandler);
		
		// create UI components for this window
		this.catBackPanelList = new JList(this.catBackPanelHandlers.toArray());
		this.catBackPanelList.setCellRenderer(new CatBackPanelHandlerListCellRenderer());
		this.catBackPanelList.setEnabled(false);
		this.catBackPanelList.setOpaque(false);
		this.listPanel = new DesignerPanel();
		try {
			this.listPanel.addTiledImage(Images.getImage(Images.GLACIER), 0.25f);
			Dimension catSize = new Dimension(70,50);
			Dimension offset = new Dimension(0, -20);
			this.listPanel.addSingleImage(Images.getImage(Images.CATBACK), catSize, true, SwingConstants.SOUTH_EAST, 0.25f, offset);
		} catch (Exception e) {
			log.error("Unable to setup images/textures for list panel.", e);
		}
		this.navNextButton = createNavButton("Next", Icons.NEXT_ICON);
		this.navPreviousButton = createNavButton("Previous", Icons.PREVIOUS_ICON);
		this.navNextButton.setHorizontalTextPosition(SwingConstants.LEFT);
		this.navNextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				int idx = catBackPanelList.getSelectedIndex();
				if (idx >= 0) {
					if (idx == (catBackPanelList.getModel().getSize()-1)) {
						catBackPanelList.setSelectedIndex(0);
					} else {
						catBackPanelList.setSelectedIndex(idx+1);
					}
				}
			}
		});
		this.navPreviousButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				int idx = catBackPanelList.getSelectedIndex();
				if (idx >= 0) {
					if (idx == 0) {
						catBackPanelList.setSelectedIndex(catBackPanelList.getModel().getSize()-1);
					} else {
						catBackPanelList.setSelectedIndex(idx-1);
					}
				}
			}
		});
		
		// create menu
		setJMenuBar(buildMenuBar());		
		
		// layout UI components for this window
		this.mainPanel = new JPanel(new BorderLayout());
		this.listPanel.setLayout(new BorderLayout());
		this.listPanel.add(this.catBackPanelList, BorderLayout.CENTER);
		JPanel versionPanel = new JPanel(new BorderLayout());
		versionPanel.setOpaque(false);
		versionPanel.add(new VersionLabel(this), BorderLayout.EAST);
		this.listPanel.add(versionPanel, BorderLayout.SOUTH);
		JScrollPane scrollPane = new JScrollPane(listPanel);
		scrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		mainPanel.add(scrollPane, BorderLayout.WEST);
		JPanel contentPanel = new JPanel(new BorderLayout());
		this.catBackPanelScrollPane = new JScrollPane();
		this.catBackPanelScrollPane.getVerticalScrollBar().setUnitIncrement(8);
		this.catBackNavPanel = new JPanel(new BorderLayout());
		this.catBackNavPanel.add(navPreviousButton, BorderLayout.WEST);
		this.catBackNavPanel.add(navNextButton, BorderLayout.EAST);
		this.navLabelPanel = new JPanel(new FlowLayout());
		this.catBackNavPanel.add(this.navLabelPanel, BorderLayout.CENTER);
		contentPanel.add(this.catBackPanelScrollPane, BorderLayout.CENTER);
		contentPanel.add(this.catBackNavPanel, BorderLayout.SOUTH);
		mainPanel.add(contentPanel, BorderLayout.CENTER);
		setContentPanel(null);
		
		// reopen any backup that was open last time application was run
		File lastOpened = this.recentlyLoadedFilesManager.getMostRecentLoadedFile();
		if (lastOpened != null && lastOpened.exists()) {
			executeOpen(lastOpened);			
		}		
		
		// set up window size and location
		setContentPane(this.mainPanel);
		pack();
		if (settings.getFrameState().isEmpty()) {
			log.info("Setting up window with default size and location.");
			setSize(800,600);
			setLocationRelativeTo(null);
		} else {
			settings.getFrameState().applyTo(this);
		}
	}

	private JButton createNavButton(String text, Icon icon) {
		JButton button = new JButton(text, icon);
		ButtonFactory.makeTransparent(button);
		button.setForeground(Color.BLUE);
		button.setFont(button.getFont().deriveFont(Font.PLAIN));
		button.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		return button;
	}
	
	private JMenuBar buildMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("File");
		JMenuItem item = fileManager.buildMenuItemNew();
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				executeNew();
			}
		});
		menu.add(item);
		item = fileManager.buildMenuItemOpen();
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				executeOpen(null);
			}
		});
		menu.add(item);
		item = fileManager.buildMenuItemClose();
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					fileManager.executeClose();
				} catch (IOException ioe) {
					errorPrompt("Error while closing.", ioe);
				}
			}
		});
		menu.add(item);
		menu.addSeparator();
		item = fileManager.buildMenuItemSave();
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					fileManager.executeSave();
				} catch (IOException ioe) {
					errorPrompt("Unable to save.", ioe);
				}
			}
		});
		menu.add(item);
		item = fileManager.buildMenuItemSaveAs();
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					fileManager.executeSaveAs();
				} catch (IOException ioe) {
					errorPrompt("Unable to save.", ioe);
				}
			}
		});
		menu.add(item);
		menu.addSeparator();
		
		item = new JMenuItem("Begin Backup");
		item.setAction(ResourceManager.getInstance().getResource(BackupEngineManager.class).getBeginBackupAction());
		fileManager.activateOnFileOpen(item);
		menu.add(item);
		
		menu.addSeparator();
		
		this.recentlyLoadedFilesManager.addFilesToMenu(menu);
		
		if (!PlatformTool.isMac()) {
			menu.addSeparator();
		}
		item = new JMenuItem("Exit");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				closeApplication();
			}
		});
		PlatformTool.addMenuItem(item, PlatformTool.MenuItemType.EXIT, menu);  
		menuBar.add(menu);
		menu = new JMenu("Window");
		item = new JMenuItem("Look and Feel");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (lafSelectionDialog.showDialog()) {
					settings.setLookAndFeelName(lafSelectionDialog.getSelectedLookAndFeelName());
					JOptionPane.showMessageDialog(CatBackFrame.this, 
							"Selected Look and Feel will be applied the next time the application is started.", 
							"Notice", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		menu.add(item);
		item = new JMenuItem("Backup History");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				BackupHistoryDialog dialog = new BackupHistoryDialog(CatBackFrame.this, backupStats);
				dialog.showDialog();
			}
		});
		fileManager.activateOnFileOpen(item);
		menu.add(item);
		JFrame logFrame = LoggingConfigurer.getLogFrame();
		if (logFrame != null) {
			item = new JMenuItem("Application Log");
			item.addActionListener(event -> logFrame.setVisible(true));
		}
		menu.add(item);
		menuBar.add(menu);
		menu = new JMenu("Help");
		item = new JMenuItem("User Guide");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					UserGuide.display();
				} catch (Exception e) {
					errorPrompt("Unable to launch user guide.", e);
				}
			}
		});
		menu.add(item);
		item = this.aboutDialog.buildMenuItem();
		PlatformTool.addMenuItem(item, PlatformTool.MenuItemType.ABOUT, menu);
		menuBar.add(menu);
		return menuBar;
	}
	
	private void errorPrompt(String message, Throwable throwable) {
		log.error(message, throwable);
		JOptionPane.showMessageDialog(this, 
				message + "\n\n" + throwable.getMessage(), 
				"Application Error", JOptionPane.ERROR_MESSAGE);
	}
	
	private void saveTreeStates(CatBackup15 backup) {
		try {
			CatBackupTreeStates states = null;
			if (CatBackSettings.TREESTATES_FILE.exists()) {
				try {
					states = FileManager.loadObject(CatBackSettings.TREESTATES_FILE, CatBackupTreeStates.class);
				} catch (IOException ioe) {
					log.error("Unable to load previous tree states.  Reinitializing with new tree states file.");
					states = new CatBackupTreeStates();
				}
			} else {
				states = new CatBackupTreeStates();
			}
			TreeState includedTreeState = new TreeState();
			TreeState excludedTreeState = new TreeState();
			includedTreeState.store(this.includedTree);
			excludedTreeState.store(this.excludedTree);
			states.setTreeStates(backup.getId(), includedTreeState, excludedTreeState);
			FileManager.saveObject(CatBackSettings.TREESTATES_FILE, states);
		} catch (IOException ioe) {
			log.error("Unable to save tree states for " + backup.getName(), ioe);
		}
	}
	
	private void loadTreeStates(CatBackup15 backup) {
		if (CatBackSettings.TREESTATES_FILE.exists()) {
			try {
				CatBackupTreeStates states = FileManager.loadObject(CatBackSettings.TREESTATES_FILE, CatBackupTreeStates.class);
				final TreeState includedState = states.getIncludedTreeState(backup.getId());
				if (includedState != null) {
					includedState.applyTo(this.includedTree);
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							Container includedTreePane = SwingUtilities.getAncestorOfClass(JScrollPane.class, includedTree);
							if (includedTreePane != null && includedTreePane instanceof JScrollPane) {
								includedState.applyTo((JScrollPane) includedTreePane);
							}
						}
					});
				}
				final TreeState excludedState = states.getExcludedTreeState(backup.getId());
				if (excludedState != null) {
					excludedState.applyTo(this.excludedTree);
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							Container excludedTreePane = SwingUtilities.getAncestorOfClass(JScrollPane.class, excludedTree);
							if (excludedTreePane != null && excludedTreePane instanceof JScrollPane) {
								excludedState.applyTo((JScrollPane) excludedTreePane);
							}
						}
					});
				}
			} catch (IOException ioe) {
				log.error("Unable to load tree states", ioe);
			}
		} 
	}

	private void initializeForNewBackup(final CatBackup15 backup) {	
		// create the checkbox file trees
		FileIconCache fileIconCache = ResourceManager.getInstance().getResource(FileIconCache.class);
		this.includedTree = CheckboxFileTreeFactory.createCheckboxFileTree(false, false, fileIconCache, "Included Files");
		this.excludedTree = CheckboxFileTreeFactory.createCheckboxFileTree(false, false, fileIconCache, "Excluded Files");
		this.includedTree.setExpandSelectedPaths(false);
		this.excludedTree.setExpandSelectedPaths(false);
		this.includedTree.setCellRenderer(new CheckboxTreeCellRenderer(
				new CatBackFileTreeCellRenderer(this.includedTree, this.excludedTree, fileIconCache)));		
		this.excludedTree.setCellRenderer(new CheckboxTreeCellRenderer(
				new CatBackFileTreeCellRenderer(this.includedTree, this.excludedTree, fileIconCache)));
		this.includedTree.setCellEditor(new CheckboxTreeCellEditor(
				new CatBackFileTreeCellRenderer(this.includedTree, this.excludedTree, fileIconCache)));
		this.excludedTree.setCellEditor(new CheckboxTreeCellEditor(
				new CatBackFileTreeCellRenderer(this.includedTree, this.excludedTree, fileIconCache)));
		
		// apply backup file selections to the file trees
		List<File> includedFiles = backup.getIncludedFiles();
		if (includedFiles != null) {
			for (File bfile : includedFiles) {
				this.includedTree.selectAddFile(bfile, backup.wasDirectory(bfile), true);
			}
		}
		List<File> excludedFiles = backup.getExcludedFiles();
		if (excludedFiles != null) {
			for (File bfile : excludedFiles) {
				this.excludedTree.selectAddFile(bfile, backup.wasDirectory(bfile), true);
			}
		}
		
		// create backup size calculator 
		if (this.backupSizeCalculator != null && this.backupSizeCalculator.isCalculating()) {
			this.backupSizeCalculator.cancel();
		}
		this.backupSizeCalculator = new BackupSizeCalculator(includedTree, excludedTree);
		
		// create a backup updater to update backup any time a tree checkbox value is changed
		this.catBackupUpdater = new CatBackupUpdater(backup, this.includedTree, this.excludedTree);
		
		// setup input processor for backup inputs
		Processor sourceProcessor = new SourceProcessor(backup);
		this.inputProcessor = new InputProcessor(sourceProcessor, CommitMode.COMMIT_ALL, true);
		this.inputProcessor.setDefaultMarkerBuilder(JTextField.class, MarkerFactory.compoundMarkerBuilder(
				MarkerFactory.backgroundMarkerBuilder(),
				MarkerFactory.toolTipMarkerBuilder()));
		// note: we don't want tool tip marking for labels as it would conflict with the standard FileLabel tool tips
		this.inputProcessor.setDefaultMarkerBuilder(JLabel.class, MarkerFactory.foregroundMarkerBuilder());
		// add custom validator message properties to the default Zenput message properties
		TimeDurationValidator.addMessageProperties(inputProcessor.getMessageProperties());
		
		// setup backup stats
		try {
			backupStats = new BackupStats(backup.getBackupDirectory());
		} catch (IOException e) {
			log.warn("Unable to read prior backup stats.  Creating new stats file.");
			backupStats = new BackupStats();
		}
		
		// initialize Backup Engine Manager for new backup
		BackupEngineManager bem = ResourceManager.getInstance().getResource(BackupEngineManager.class);
		bem.initializeForBackup(inputProcessor, excludedTree, backupStats);
		
		// setup all CatBack panels
		BackupResources res = new BackupResources();
		res.setBackupSizeCalculator(this.backupSizeCalculator);
		res.setBackupStats(backupStats);
		res.setExcludedTree(this.excludedTree);
		res.setFileManager(this.fileManager);
		res.setIncludedTree(this.includedTree);
		res.setInputProcessor(this.inputProcessor);
		for (CatBackPanelHandler catBackPanelHandler : this.catBackPanelHandlers) {
			try {
				catBackPanelHandler.backupOpened(res);
			} catch (ZenputException ie) {
				log.error("Error setting up panel " + catBackPanelHandler.getTitle(), ie);
			}
		}
		for (CatBackPanelHandler catBackPanelHandler : this.catBackPanelHandlers) {
			// this has to happen after all handlers have finished their backupOpened setup
			catBackPanelHandler.validateFields();
		}
		
		// restore any previously saved tree state
		loadTreeStates(backup);
		
		log.info("Backup " + backup.getName() + " loaded.");
	}
	
	public void setDryRun(boolean dryRun, Long speedFactor) {
		final String dryRunText = " [DRY RUN]";
		BackupEngineManager bem = ResourceManager.getInstance().getResource(BackupEngineManager.class);
		bem.setDryRunSpeedFactor(speedFactor);
		if (dryRun == bem.isDryRun()) {
			return;
		}
		bem.setDryRun(dryRun);
		if (dryRun) {
			log.warn("DRY RUN mode enabled.  Backups will be simulated.  No changes to the file system will take place when backup is executed.");
			final String message = "Application is running in DRY RUN mode.\n\nBackups will be simulated only, and will not actually produce any new backups, modify previous backup locations,\nor update any backup statistics.  However, changes to backup settings will take affect.";
			setTitle(getTitle() + dryRunText);
			SwingUtilities.invokeLater(() -> {
				JOptionPane.showMessageDialog(this, message, "Dry Run Warning", JOptionPane.WARNING_MESSAGE);
			});
		} else {
			log.warn("DRY RUN mode disabled.  Backups will no longer be simulated.");
			setTitle(getTitle().substring(0, getTitle().length()-dryRunText.length()));
		}
	}
	
	public void executeNew() {
		CatBackup15 backup = new CatBackup15();
		try {
			if (fileManager.executeNew(backup) != null) { 
				initializeForNewBackup(backup);
				this.catBackPanelList.addListSelectionListener(this);
				this.catBackPanelList.setEnabled(true);
				this.catBackPanelList.setSelectedIndex(1);	// name and location panel
			}
		} catch (IOException ioe) {
			errorPrompt("Unable to create new backup profile.", ioe);
		}		
	}

	public void executeOpen(File file) {
		try {
			String key = (file == null)? fileManager.executeOpen() : fileManager.executeOpen(file);
			if (key != null) {
				initializeForNewBackup(fileManager.getObject());
				this.catBackPanelList.addListSelectionListener(this);
				this.catBackPanelList.setEnabled(true);
				this.catBackPanelList.setSelectedIndex(0);	// summary panel
			}
		} catch (IOException ioe) {
			errorPrompt("Unable to open file", ioe);
		}
	}
	
	private void setContentPanel(CatBackPanelHandler panelHandler) {
		// update previous panel handler
		if (this.previousPanelHandler != null) {
			this.previousPanelHandler.panelDeactivating();			
		} 
		this.previousPanelHandler = panelHandler;
		
		// setup main content panel
		if (panelHandler == null) {
			this.currentCatBackPanel = this.emptyPanelHandler.getComponent();
			panelHandler = this.emptyPanelHandler;
		} else {
			JComponent component = panelHandler.getComponent();
			this.currentCatBackPanel = new JPanel(new BorderLayout());
			this.currentCatBackPanel.add(component, BorderLayout.NORTH);	// compress component
			JPanel centerPanel = new JPanel();
			centerPanel.setBackground(component.getBackground());	// doesn't really work with Nimbus derived colors, but doesn't look bad enough to fix
			this.currentCatBackPanel.add(centerPanel, BorderLayout.CENTER);
			this.currentCatBackPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		}
		panelHandler.panelActivating();
		this.catBackPanelScrollPane.setViewportView(this.currentCatBackPanel);
		
		// setup status label
		if (this.navLabel != null) {
			this.navLabelPanel.remove(this.navLabel);
		}
		this.navLabel = panelHandler.getStatusLabel();
		if (this.navLabel != null) {
			this.navLabelPanel.add(this.navLabel);
		}
		this.navLabelPanel.revalidate();
		this.navLabelPanel.repaint();
		
		// update nav buttons
		this.navPreviousButton.setEnabled(panelHandler.isNavBackAvailable());
		this.navPreviousButton.setVisible(panelHandler.isNavBackAvailable());
		this.navPreviousButton.setText(panelHandler.getNavBackText());
		this.navNextButton.setEnabled(panelHandler.isNavForwardAvailable());
		this.navNextButton.setVisible(panelHandler.isNavForwardAvailable());
		this.navNextButton.setText(panelHandler.getNavForwardText());
		
		panelHandler.panelActivated();
	}

	@Override
	public void actionPerformed(RecentlyLoadedActionEvent event) {
		executeOpen(event.getFile());		
	}

	@Override
	public void beforeSaveOrClose(CatBackup15 toSave) {
		saveTreeStates(toSave);
		try {
			this.inputProcessor.validate();
		} catch (ZenputException ze) {
			log.error("Unable to validate fields.", ze);
		}
	}
	
	@Override
	public void afterOpen(String key) {
		// no action required
	}
	
	@Override
	public void afterClose() {
		for (CatBackPanelHandler catBackPanelHandler : this.catBackPanelHandlers) {
			catBackPanelHandler.backupClosed();
		}
		this.catBackupUpdater.destroy();
		this.includedTree = null;
		this.excludedTree = null;
		this.catBackPanelList.removeListSelectionListener(this);
		this.catBackPanelList.clearSelection();
		this.catBackPanelList.setEnabled(false);
		this.previousPanelHandler = null;
		this.inputProcessor.close();
		this.inputProcessor = null;
		setContentPanel(null);
	}

	@Override
	public void filePathChange(String newAbsolutePath) {
		// no action required
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e != null && e.getSource() == this.catBackPanelList && !e.getValueIsAdjusting()) {
			CatBackPanelHandler catBackPanelHandler = (CatBackPanelHandler) this.catBackPanelList.getSelectedValue();
			if (catBackPanelHandler == null) {
				setContentPanel(null);
			} else {
				setContentPanel(catBackPanelHandler);
			}
		}
	}
}
