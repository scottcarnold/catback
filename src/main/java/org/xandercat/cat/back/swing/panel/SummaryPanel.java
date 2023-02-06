package org.xandercat.cat.back.swing.panel;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.xandercat.cat.back.BackupSizeCalculator;
import org.xandercat.cat.back.BackupSizeListener;
import org.xandercat.cat.back.CatBackup;
import org.xandercat.cat.back.engine.BackupEngineManager;
import org.xandercat.cat.back.engine.BackupStat;
import org.xandercat.cat.back.engine.BackupStats;
import org.xandercat.cat.back.engine.BackupStatus;
import org.xandercat.cat.back.media.Icons;
import org.xandercat.cat.back.swing.frame.CatBackFrame;
import org.xandercat.swing.component.ComponentFactory;
import org.xandercat.swing.file.BinaryPrefix;
import org.xandercat.swing.file.FileManager;
import org.xandercat.swing.file.FileManagerListener;
import org.xandercat.swing.file.FilesSize;
import org.xandercat.swing.label.FileLabel;
import org.xandercat.swing.label.RotatingIconLabel;
import org.xandercat.swing.panel.GroupAlignedPanelBuilder;
import org.xandercat.swing.util.FileUtil;
import org.xandercat.swing.util.ResourceManager;
import org.xandercat.swing.zenput.error.ValidationException;
import org.xandercat.swing.zenput.error.ZenputException;
import org.xandercat.swing.zenput.processor.InputProcessor;
import org.xandercat.swing.zenput.util.ValidationDialogUtil;

/**
 * Summary panel for a backup.  Any time the backup is changed, the method update() should
 * be called.
 * 
 * @author Scott Arnold
 */
public class SummaryPanel implements CatBackPanel, BackupSizeListener, FileManagerListener<CatBackup>, ActionListener {

	private CatBackFrame catBackFrame;
	private JPanel panel;
	private CatBackup backup;
	private JLabel backupNameLabel;
	private FileLabel catBackupFileLabel;
	private FileLabel backupLocationLabel;
	private RotatingIconLabel sizeHeadingLabel;
	private JLabel lastCompletedBackupLabel;
	private JLabel sizeLabel;
	private JLabel filesLabel;
	private JButton beginBackupButton;
	private JLabel ibLabel1;
	private JLabel ibLabel2;
	private JLabel ibLabel3;
	private InputProcessor inputProcessor;
	private JLabel validationLabel;
	private Color defaultValidationForegroundColor;
	private JButton showValidationExceptionsButton;
	private List<ValidationException> validationExceptions;
	private BackupStats backupStats;
	
