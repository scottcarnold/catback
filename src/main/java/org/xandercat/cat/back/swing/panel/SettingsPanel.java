package org.xandercat.cat.back.swing.panel;

import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.xandercat.cat.back.media.Icons;
import org.xandercat.swing.component.ComponentFactory;
import org.xandercat.swing.datetime.TimeDuration;
import org.xandercat.swing.file.ByteSize;
import org.xandercat.swing.panel.GroupAlignedPanelBuilder;
import org.xandercat.swing.zenput.adapter.InputAccessor;
import org.xandercat.swing.zenput.adapter.ReflectionAccessor;
import org.xandercat.swing.zenput.converter.SameTypeConverter;
import org.xandercat.swing.zenput.error.ZenputException;
import org.xandercat.swing.zenput.processor.InputProcessor;

/**
 * Panel for editing Backup settings.
 * 
 * @author Scott Arnold
 */
public class SettingsPanel implements CatBackPanel, ItemListener {

	private JPanel panel;
	private JCheckBox showMoveCopyDialogCheckBox;
	private JCheckBox leaveCopyWindowOpenCheckBox;
	private JCheckBox limitIncrementalBackupsCheckBox;
	private JLabel keepForAtLeastLabel;
	private TimeDurationInputPanel keepForAtLeastTimeInput;
	private JLabel keepNoMoreThanTimeLabel;
	private TimeDurationInputPanel keepNoMoreThanTimeInput;
	private JLabel keepNoMoreThanSizeLabel;
	private ByteSizeInputPanel keepNoMoreThanSizeInput;
	private JLabel noteLabel;
	private JCheckBox scanLastBackupCheckBox;
	private JLabel errorsUntilBackupHaltLabel;
	private JTextField errorsUntilBackupHaltTextField;
	
	public SettingsPanel() {
		this.showMoveCopyDialogCheckBox = ComponentFactory.createInputCheckBox(null);
		this.leaveCopyWindowOpenCheckBox = ComponentFactory.createInputCheckBox(null);
		this.limitIncrementalBackupsCheckBox = ComponentFactory.createInputCheckBox(null);
		this.limitIncrementalBackupsCheckBox.addItemListener(this);
		this.keepForAtLeastLabel = ComponentFactory.createInputLabel("Keep incremental backups for at least:");
		this.keepForAtLeastTimeInput = new TimeDurationInputPanel(5);
		this.keepNoMoreThanTimeLabel = ComponentFactory.createInputLabel("Keep incremental backups for no more than:");
		this.keepNoMoreThanTimeInput = new TimeDurationInputPanel(5);
		this.keepNoMoreThanSizeLabel = ComponentFactory.createInputLabel("Do not allow incremental backups to exceed:");
		this.keepNoMoreThanSizeInput = new ByteSizeInputPanel(5);
		this.scanLastBackupCheckBox = ComponentFactory.createInputCheckBox(null);
		this.noteLabel = ComponentFactory.createDetailLabel("Keep for at least duration overrides other incremental backup limits.");
		this.errorsUntilBackupHaltLabel = ComponentFactory.createInputLabel("Number of move/copy errors until backup is halted:");
		this.errorsUntilBackupHaltTextField = new JTextField(4);
		
		updateIncrementalBackupEnabledStates();

		this.showMoveCopyDialogCheckBox.setText("Show files to be moved/copied before move/copy step");
		this.leaveCopyWindowOpenCheckBox.setText("Leave copy window open after successful backup");
		this.limitIncrementalBackupsCheckBox.setText("Limit incremental backups");
		this.scanLastBackupCheckBox.setText("Perform full scan of previous backup");
		
		GroupAlignedPanelBuilder builder = new GroupAlignedPanelBuilder();
		builder.addHeading(ComponentFactory.createTitlePanel("General Preferences"), 0, 10);
		builder.addRow(null, this.showMoveCopyDialogCheckBox);
		builder.addRow(null, this.leaveCopyWindowOpenCheckBox);
		builder.addRow(null, this.scanLastBackupCheckBox);
		builder.addVerticalStrut(10);
		builder.addRow(null, this.errorsUntilBackupHaltLabel);
		builder.addRow(null, this.errorsUntilBackupHaltTextField);
		builder.addHeading(ComponentFactory.createTitlePanel("Incremental Backups"), 10, 10);
		builder.addRow(null, this.limitIncrementalBackupsCheckBox);
		builder.addVerticalStrut(10);
		builder.addRow(null, this.keepForAtLeastLabel);
		builder.addRow(null, this.keepForAtLeastTimeInput);
		noteLabel.setFont(noteLabel.getFont().deriveFont(Font.PLAIN));
		builder.addVerticalStrut(10);
		builder.addRow(null, noteLabel);
		builder.addVerticalStrut(10);
		builder.addRow(null, this.keepNoMoreThanTimeLabel);
		builder.addRow(null, this.keepNoMoreThanTimeInput);
		builder.addVerticalStrut(10);
		builder.addRow(null, this.keepNoMoreThanSizeLabel);
		builder.addRow(null, this.keepNoMoreThanSizeInput);
		this.panel = builder.build();
	}
	
