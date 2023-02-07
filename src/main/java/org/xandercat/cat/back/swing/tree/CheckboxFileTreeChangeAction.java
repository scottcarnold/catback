package org.xandercat.cat.back.swing.tree;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JComponent;

import org.xandercat.swing.tree.CheckboxFileTree;

/**
 * Action class for selecting (checking) or unselecting (unchecking) a file in a checkbox
 * file tree.
 * 
 * @author Scott Arnold
 */
public class CheckboxFileTreeChangeAction extends AbstractAction {

	private static final long serialVersionUID = 2009102401L;
	
	private File file;
	private boolean isDirectory;
	private CheckboxFileTree tree;
	private boolean checked;
	private JComponent repaintComponent;
	
	public CheckboxFileTreeChangeAction(File file, CheckboxFileTree tree, boolean checked) {
		this(file, file.isDirectory(), tree, checked);
	}
	
	public CheckboxFileTreeChangeAction(File file, boolean isDirectory, CheckboxFileTree tree, boolean checked) {
		super();
		this.file = file;
		this.isDirectory = isDirectory;
		this.tree = tree;
		this.checked = checked;
		updateEnabled();
	}
	
	public void updateEnabled() {
		setEnabled(checked != tree.isChecked(file));
	}
	
	public void setRepaintComponent(JComponent component) {
		this.repaintComponent = component;
	}
	
	public void actionPerformed(ActionEvent event) {
		tree.selectAddFile(file, isDirectory, checked);
		if (repaintComponent != null) {
			repaintComponent.repaint();
		}
	}
}
