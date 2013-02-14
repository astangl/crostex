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
import javax.swing.SwingUtilities;
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
 */
public class NewCrosswordDialog extends JDialog {
	/** chosen width and height, -1 if cancelled */
	private int height_ = -1;
	private int width_ = -1;

	/** 15x15 default dimension button */
	private JRadioButton button15x15_ = new JRadioButton(Message.BUTTON_15X15.toString());
	
	/** 21x21 radio button */
	private JRadioButton button21x21_ = new JRadioButton(Message.BUTTON_21X21.toString());
	
	/** 23x23 radio button */
	private JRadioButton button23x23_ = new JRadioButton(Message.BUTTON_23X23.toString());
	
	/** custom radio button */
	private JRadioButton buttonCustom_ = new JRadioButton(Message.BUTTON_CUSTOM.toString());
	
	/** custom width text field */
	private JTextField customWidthField_ = new JTextField(3);
	
	/** custom height text field */
	private JTextField customHeightField_ = new JTextField(3);
	
	/** grid preview panel */
	private JPanel gridPreviewPanel_ = new GridPreviewPanel();
	
	/** full collection of all grid templates */
//	private final Collection<Grid> gridTemplates_;
	private final GridsDb gridsdb_;
	
	/** grid picker table model */
	private GridPickerTableModel tableModel_;
	
	/** table */
	private JTable table_;
	
	public NewCrosswordDialog(GridsDb gridsdb) {
		super((Frame)null, Message.DIALOG_TITLE_NEW_CROSSWORD.toString(), true);
		
		gridsdb_ = gridsdb;
//		gridTemplates_ = gridTemplates;
		tableModel_ = new GridPickerTableModel(gridsdb_.getGrids());
		tableModel_.setGridDimensions(15, 15);

		JPanel buttonsPanel = new JPanel();
		JButton okButton = new JButton(Message.BUTTON_OK.toString());
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (buttonCustom_.isSelected()) {
					try {
						height_ = Integer.parseInt(customHeightField_.getText());
						width_ = Integer.parseInt(customWidthField_.getText());
					} catch (NumberFormatException e) {
						// Should not be possible!
						throw new RuntimeException("NumberFormatException unexpectedly thrown for " + customHeightField_.getText()
								+ ", " + customWidthField_.getText(), e);
					}
				} else {
					int dim = button23x23_.isSelected() ? 23 : button21x21_.isSelected() ? 21 : 15;
					height_ = dim;
					width_ = dim;
				}
				dispose();
				System.out.println("Final height = " + height_ + ", width = " + width_);
			}
		});
		JButton cancelButton = new JButton(Message.BUTTON_CANCEL.toString());
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				dispose();
				
				// Throw away table so we cannot return a "selected grid"
				table_ = null;
				tableModel_ = null;
			}
		});

		buttonsPanel.add(okButton);
		buttonsPanel.add(cancelButton);
		
		getContentPane().add(new NewCrosswordPanel(), BorderLayout.CENTER);
