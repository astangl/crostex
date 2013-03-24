/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
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
	public void testParsingBigCase1() throws JsonSerializationException {
		Object obj = new JsonSerializer().parseJsonString(BIG_CASE_1);
		assertNotNull(obj);
		assertTrue(obj instanceof Map);
		testParsingBigCase1Map((Map<String, ?>)obj);
	}
	
	private void testParsingBigCase1Map(Map<String, ?> map) {
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
	public void testParsingBigCase2() throws JsonSerializationException {
		Object obj = new JsonSerializer().parseJsonString(BIG_CASE_2);
		assertNotNull(obj);
		assertTrue(obj instanceof List);
		List<?> list = (List<?>)obj;
		assertEquals(3, list.size());
		for (Object element : list)
			testParsingBigCase1Map((Map<String, ?>)element);
	}
	
	@Test
	public void testParsingBigCase1InUtf8() throws JsonSerializationException, UnsupportedEncodingException {
		Object obj = new JsonSerializer().parseJsonBytes(BIG_CASE_1.getBytes("UTF-8"));
		assertNotNull(obj);
		assertTrue(obj instanceof Map);
		testParsingBigCase1Map((Map<String, ?>)obj);
	}
	
	@Test
	public void testParsingBigCase1InUtf16le() throws JsonSerializationException, UnsupportedEncodingException {
		Object obj = new JsonSerializer().parseJsonBytes(BIG_CASE_1.getBytes("UTF-16LE"));
		assertNotNull(obj);
		assertTrue(obj instanceof Map);
		testParsingBigCase1Map((Map<String, ?>)obj);
	}
	
	@Test
	public void testParsingBigCase1InUtf16be() throws JsonSerializationException, UnsupportedEncodingException {
		Object obj = new JsonSerializer().parseJsonBytes(BIG_CASE_1.getBytes("UTF-16BE"));
		assertNotNull(obj);
		assertTrue(obj instanceof Map);
		testParsingBigCase1Map((Map<String, ?>)obj);
	}
	
	@Test
	public void testParsingEscapedStrings() throws JsonSerializationException {
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
	public void testParsingEmptyArray() throws JsonSerializationException {
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
	public void testParsingEmptyObject() throws JsonSerializationException {
		Object obj = new JsonSerializer().parseJsonString("{}");
		assertTrue(obj instanceof Map);
		Map<?,?> map = (Map<?,?>)obj;
		assertTrue(map.isEmpty());
		
		obj = new JsonSerializer().parseJsonString("   \t\t { \r\n  }\n");
		assertTrue(obj instanceof Map);
		map = (Map<?,?>)obj;
		assertTrue(map.isEmpty());
	}
	
	@Test(expected=JsonSerializationException.class)
	public void testParsingEmptyParse() throws JsonSerializationException {
		new JsonSerializer().parseJsonString("");
	}
	
	@Test(expected=JsonSerializationException.class)
	public void testParsingBadLeadingZero() throws JsonSerializationException {
		new JsonSerializer().parseJsonString("[00]");
	}

	@Test(expected=JsonSerializationException.class)
	public void testParsingBadLeadingPlus() throws JsonSerializationException {
		new JsonSerializer().parseJsonString("[+1]");
	}

	@Test(expected=JsonSerializationException.class)
	public void testParsingBadExponent() throws JsonSerializationException {
		new JsonSerializer().parseJsonString("[123E, 4]");
	}

	@Test(expected=JsonSerializationException.class)
	public void testParsingBadMissingFraction1() throws JsonSerializationException {
		new JsonSerializer().parseJsonString("[123.,35]");
	}

	@Test(expected=JsonSerializationException.class)
	public void testParsingBadMissingFraction2() throws JsonSerializationException {
		new JsonSerializer().parseJsonString("[-123.]");
	}

	@Test(expected=JsonSerializationException.class)
	public void testParsingBadExtraSpace() throws JsonSerializationException {
		new JsonSerializer().parseJsonString("[123E 45, 4]");
	}

	@Test(expected=JsonSerializationException.class)
	public void testParsingBadBareNumber() throws JsonSerializationException {
		new JsonSerializer().parseJsonString("4");
	}

	@Test(expected=JsonSerializationException.class)
	public void testParsingBadBareString1() throws JsonSerializationException {
		new JsonSerializer().parseJsonString("\"name\"");
	}

	@Test(expected=JsonSerializationException.class)
	public void testParsingBadBareString2() throws JsonSerializationException {
		new JsonSerializer().parseJsonString("name");
	}
	
	@Test
	public void testEncodingSimpleMap() throws JsonSerializationException {
		Map<String, Object> map = new HashMap<String, Object>();
		addDataToMap(map);
		
		String string = new JsonSerializer().toJsonString(map);
		checkStringifiedMap(string);
		System.out.println(string);
		/*
		assertTrue(string.contains("\"author\":\"Jonathan Swift\""));
		assertTrue(string.contains("\"title\":\"A Modest Proposal\""));
		assertTrue(string.contains("\"year\":1729"));
		assertTrue(string.contains("\"notes\":\"line1\\u000aline2\\u000d\\u000aline3\\u000athere is no line 4\\u0008\\u0008\\u0008\\u0008\\u0008\\u0008\\u0008\\u0008\\u0008\\u0008\\u0008\\u0008\\u0008\\u0008\\u0008\\u0008\\u0008\\u0008"));
		*/
	}
	
	@Test
	public void testEncodingArrayWithEmbeddedMapAndNullsAndBooleans() throws JsonSerializationException {
		List<Object> list = new ArrayList<Object>();
		addDataToList(list);
		Map<String, Object> map = new HashMap<String, Object>();
		addDataToMap(map);
		list.add(map);
		String string = new JsonSerializer().toJsonString(list);
		System.out.println(string);
		assertTrue(string.startsWith("[true,-6.717,0,2.0E-12,false,null,{\""));
		checkStringifiedMap(string);
		
	}
	
	@Test(expected=JsonSerializationException.class)
	public void testEncodingWithBackrefs() throws JsonSerializationException
	{
		List<Object> list = new ArrayList<Object>();
		list.add(list);
		String string = new JsonSerializer().toJsonString(list);
		System.out.println(string);
	}
	
	private void addDataToMap(Map<String, Object> map) {
		String title_key = "title";
		String title_value = "A Modest Proposal";
		String author_key = "author";
		String author_value = "Jonathan Swift";
		String notes_key = "notes";
		String notes_value = "line1\nline2\r\nline3\nthere is no line 4\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b";
		String year_key = "year";
		Double year_value = new Double("1729");
		map.put(title_key, title_value);
		map.put(author_key, author_value);
		map.put(notes_key, notes_value);
		map.put(year_key, year_value);
	}
	
	private void addDataToList(List<Object> list) {
		list.add(Boolean.TRUE);
		list.add(new Double("-6.717"));
		list.add(new Double("0"));
		list.add(new Double("2e-12"));
		list.add(Boolean.FALSE);
		list.add(null);
	}
	
	private void checkStringifiedMap(String string) {
		assertTrue(string.contains("\"author\":\"Jonathan Swift\""));
		assertTrue(string.contains("\"title\":\"A Modest Proposal\""));
		assertTrue(string.contains("\"year\":1729"));
		assertTrue(string.contains("\"notes\":\"line1\\u000aline2\\u000d\\u000aline3\\u000athere is no line 4\\u0008\\u0008\\u0008\\u0008\\u0008\\u0008\\u0008\\u0008\\u0008\\u0008\\u0008\\u0008\\u0008\\u0008\\u0008\\u0008\\u0008\\u0008"));
	}
}
