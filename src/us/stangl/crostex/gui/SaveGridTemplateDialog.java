/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import us.stangl.crostex.Grid;
import us.stangl.crostex.GridsDb;
import us.stangl.crostex.util.Message;

/**
 * Dialog box for saving a grid as a new template.
 */
public class SaveGridTemplateDialog  extends JDialog {
	
	/** name text field */
	private JTextField nameField_ = new JTextField(20);
	
	/** description text field */
	private JTextField descriptionField_ = new JTextField(20);
	
	/** reference to main grids db */
	private final GridsDb gridsDb_;
	
	/** reference to grid being saved */
	private final Grid grid_;

	public SaveGridTemplateDialog(GridsDb gridsDb, Grid grid) {
		super((Frame)null, Message.DIALOG_TITLE_SAVE_GRID_TEMPLATE.toString(), true);
		gridsDb_ = gridsDb;
		grid_ = grid;
		
		JPanel buttonsPanel = new JPanel();
		JButton okButton = new JButton(Message.BUTTON_OK.toString());
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				System.out.println("Clicked OK");
				String name = nameField_.getText().trim();
				String description = descriptionField_.getText().trim();
				if (name.length() == 0 || description.length() == 0) {
					JOptionPane.showMessageDialog(SaveGridTemplateDialog.this,
							Message.DIALOG_TEXT_NONEMPTY_NAME_DESCRIPTION.toString(),
							Message.DIALOG_TITLE_NONEMPTY_NAME_DESCRIPTION.toString(),
							JOptionPane.ERROR_MESSAGE);
				} else {
					for (Grid grid : gridsDb_.getGrids()) {
						if (grid.getName().equals(name)) {
							String msg = MessageFormat.format(Message.DIALOG_TEXT_OVERWRITE_EXISTING_GRID.toString(), name);
							int confirmOverwrite = JOptionPane.showConfirmDialog(SaveGridTemplateDialog.this,
									msg,
									Message.DIALOG_TITLE_OVERWRITE_EXISTING_GRID.toString(),
									JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
							if (confirmOverwrite == JOptionPane.NO_OPTION) {
								abort();
								return;
							}
						}
						if (grid.isStructureEqualTo(grid_)) {
							String msg = MessageFormat.format(Message.DIALOG_TEXT_CONFIRM_DUPLICATE_GRID.toString(), grid.getName());
							int confirmOverwrite = JOptionPane.showConfirmDialog(SaveGridTemplateDialog.this,
									msg,
									Message.DIALOG_TITLE_CONFIRM_DUPLICATE_GRID.toString(),
									JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
							if (confirmOverwrite == JOptionPane.NO_OPTION) {
								abort();
								return;
							}
						}
					}
					dispose();
				}
			}
		});
		JButton cancelButton = new JButton(Message.BUTTON_CANCEL.toString());
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				System.out.println("Clicked Cancel");
				abort();
			}
		});

		buttonsPanel.add(okButton);
		buttonsPanel.add(cancelButton);
		
		getContentPane().add(new SaveGridTemplatePanel(), BorderLayout.CENTER);
//		getContentPane().add(new GridPickerPanel(), BorderLayout.EAST);
		getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
		pack();
		setVisible(true);

	}
	
	public JTextField getNameField() {
		return nameField_;
	}
	public JTextField getDescriptionField() {
		return descriptionField_;
	}

	/** abort dialog box, for example, when Cancel is clicked */
	private void abort() {
		nameField_ = null;
		descriptionField_ = null;
		dispose();
	}
	
	/**
	 * Panel for a New Crossword dialog box.
	 */
	private class SaveGridTemplatePanel extends JPanel {
		/** name label */
		private JLabel nameLabel_ = new JLabel(Message.LABEL_NAME.toString());
		
		/** description label */
		private JLabel descriptionLabel_ = new JLabel(Message.LABEL_DESCRIPTION.toString());
		

		public SaveGridTemplatePanel() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			Box nameBox = new Box(BoxLayout.X_AXIS);
			nameBox.add(nameLabel_);
			nameBox.add(Box.createRigidArea(new Dimension(5, 0)));
			nameBox.add(nameField_);
			add(nameBox);
			
			Box descriptionBox = new Box(BoxLayout.X_AXIS);
			descriptionBox.add(descriptionLabel_);
			descriptionBox.add(Box.createRigidArea(new Dimension(5, 0)));
			descriptionBox.add(descriptionField_);
			add(descriptionBox);

//			add(nameField_);
//			add(descriptionField_);
		}
	}
}
