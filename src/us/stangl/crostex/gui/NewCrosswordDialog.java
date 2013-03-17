/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import us.stangl.crostex.Grid;
import us.stangl.crostex.GridsDb;
import us.stangl.crostex.util.Message;

/**
 * Dialog box prompting for creation of a new crossword.
 * @author Alex Stangl
 */
public class NewCrosswordDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	// logger
	private static final Logger LOG = Logger.getLogger(NewCrosswordDialog.class.getName());

	// chosen width and height, or -1 if cancelled
	private int height = -1;
	private int width = -1;

	// 15x15 default dimension button
	private JRadioButton button15x15 = new JRadioButton(Message.BUTTON_15X15.toString());
	
	// 21x21 radio button
	private JRadioButton button21x21 = new JRadioButton(Message.BUTTON_21X21.toString());
	
	// 23x23 radio button
	private JRadioButton button23x23 = new JRadioButton(Message.BUTTON_23X23.toString());
	
	// custom radio button
	private JRadioButton buttonCustom = new JRadioButton(Message.BUTTON_CUSTOM.toString());
	
	// custom width text field
	private JTextField customWidthField = new JTextField(3);
	
	// custom height text field
	private JTextField customHeightField = new JTextField(3);
	
	// grid preview panel
	private JPanel gridPreviewPanel = new GridPreviewPanel();
	
	// full collection of all grid templates
	private final GridsDb gridsdb;
	
	// grid picker table model
	private GridPickerTableModel tableModel;
	
	// table
	private JTable table;
	
	public NewCrosswordDialog(GridsDb gridsdb) {
		super((Frame)null, Message.DIALOG_TITLE_NEW_CROSSWORD.toString(), true);
		
		this.gridsdb = gridsdb;
		tableModel = new GridPickerTableModel(this.gridsdb.getGrids());
		tableModel.setGridDimensions(15, 15);

		JPanel buttonsPanel = new JPanel();
		JButton okButton = new JButton(Message.BUTTON_OK.toString());
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (buttonCustom.isSelected()) {
					try {
						height = Integer.parseInt(customHeightField.getText());
						width = Integer.parseInt(customWidthField.getText());
					} catch (NumberFormatException e) {
						// Should not be possible!
						throw new RuntimeException("NumberFormatException unexpectedly thrown for " + customHeightField.getText()
								+ ", " + customWidthField.getText(), e);
					}
				} else {
					int dim = button23x23.isSelected() ? 23 : button21x21.isSelected() ? 21 : 15;
					height = dim;
					width = dim;
				}
				dispose();
				System.out.println("Final height = " + height + ", width = " + width);
			}
		});
		JButton cancelButton = new JButton(Message.BUTTON_CANCEL.toString());
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				dispose();
				
				// Throw away table so we cannot return a "selected grid"
				table = null;
				tableModel = null;
			}
		});

		buttonsPanel.add(okButton);
		buttonsPanel.add(cancelButton);
		
		getContentPane().add(new NewCrosswordPanel(), BorderLayout.CENTER);
		getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
		pack();
		setVisible(true);
	}

	public Grid getSelectedGridTemplate() {
		if (table != null && tableModel != null) {
			int row = table.getSelectedRow();
			return row == -1 ? null : tableModel.getGridAtRow(row);
		}
		return null;
	}

	/**
	 * @return the height
	 */
	public int getGridHeight() {
		return height;
	}

	/**
	 * @return the width
	 */
	public int getGridWidth() {
		return width;
	}

	/**
	 * Panel for a New Crossword dialog box.
	 */
	private class NewCrosswordPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		public NewCrosswordPanel() {
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			add(new LeftPanel());
			add(new GridPickerPanel());
		}
		
		private class LeftPanel extends JPanel {
			private static final long serialVersionUID = 1L;

			public LeftPanel() {
				setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
				add(new DimensionPanel());
				add(gridPreviewPanel);
				
			}
		}
		
		private final class DimensionPanel extends JPanel {
			private static final long serialVersionUID = 1L;

			// button group
			private ButtonGroup buttonGroup = new ButtonGroup();
			
			// custom width label
			private JLabel widthLabel = new JLabel(Message.TEXT_CELLS_WIDE.toString());
			
			// custom height label
			private JLabel heightLabel = new JLabel(Message.TEXT_CELLS_HIGH.toString());
			
			public DimensionPanel() {
				setBorder(BorderFactory.createTitledBorder(Message.PANEL_DIMENSIONS_TITLE.toString()));
				Box box = new Box(BoxLayout.Y_AXIS);
				add(box);

				button15x15.setSelected(true);
				
				addWidthHeightListener(button15x15, 15, 15);
				box.add(button15x15);
				buttonGroup.add(button15x15);

				addWidthHeightListener(button21x21, 21, 21);
				box.add(button21x21);
				buttonGroup.add(button21x21);

				addWidthHeightListener(button23x23, 23, 23);
				box.add(button23x23);
				buttonGroup.add(button23x23);

				box.add(buttonCustom);
				buttonGroup.add(buttonCustom);
				
				box.add(new CustomDimensionPanel());
			}
			
			// add ItemListener to the specified radio button to set the specified width and height
			private void addWidthHeightListener(final JRadioButton button, final int width, final int height) {
				button.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent evt) {
						if (button.isSelected()) {
							NewCrosswordDialog.this.height = 23;
							NewCrosswordDialog.this.width = 23;
							tableModel.setGridDimensions(width, height);
						}
					}
				});
			}

			private class CustomDimensionPanel extends JPanel {
				private static final long serialVersionUID = 1L;

				public CustomDimensionPanel() {
					setAlignmentX(0.0f);
					Box outerBox = new Box(BoxLayout.X_AXIS);
					outerBox.add(Box.createRigidArea(new Dimension(25, 0)));
					Box innerBox = new Box(BoxLayout.Y_AXIS);
					outerBox.add(innerBox);
					add(outerBox);

					Box innermostBox1 = new Box(BoxLayout.X_AXIS);
					innermostBox1.add(customWidthField);
					innermostBox1.add(Box.createRigidArea(new Dimension(5, 0)));
					innermostBox1.add(widthLabel);
					innerBox.add(innermostBox1);
					
					Box innermostBox2 = new Box(BoxLayout.X_AXIS);
					
					innermostBox2.add(customHeightField);
					innermostBox2.add(Box.createRigidArea(new Dimension(5, 0)));
					innermostBox2.add(heightLabel);
					innerBox.add(innermostBox2);
					
					// Add validators to only allow positive integer values
					customHeightField.setDocument(new IntegerDocument());
					customWidthField.setDocument(new IntegerDocument());
					setCustomFieldsEnabled(false);
					
					buttonCustom.addItemListener(new CustomButtonActionListener());
				}
			}
			
			private void setCustomFieldsEnabled(boolean value) {
				LOG.fine("Setting all to " + value);
				widthLabel.setEnabled(value);
				heightLabel.setEnabled(value);
				customWidthField.setEnabled(value);
				customHeightField.setEnabled(value);
				
				if (! value) {
//					customWidthField_.setText("");
//					customHeightField_.setText("");
				}
			}
			
			private class CustomButtonActionListener implements ItemListener {
				public void itemStateChanged(ItemEvent evt) {
					setCustomFieldsEnabled(buttonCustom.isSelected());
				}
			}
			
			// Integer document for enforcing validation of Integer textfields
			private class IntegerDocument extends PlainDocument {
				private static final long serialVersionUID = 1L;

				public void insertString(int offset, String s, AttributeSet attributeSet) throws BadLocationException {
					try {
						if (Integer.parseInt(s) > 0) {
							super.insertString(offset, s, attributeSet);
							return;
						}
					} catch (NumberFormatException e) {
					}
					// Only allow positive integer values
					Toolkit.getDefaultToolkit().beep();
				}
			}
		}

	
		private class GridPickerPanel extends JPanel {
			private static final long serialVersionUID = 1L;

			public GridPickerPanel() {
				setBorder(BorderFactory.createTitledBorder(Message.PANEL_GRID_PICKER_TITLE.toString()));
				Box box = new Box(BoxLayout.Y_AXIS);
				add(box);

				table = new JTable(tableModel);
				table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				table.getSelectionModel().addListSelectionListener(new MyTableSelectionListener());
				JScrollPane scrollPane = new JScrollPane(table);
				scrollPane.setPreferredSize(new Dimension(400, 400));
				table.setPreferredSize(new Dimension(100, 100));
				box.add(scrollPane);
			}
		}
		
		private class MyTableSelectionListener implements ListSelectionListener {
			public void valueChanged(ListSelectionEvent evt) {
				gridPreviewPanel.repaint();
			}
		}
	}
	private class GridPreviewPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		public GridPreviewPanel() {
			setBorder(BorderFactory.createTitledBorder(Message.PANEL_PREVIEW_TITLE.toString()));
			add(new GridPreviewSubpanel());
		}
		
		private class GridPreviewSubpanel extends JPanel {
			private static final long serialVersionUID = 1L;

			public GridPreviewSubpanel() {
				setPreferredSize(new Dimension(23 * 5 + 1, 23 * 5 + 1));
			}
			
			/**
			 * Paint grid thumbnail
			 */
			public void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D)g;
				super.paintComponent(g);
				
				// Draw grid
				if (table != null && tableModel != null) {
					int row = table.getSelectedRow();
					if (row != -1)
						tableModel.getGridAtRow(row).renderThumbnail(g2, getWidth(), getHeight());
				}
			}
		}
	}
}
