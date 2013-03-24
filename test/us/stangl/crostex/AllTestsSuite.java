/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex;

import junit.framework.TestSuite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import us.stangl.crostex.constraint.OnePolyominoGridConstraintTest;
import us.stangl.crostex.constraint.SymmetryGridConstraintTest;
import us.stangl.crostex.gui.CrosswordPanelTest;
import us.stangl.crostex.io.JsonSerializerTest;
import us.stangl.crostex.util.CircularListTest;
import us.stangl.crostex.util.IdentityHashSetTest;
import us.stangl.crostex.util.MessageTest;
import us.stangl.crostex.util.MiscUtilsTest;
import us.stangl.crostex.util.StackTest;
import us.stangl.crostex.util.StringUtilsTest;
 
/**
 * Test suite to run all unit tests.
 * @author Alex Stangl
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	CircularListTest.class,
	CrosswordPanelTest.class,
	IdentityHashSetTest.class,
	JsonSerializerTest.class,
	MessageTest.class,
	MiscUtilsTest.class,
	OnePolyominoGridConstraintTest.class,
	RomanNumeralGeneratorTest.class,
	StackTest.class,
	StringUtilsTest.class,
	SymmetryGridConstraintTest.class,
	TrieTest.class
})
public class AllTestsSuite extends TestSuite {
    // the class remains completely empty, 
    // being used only as a holder for the above annotations
}
