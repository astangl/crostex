/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.constraint;

import org.junit.*;

import static org.junit.Assert.*;

import us.stangl.crostex.Grid;
import us.stangl.crostex.constraint.GridConstraint;
import us.stangl.crostex.constraint.SymmetryGridConstraint;

/**
 * JUnit tests for SymmetryGridConstraint.
 */
public class SymmetryGridConstraintTest {
	@Test
	public void test5x5() {
		Grid grid = new Grid(5, 5, "", "");
		GridConstraint constraint = new SymmetryGridConstraint();
		
		assertTrue(constraint.satisfiedBy(grid));
		
		grid.getCell(0, 4).setBlack(true);
		assertFalse(constraint.satisfiedBy(grid));
		
		grid.getCell(1, 1).setBlack(true);
		assertFalse(constraint.satisfiedBy(grid));

		grid.getCell(4, 0).setBlack(true);
		assertFalse(constraint.satisfiedBy(grid));
		
		grid.getCell(3, 3).setBlack(true);
		assertTrue(constraint.satisfiedBy(grid));
		
	}

}
