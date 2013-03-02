/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.util;

import java.util.ResourceBundle;

/**
 * Enumeration of all displayed strings. Every value here should have a corresponding
 * value in Messages.properties (and any translated versions).
 */
public enum Message {
	EDIT_MENU_HEADER,				// header for Edit menu (e.g., Edit)
	EDIT_MENU_OPTION_UNDO,			// Edit menu Undo option
	EDIT_MENU_OPTION_REDO,			// Edit menu Redo option
	EDIT_MENU_OPTION_SET_TO_BLACK,	// Edit menu Set to Black option
	FILE_MENU_HEADER,				// header for File menu (e.g., File)
	FILE_MENU_OPTION_NEW,			// File menu New ... option
	FILE_MENU_OPTION_EXIT,			// File menu Exit option
	FILE_MENU_OPTION_SAVE_GRID_AS_TEMPLATE,	// File menu Save Grid as Template... option
	CELL_POPUP_MENU_OPTION_TOGGLE_CELL_BLACK,	// popup menu for cell, option to toggle cell black/white
	BUTTON_15X15,					// 15 x 15 button
	BUTTON_21X21,					// 21 x 21 button
	BUTTON_23X23,					// 23 x 23 button
	BUTTON_OK,						// text for OK button
	BUTTON_CANCEL,					// text for Cancel button
	BUTTON_CUSTOM,					// text for Custom button
	BUTTON_PICK_DATA_DIRECTORY,		// text for pick data directory button
	CLUES_TAB_TITLE,				// title for clues tab
	DEFAULT_GRID_NAME,				// default (empty) grid name
	DEFAULT_GRID_DESCRIPTION,		// default (empty) grid description 
	DIALOG_TITLE_NEW_CROSSWORD,		// title for New Crossword dialog box
	DIALOG_TEXT_NONEMPTY_NAME_DESCRIPTION,	// text for dialog reporting that name and description must be non-blank
	DIALOG_TITLE_NONEMPTY_NAME_DESCRIPTION,	// title for ""
	DIALOG_TEXT_SET_DATA_DIRECTORY,	// text for Set Data Directory dialog box
	DIALOG_TEXT_CONFIRM_DATA_DIRECTORY_CREATE, // text confirming creation of data directory
	DIALOG_TEXT_OVERWRITE_EXISTING_GRID,		// text for dialog prompting whether to overwrite existing grid template with same name
	DIALOG_TITLE_OVERWRITE_EXISTING_GRID,		// title for ""
	DIALOG_TEXT_CONFIRM_DUPLICATE_GRID,			// text for dialog confirming whether to create duplicate (by structure) grid template
	DIALOG_TITLE_CONFIRM_DUPLICATE_GRID,		// title for ""
	DIALOG_TEXT_UNABLE_TO_CREATE_DATA_DIRECTORY,	// text unable to create data directory {0}
	DIALOG_TITLE_SAVE_GRID_TEMPLATE,				// title for Save Grid Template dialog box
	DIALOG_TITLE_SET_DATA_DIRECTORY,// title for Set Data Directory dialog box
	DIALOG_TITLE_CONFIRM_DATA_DIRECTORY_CREATE,	// title for confirm data directory create dialog box
	DIALOG_TITLE_UNABLE_TO_CREATE_DATA_DIRECTORY,	// title unable to create data directory
	LABEL_DESCRIPTION,				// label Description
	LABEL_NAME,						// label Name
	PANEL_DIMENSIONS_TITLE,			// title for Dimensions panel
	PANEL_GRID_PICKER_TITLE,		// title for Grid picker panel
	PANEL_PREVIEW_TITLE,			// title for Preview panel
	MAIN_FRAME_TITLE,				// title for main JFrame
	STATS_TAB_TITLE,				// title for Stats tab
	TEXT_CELLS_WIDE,				// text "cells wide"
	TEXT_CELLS_HIGH,				// text "cells high"
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
	 * @return internationalized string resource corresponding 
	 */
	public String toString() {
		return resourceBundle_.getString(name());
	}
}
