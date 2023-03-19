package org.xandercat.cat.back.swing.panel;

import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import org.xandercat.cat.back.BackupSizeCalculator;
import org.xandercat.cat.back.BackupSizeListener;
import org.xandercat.cat.back.CatBackup16;
import org.xandercat.cat.back.media.Icons;
import org.xandercat.cat.back.swing.tree.CheckboxFileTreeChangeAction;
import org.xandercat.swing.component.ComponentFactory;
import org.xandercat.swing.file.BinaryPrefix;
import org.xandercat.swing.file.FileInfoAction;
import org.xandercat.swing.file.FilesSize;
import org.xandercat.swing.file.icon.FileIconCache;
import org.xandercat.swing.label.RotatingIconLabel;
import org.xandercat.swing.tree.CheckboxFileTree;
import org.xandercat.swing.tree.CheckboxFileTreeNode;
import org.xandercat.swing.tree.CheckboxFileTreeRefreshNodeAction;
import org.xandercat.swing.util.FileUtil;
import org.xandercat.swing.util.ResourceManager;

/**
 * Component provider for CatBack included and excluded checkbox file trees.
 * 
 * @author Scott Arnold
 */
public class CheckboxFileTreePanel implements CatBackPanel, BackupSizeListener, MouseListener {

	public static Type INCLUDED = Type.INCLUDED;
	public static Type EXCLUDED = Type.EXCLUDED;
	
	private static enum Type {
		INCLUDED, EXCLUDED;
	}

	private CheckboxFileTree tree;
	private CheckboxFileTree excludedTree;
	private Type type;
	private RotatingIconLabel sizeLabel;
	private CatBackup16 backup;
	
	public CheckboxFileTreePanel(Type type) {
		this.type = type;
	}
	
	@Override
	public void backupClosed() {
		this.sizeLabel.stopAnimate();
		this.sizeLabel.setText("");
		this.tree.removeMouseListener(this);
		this.tree = null;
		this.excludedTree = null;
		this.backup = null;
	}

	@Override
	public List<String> backupOpened(BackupResources backupResources) {
		BackupSizeCalculator backupSizeCalculator = backupResources.getBackupSizeCalculator();
		this.backup = backupResources.getFileManager().getObject();
		this.sizeLabel = ComponentFactory.createSpinnerLabel(" ");
		this.excludedTree = backupResources.getExcludedTree();
		FilesSize filesSize = null;
		if (type == INCLUDED) {
			this.tree = backupResources.getIncludedTree();
			filesSize = backupSizeCalculator.getTotalSize();
		} else {
			this.tree = excludedTree;
			filesSize = backupSizeCalculator.getExcludedSize();
		}
		this.tree.addMouseListener(this);
		setSizeLabel(filesSize);
		backupSizeCalculator.addBackupSizeListener(this);
		return new ArrayList<String>();
	}

	@Override
	public void panelActivating() {
		// no action required	
	}
	
	@Override
	public void panelActivated() {
		// no action required	
	}

	@Override
	public void panelDeactivating() {
		// no action required	
	}

	private void setSizeLabel(FilesSize size) {
		if (size == null) {
			sizeLabel.setText("Calculating Size...");
			sizeLabel.startAnimate();
		} else {
			sizeLabel.setText(FileUtil.formatFileSize(size.getBytes(), BinaryPrefix.GiB));
			sizeLabel.stopAnimate();
		}
	}
	
	public void checkForPopup(MouseEvent event) {
		if (event.isPopupTrigger() && this.tree != null) {
			CheckboxFileTree tree = (CheckboxFileTree) event.getSource();
			TreePath path = tree.getPathForLocation(event.getX(), event.getY());
			if (path != null) {
				CheckboxFileTreeNode node = (CheckboxFileTreeNode) path.getLastPathComponent();
				File file = node.getFile();
				Window parent = SwingUtilities.getWindowAncestor(tree);
				FileIconCache fileIconCache = ResourceManager.getInstance().getResource(FileIconCache.class);
				JPopupMenu popupMenu = new JPopupMenu();
				JMenuItem menuItem = new JMenuItem(new FileInfoAction(parent, file, fileIconCache));
				popupMenu.add(menuItem);
				popupMenu.addSeparator();
				CheckboxFileTreeChangeAction includeAction = new CheckboxFileTreeChangeAction(file, this.excludedTree, false);
				menuItem = new JMenuItem(includeAction);
				menuItem.setText("Include");
				popupMenu.add(menuItem);
				CheckboxFileTreeChangeAction excludeAction = new CheckboxFileTreeChangeAction(file, this.excludedTree, true);
				menuItem = new JMenuItem(excludeAction);
				menuItem.setText("Exclude");
				if (this.type == INCLUDED) {
					includeAction.setRepaintComponent(this.tree);
					excludeAction.setRepaintComponent(this.tree);
				}
				popupMenu.add(menuItem);
				popupMenu.addSeparator();
				CheckboxFileTreeRefreshNodeAction refreshAction = null;
				if (this.type == INCLUDED) {
					refreshAction = new CheckboxFileTreeRefreshNodeAction(tree, node, backup.getIncludedFiles(), backup.getIncludedDirectories());
				} else {
					refreshAction = new CheckboxFileTreeRefreshNodeAction(tree, node, backup.getExcludedFiles(), backup.getExcludedDirectories());
				}
				menuItem = new JMenuItem(refreshAction);
				popupMenu.add(menuItem);
				popupMenu.show(tree, event.getX(), event.getY());
			}
		}
	}	
	@Override
	public JComponent getComponent() {
		return tree;
	}

	@Override
	public Icon getIcon() {
		return (type == INCLUDED)? Icons.FOLDER_BLUE_ICON : Icons.FOLDER_RED_ICON;
	}

	@Override
	public String getTitle() {
		return (type == INCLUDED)? "Included Files" : "Excluded Files";
	}

	@Override
	public JLabel getStatusLabel() {
		return sizeLabel;
	}

	@Override
	public void backupSizeCalculating() {
		setSizeLabel(null);
	}

	@Override
	public void backupSizeChange(FilesSize backupSize, FilesSize excludedSize) {
		setSizeLabel((type == INCLUDED)? backupSize : excludedSize);
	}
	
	@Override
	public void mouseClicked(MouseEvent event) {
	}

	@Override
	public void mouseEntered(MouseEvent event) {
	}

	@Override
	public void mouseExited(MouseEvent event) {
	}

	@Override
	public void mousePressed(MouseEvent event) {
		checkForPopup(event);
	}

	@Override
	public void mouseReleased(MouseEvent event) {
		checkForPopup(event);
	}	
}
