/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.gui;

import java.io.File;
import java.text.MessageFormat;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import us.stangl.crostex.util.Message;

/**
 * JFileChooser that confirms overwrite before overwriting a file.
 * @author Alex Stangl
 */
public class FileChooserConfirmingOverwrite extends JFileChooser {
	private static final long serialVersionUID = 1L;

	@Override
	public void approveSelection() {
		File f = getSelectedFile();
		if (f.exists() && getDialogType() == SAVE_DIALOG) {
			String msg = MessageFormat.format(Message.DIALOG_TEXT_OVERWRITE_EXISTING_FILE.toString(), f.getPath());
			int result = JOptionPane.showConfirmDialog(this, msg,
					Message.DIALOG_TITLE_OVERWRITE_FILE.toString(), JOptionPane.YES_NO_CANCEL_OPTION);
			if (result == JOptionPane.YES_OPTION)
				super.approveSelection();
			else if (result == JOptionPane.CANCEL_OPTION)
				cancelSelection();
		} else {
			super.approveSelection();
		}
	}
}