	@Override
	public void backupClosed() {
		// no action required
	}

	@Override
	public List<String> backupOpened(BackupResources backupResources) throws ZenputException {
		InputProcessor inputProcessor = backupResources.getInputProcessor();
		inputProcessor.registerInput("showFilesBeforeMoveCopy", this.showMoveCopyDialogCheckBox);
		inputProcessor.registerInput("alwaysLeaveCopyWindowOpen", this.leaveCopyWindowOpenCheckBox);
		inputProcessor.registerInput("scanLastBackup", this.scanLastBackupCheckBox);
		inputProcessor.registerInput("errorsUntilBackupHalt", this.errorsUntilBackupHaltTextField);
		inputProcessor.registerInput("limitIncrementalBackups", this.limitIncrementalBackupsCheckBox);
		InputAccessor<TimeDuration> tdAccessor = new ReflectionAccessor<TimeDuration>(keepForAtLeastTimeInput, "timeDuration", TimeDuration.class);
		inputProcessor.registerInput("keepAtLeastTime", tdAccessor, new SameTypeConverter<TimeDuration>());
		tdAccessor = new ReflectionAccessor<TimeDuration>(keepNoMoreThanTimeInput, "timeDuration", TimeDuration.class);
		inputProcessor.registerInput("keepNoMoreThanTime", tdAccessor, new SameTypeConverter<TimeDuration>());
		InputAccessor<ByteSize> bsAccessor = new ReflectionAccessor<ByteSize>(keepNoMoreThanSizeInput, "byteSize", ByteSize.class);
		inputProcessor.registerInput("keepNoMoreThanBytes", bsAccessor, new SameTypeConverter<ByteSize>());
		return Arrays.asList("showFilesBeforeMoveCopy", "alwaysLeaveCopyWindowOpen", 
				"limitIncrementalBackups", "keepAtLeastTime", "keepNoMoreThanTime", "keepNoMoreThanBytes");
	}

	@Override
	public void panelActivating() {
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
	public Icon getIcon() {       
		return Icons.SETTINGS_ICON;
	}

	@Override
	public String getTitle() {
		return "Backup Settings";
	}

	@Override
	public JLabel getStatusLabel() {
		return null;
	}

	private void updateIncrementalBackupEnabledStates() {
		boolean limit = this.limitIncrementalBackupsCheckBox.isSelected();
		this.keepForAtLeastLabel.setEnabled(limit);
		this.keepForAtLeastTimeInput.setEnabled(limit);
		this.keepNoMoreThanSizeLabel.setEnabled(limit);
		this.keepNoMoreThanSizeInput.setEnabled(limit);
		this.keepNoMoreThanTimeLabel.setEnabled(limit);
		this.keepNoMoreThanTimeInput.setEnabled(limit);	
		this.noteLabel.setEnabled(limit);
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e != null && e.getSource() == this.limitIncrementalBackupsCheckBox) {
			updateIncrementalBackupEnabledStates();
		}
	}
}
