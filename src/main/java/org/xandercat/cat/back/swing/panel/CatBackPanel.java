package org.xandercat.cat.back.swing.panel;

import java.util.List;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.xandercat.swing.zenput.error.ZenputException;

public interface CatBackPanel {
	
	public String getTitle();
	
	public Icon getIcon();
	
	/**
	 * Setup panel for a newly opened backup.  Returned list should be a list of input field
	 * names the panel relies on.  If the panel relies on all input fields, a value of null
	 * can be returned.  If the panel relies on no input fields, an empty list can be returned.  
	 * 
	 * @param backupResources
	 * @return
	 * @throws ZenputException
	 */
	public List<String> backupOpened(BackupResources backupResources) throws ZenputException;
	
	public void backupClosed();
	
	public void panelActivating();
	
	public void panelActivated();
	
	public void panelDeactivating();
	
	public JComponent getComponent();
	
	public JLabel getStatusLabel();
}
