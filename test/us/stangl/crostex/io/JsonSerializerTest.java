/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import us.stangl.crostex.util.MiscUtils;

/**
 * Unit tests for JsonSerializer.
 * @author Alex Stangl
 */
public class JsonSerializerTest {

	// big JSON test cases
	private static final String BIG_CASE_1 = " { \"name\" : \"Daily Planet Crossword, December 25, 2013\","
			+ "\"author\":\"Clark Kent\", \"description\":\"\",\"notes\":null,\"width\" : 15, \"height\": 15  \t\r\n, "
			+ "\"values\" :   [false ,\ttrue,null,\rnull\n,\rnull,0.567,123.7682,-1E22,-0.666,-321.77,-321,2.3E+5,"
			+ "\"crypto\"]  }";
	private static final String BIG_CASE_2 = " [ " + BIG_CASE_1 + "," + BIG_CASE_1 + ",  \t" + BIG_CASE_1 + "]";
	
	@SuppressWarnings("unchecked")
	@Test
	public void testBigCase1() throws JsonParsingException {
		Object obj = new JsonSerializer().parseJsonString(BIG_CASE_1);
		assertNotNull(obj);
		assertTrue(obj instanceof Map);
		testBigCase1Map((Map<String, ?>)obj);
	}
	
	private void testBigCase1Map(Map<String, ?> map) {
		assertEquals(7, map.size());
		assertEquals("Daily Planet Crossword, December 25, 2013", map.get("name"));
		assertEquals("Clark Kent", map.get("author"));
		assertEquals("", map.get("description"));
		assertTrue(map.containsKey("notes"));
		assertNull(map.get("notes"));
		assertEquals(new Double("15"), map.get("width"));
		assertEquals(new Double("15"), map.get("height"));
		List<?> values = (List<?>)map.get("values");
		assertEquals(13, values.size());
		assertEquals(MiscUtils.<Object>arrayList(false, true, null, null, null, new Double("0.567"), new Double("123.7682"),
				new Double("-1E22"), new Double("-0.666"), new Double("-321.77"), new Double("-321"),
				new Double("2.3E+5"), "crypto"), values);
	}
	
	@Test
	public void testBigCase2() throws JsonParsingException {
		Object obj = new JsonSerializer().parseJsonString(BIG_CASE_2);
		assertNotNull(obj);
		assertTrue(obj instanceof List);
		List<?> list = (List<?>)obj;
		assertEquals(3, list.size());
		for (Object element : list)
			testBigCase1Map((Map<String, ?>)element);
	}
	
	@Test
	public void testBigCase1InUtf8() throws JsonParsingException, UnsupportedEncodingException {
		Object obj = new JsonSerializer().parseJsonBytes(BIG_CASE_1.getBytes("UTF-8"));
		assertNotNull(obj);
		assertTrue(obj instanceof Map);
		testBigCase1Map((Map<String, ?>)obj);
	}
	
	@Test
	public void testBigCase1InUtf16le() throws JsonParsingException, UnsupportedEncodingException {
		Object obj = new JsonSerializer().parseJsonBytes(BIG_CASE_1.getBytes("UTF-16LE"));
		assertNotNull(obj);
		assertTrue(obj instanceof Map);
		testBigCase1Map((Map<String, ?>)obj);
	}
	
	@Test
	public void testBigCase1InUtf16be() throws JsonParsingException, UnsupportedEncodingException {
		Object obj = new JsonSerializer().parseJsonBytes(BIG_CASE_1.getBytes("UTF-16BE"));
		assertNotNull(obj);
		assertTrue(obj instanceof Map);
		testBigCase1Map((Map<String, ?>)obj);
	}
	
	@Test
	public void testEscapedStrings() throws JsonParsingException {
		Object obj = new JsonSerializer().parseJsonString("[ \"\\\"Quoted String\\\"\", \"Line1\\nLine2\", \"Line1\\rLine2\", \"\\u0041l\\u0065x\"]");
		assertNotNull(obj);
		assertTrue(obj instanceof List);
		List<String> list = (List<String>)obj;
		assertEquals(4, list.size());
		assertEquals("\"Quoted String\"", list.get(0));
		assertEquals("Line1\nLine2", list.get(1));
		assertEquals("Line1\rLine2", list.get(2));
		assertEquals("Alex", list.get(3));
	}
	
	@Test
	public void testEmptyArray() throws JsonParsingException {
		Object obj = new JsonSerializer().parseJsonString("[]");
		assertTrue(obj instanceof List);
		List<?> list = (List<?>)obj;
		assertTrue(list.isEmpty());
		
		obj = new JsonSerializer().parseJsonString("   \t [ \r\n  ]\n");
		assertTrue(obj instanceof List);
		list = (List<?>)obj;
		assertTrue(list.isEmpty());
	}
	
	@Test
	public void testEmptyObject() throws JsonParsingException {
		Object obj = new JsonSerializer().parseJsonString("{}");
		assertTrue(obj instanceof Map);
		Map<?,?> map = (Map<?,?>)obj;
		assertTrue(map.isEmpty());
		
		obj = new JsonSerializer().parseJsonString("   \t\t { \r\n  }\n");
		assertTrue(obj instanceof Map);
		map = (Map<?,?>)obj;
		assertTrue(map.isEmpty());
	}
	
	@Test(expected=JsonParsingException.class)
	public void testEmptyParse() throws JsonParsingException {
		new JsonSerializer().parseJsonString("");
	}
	
	@Test(expected=JsonParsingException.class)
	public void testBadLeadingZero() throws JsonParsingException {
		new JsonSerializer().parseJsonString("[00]");
	}

	@Test(expected=JsonParsingException.class)
	public void testBadLeadingPlus() throws JsonParsingException {
		new JsonSerializer().parseJsonString("[+1]");
	}

	@Test(expected=JsonParsingException.class)
	public void testBadExponent() throws JsonParsingException {
		new JsonSerializer().parseJsonString("[123E, 4]");
	}

	@Test(expected=JsonParsingException.class)
	public void testBadMissingFraction1() throws JsonParsingException {
		new JsonSerializer().parseJsonString("[123.,35]");
	}

	@Test(expected=JsonParsingException.class)
	public void testBadMissingFraction2() throws JsonParsingException {
		new JsonSerializer().parseJsonString("[-123.]");
	}

	@Test(expected=JsonParsingException.class)
	public void testBadExtraSpace() throws JsonParsingException {
		new JsonSerializer().parseJsonString("[123E 45, 4]");
	}

	@Test(expected=JsonParsingException.class)
	public void testBadBareNumber() throws JsonParsingException {
		new JsonSerializer().parseJsonString("4");
	}

	@Test(expected=JsonParsingException.class)
	public void testBadBareString1() throws JsonParsingException {
		new JsonSerializer().parseJsonString("\"name\"");
	}

	@Test(expected=JsonParsingException.class)
	public void testBadBareString2() throws JsonParsingException {
		new JsonSerializer().parseJsonString("name");
	}
}