//		getContentPane().add(new GridPickerPanel(), BorderLayout.EAST);
		getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
		pack();
		setVisible(true);
	}

	public Grid getSelectedGridTemplate() {
		if (table_ != null && tableModel_ != null) {
			int row = table_.getSelectedRow();
			return row == -1 ? null : tableModel_.getGridAtRow(row);
		}
		return null;
	}

	/**
	 * @return the height
	 */
	public int getGridHeight() {
		return height_;
	}

	/**
	 * @return the width
	 */
	public int getGridWidth() {
		return width_;
	}

	/**
	 * Panel for a New Crossword dialog box.
	 */
	private class NewCrosswordPanel extends JPanel {

		public NewCrosswordPanel() {
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			add(new LeftPanel());
			add(new GridPickerPanel());
		}
		
		private class LeftPanel extends JPanel {
			public LeftPanel() {
				setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
				add(new DimensionPanel());
				add(gridPreviewPanel_);
				
			}
		}
		
		private class DimensionPanel extends JPanel {
			/** button group */
			private ButtonGroup buttonGroup_ = new ButtonGroup();
			
			/** custom width label */
			private JLabel widthLabel_ = new JLabel(Message.TEXT_CELLS_WIDE.toString());
			
			/** custom height label */
			private JLabel heightLabel_ = new JLabel(Message.TEXT_CELLS_HIGH.toString());
			
			public DimensionPanel() {
//				setBorder(new CompoundBorder(
//						BorderFactory.createTitledBorder("Dimensions"),
//						BorderFactory.createEmptyBorder(10,10,10,10)));
				setBorder(BorderFactory.createTitledBorder(Message.PANEL_DIMENSIONS_TITLE.toString()));
//				setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
				Box box = new Box(BoxLayout.Y_AXIS);
				add(box);

				button15x15_.setSelected(true);
				button15x15_.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent evt) {
						if (button15x15_.isSelected()) {
							height_ = 15;
							width_ = 15;
							tableModel_.setGridDimensions(width_, height_);
						}
					}
				});
				box.add(button15x15_);
				buttonGroup_.add(button15x15_);

				button21x21_.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent evt) {
						if (button21x21_.isSelected()) {
							height_ = 21;
							width_ = 21;
							tableModel_.setGridDimensions(width_, height_);
						}
					}
				});
				box.add(button21x21_);
				buttonGroup_.add(button21x21_);

				button23x23_.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent evt) {
						if (button23x23_.isSelected()) {
							height_ = 23;
							width_ = 23;
							tableModel_.setGridDimensions(width_, height_);
						}
					}
				});
				box.add(button23x23_);
				buttonGroup_.add(button23x23_);

				box.add(buttonCustom_);
				buttonGroup_.add(buttonCustom_);
				
				box.add(new CustomDimensionPanel());
			}

			private class CustomDimensionPanel extends JPanel {
				public CustomDimensionPanel() {
					setAlignmentX(0.0f);
					Box outerBox = new Box(BoxLayout.X_AXIS);
					outerBox.add(Box.createRigidArea(new Dimension(25, 0)));
					Box innerBox = new Box(BoxLayout.Y_AXIS);
					outerBox.add(innerBox);
					add(outerBox);

					Box innermostBox1 = new Box(BoxLayout.X_AXIS);
					innermostBox1.add(customWidthField_);
					innermostBox1.add(Box.createRigidArea(new Dimension(5, 0)));
					innermostBox1.add(widthLabel_);
					innerBox.add(innermostBox1);
					
					Box innermostBox2 = new Box(BoxLayout.X_AXIS);
					
					innermostBox2.add(customHeightField_);
					innermostBox2.add(Box.createRigidArea(new Dimension(5, 0)));
					innermostBox2.add(heightLabel_);
					innerBox.add(innermostBox2);
					
					// Add validators to only allow positive integer values
					customHeightField_.setDocument(new IntegerDocument());
					customWidthField_.setDocument(new IntegerDocument());
					setCustomFieldsEnabled(false);
					
					buttonCustom_.addItemListener(new CustomButtonActionListener());
				}
			}
			
			private void setCustomFieldsEnabled(boolean value) {
				System.out.println("Setting all to " + value);
				widthLabel_.setEnabled(value);
				heightLabel_.setEnabled(value);
				customWidthField_.setEnabled(value);
				customHeightField_.setEnabled(value);
				
				if (! value) {
//					customWidthField_.setText("");
//					customHeightField_.setText("");
				}
			}
			
			private class CustomButtonActionListener implements ItemListener {
				public void itemStateChanged(ItemEvent evt) {
					setCustomFieldsEnabled(buttonCustom_.isSelected());
				}
			}
			
			// Integer document for enforcing validation of Integer textfields
			private class IntegerDocument extends PlainDocument {
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
			public GridPickerPanel() {
				setBorder(BorderFactory.createTitledBorder(Message.PANEL_GRID_PICKER_TITLE.toString()));
//				setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
				Box box = new Box(BoxLayout.Y_AXIS);
				add(box);

				table_ = new JTable(tableModel_);
				table_.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				table_.getSelectionModel().addListSelectionListener(new MyTableSelectionListener());
				JScrollPane scrollPane = new JScrollPane(table_);
				scrollPane.setPreferredSize(new Dimension(400, 400));
				table_.setPreferredSize(new Dimension(100, 100));
				box.add(scrollPane);
//				getContentPane().add(scrollPane);
			}
		}
		
		private class MyTableSelectionListener implements ListSelectionListener {
			public void valueChanged(ListSelectionEvent evt) {
				gridPreviewPanel_.repaint();
			}
		}
	}
	private class GridPreviewPanel extends JPanel {
		public GridPreviewPanel() {
			setBorder(BorderFactory.createTitledBorder(Message.PANEL_PREVIEW_TITLE.toString()));
			add(new GridPreviewSubpanel());
		}
		
		private class GridPreviewSubpanel extends JPanel {
			
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
				if (table_ != null && tableModel_ != null) {
					int row = table_.getSelectedRow();
					if (row != -1)
						tableModel_.getGridAtRow(row).renderThumbnail(g2, getWidth(), getHeight());
				}
			}
		}
	}
}
