package org.xandercat.cat.back;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.xandercat.swing.tree.TreeState;

/**
 * Tree states for backup files.  This class stores information on what nodes of the trees
 * are expanded.  This is stored separately from the backups themselves because changing
 * the expanded state of a tree is not considered a change to the actual backup (a user should
 * not be asked if they want to save their changes if the only change was expanding or 
 * collapsing a node of the tree).
 * 
 * @author Scott Arnold
 */
public class CatBackupTreeStates implements Serializable {

	private static final long serialVersionUID = 2009103001L;
	
	private Map<String, TreeState> includedTreeStates = new HashMap<String, TreeState>();
	private Map<String, TreeState> excludedTreeStates = new HashMap<String, TreeState>();
	
	public CatBackupTreeStates() {
	}
	
	public void setTreeStates(String uuid, TreeState includedTreeState, TreeState excludedTreeState) {
		this.includedTreeStates.put(uuid, includedTreeState);
		this.excludedTreeStates.put(uuid, excludedTreeState);
	}
	
	public TreeState getIncludedTreeState(String uuid) {
		return this.includedTreeStates.get(uuid);
	}
	
	public TreeState getExcludedTreeState(String uuid) {
		return this.excludedTreeStates.get(uuid);
	}
}