	public SummaryPanel(CatBackFrame catBackFrame, FileManager<CatBackup> fileManager) {
		this.catBackFrame = catBackFrame;
		this.backupNameLabel = new JLabel();
		this.catBackupFileLabel = new FileLabel(false);
		this.backupLocationLabel = new FileLabel(false);
		this.backupNameLabel.setFont(this.backupNameLabel.getFont().deriveFont(Font.BOLD, 16f));
		this.backupNameLabel.setForeground(ComponentFactory.TITLE_COLOR);
		JLabel baseSizeHeadingLabel = ComponentFactory.createInputLabel("Backup Size:");
		this.sizeHeadingLabel = ComponentFactory.createSpinnerLabel(baseSizeHeadingLabel);
		this.sizeLabel = ComponentFactory.createDetailLabel(" ");
		this.filesLabel = ComponentFactory.createDetailLabel(" ");
		this.ibLabel1 = ComponentFactory.createDetailLabel(" ");
		this.ibLabel2 = ComponentFactory.createDetailLabel(" ");
		this.ibLabel3 = ComponentFactory.createDetailLabel(" ");
		this.lastCompletedBackupLabel = ComponentFactory.createDetailLabel(" ");
		this.beginBackupButton = new JButton();
		BackupEngineManager backupEngineManager = ResourceManager.getInstance().getResource(BackupEngineManager.class);
		this.beginBackupButton.setAction(backupEngineManager.getBeginBackupAction());
		this.validationLabel = ComponentFactory.createDetailLabel(" ");
		this.showValidationExceptionsButton = new JButton(Icons.QUESTION_ICON);
		this.showValidationExceptionsButton.addActionListener(this);
		this.defaultValidationForegroundColor = this.validationLabel.getForeground();
		
		GroupAlignedPanelBuilder builder = new GroupAlignedPanelBuilder(true, 20);
		builder.addHeading(this.backupNameLabel, 0, 10);
		builder.addRow(ComponentFactory.createInputLabel("Cat Backup File:"), this.catBackupFileLabel);
		builder.addVerticalStrut(10);
		builder.addRow(ComponentFactory.createInputLabel("Backup Location:"), this.backupLocationLabel);
		builder.addVerticalStrut(10);
		builder.addRow(this.sizeHeadingLabel, this.sizeLabel);
		builder.addRow(null, this.filesLabel);
		builder.addVerticalStrut(10);
		builder.addRow(ComponentFactory.createInputLabel("Incremental Backups:"), this.ibLabel1);
		builder.addRow(null, this.ibLabel2);
		builder.addRow(null, this.ibLabel3);
		builder.addVerticalStrut(10);
		builder.addRow(ComponentFactory.createInputLabel("Last Completed Backup:"), this.lastCompletedBackupLabel);
		builder.addVerticalStrut(10);
		JPanel validationPanel = new JPanel(new FlowLayout());
		validationPanel.add(this.validationLabel);
		validationPanel.add(this.showValidationExceptionsButton);
		builder.addRow(ComponentFactory.createInputLabel("Validation:"), validationPanel);
		builder.addVerticalStrut(10);
		builder.addRow(null, this.beginBackupButton);
		this.panel = builder.build();
		
		fileManager.addFileManagerListener(this);
	}
	
	@Override
	public void backupClosed() {
		this.backup = null;
		this.backupNameLabel.setText(null);
		this.catBackupFileLabel.setFile(null);
		this.backupLocationLabel.setFile(null);
		this.lastCompletedBackupLabel.setText(null);
		this.sizeHeadingLabel.stopAnimate();
		this.sizeLabel.setText(null);
		this.filesLabel.setText(null);
		this.ibLabel1.setText(null);
		this.ibLabel2.setText(null);
		this.ibLabel3.setText(null);
		this.validationLabel.setText(null);
		this.inputProcessor = null;

	}

	@Override
	public List<String> backupOpened(BackupResources backupResources) throws ZenputException {
		FileManager<CatBackup> fileManager = backupResources.getFileManager();
		this.backup = fileManager.getObject();
		this.backupStats = backupResources.getBackupStats();
		this.inputProcessor = backupResources.getInputProcessor();
		if (fileManager.getFileName() == null) {
			this.catBackupFileLabel.setText("<Not Created Yet>");
			this.catBackupFileLabel.setIcon(null);
		} else {
			this.catBackupFileLabel.setFile(new File(fileManager.getFileName()));
		}
		BackupSizeCalculator backupSizeCalculator = backupResources.getBackupSizeCalculator();
		FilesSize totalSize = backupSizeCalculator.getTotalSize();
		FilesSize excludedSize = backupSizeCalculator.getExcludedSize();
		setSizes(totalSize, excludedSize);
		setStats();
		backupSizeCalculator.addBackupSizeListener(this);
		return null;
	}

