package org.xandercat.cat.back.swing.frame;

import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xandercat.cat.back.file.FileCopier;
import org.xandercat.cat.back.file.FileCopierPathGenerator;
import org.xandercat.cat.back.file.FileCopyListener;
import org.xandercat.cat.back.file.FileCopyProgressListener;
import org.xandercat.cat.back.media.Icons;
import org.xandercat.cat.back.swing.file.SwingFileCopier;
import org.xandercat.cat.back.swing.table.FileErrorTableModel;
import org.xandercat.cat.back.swing.table.FileOverwriteTableModel;
import org.xandercat.swing.component.MessageScrollPane;
import org.xandercat.swing.file.icon.FileIconCache;
import org.xandercat.swing.table.ComponentRenderer;
import org.xandercat.swing.table.FileTable;
import org.xandercat.swing.table.JTableButtonMouseListener;
import org.xandercat.swing.worker.SwingWorkerUtil;

/**
 * FileCopyProcessFrame handles copying a set of files, displaying progress as it proceeds, and 
 * providing the means for the user to resolve copy problems.
 * 
 * To use FileCopyProcessFrame, construct it, and then call copy to launch the copy process.
 * 
 *     FileCopyProcessFrame frame = new FileCopyProcessFrame(...);
 *     frame.copy();
 * 
 * @author Scott C Arnold
 */
public class FileCopyProcessFrame extends JFrame implements FileCopyListener, WindowListener {

	//TODO:  Add way to save problem files for later retry and resolution
	//TODO:  Layout on the copy process status tab could be nicer
	//TODO:  Either turn off row selection in resolution tab or provide a way to perform actions on selected rows
	//TODO:  Address issue with Files Copied including directories (but the backup engine doesn't do this, so the counts are off between the two)
	//       Consider maybe changing to only count files, and maybe show directories as [CREATED] instead of [COPIED] as [COPIED] can be misleading to users
	
	private static final Logger log = LogManager.getLogger(FileCopyProcessFrame.class);
	private static final long serialVersionUID = 2009022101L;
	private static final String FILES_COPIED_MSG = "Files Copied: ";
	private static final String ALREADY_EXIST_MSG = "Already Existing Files: ";
	private static final String ERROR_MSG = "File Copy Errors: ";
	private static final String SKIP_MSG = "Skipped Files: ";
	
	private JLabel headingLabel;
	private JLabel filesCopiedLabel;
	private JLabel filesAlreadyExistLabel;
	private JLabel copyErrorsLabel;
	private JLabel skippedLabel;
	private MessageScrollPane messageScrollPane;
	private List<File> files;
	private FileCopierPathGenerator pathGenerator;
	private File destination;
	private File source;
	private boolean copyComplete;
	private SwingFileCopier fileCopier;
	private JScrollPane overwritePane;
	private JScrollPane errorPane;
	private JSplitPane resolutionSplitPane;
	private JButton overwriteAllButton;
	private JButton overwriteCancelAllButton;
	private JButton errorRetryAllButton;
	private JButton errorCancelAllButton;
	private FileOverwriteTableModel overwriteModel;
	private FileErrorTableModel errorModel;
	private boolean startCopyMinimized;		// start the copy process frame minimized
	private boolean autoclose;				// autoclose if no problems to resolve
	private int copied;
	private int toCopy;
	private List<FileCopyListener> fileCopyListeners;
	private List<FileCopyProgressListener> fileCopyProgressListeners;
	private FileIconCache fileIconCache;
	private int errorsUntilHalt;
	private int errorCount;
	private boolean haltedDueToErrors;
	private boolean testMode = false;
	
	public FileCopyProcessFrame(List<File> files, FileIconCache fileIconCache,
			File destination, File source, boolean startCopyMinimized, boolean autoclose, int errorsUntilHalt) {
		super("Copy " + files.size() + " Files");
		this.toCopy = files.size();
		this.files = files;
		this.fileIconCache = fileIconCache;
		this.destination = destination;
		this.source = source;
		this.startCopyMinimized = startCopyMinimized;
		this.autoclose = autoclose;
		this.errorsUntilHalt = errorsUntilHalt;
		initialize();
	}
	
	public FileCopyProcessFrame(List<File> files, FileIconCache fileIconCache,
			FileCopierPathGenerator pathGenerator, boolean startCopyMinimized, boolean autoclose, int errorsUntilHalt) {
		super("Copy " + files.size() + " Files");
		this.toCopy = files.size();
		this.files = files;
		this.fileIconCache = fileIconCache;
		this.pathGenerator = pathGenerator;
		this.startCopyMinimized = startCopyMinimized;
		this.autoclose = autoclose;
		this.errorsUntilHalt = errorsUntilHalt;
		initialize();		
	}
	
