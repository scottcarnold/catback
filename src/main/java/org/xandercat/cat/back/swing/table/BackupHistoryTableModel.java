package org.xandercat.cat.back.swing.table;

import java.util.Date;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.xandercat.cat.back.engine.BackupStat;
import org.xandercat.cat.back.engine.BackupStats;
import org.xandercat.cat.back.engine.BackupStatus;

public class BackupHistoryTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 2010100201L;

	private static final String[] COLUMN_NAMES = new String[] { "Date", "Duration", "Status", "Files Copied", "Files Moved", "Total Files", "Backup Size", "Incremental Size"};

	private List<BackupStat> stats;
	
	public BackupHistoryTableModel(BackupStats backupStats) {
		this.stats = backupStats.getBackupStats();
	}
	
	@Override
	public int getColumnCount() {
		return COLUMN_NAMES.length;
	}

	@Override
	public int getRowCount() {
		return stats.size();
	}

	@Override
	public String getColumnName(int column) {
		return COLUMN_NAMES[column];
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return Date.class;
		case 1:
			return Long.class;
		case 2:
			return BackupStatus.class;
		case 3:
		case 4:
		case 5:
			return Integer.class;
		case 6:
		case 7:
			return Long.class;
		default:
			return String.class;
		}
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		BackupStat stat = stats.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return stat.getDateStarted();
		case 1:
			return stat.getDateFinished().getTime() - stat.getDateStarted().getTime();
		case 2:
			return stat.getBackupStatus();
		case 3:
			return stat.getFilesCopied();
		case 4:
			return stat.getFilesMoved();
		case 5:
			return stat.getTotalFiles();
		case 6:
			return stat.getBackupSize();
		case 7:
			return stat.getIncrementalBackupSize();
		default:
			return "Unknown";
		}
	}

}
