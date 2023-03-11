package org.xandercat.cat.back;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.xandercat.swing.frame.FrameState;

/**
 * Settings and preferences for the CatBack application.  
 * 
 * @author Scott Arnold
 */
public class CatBackSettings implements Serializable {

	private static final long serialVersionUID = 2010081301L;
	
	public static final int MAX_RECENTLY_LOADED_FILES = 5;
	public static final File SETTINGS_FILE = new File("catback_settings.dat");
	public static final File TREESTATES_FILE = new File("catback_trees.dat");
	public static final String BACKUP_STATS_FILE_NAME = ".catback_stats";
	
	private List<File> recentlyLoadedFiles = new ArrayList<File>();
	private String lookAndFeelName;
	private FrameState frameState = new FrameState();
	
	public List<File> getRecentlyLoadedFiles() {
		return recentlyLoadedFiles;
	}
	
	public void setRecentlyLoadedFiles(List<File> recentlyLoadedFiles) {
		this.recentlyLoadedFiles = recentlyLoadedFiles;
	}
	
	public String getLookAndFeelName() {
		return lookAndFeelName;
	}
	
	public void setLookAndFeelName(String lookAndFeelName) {
		this.lookAndFeelName = lookAndFeelName;
	}

	public FrameState getFrameState() {
		return frameState;
	}

	public void setFrameState(FrameState frameState) {
		this.frameState = frameState;
	}
}
