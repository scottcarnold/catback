package org.xandercat.cat.back.swing.panel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.xandercat.cat.back.CatBackup16;
import org.xandercat.cat.back.engine.BackupEngineListener;
import org.xandercat.cat.back.engine.BackupEngineManager;
import org.xandercat.cat.back.media.Icons;
import org.xandercat.swing.component.ComponentFactory;
import org.xandercat.swing.file.BinaryPrefix;
import org.xandercat.swing.file.icon.FileIconCache;
import org.xandercat.swing.label.FileLabel;
import org.xandercat.swing.label.RotatingIconLabel;
import org.xandercat.swing.table.ColumnToggler;
import org.xandercat.swing.table.FileTable;
import org.xandercat.swing.table.FileTableModel;
import org.xandercat.swing.table.FileTableModelLoaderListener;
import org.xandercat.swing.util.FileUtil;
import org.xandercat.swing.util.ResourceManager;
import org.xandercat.swing.zenput.adapter.InputAccessor;
import org.xandercat.swing.zenput.adapter.ReflectionAccessor;
import org.xandercat.swing.zenput.converter.FileConverter;
import org.xandercat.swing.zenput.error.ZenputException;
import org.xandercat.swing.zenput.processor.InputProcessor;

/**
 * Component provider for backup name and location.
 * 
 * @author Scott Arnold
 */
public class NameLocationPanel implements CatBackPanel, ActionListener, FileTableModelLoaderListener, BackupEngineListener {
	
	private JPanel panel;
	private JTextField backupNameField;
	private FileLabel backupLocationField;
	private JButton locationSelectButton;
	private JFileChooser fileChooser;
	private JLabel spaceAvailableLabel;
	private RotatingIconLabel locationContentsLabel;
	private FileTable locationContentsTable;
	private FileTableModel locationContentsTableModel;
	
	public NameLocationPanel() {
		this.panel = new JPanel();
		this.panel.setLayout(new BoxLayout(this.panel, BoxLayout.Y_AXIS));
		this.backupLocationField = new FileLabel(false);
		this.backupNameField = new JTextField();
		this.fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		this.locationSelectButton = new JButton("Select...");
		this.locationSelectButton.addActionListener(this);
		JComponent comp = ComponentFactory.createTitlePanel("Backup Name");
		comp.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.panel.add(comp);
		this.panel.add(this.backupNameField);
		this.panel.add(Box.createVerticalStrut(20));
		comp = ComponentFactory.createTitlePanel("Backup Location");
		comp.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.panel.add(comp);
		JPanel locationPanel = new JPanel(new BorderLayout());
		locationPanel.add(this.backupLocationField, BorderLayout.CENTER);
		locationPanel.add(this.locationSelectButton, BorderLayout.EAST);
		this.panel.add(locationPanel);
		this.panel.add(Box.createVerticalStrut(10));
		JPanel spaceAvailPanel = new JPanel();
		spaceAvailPanel.setLayout(new BoxLayout(spaceAvailPanel, BoxLayout.X_AXIS));
		this.spaceAvailableLabel = new JLabel();
		spaceAvailPanel.add(this.spaceAvailableLabel);
		spaceAvailPanel.add(Box.createHorizontalGlue());
		this.panel.add(spaceAvailPanel);
		this.panel.add(Box.createVerticalStrut(10));
		JPanel tableHeaderPanel = new JPanel(new BorderLayout());
		this.locationContentsLabel = ComponentFactory.createSpinnerLabel("");
		tableHeaderPanel.add(this.locationContentsLabel, BorderLayout.WEST);
		this.panel.add(tableHeaderPanel);
		FileIconCache fileIconCache = ResourceManager.getInstance().getResource(FileIconCache.class);
		this.locationContentsTableModel = new FileTableModel();
		this.locationContentsTableModel.setUseDirectorySizeCache(true);
		this.locationContentsTable = new FileTable(this.locationContentsTableModel, fileIconCache, null);
		this.locationContentsTable.setAutoCreateRowSorter(true);
		new ColumnToggler(this.locationContentsTable);
		this.locationContentsTable.setDirectoryColumnVisible(false);
		this.panel.add(this.locationContentsTable.getTableHeader());
		this.panel.add(this.locationContentsTable);
		this.panel.add(Box.createVerticalGlue());
		BackupEngineManager bem = ResourceManager.getInstance().getResource(BackupEngineManager.class);
		bem.addBackupEngineListener(this);
	}
	
