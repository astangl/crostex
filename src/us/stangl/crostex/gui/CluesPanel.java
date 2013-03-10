/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.gui;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import us.stangl.crostex.Clue;
import us.stangl.crostex.Grid;
import us.stangl.crostex.GridChangeListener;
import us.stangl.crostex.util.Message;

/**
 * Panel to display clues on a side tab.
 * @author Alex Stangl
 */
public final class CluesPanel extends JPanel implements GridChangeListener {
	private static final long serialVersionUID = 1L;
	
	// length to use for text fields
	private static final int TEXT_FIELD_LENGTH = 40;
	
	// associated grid
	private final Grid grid;
	

	public CluesPanel(Grid grid) {
		this.grid = grid;
		grid.addChangeListener(this);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		regenerate();
	}
	
	public void regenerate() {
		removeAll();
		GridBagLayout gbl = new GridBagLayout();
		//Border border = BorderFactory.createLineBorder(Color.RED);
		JPanel topPanel = new JPanel();
		//topPanel.setBorder(border);
		topPanel.setLayout(gbl);
		
		int row = 0;
		topPanel.add(new JLabel(Message.LABEL_ACROSS.toString()), new GBC(0, row).anchor(GBC.NORTHWEST));
		topPanel.add(new JLabel(), new GBC(1, row).weightx(1.0).gridwidth(GBC.REMAINDER));
		++row;
		List<Clue> acrossClues = grid.validateAndGetAcrossClues();
		for (final Clue clue : acrossClues) {
			topPanel.add(new JLabel(Integer.toString(clue.getWordNumber()) + "  " + clue.getGridWord()), new GBC(0, row).anchor(GBC.NORTHWEST));
			final JTextField textField = new JTextField(clue.getClueText(), TEXT_FIELD_LENGTH);
			boolean editable = clue.isWordComplete();
			if (editable) {
				textField.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						clue.setClueText(textField.getText());
					}
				});
				GuiUtils.addListenerToCommitOnFocusLost(textField);
			}
			textField.setEnabled(editable);
			textField.setEditable(editable);
			topPanel.add(textField, new GBC(1, row).anchor(GBC.NORTHWEST).weightx(1.0).gridwidth(GBC.REMAINDER));
			++row;
		}
		//topPanel.add();
		topPanel.add(new JLabel(Message.LABEL_DOWN.toString()), new GBC(0, row).anchor(GBC.NORTHWEST));
		++row;
		List<Clue> downClues = grid.validateAndGetDownClues();
		for (final Clue clue : downClues) {
			topPanel.add(new JLabel(Integer.toString(clue.getWordNumber()) + "  " + clue.getGridWord()), new GBC(0, row).anchor(GBC.NORTHWEST));
			final JTextField textField = new JTextField(clue.getClueText(), TEXT_FIELD_LENGTH);
			boolean editable = clue.isWordComplete();
			if (editable) {
				textField.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						clue.setClueText(textField.getText());
					}
				});
				GuiUtils.addListenerToCommitOnFocusLost(textField);
			}
			textField.setEnabled(editable);
			textField.setEditable(editable);
			topPanel.add(textField, new GBC(1, row).anchor(GBC.NORTHWEST).weightx(1.0).gridwidth(GBC.REMAINDER));
			++row;
		}
		add(topPanel);
	}
	
	/* (non-Javadoc)
	 * @see us.stangl.crostex.GridChangeListener#handleChange(us.stangl.crostex.Grid)
	 */
	@Override
	public void handleChange(Grid grid) {
		regenerate();
	}

}