	private void initialize() {
		addWindowListener(this);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.toCopy = files.size();
		buildResolutionSplitPane();
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Copy Process", buildCopyProcessPanel());
		tabbedPane.addTab("Problem Resolution", resolutionSplitPane);
		setContentPane(tabbedPane);
		pack();
		setLocationRelativeTo(null);	
		this.fileCopyListeners = new ArrayList<FileCopyListener>();
	}
	
	private JPanel buildCopyProcessPanel() {
		JPanel copyProcessPanel = new JPanel();
		copyProcessPanel.setLayout(new BoxLayout(copyProcessPanel, BoxLayout.Y_AXIS));
		headingLabel = new JLabel("Preparing to copy files...");
		filesCopiedLabel = new JLabel(FILES_COPIED_MSG + "0");
		filesAlreadyExistLabel = new JLabel(ALREADY_EXIST_MSG + "0");
		copyErrorsLabel = new JLabel(ERROR_MSG + "0");
		skippedLabel = new JLabel(SKIP_MSG + "0");
		copyProcessPanel.add(headingLabel);
		copyProcessPanel.add(filesCopiedLabel);
		copyProcessPanel.add(skippedLabel);
		copyProcessPanel.add(filesAlreadyExistLabel);
		copyProcessPanel.add(copyErrorsLabel);
		messageScrollPane = new MessageScrollPane();
		messageScrollPane.setPreferredSize(600, 160);
		copyProcessPanel.add(messageScrollPane);
		return copyProcessPanel;
	}
	
