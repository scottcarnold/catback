package org.xandercat.cat.back.swing.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xandercat.swing.file.BinaryPrefix;
import org.xandercat.swing.file.icon.FileIconCache;
import org.xandercat.swing.table.FileData;
import org.xandercat.swing.table.FileTable;
import org.xandercat.swing.table.FileTableModel;
import org.xandercat.swing.util.FileUtil;
import org.xandercat.swing.util.PlatformTool;

/**
 * Dialog to display files to be moved and copied as part of the backup process.  Two  
 * tabs will be displayed, each with a table of files.  First tab shows files that will be
 * moved from the main backup folder to an incremental folder.  Second tab shows files that
 * will be copied from the user's computer ot the main backup folder.
 * 
 * @author Scott Arnold
 */
public class MoveCopyDialog extends JDialog {

	private static final long serialVersionUID = 2010061101L;
	private static final Logger log = LogManager.getLogger(MoveCopyDialog.class);
	
	public static final int CONTINUE_OPTION = 1;
	public static final int CANCEL_OPTION = 0;
	
	private FileIconCache iconCache;
	private FileTableModel moveModel;
	private FileTableModel copyModel;
	private JLabel moveLabel;
	private JLabel copyLabel;
	private int result = CANCEL_OPTION;
	
	public MoveCopyDialog(Frame parent, FileIconCache iconCache) {
		super(parent, "Files to Move/Copy for Backup", true);
		initialize(iconCache, parent);
	}
	public MoveCopyDialog(Dialog parent, FileIconCache iconCache) {
		super(parent, "Files to Move/Copy for Backup", true);
		initialize(iconCache, parent);
	}
	
	private void initialize(FileIconCache iconCache, Component parent) {
		this.iconCache = iconCache;
		this.moveLabel = new JLabel();
		this.copyLabel = new JLabel();
		this.moveModel = new FileTableModel();
		this.copyModel = new FileTableModel();
		JPanel mainPanel = new JPanel(new BorderLayout());
		JTabbedPane tabbedPane = new JTabbedPane();
		JPanel filesToCopyPanel = buildStandardPanel(
				"These files will be copied from your computer to your main backup folder.  Such files have either been added to your backup set, are new, or have been renamed, moved, or edited since the last backup.",
				this.copyModel,
				this.copyLabel);
		tabbedPane.addTab("Files to Copy", filesToCopyPanel);
		JPanel filesToMovePanel = buildStandardPanel(
				"These files will be moved from your main backup folder to an incremental backup folder.  Such files have either been removed from your backup set, deleted, renamed, moved, or edited on your computer.",
				this.moveModel,
				this.moveLabel);
		tabbedPane.addTab("Files to Move", filesToMovePanel);
		mainPanel.add(tabbedPane, BorderLayout.CENTER);
		JPanel buttonPanel = new JPanel(new FlowLayout());
		JButton okButton = new JButton("Continue Backup");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				result = CONTINUE_OPTION;
				setVisible(false);
			}
		});
		JButton cancelButton = new JButton("Cancel Backup");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				result = CANCEL_OPTION;
				setVisible(false);
			}
		});
		PlatformTool.addOkCancelButtons(buttonPanel, okButton, cancelButton);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		setContentPane(mainPanel);
		setPreferredSize(new Dimension(600,600));
		pack();
		setLocationRelativeTo(parent);
	}
	
	private JPanel buildStandardPanel(String text, FileTableModel model, JLabel statusLabel) {
		JPanel panel = new JPanel(new BorderLayout());
		JTextPane textPane = new JTextPane();
		textPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		textPane.setBackground(new Color(0, 0, 0, 0));
		textPane.setEditable(false);
		StyledDocument doc = textPane.getStyledDocument();
		Style style = doc.addStyle("myStyle", null);
		StyleConstants.setBold(style, true);
		StyleConstants.setFontSize(style, 16);
		StyleConstants.setBackground(style, panel.getBackground());
		try {
			doc.insertString(0, text, style);
		} catch (Exception e) {
			log.error("Unable to setup document properly", e);
			textPane.setText(text);
		}
		//JPanel tpPanel = new JPanel(new BorderLayout());
		//tpPanel.add(textPane, BorderLayout.CENTER);
		panel.add(textPane, BorderLayout.NORTH);
		FileTable table = new FileTable(model, iconCache, 1);
		table.setAutoCreateRowSorter(true);
		JScrollPane scrollPane = new JScrollPane(table);
		panel.add(scrollPane, BorderLayout.CENTER);
		panel.add(statusLabel, BorderLayout.SOUTH);
		return panel;
	}
	
	public void setFilesToMove(List<FileData> files) {
		setFiles(this.moveModel, this.moveLabel, files);
	}
	
	public void setFilesToCopy(List<FileData> files) {
		setFiles(this.copyModel, this.copyLabel, files);
	}
	
	private void setFiles(FileTableModel model, JLabel label, List<FileData> files) {
		model.setElements(files);
		long totalSize = 0;
		for (FileData file : files) {
			totalSize += file.getLength().longValue();
		}
		label.setText("Total size of files: " + FileUtil.formatFileSize(totalSize, BinaryPrefix.GiB));
	}
	
	public int showDialog() {
		setVisible(true);
		return result;
	}
}
