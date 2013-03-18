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
	 * Set number associated with clue
	 * @param number number associated with clue
	 */
	void setNumber(int number);
	
	/**
	 * @return direction of this clue
	 */
	AcrossDownDirection getDirection();
	
	/**
	 * Set direction associated with this clue.
	 * @param direction direction associated with this clue
	 */
	void setDirection(AcrossDownDirection direction);

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
