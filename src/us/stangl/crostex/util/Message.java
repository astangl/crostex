/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.util;

import java.util.ResourceBundle;

import javax.swing.JLabel;

/**
 * Enumeration of all displayed strings. Every value here should have a corresponding
 * value in Messages.properties (and any translated versions).
 * @author Alex Stangl
 */
public enum Message {
	BUTTON_15X15,					// 15 x 15 button
	BUTTON_21X21,					// 21 x 21 button
	BUTTON_23X23,					// 23 x 23 button
	BUTTON_CANCEL,					// text for Cancel button
	BUTTON_CUSTOM,					// text for Custom button
	BUTTON_OK,						// text for OK button
	BUTTON_PICK_DATA_DIRECTORY,		// text for pick data directory button
	CELL_POPUP_MENU_OPTION_AUTOFILL,			// popup menu option to auto-fill
	CELL_POPUP_MENU_OPTION_ENTER_REBUS,			// popup menu option to enter rebus
	CELL_POPUP_MENU_OPTION_TOGGLE_CELL_BLACK,	// popup menu for cell, option to toggle cell black/white
	CELL_POPUP_MENU_OPTION_TOGGLE_CELL_CIRCLED,	// popup menu for cell, option to toggle cell circled/uncircled
	COMBO_BOX_OPTION_SKIP_BLACK,	// combo box Skip Black option
	COMBO_BOX_OPTION_SKIP_BLACK_AND_FILLED,		// combo box Skip Black and Filled option
	COMBO_BOX_OPTION_SKIP_NONE,		// combo box Skip None option
	DEFAULT_GRID_DESCRIPTION,		// default (empty) grid description 
	DEFAULT_GRID_NAME,				// default (empty) grid name
	DIALOG_TEXT_CONFIRM_DATA_DIRECTORY_CREATE, // text confirming creation of data directory
	DIALOG_TEXT_CONFIRM_DUPLICATE_GRID,			// text for dialog confirming whether to create duplicate (by structure) grid template
	DIALOG_TEXT_ENTER_REBUS_TEXT,	// text for dialog prompting for rebus text
	DIALOG_TEXT_NONEMPTY_NAME_DESCRIPTION,	// text for dialog reporting that name and description must be non-blank
	DIALOG_TEXT_OVERWRITE_EXISTING_FILE,	// text for dialog confirming whether to overwrite an existing file
	DIALOG_TEXT_OVERWRITE_EXISTING_GRID,		// text for dialog prompting whether to overwrite existing grid template with same name
	DIALOG_TEXT_SET_DATA_DIRECTORY,	// text for Set Data Directory dialog box
	DIALOG_TEXT_UNABLE_TO_CREATE_DATA_DIRECTORY,	// text unable to create data directory {0}
	DIALOG_TITLE_CONFIRM_DATA_DIRECTORY_CREATE,	// title for confirm data directory create dialog box
	DIALOG_TITLE_CONFIRM_DUPLICATE_GRID,		// title for ""
	DIALOG_TITLE_NEW_CROSSWORD,		// title for New Crossword dialog box
	DIALOG_TITLE_NONEMPTY_NAME_DESCRIPTION,	// title for ""
	DIALOG_TITLE_OVERWRITE_EXISTING_GRID,		// title for dialog box confirming whether to overwrite existing grid
	DIALOG_TITLE_OVERWRITE_FILE,		// title for dialog box confirming whether to overwrite existing file
	DIALOG_TITLE_PREFERENCES,		// title for Preferences dialog box
	DIALOG_TITLE_SAVE_GRID_TEMPLATE,				// title for Save Grid Template dialog box
	DIALOG_TITLE_SET_DATA_DIRECTORY,// title for Set Data Directory dialog box
	DIALOG_TITLE_UNABLE_TO_CREATE_DATA_DIRECTORY,	// title unable to create data directory
	EDIT_MENU_HEADER,				// header for Edit menu (e.g., Edit)
	EDIT_MENU_OPTION_PREFERENCES,	// Edit menu option Preferences
	EDIT_MENU_OPTION_REDO,			// Edit menu Redo option
	EDIT_MENU_OPTION_SET_TO_BLACK,	// Edit menu Set to Black option
	EDIT_MENU_OPTION_UNDO,			// Edit menu Undo option
	FILE_MENU_HEADER,				// header for File menu (e.g., File)
	FILE_MENU_OPTION_EXIT,			// File menu Exit option
	FILE_MENU_OPTION_EXPORT_AS_IPUZ,	// File menu Export as IPUZ option
	FILE_MENU_OPTION_EXPORT_AS_JPZ,	// File menu Export as JPZ option
	FILE_MENU_OPTION_EXPORT_AS_PUZ,	// File menu Export as PUZ option
	FILE_MENU_OPTION_IMPORT_IPUZ,	// File menu Import IPUZ option
	FILE_MENU_OPTION_IMPORT_JPZ,	// File menu Import JPZ option
	FILE_MENU_OPTION_IMPORT_PUZ,	// File menu Import PUZ file option
	FILE_MENU_OPTION_NEW,			// File menu New ... option
	FILE_MENU_OPTION_PRINT,			// File menu Print option
	FILE_MENU_OPTION_SAVE_GRID_AS_TEMPLATE,	// File menu Save Grid as Template... option
	HELP_ABOUT_DIALOG_TEXT,			// text in Help/About dialog box
	HELP_HTML_CONTENTS,				// Help Contents, in HTML
	HELP_MENU_HEADER,				// header for Help menu
	HELP_MENU_OPTION_ABOUT,			// Help menu option About Crostex
	HELP_MENU_OPTION_HELP_CONTENTS,	// Help menu option Help Contents
	LABEL_ACROSS,					// label Across
	LABEL_AUTHOR,					// label Author
	LABEL_COPYRIGHT,				// label Copyright
	LABEL_DATA_DIRECTORY,			// label Data Directory
	LABEL_DEFAULT_AUTHOR,			// label Default Author
	LABEL_DEFAULT_COPYRIGHT,		// label Default Copyright
	LABEL_DEFAULT_NOTES,			// label Default Notes
	LABEL_DESCRIPTION,				// label Description
	LABEL_DOWN,						// label Down
	LABEL_ENFORCE_SYMMETRY,			// label Enforce Symmetry
	LABEL_LETTER_FREQUENCY_CHART,	// label for letter frequency chart
	LABEL_NAME,						// label Name
	LABEL_NOTES,					// label Notes
	LABEL_SHOW_NUMBERS,				// label Show Numbers
	LABEL_TITLE,					// label Title
	LABEL_WRAPAROUND_CURSOR,		// label Wraparound Cursor
	MAIN_FRAME_TITLE,				// title for main JFrame
	MESSAGE_MIN_3_LETTER_WORD_CONSTRAINT_MET,		// message Minimum 3-letter word constraint met
	MESSAGE_MIN_3_LETTER_WORD_CONSTRAINT_VIOLATED,	// message Minimum 3-letter word constraint violated
	MESSAGE_SINGLE_POLYOMINO_CONSTRAINT_MET,		// message Single Polyomino constraint met
	MESSAGE_SINGLE_POLYOMINO_CONSTRAINT_VIOLATED,	// message Single Polyomino constraint violated
	MESSAGE_SYMMETRIC_GRID_CONSTRAINT_MET,			// message Symmetric grid constraint met
	MESSAGE_SYMMETRIC_GRID_CONSTRAINT_VIOLATED,		// message Symmetric grid constraint violated
	PANEL_DIMENSIONS_TITLE,			// title for Dimensions panel
	PANEL_GRID_PICKER_TITLE,		// title for Grid picker panel
	PANEL_PREVIEW_TITLE,			// title for Preview panel
	TAB_TITLE_CLUES,				// title for clues tab
	TAB_TITLE_REBUS,				// title for Rebus tab
	TAB_TITLE_STATS,				// title for Stats tab
	TEXT_CELLS_HIGH,				// text "cells high"
	TEXT_CELLS_WIDE,				// text "cells wide"
	TEXT_DESCRIPTION,				// text "description"
	TEXT_NAME,						// text "name"
	TEXT_NUMBER_OF_WORDS,			// text "Number of Words"
	TEXT_PERCENT_BLACK,				// text "% black"
	UNTITLED_TAB_TITLE,				// title for tabs for Untitled crosswords
	;
	
	private static ResourceBundle resourceBundle_;
	static {
		resourceBundle_ = ResourceBundle.getBundle("Messages");
	}
	
	/**
	 * @return internationalized string resource corresponding to this message 
	 */
	public String toString() {
		return resourceBundle_.getString(name());
	}
	
	/**
	 * @return JLabel with internationalized string resource corresponding to this message 
	 */
	public JLabel label() {
		return new JLabel(toString());
	}
}
