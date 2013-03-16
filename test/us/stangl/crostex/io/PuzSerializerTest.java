/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.io;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import us.stangl.crostex.Grid;
import us.stangl.crostex.GridFactory;

/**
 * Unit tests for PUZ serialization logic.
 * @author Alex Stangl
 */
public class PuzSerializerTest {
	@Test()
	public void testPuzRead() throws IOException {
		//FileReader reader = new FileReader();
		byte[] bytes = FileReader.getFileBytes(new File("test", "BostonGlobe19221210.puz"));
		PuzSerializer serializer = new PuzSerializer();
		Grid grid = serializer.fromBytes(bytes, new GridFactory(), true);
		assertEquals(23, grid.getWidth());
		assertEquals(22, grid.getHeight());
		byte[] outputBytes = serializer.toPuz(grid);
		System.out.println("outputBytes = " + outputBytes);
	}
}
