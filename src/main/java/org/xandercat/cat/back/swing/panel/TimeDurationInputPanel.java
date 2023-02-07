package org.xandercat.cat.back.swing.panel;

import java.awt.FlowLayout;
import java.text.NumberFormat;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;

import org.xandercat.swing.datetime.TimeDuration;
import org.xandercat.swing.zenput.marker.MarkTargetProvider;

public class TimeDurationInputPanel extends JPanel implements MarkTargetProvider {

	private static final long serialVersionUID = 1L;

	private JFormattedTextField valueTextField;
	private JComboBox unitComboBox;
	private Object[] markTargets = new Object[2];
	
	public TimeDurationInputPanel() {
		this(0);
	}
	
	public TimeDurationInputPanel(int textFieldColumns) {
		super(new FlowLayout());
		this.valueTextField = new JFormattedTextField(NumberFormat.getIntegerInstance());
		if (textFieldColumns > 0) {
			this.valueTextField.setColumns(textFieldColumns);
		}
		this.unitComboBox = new JComboBox(TimeDuration.Unit.values());
		add(this.valueTextField);
		add(this.unitComboBox);
		markTargets[0] = valueTextField;
		markTargets[1] = unitComboBox;
	}
	
	public TimeDuration getTimeDuration() {
		String text = this.valueTextField.getText();
		if (text == null || text.trim().length() == 0) {
			return null;
		}
		int value = Integer.parseInt(text);
		TimeDuration.Unit unit = (TimeDuration.Unit) this.unitComboBox.getSelectedItem();
		return new TimeDuration(value, unit);
	}
	
	public void setTimeDuration(TimeDuration timeDuration) {
		this.valueTextField.setText(String.valueOf(timeDuration.getValue()));
		this.unitComboBox.setSelectedItem(timeDuration.getUnit());
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
