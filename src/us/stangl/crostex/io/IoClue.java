/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.io;

import us.stangl.crostex.AcrossDownDirection;

/**
 * Interface for a clue.
 * @author Alex Stangl
 */
public interface IoClue {
	/**
	 * @return number associated with clue
	 */
	int getNumber();
	
	/**
	 * @return direction of this clue
	 */
	AcrossDownDirection getDirection();

	/**
	 * @return text of clue
	 */
	String getClueText();
	
	/**
	 * Set new clue text.
	 * @param text new clue text
	 */
	void setClueText(String text);
	
}
