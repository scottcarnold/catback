package org.xandercat.cat.back.swing.panel;

import java.awt.FlowLayout;
import java.text.NumberFormat;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;

import org.xandercat.swing.file.BinaryPrefix;
import org.xandercat.swing.file.ByteSize;
import org.xandercat.swing.zenput.marker.MarkTargetProvider;

public class ByteSizeInputPanel extends JPanel implements MarkTargetProvider {
	
	private static final long serialVersionUID = 1L;

	private JFormattedTextField valueTextField;
	private JComboBox<BinaryPrefix> unitComboBox;
	private Object[] markTargets = new Object[2];
	
	public ByteSizeInputPanel() {
		this(0);
	}
	
	public ByteSizeInputPanel(int textFieldColumns) {
		super(new FlowLayout());
		this.valueTextField = new JFormattedTextField(NumberFormat.getNumberInstance());
		if (textFieldColumns > 0) {
			this.valueTextField.setColumns(textFieldColumns);
		}
		this.unitComboBox = new JComboBox<BinaryPrefix>(BinaryPrefix.values());
		add(this.valueTextField);
		add(this.unitComboBox);
		markTargets[0] = valueTextField;
		markTargets[1] = unitComboBox;
	}
	
	public ByteSize getByteSize() {
		String text = this.valueTextField.getText();
		if (text == null || text.trim().length() == 0) {
			return null;
		}
		double value = Double.parseDouble(text);
		BinaryPrefix unit = (BinaryPrefix) this.unitComboBox.getSelectedItem();
		return new ByteSize(value, unit);
	}
	
	public void setByteSize(ByteSize byteSize) {
		this.valueTextField.setText(NumberFormat.getNumberInstance().format(byteSize.getValue()));
		this.unitComboBox.setSelectedItem(byteSize.getUnit());
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		this.valueTextField.setEnabled(enabled);
		this.unitComboBox.setEnabled(enabled);
	}

	@Override
	public Object[] getMarkTargets() {
		return markTargets;
	}
}