	private void setLocationDetails(String backupLocation) {
		if (backupLocation != null) {
			File file = new File(backupLocation);
			this.backupLocationField.setFile(file);
			if (file.exists()) {
				File fileRoot = file;
				File beforeFileRoot = file;
				while (fileRoot.getParentFile() != null) {
					beforeFileRoot = fileRoot;
					fileRoot = fileRoot.getParentFile();
				}
				if ("\\\\".equals(fileRoot.getAbsolutePath())) {
					// specifying a network location on windows just returns "\\" (unescaped) as the file root.
					fileRoot = beforeFileRoot;
				}
				long usableSpace = fileRoot.getUsableSpace();
				String spaceAvail = (usableSpace == 0)? "unknown" : FileUtil.formatFileSize(fileRoot.getUsableSpace(), BinaryPrefix.TiB);
				this.spaceAvailableLabel.setText("Space Available on " + fileRoot.getAbsolutePath() + " is " + spaceAvail);
				this.locationContentsLabel.setText("Location Contents (Loading...)");
				this.locationContentsLabel.startAnimate();
				this.locationContentsTableModel.setDirectory(file, this);
			} else {
				this.spaceAvailableLabel.setText(null);
				this.locationContentsLabel.setText(null);
				this.locationContentsTableModel.clear();
			}
		} else {
			this.spaceAvailableLabel.setText(null);
			this.locationContentsLabel.setText(null);
			this.backupLocationField.setText("<No Location Set>");
		}
	}
	
	@Override
	public Icon getIcon() {
		return Icons.HDD_ICON;
	}

	@Override
	public String getTitle() {
		return "Name & Location";
	}

	@Override
	public void backupClosed() {
		this.backupLocationField.setText(null);
		this.backupNameField.setText(null);
		this.spaceAvailableLabel.setText(null);
		this.locationContentsTableModel.clear();
	}

	@Override
	public List<String> backupOpened(BackupResources backupResources) throws ZenputException {
		CatBackup16 backup = backupResources.getFileManager().getObject();
		InputProcessor inputProcessor = backupResources.getInputProcessor();
		inputProcessor.registerInput("name", this.backupNameField);
		InputAccessor<String> locationAccessor = new ReflectionAccessor<String>(this.backupLocationField, "absolutePath", String.class);
		inputProcessor.registerInput("backupDirectory", locationAccessor, new FileConverter());
		setLocationDetails(backup.getBackupDirectoryPath());
		return Arrays.asList("name", "backupDirectory");
	}
	
	@Override
	public void panelActivating() {
		// no action required
	}

	@Override
	public void panelActivated() {
		// no action required
	}

	@Override
	public void panelDeactivating() {
		// no action required
	}
	
	@Override
	public JComponent getComponent() {
		return panel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// assume the only action is selecting a backup location
		if (backupLocationField.getText().trim().length() > 0) {
			File f = new File(backupLocationField.getText());
			if (f.exists() && f.getParentFile() != null && f.getParentFile().isDirectory()) {
				fileChooser.setCurrentDirectory(f.getParentFile());
			}
		}
		int result = fileChooser.showOpenDialog(null);
		if (result == JFileChooser.APPROVE_OPTION && fileChooser.getSelectedFile() != null) {
			backupLocationField.setText(fileChooser.getSelectedFile().getAbsolutePath());
		}
		setLocationDetails(backupLocationField.getText());
	}

	@Override
	public JLabel getStatusLabel() {
		return null;
	}

	@Override
	public void fileTableLoadingCancelled() {
		this.locationContentsLabel.stopAnimate();
		this.locationContentsLabel.setText("Content Loading Incomplete.");	
	}

	@Override
	public void fileTableLoadingComplete() {
		this.locationContentsLabel.stopAnimate();
		this.locationContentsLabel.setText("Location Contents");
	}

	@Override
	public void backupEngineComplete(String backupId, boolean resolutionRequired, boolean copyCancelled) {
		this.locationContentsLabel.setText("Location Contents (Loading...)");
		this.locationContentsLabel.startAnimate();
		locationContentsTableModel.refresh(this);	// refresh table in case there is a new incremental directory
	}
}
