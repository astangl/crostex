/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.constraint;

import junit.framework.TestCase;
import us.stangl.crostex.Grid;
import us.stangl.crostex.constraint.GridConstraint;
import us.stangl.crostex.constraint.OnePolyominoGridConstraint;

/**
 * JUnit tests for OnePolyominoGridConstraint
 * @author Alex Stangl
 */
public class OnePolyominoGridConstraintTest extends TestCase {

	public void testBasic() {
		Grid grid = new Grid(3, 3, "", "");
		GridConstraint constraint = new OnePolyominoGridConstraint();
		assertTrue(constraint.satisfiedBy(grid));
		grid.getCell(1, 1).setBlack(true);
		assertTrue(constraint.satisfiedBy(grid));
		grid.getCell(0, 2).setBlack(true);
		assertTrue(constraint.satisfiedBy(grid));
		grid.getCell(2, 0).setBlack(true);
		assertFalse(constraint.satisfiedBy(grid));
	}
}