	@Override
	public void panelActivating() {
		this.validationExceptions = null;
		if (inputProcessor != null) {
			this.validationExceptions = inputProcessor.getErrors();
			if (this.validationExceptions.size() == 0) {
				this.validationLabel.setForeground(defaultValidationForegroundColor);
				this.validationLabel.setText("All input valid.");
				this.showValidationExceptionsButton.setVisible(false);
			} else {
				this.validationLabel.setForeground(Color.RED);
				this.validationLabel.setText(validationExceptions.size() + " input field(s) invalid.");
				this.showValidationExceptionsButton.setVisible(true);
			}
		}
		if (backup.getName() == null || backup.getName().trim().length() == 0) {
			this.backupNameLabel.setText("Unnamed Backup");
		} else {
			this.backupNameLabel.setText(backup.getName());
		}
		if (backup.getBackupDirectory() == null) {
			this.backupLocationLabel.setText("<No Location Selected>");
		} else {
			this.backupLocationLabel.setFile(backup.getBackupDirectory());
		}
		if (backup.isLimitIncrementalBackups()) {
			this.ibLabel1.setText("Keep for at least " + getStrValue(backup.getKeepAtLeastTime()) + ".");
			this.ibLabel2.setText("Keep not more than " 
					+ getStrValue(backup.getKeepNoMoreThanTime()) 
					+ " or " 
					+ getStrValue(backup.getKeepNoMoreThanBytes())
					+ ".");
			this.ibLabel3.setText(null);
		} else {
			this.ibLabel1.setText("Unlimited.");
			this.ibLabel2.setText(null);
			this.ibLabel3.setText(null);
		}
		setStats();
	}
	
	private String getStrValue(Object o) {
		return (o == null)? "?" : o.toString();
	}
	
	@Override
	public void panelDeactivating() {
		// no action required
	}
	
	private void setSizes(FilesSize totalSize, FilesSize excludedSize) {
		if (totalSize == null || excludedSize == null) {
			this.sizeHeadingLabel.startAnimate();
		} else {
			this.sizeHeadingLabel.stopAnimate();
		}
		if (totalSize != null) {
			this.sizeLabel.setText(FileUtil.formatFileSize(totalSize.getBytes(), BinaryPrefix.TiB) + " total size.");
			this.filesLabel.setText(NumberFormat.getInstance().format(totalSize.getFiles()) 
					+ " files, " 
					+ NumberFormat.getInstance().format(totalSize.getDirectories()) 
					+ " directories.");
		} else {
			this.sizeLabel.setText(null);
			this.filesLabel.setText(null);
		}
	}
	
	private void setStats() {
		BackupStat lastCompletedStat = backupStats.getLatestStat(BackupStatus.COMPLETED);
		if (lastCompletedStat == null) {
			this.lastCompletedBackupLabel.setText("None");
		} else {
			this.lastCompletedBackupLabel.setText(DateFormat.getDateTimeInstance().format(lastCompletedStat.getDateFinished()));
		}
	}
	
	@Override
	public JComponent getComponent() {
		return panel;
	}

	@Override
	public Icon getIcon() {
		return Icons.SUMMARY_ICON;
	}

	@Override
	public String getTitle() {
		return "Summary";
	}

	@Override
	public JLabel getStatusLabel() {
		return null;
	}

	@Override
	public void backupSizeCalculating() {
		setSizes(null, null);	
	}

	@Override
	public void backupSizeChange(FilesSize backupSize, FilesSize excludedSize) {
		setSizes(backupSize, excludedSize);
	}
	
	@Override
	public void afterOpen(String key) {
		// no action required
		
	}
	@Override
	public void afterClose() {
		// no action required
	}

	@Override
	public void beforeSaveOrClose(CatBackup toSave) {
		// no action required
	}

	@Override
	public void filePathChange(String newAbsolutePath) {
		this.catBackupFileLabel.setFilePath(newAbsolutePath);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.showValidationExceptionsButton 
				&& inputProcessor != null
				&& validationExceptions != null) {
			ValidationDialogUtil.showMessageDialog(catBackFrame, 
					inputProcessor, 
					validationExceptions,
					"The following errors must be corrected before a backup can be started:");
		}
	}
}
