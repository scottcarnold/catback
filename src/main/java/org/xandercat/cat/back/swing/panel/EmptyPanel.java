package org.xandercat.cat.back.swing.panel;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.xandercat.cat.back.media.Images;
import org.xandercat.cat.back.swing.frame.CatBackFrame;
import org.xandercat.swing.component.ButtonFactory;
import org.xandercat.swing.panel.DesignerPanel;

/**
 * Empty panel to be displayed when no backup is open in the application.
 * 
 * @author Scott Arnold
 */
public class EmptyPanel extends DesignerPanel implements CatBackPanel {

	private static final long serialVersionUID = 2010072501L;
	
	private CatBackFrame catBackFrame;
	
	public EmptyPanel(final CatBackFrame catBackFrame) {
		super();
		this.catBackFrame = catBackFrame;
		addSingleImage(Images.getImage(Images.CATBACK), true, true, SwingConstants.CENTER, 0.1f);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JButton newProfileButton = createLinkButton("create a new backup...", Component.CENTER_ALIGNMENT);
		newProfileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				catBackFrame.executeNew();
			}
		});
		JButton openProfileButton = createLinkButton("open an existing backup...", Component.CENTER_ALIGNMENT);
		openProfileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				catBackFrame.executeOpen(null);
			}
		});
		JLabel welcomeLabel = new JLabel("Welcome to " + catBackFrame.getApplicationName() + "!");
		JLabel qLabel = new JLabel("How would you like to get started?");
		welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		qLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		welcomeLabel.setFont(welcomeLabel.getFont().deriveFont(Font.BOLD));
		qLabel.setFont(qLabel.getFont().deriveFont(Font.PLAIN));
		add(Box.createVerticalStrut(50));
		add(welcomeLabel);
		add(qLabel);
		add(Box.createVerticalStrut(20));
		add(newProfileButton);
		add(Box.createVerticalStrut(20));
		add(openProfileButton);
	}
	
	private JButton createLinkButton(String text, float align) {
		JButton button = new JButton(text);
		ButtonFactory.makeLink(catBackFrame, button);
		button.setAlignmentX(align);
		button.setFont(button.getFont().deriveFont(Font.PLAIN));
		return button;
	}

	@Override
	public void backupClosed() {
		// no action required
	}

	@Override
	public List<String> backupOpened(BackupResources backupResources) {
		return null;
	}

	@Override
	public void panelActivating() {
		// no action required
	}

	@Override
	public void panelDeactivating() {
		// no action required
	}
	
	@Override
	public JComponent getComponent() {
		return this;
	}

	@Override
	public Icon getIcon() {
		return null;
	}

	@Override
	public JLabel getStatusLabel() {
		return null;
	}

	@Override
	public String getTitle() {
		return "Empty Panel";
	}
}
