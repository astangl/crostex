/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.util;

import org.junit.*;

import us.stangl.crostex.util.Message;

import static org.junit.Assert.*;

/**
 * JUnit tests. for Message.
 */
public class MessageTest {
	@Test
	public void testAllMessagesInResourceBundle() {
		for (Message message : Message.values()) {
			assertNotNull("Message." + message.name() + " missing from Messages.properties",
					message.toString());
		}
	}

}