	private void buildResolutionSplitPane() {
		// overwrite panel
		JPanel overwritePanel = new JPanel();
		overwritePanel.setLayout(new BoxLayout(overwritePanel, BoxLayout.Y_AXIS));
		overwritePanel.setBorder(BorderFactory.createLoweredBevelBorder());
		JPanel overwriteHeadingPanel = new JPanel(new FlowLayout());
		overwriteHeadingPanel.add(new JLabel("Files That Already Exist:"));
		overwriteAllButton = new JButton("Overwrite All", Icons.OVERWRITE_ICON);
		overwriteCancelAllButton = new JButton("Cancel All", Icons.CANCEL_ICON);
		overwriteHeadingPanel.add(overwriteCancelAllButton);
		overwriteHeadingPanel.add(overwriteAllButton);
		overwritePanel.add(overwriteHeadingPanel);
		overwritePane = new JScrollPane();
		overwritePanel.add(overwritePane);
		// error panel
		JPanel errorPanel = new JPanel();
		errorPanel.setLayout(new BoxLayout(errorPanel, BoxLayout.Y_AXIS));
		errorPanel.setBorder(BorderFactory.createLoweredBevelBorder());
		JPanel errorHeadingPanel = new JPanel(new FlowLayout());
		errorHeadingPanel.add(new JLabel("File Copy Errors:"));
		errorRetryAllButton = new JButton("Retry All", Icons.RETRY_ICON);
		errorCancelAllButton = new JButton("Cancel All", Icons.CANCEL_ICON);
		errorHeadingPanel.add(errorCancelAllButton);
		errorHeadingPanel.add(errorRetryAllButton);
		errorPanel.add(errorHeadingPanel);
		errorPane = new JScrollPane();
		errorPanel.add(errorPane);
		// split pane
		resolutionSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, overwritePanel, errorPanel);
		resolutionSplitPane.setOneTouchExpandable(true);
	}
	
	public void enableTestMode() {
		this.testMode = true;
	}
	
	public void addFileCopyListener(FileCopyListener listener) {
		fileCopyListeners.add(listener);
	}
	
	public void removeFileCopyListener(FileCopyListener listener) {
		fileCopyListeners.remove(listener);
	}
	
	public void addFileCopyProgressListener(FileCopyProgressListener listener) {
		if (fileCopyProgressListeners == null) {
			fileCopyProgressListeners = new ArrayList<FileCopyProgressListener>();
		}
		fileCopyProgressListeners.add(listener);
	}
	
	public void removeFileCopyProgressListener(FileCopyProgressListener listener) {
		fileCopyProgressListeners.remove(listener);
	}
	
	public void copy() {
		if (startCopyMinimized) {
			setState(Frame.ICONIFIED);
		}
		setVisible(true);
		try {
			// create file copier
			if (pathGenerator == null) {
				fileCopier = new SwingFileCopier(files, destination, source);
			} else {
				fileCopier = new SwingFileCopier(files, pathGenerator);
			}
			if (testMode) {
				fileCopier.enableTestMode();
			}
			fileCopier.addFileCopyListener(this);
			for (FileCopyListener listener : this.fileCopyListeners) {
				fileCopier.addFileCopyListener(listener);
			}
			if (this.fileCopyProgressListeners != null) {
				for (FileCopyProgressListener listener : this.fileCopyProgressListeners) {
					fileCopier.addFileCopyProgressListener(listener);
				}
			}
			if (testMode) {
				headingLabel.setText("Copying " + files.size() + " files (SIMULATED)...");
			} else {
				headingLabel.setText("Copying " + files.size() + " files...");
			}
			
			// set up overwrite files table
			overwriteModel = new FileOverwriteTableModel(fileCopier, overwriteAllButton, overwriteCancelAllButton);
			FileTable overwriteTable = new FileTable(overwriteModel, fileIconCache, null);
			overwriteTable.setDefaultRenderer(JButton.class, new ComponentRenderer());
			overwriteTable.addMouseListener(new JTableButtonMouseListener(overwriteTable));
			overwritePane.setViewportView(overwriteTable);
			
			// set up error files table
			errorModel = new FileErrorTableModel(fileCopier, errorRetryAllButton, errorCancelAllButton);
			FileTable errorTable = new FileTable(errorModel, fileIconCache, null);
			errorTable.setDefaultRenderer(JButton.class, new ComponentRenderer());
			errorTable.addMouseListener(new JTableButtonMouseListener(errorTable));
			errorPane.setViewportView(errorTable);
			
			// cleanup and start copying files
			resolutionSplitPane.setDividerLocation(0.5d);
			SwingWorkerUtil.execute(fileCopier);
		} catch (IllegalArgumentException iae) {
			headingLabel.setText("Unable to start copy process.");
			messageScrollPane.addMessage("Unable to start copy process: " + iae.getMessage());
			log.error("Unable to start copy process", iae);
		}
	}

	/**
	 * Cancel any copy currently in progress (if any).
	 */
	public boolean cancelCopyInProgress() {
		if (fileCopier != null) {
			fileCopier.cancel();
			return true;
		}
		return false;
	}
	
	public boolean isHaltedDueToErrors() {
		return haltedDueToErrors;
	}
	
	public void fileCopying(File from, File to) {
		messageScrollPane.addMessage("Copying file " + from.getAbsolutePath());
	}

	public void fileCopied(File from, File to, FileCopier.CopyResult result) {
		switch (result) {
		case ALREADY_EXISTS:
			filesAlreadyExistLabel.setText(ALREADY_EXIST_MSG + fileCopier.getOverwriteFiles().size());
			break;
		case COPIED:
			copied++;
			filesCopiedLabel.setText(FILES_COPIED_MSG + fileCopier.getCopiedFiles().size());
			break;
		case ERROR:
			errorCount++;
			copyErrorsLabel.setText(ERROR_MSG + fileCopier.getErrorFiles().size());
			break;
		case SKIPPED:
			copied++;
			skippedLabel.setText(SKIP_MSG + fileCopier.getSkippedFiles().size());
			break;
		}
		messageScrollPane.appendMessage(" [" + result.toString() + "]");
		setTitle(copied + "/" + toCopy + " copied");
		if (errorCount > 0 && errorCount % errorsUntilHalt == 0) {
			int choice = JOptionPane.showConfirmDialog(this, 
					errorCount + " copy errors have occurred.  Do you wish to continue the copy process?", 
					"Continue?", 
					JOptionPane.YES_NO_OPTION, 
					JOptionPane.ERROR_MESSAGE);
			if (choice == JOptionPane.NO_OPTION) {
				haltedDueToErrors = true;
				fileCopier.cancel();
			}
		}
	}
	
	public void copyComplete(boolean resolutionRequired, boolean copyCancelled) {
		this.copyComplete = true;
		if (resolutionRequired) {
			messageScrollPane.addMessage("Process complete.  Some files need resolution.");
			headingLabel.setText("Process complete.  Some files need resolution.");
			if (getState() == Frame.ICONIFIED) {
				setState(Frame.NORMAL);
			}
		} else {
			String message = copyCancelled? "Process cancelled." : "Process complete.";
			messageScrollPane.addMessage(message);
			headingLabel.setText(message);
			if (autoclose) {
				setVisible(false);
				dispose();
				return;
			}
		}
		if (!isFocusOwner()) {
			requestFocus();
		}
	}
	
	public void windowActivated(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {		
	}

	public void windowClosing(WindowEvent e) {
		if (!copyComplete) {
			int result = JOptionPane.showConfirmDialog(this,
					"Files are still being copied.\nCancel copy?",
					"Confirm Cancel Copy",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);
			if (result == JOptionPane.NO_OPTION) {
				return;
			}
			fileCopier.cancel();
		}
		if (overwriteModel.getRowCount() + errorModel.getRowCount() > 0) {
			Object[] options = {"Close Copy Window", "Cancel"};
			int result = JOptionPane.showOptionDialog(this, 
					"You have not indicated how to handle some files that could not be copied.\n"
					+ "Indicate how to handle these files on the Problem Resolution tab.\n\n"
					+ "Choose Close to close without resolving uncopied files.", 
					"Confirm Exit Without Resolution", 
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE,
					null, options, options[0]);
			if (result == 1) {
				return;
			}
		}	
		setVisible(false);
		dispose();
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}
}

