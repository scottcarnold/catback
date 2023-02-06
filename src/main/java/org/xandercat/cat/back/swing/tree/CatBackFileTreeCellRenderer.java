package org.xandercat.cat.back.swing.tree;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.io.File;

import javax.swing.JLabel;
import javax.swing.JTree;

import org.xandercat.swing.file.icon.FileIconCache;
import org.xandercat.swing.tree.CheckboxFileTree;
import org.xandercat.swing.tree.FileTreeCellRenderer;
import org.xandercat.swing.tree.FileTreeNode;

/**
 * File tree renderer that renders label text in different font colors and styles 
 * depending on the included and excluded state of the represented file.
 * 
 * This is only a file tree renderer and does not render checkboxes.  An instance of
 * this renderer should be wrapped in a CheckboxTreeCellRenderer for checkbox support.  
 * 
 * @author Scott Arnold
 */
public class CatBackFileTreeCellRenderer extends FileTreeCellRenderer {

	public static final Color COLOR_INACTIVE = Color.DARK_GRAY;
	public static final Color COLOR_ACTIVE = Color.BLACK;
	public static final Color COLOR_INCLUDED = Color.BLUE;
	public static final Color COLOR_EXCLUDED = Color.RED;
	public static final int FONT_STYLE_INACTIVE = Font.PLAIN;
	public static final int FONT_STYLE_ACTIVE = Font.BOLD;
	public static final int FONT_STYLE_INCLUDED = Font.BOLD;
	public static final int FONT_STYLE_EXCLUDED = Font.BOLD;
	
	private CheckboxFileTree includedTree;
	private CheckboxFileTree excludedTree;
	private Font inactiveFont;
	private Font activeFont;
	private Font includedFont;
	private Font excludedFont;
	
	public CatBackFileTreeCellRenderer(CheckboxFileTree includedTree, CheckboxFileTree excludedTree, FileIconCache fileIconCache) {
		super(fileIconCache);
		this.includedTree = includedTree;
		this.excludedTree = excludedTree;
	}
	
	private void initializeFonts(Font baseFont) {
		this.activeFont = baseFont.deriveFont(FONT_STYLE_ACTIVE);
		this.inactiveFont = baseFont.deriveFont(FONT_STYLE_INACTIVE);
		this.includedFont = baseFont.deriveFont(FONT_STYLE_INCLUDED);
		this.excludedFont = baseFont.deriveFont(FONT_STYLE_EXCLUDED);		
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		JLabel label = (JLabel) super.getTreeCellRendererComponent(
				tree, value, selected, expanded, leaf, row, hasFocus);
		FileTreeNode node = (FileTreeNode) value;
		File file = node.getFile();
		if (this.activeFont == null) {
			initializeFonts(label.getFont());
		}
		if (file != null) {
			if (this.excludedTree.isChecked(file)) {
				label.setFont(this.excludedFont);
				if (!selected) label.setForeground(COLOR_EXCLUDED);
			} else if (this.includedTree.isChecked(file)) {
				label.setFont(this.includedFont);
				if (!selected) label.setForeground(COLOR_INCLUDED);
			} else if (this.includedTree.isDescendantChecked(file) 
					|| this.excludedTree.isDescendantChecked(file)) {
				label.setFont(this.activeFont);
				if (!selected) label.setForeground(COLOR_ACTIVE);
			} else {
				label.setFont(this.inactiveFont);
				if (!selected) label.setForeground(COLOR_INACTIVE); 
			}
		} else if (node.isRoot()) {
			if (((CheckboxFileTree) tree).getCheckedFiles().size() > 0) {
				label.setFont(this.activeFont);
				if (!selected) label.setForeground(COLOR_ACTIVE);				
			} else {
				label.setFont(this.inactiveFont);
				if (!selected) label.setForeground(COLOR_INACTIVE); 				
			}
		}
		return label;
	}
	
	
}
