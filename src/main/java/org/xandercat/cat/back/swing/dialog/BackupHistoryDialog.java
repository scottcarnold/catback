package org.xandercat.cat.back.swing.dialog;

import java.awt.Frame;
import java.text.DateFormat;
import java.util.Date;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableModel;

import org.xandercat.cat.back.engine.BackupStats;
import org.xandercat.cat.back.swing.table.BackupHistoryTableModel;
import org.xandercat.swing.file.BinaryPrefix;
import org.xandercat.swing.table.ColumnToggler;
import org.xandercat.swing.table.FileSizeRenderer;
import org.xandercat.swing.table.FormatRenderer;
import org.xandercat.swing.table.TimeLengthRenderer;

public class BackupHistoryDialog extends JDialog {

	private static final long serialVersionUID = 2010100201L;

	public BackupHistoryDialog(Frame owner, BackupStats backupStats) {
		super(owner, "Backup History", true);
		TableModel tableModel = new BackupHistoryTableModel(backupStats);
		JTable table = new JTable(tableModel);
		table.setAutoCreateRowSorter(true);
		new ColumnToggler(table);
		table.getColumnModel().getColumn(1).setCellRenderer(new TimeLengthRenderer());
		FileSizeRenderer renderer = new FileSizeRenderer(SwingConstants.RIGHT, BinaryPrefix.TiB);
		renderer.setMaxFractionDigits(1);
		table.getColumnModel().getColumn(6).setCellRenderer(renderer);
		renderer = new FileSizeRenderer(SwingConstants.RIGHT, BinaryPrefix.TiB);
		renderer.setMaxFractionDigits(1);
		table.getColumnModel().getColumn(7).setCellRenderer(renderer);
		table.setDefaultRenderer(Date.class, new FormatRenderer(DateFormat.getDateTimeInstance()));
		JScrollPane scrollPane = new JScrollPane(table);
		setContentPane(scrollPane);
		setSize(800, 400);
		setLocationRelativeTo(owner);
	}
	
	public void showDialog() {
		setVisible(true);
	}
}
