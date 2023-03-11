package org.xandercat.cat.back.swing.panel;

import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xandercat.swing.zenput.error.ZenputException;
import org.xandercat.swing.zenput.processor.InputProcessor;

/**
 * Wrapper for CatBackPanels that also provides panel navigation and other
 * properties needed by the main application.
 *  
 * @author Scott Arnold
 */
public class CatBackPanelHandler implements CatBackPanel {

	private static final Logger log = LogManager.getLogger(CatBackPanelHandler.class);
	
	private CatBackPanel catBackPanel;
	private boolean navBackAvailable = true;
	private boolean navForwardAvailable = true;
	private String navBackText = "Previous";
	private String navForwardText = "Next";
	private List<String> inputNames = new ArrayList<String>();
	private boolean inputValid = true;
	private InputProcessor inputProcessor;
	private int verticalScrollValue;
	private int horizontalScrollValue;
	
	public CatBackPanelHandler(CatBackPanel catBackPanel) {
		this.catBackPanel = catBackPanel;
	}

	public boolean isNavBackAvailable() {
		return navBackAvailable;
	}

	public void setNavBackAvailable(boolean navBackAvailable) {
		this.navBackAvailable = navBackAvailable;
	}

	public boolean isNavForwardAvailable() {
		return navForwardAvailable;
	}

	public void setNavForwardAvailable(boolean navForwardAvailable) {
		this.navForwardAvailable = navForwardAvailable;
	}

	public String getNavBackText() {
		return navBackText;
	}

	public void setNavBackText(String navBackText) {
		this.navBackText = navBackText;
	}

	public String getNavForwardText() {
		return navForwardText;
	}

	public void setNavForwardText(String navForwardText) {
		this.navForwardText = navForwardText;
	}

	public void backupClosed() {
		catBackPanel.backupClosed();
		this.inputValid = true;
		this.inputProcessor = null;
		this.inputNames.clear();
	}

	public List<String> backupOpened(BackupResources backupResources) throws ZenputException {
		List<String> inputFields = catBackPanel.backupOpened(backupResources);
		this.inputProcessor = backupResources.getInputProcessor();
		if (inputFields != null) {
			this.inputNames.addAll(inputFields);
		}
		return inputFields;
	}

	public void validateFields() {
		if (inputProcessor != null) {
			try {
				if (inputNames == null) {
					inputValid = inputProcessor.validate();
				} else {
					inputValid = inputProcessor.validate(inputNames);
				}	
			} catch (ZenputException ze) {
				log.error("Unable to validate fields.", ze);
			}
		}
	}
	
	public void panelActivating() {
		catBackPanel.panelActivating();	
		validateFields();
		
	}

	public void panelActivated() {
		catBackPanel.panelActivated();
		Container container = SwingUtilities.getAncestorOfClass(JScrollPane.class, catBackPanel.getComponent());
		if (container != null && container instanceof JScrollPane) {
			if (this.verticalScrollValue > 0) {
				JScrollBar verticalScrollBar = ((JScrollPane) container).getVerticalScrollBar();
				if (verticalScrollBar != null) {
					verticalScrollBar.setValue(this.verticalScrollValue);
				}
			}
			if (this.horizontalScrollValue > 0) {
				JScrollBar horizontalScrollBar = ((JScrollPane) container).getHorizontalScrollBar();
				if (horizontalScrollBar != null) {
					horizontalScrollBar.setValue(this.horizontalScrollValue);
				}
			}
		}
	}
	
	public void panelDeactivating() {
		this.verticalScrollValue = 0;
		this.horizontalScrollValue = 0;
		Container container = SwingUtilities.getAncestorOfClass(JScrollPane.class, catBackPanel.getComponent());
		if (container != null && container instanceof JScrollPane) {
			JScrollBar verticalScrollBar = ((JScrollPane) container).getVerticalScrollBar();
			if (verticalScrollBar != null) {
				this.verticalScrollValue = verticalScrollBar.getValue();
			}
			JScrollBar horizontalScrollBar = ((JScrollPane) container).getHorizontalScrollBar();
			if (horizontalScrollBar != null) {
				this.horizontalScrollValue = horizontalScrollBar.getValue();
			}			
		}
		catBackPanel.panelDeactivating();
		validateFields();
	}
	
	public JComponent getComponent() {
		return catBackPanel.getComponent();
	}

	public Icon getIcon() {
		return catBackPanel.getIcon();
	}

	public JLabel getStatusLabel() {
		return catBackPanel.getStatusLabel();
	}

	public String getTitle() {
		return catBackPanel.getTitle();
	}
	
	public List<String> getInputNames() {
		return inputNames;
	}

	public boolean isInputValid() {
		return inputValid;
	}
}
