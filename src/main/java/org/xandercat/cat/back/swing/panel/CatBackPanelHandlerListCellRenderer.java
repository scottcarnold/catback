package org.xandercat.cat.back.swing.panel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import org.xandercat.cat.back.media.Icons;
import org.xandercat.swing.util.ImageUtil;

public class CatBackPanelHandlerListCellRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = 2010072501L;
	private static final Color SELECTION_COLOR = new Color(50, 100, 250, 100);
		
	private boolean selected;
	
	public CatBackPanelHandlerListCellRenderer() {
		super();
		setOpaque(false);
		
	}
	public Component getListCellRendererComponent(JList<?> list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		CatBackPanelHandler panel = (CatBackPanelHandler) value;
		super.getListCellRendererComponent(list, panel.getTitle(), index, isSelected, cellHasFocus);
		setFont(getFont().deriveFont(Font.PLAIN, 22));
		setForeground(isSelected? Color.WHITE : Color.BLACK);
		setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		if (panel.isInputValid()) {
			setIcon(panel.getIcon());
		} else {
			setIcon(ImageUtil.overlayIcon(panel.getIcon(), Icons.WARNING_ICON));
		}
		selected = isSelected;
		return this;
	}
	
	@Override
	protected void paintComponent(Graphics graphics) {
		Graphics2D graphics2D = (Graphics2D) graphics;
		graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		if (selected) {
			graphics.setColor(SELECTION_COLOR); 
			int height = getHeight();
			graphics2D.fillRoundRect(0, 0, getWidth(), height, height/2, height/2);
			graphics.setColor(SELECTION_COLOR);
			graphics2D.drawRoundRect(0, 0, getWidth()-1, height-1, height/2, height/2);
		}
		super.paintComponent(graphics);

	}
}
