package org.xandercat.cat.back.importer;

import java.util.HashSet;
import java.util.Set;

import org.xandercat.cat.back.CatBackup;
import org.xandercat.cat.back.CatBackup15;
import org.xandercat.swing.file.Importer;

/**
 * Importer for importing older CatBack backup versions to the latest version.
 * 
 * @author Scott Arnold
 */
public class OldBackupImporter implements Importer<CatBackup15> {

	private final Set<Class<?>> fromObjectClasses = new HashSet<Class<?>>();
	
	public OldBackupImporter() {
		fromObjectClasses.add(org.xandercat.cat.back.CatBackup.class);
	}
	
	@Override
	public Set<Class<?>> getFromObjectClasses() {
		return fromObjectClasses;
	}

	@Override
	public String getImportConfirmationMessage(Object o) {
		return "This backup you are trying to load is from an older version of CatBack.\n\n"
			+ "Would you like to import this backup to the current version?\n\n"
			+ "Note: After importing, unless you chose to Save As when saving your backup, the old version will be overwritten.";
	}

	@Override
	public CatBackup15 importObject(Object fromObject) {
		CatBackup15 backup = null;
		if (fromObject instanceof CatBackup) {
			CatBackup oldBackup = (CatBackup) fromObject;
			backup = new CatBackup15(oldBackup.getId());
			backup.setAlwaysLeaveCopyWindowOpen(oldBackup.isAlwaysLeaveCopyWindowOpen());
			backup.setBackupDirectory(oldBackup.getBackupDirectory());
			backup.setErrorsUntilBackupHalt(oldBackup.getErrorsUntilBackupHalt());
			backup.setExcludedFiles(oldBackup.getExcludedFiles());
			backup.setIncludedFiles(oldBackup.getIncludedFiles());
			backup.setKeepAtLeastTime(upgradeTimeDuration(oldBackup.getKeepAtLeastTime()));
			backup.setKeepNoMoreThanBytes(upgradeByteSize(oldBackup.getKeepNoMoreThanBytes()));
			backup.setKeepNoMoreThanTime(upgradeTimeDuration(oldBackup.getKeepNoMoreThanTime()));
			backup.setLimitIncrementalBackups(oldBackup.isLimitIncrementalBackups());
			backup.setName(oldBackup.getName());
			backup.setScanLastBackup(oldBackup.isScanLastBackup());
			backup.setShowFilesBeforeMoveCopy(oldBackup.isShowFilesBeforeMoveCopy());
		}
		return backup;
	}

	private org.xandercat.swing.datetime.TimeDuration upgradeTimeDuration(org.xandercat.common.util.TimeDuration oldDuration) {
		if (oldDuration == null) {
			return null;
		}
		org.xandercat.swing.datetime.TimeDuration newDuration = new org.xandercat.swing.datetime.TimeDuration();
		newDuration.setValue(oldDuration.getValue());
		switch (oldDuration.getUnit()) {
		case DAY:
			newDuration.setUnit(org.xandercat.swing.datetime.TimeDuration.Unit.DAY);
			break;
		case WEEK:
			newDuration.setUnit(org.xandercat.swing.datetime.TimeDuration.Unit.WEEK);
			break;
		case MONTH:
			newDuration.setUnit(org.xandercat.swing.datetime.TimeDuration.Unit.MONTH);
			break;
		case YEAR:
			newDuration.setUnit(org.xandercat.swing.datetime.TimeDuration.Unit.YEAR);
			break;
		}
		return newDuration;
	}
	
	private org.xandercat.swing.file.ByteSize upgradeByteSize(org.xandercat.common.util.ByteSize oldSize) {
		if (oldSize == null) {
			return null;
		}
		org.xandercat.swing.file.ByteSize newSize = new org.xandercat.swing.file.ByteSize(oldSize.getBytes());
		return newSize;
	}
}
