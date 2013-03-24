/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.io;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * JSON serializer, RFC4627 compliant except that it doesn't handle UTF-32.
 * @author Alex Stangl
 */
public class JsonSerializer {
	// enumeration of possible UTF encodings
	private enum UtfEncoding { UTF_32BE, UTF_32LE, UTF_16BE, UTF_16LE, UTF_8 };

	// literal encodings for true/false/null
	private static final char[] TRUE = "true".toCharArray();
	private static final char[] FALSE = "false".toCharArray();
	private static final char[] NULL = "null".toCharArray();
	
	// whitespace characters
	private static final String WHITESPACE = " \t\r\n";
	
	public String parseBytesToString(byte[] bytes) throws JsonParsingException {
		UtfEncoding encoding = detectUtfEncoding(bytes);
		if (encoding == UtfEncoding.UTF_32BE || encoding == UtfEncoding.UTF_32LE)
			throw new JsonParsingException("Cannot handle UTF-32 encoding: " + encoding);
		if (bytes.length < 4)
			throw new JsonParsingException("Expected JSON to be at least 4 bytes. Was "
					+ bytes.length + " bytes.");
		
		// use enum name with _ replaced with - as the Charset name
		try {
			return new String(bytes, encoding.name().replace('_', '-'));
		} catch (UnsupportedEncodingException e) {
			throw new JsonParsingException("UnsupportedEncodingException unexpectedly caught", e);
		}
	}
	
	public Object parseJsonBytes(byte[] bytes) throws JsonParsingException {
		return parseJsonString(parseBytesToString(bytes));
	}
	
	public Object parseJsonString(String string) throws JsonParsingException {
		return new ParserImpl(string).parseObjectOrArray();
	}

	// looking at first 4 bytes, determine type of UTF encoding, from RFC4627
	private UtfEncoding detectUtfEncoding(byte[] bytes) throws JsonParsingException {
		if (bytes.length < 4)
			throw new JsonParsingException("Expected JSON to be at least 4 bytes. Was "
					+ bytes.length + " bytes.");
		if (bytes[0] != 0 && bytes[1] != 0 && bytes[2] != 0)
			return UtfEncoding.UTF_8;
		if (bytes[0] == 0)
			return bytes[1] == 0 ? UtfEncoding.UTF_32BE : UtfEncoding.UTF_16BE;
		return bytes[2] == 0 ? UtfEncoding.UTF_32LE : UtfEncoding.UTF_16LE;
	}
	
	private static class ParserImpl {
		// JSON data in both array and string form
		private final char[] cs;
		private final String string;

		private int currIndex = 0;
		
		public ParserImpl(String string) {
			cs = string.toCharArray();
			this.string = string;
		}
		
		public Object parseObjectOrArray() throws JsonParsingException {
			currIndex = indexOfNextNonWhitespaceChar("object or array");
			char c = getCharAtOffset(currIndex, "object or array");
			if (c == '[')
				return parseArray();
			else if (c == '{')
				return parseObject();
			else
				throw new JsonParsingException("Parsed unexpected character '" + c + "' when expecting object or array at offset " + currIndex);
		}
		
		public List<Object> parseArray() throws JsonParsingException {
			assertStartCharacter('[', "array");
			List<Object> array = new ArrayList<Object>();
			while (true) {
				currIndex = indexOfNextNonWhitespaceChar("array");
				char c = getCharAtOffset(currIndex++, "array");
				if (! array.isEmpty() && c == ',') {
					currIndex = indexOfNextNonWhitespaceChar("array");
					c = getCharAtOffset(currIndex++, "array");
				}
				if (c == ']') {
					return array;
				}
				--currIndex;
				array.add(parseValue());
			}
		}
		
		public Map<String, Object> parseObject() throws JsonParsingException {
			assertStartCharacter('{', "object");
			Map<String, Object> object = new HashMap<String, Object>();
			while (true) {
				currIndex = indexOfNextNonWhitespaceChar("object");
				char c = getCharAtOffset(currIndex++, "object");
				if (! object.isEmpty() && c != '}') {
					if (c != ',')
						throw new JsonParsingException("Unexpectedly encountered '" + c
								+ "' when expecting name string when parsing object at offset " + (currIndex - 1));
					currIndex = indexOfNextNonWhitespaceChar("object");
					c = getCharAtOffset(currIndex++, "object");
				}
				if (c == '}') {
					return object;
				}
				--currIndex;
				if (c != '"')
					throw new JsonParsingException("Unexpectedly encountered '" + c
							+ "' when expecting name string when parsing object at offset " + currIndex);
				String name = parseString();
				currIndex = indexOfNextNonWhitespaceChar("object");
				c = getCharAtOffset(currIndex, "object");
				if (c != ':')
					throw new JsonParsingException("Encountered unexpected character " + c 
							+ " when expecting ':' while parsing object at offset " + currIndex);
				++currIndex;
				
				// RFC4627 says names in object SHOULD be unique, meaning it's possible they will not be.
				// Standard handling for this appears to be last one wins, so we quietly do that here.

				object.put(name, parseValue());
			}
		}
		
		public String parseString() throws JsonParsingException {
			assertStartCharacter('"', "string");

			int startString = currIndex;
			StringBuilder builder = new StringBuilder();
			while (true) {
				char c = getCharAtOffset(currIndex++, "string");
				if (c == '"')
					return builder.toString();
				if (c == '\\') {
					c = getCharAtOffset(currIndex++, "string");
					if ("\"\\/".indexOf(c) != -1)
						builder.append(c);
					else if ("bfnrt".indexOf(c) != -1) {
						// two character escape
						builder.append("\b\f\n\r\t".charAt("bfnrt".indexOf(c)));
					} else if (c == 'u') {
						for (int i = 0; i < 4; ++i) {
							c = getCharAtOffset(currIndex++, "string");
							if ("0123456789abcdefABCDEF".indexOf(c) == -1)
								throw new JsonParsingException("Unexpected character '" + c
										+ "' encountered during hex escape sequence while parsing string at offset " + startString);
						}
						int codePoint = Integer.parseInt(string.substring(currIndex-4, currIndex), 16);
						builder.append(Character.toChars(codePoint));
					} else {
						throw new JsonParsingException("Unexpectedly encountered '" + c 
								+ "' after escape when parsing string at offset " + startString);
					}
				}
				else {
					builder.append(c);
				}
			}
		}
		
		public Double parseNumber() throws JsonParsingException {
			int i = indexOfNextNonWhitespaceChar("number");
			int startNumber = i;

			char c = getCharAtOffset(i, "number");
			if (c == '-')
				c = getCharAtOffset(++i, "number");

			if (c == '0') {
				c = getCharAtOffset(++i, "number");
				if (c >= '0' && c <= '9')
					throw new JsonParsingException("Number with leading zero at offset " + startNumber);
			} else if (c >= '1' && c <= '9') {
				while (c >= '0' && c <= '9')
					c = getCharAtOffset(++i, "number");
			} else {
				throw new JsonParsingException("Unexpected character " + c + " encountered trying to parse number at offset " + startNumber);
			}
			
			if (c == '.') {
				c = getCharAtOffset(++i, "number");
				if (! (c >= '0' && c <= '9'))
					throw new JsonParsingException("Number with unexpected character '" + c + "' following decimal point at offset " + startNumber);
				while (c >= '0' && c <= '9')
					c = getCharAtOffset(++i, "number");
			}
			
			if (c == 'e' || c == 'E') {
				c = getCharAtOffset(++i, "number");
				if (c == '-' || c == '+')
					c = getCharAtOffset(++i, "number");
				if (! (c >= '0' && c <= '9'))
					throw new JsonParsingException("Number with unexpected character '" + c + "' following exponent indicator at offset " + startNumber);
				while (c >= '0' && c <= '9')
					c = getCharAtOffset(++i, "number");
			}
			
			currIndex = i;
			try {
				return Double.parseDouble(string.substring(startNumber, i));
			} catch (NumberFormatException e) {
				throw new JsonParsingException("Unexpectedly caught NumberFormatException trying to parse number at offset " + startNumber, e);
			}
		}
		
		/**
		 * @return next value, parsed starting at currIndex, leaving currIndex pointing after value
		 * @throws RuntimeException if value could not be successfully parsed
		 */
		public Object parseValue() throws JsonParsingException {
			currIndex = indexOfNextNonWhitespaceChar("value");
			char c = getCharAtOffset(currIndex, "value");
			if (c == '[')
				return parseArray();
			if (c == '{')
				return parseObject();
			if (c == '"')
				return parseString();
			if (c == '-' || (c >= '0' && c <= '9'))
				return parseNumber();
			if (arrayCompare(currIndex, FALSE)) {
				currIndex += FALSE.length;
				return Boolean.FALSE;
			}
			if (arrayCompare(currIndex, TRUE)) {
				currIndex += TRUE.length;
				return Boolean.TRUE;
			}
			if (arrayCompare(currIndex, NULL)) {
				currIndex += NULL.length;
				// TODO maybe return null placeholder here instead
				return null;
			}
			throw new JsonParsingException("Unexpected character '" + c + "' encountered when trying to parse value at offset " + currIndex);
		}
		
		// assert expected start character for expected type, throwing appropriate exception otherwise
		// If successful, leaves currIndex set one past character
		private void assertStartCharacter(char c, String whatsBeingParsed) throws JsonParsingException {
			currIndex = indexOfNextNonWhitespaceChar(whatsBeingParsed);
			char peek = getCharAtOffset(currIndex, whatsBeingParsed);
			if (peek != c)
				throw new JsonParsingException("Encountered unexpected character '"
						+ peek + "' when was expecting '" + c + "' when trying to parse "
						+ whatsBeingParsed + " at offset " + currIndex);
			++currIndex;
		}
		
		// get character at specified offset, throwing parse exception if EOF reached
		private char getCharAtOffset(int offset, String whatsBeingParsed) throws JsonParsingException {
			if (offset < 0)
				throw new JsonParsingException("Tried to access bogus offset " + offset
						+ ", trying to parse " + whatsBeingParsed + ". Actual range 0.." + Integer.toString(cs.length - 1));
			if (offset >= cs.length)
				throw new JsonParsingException("Unexpectedly reached EOF when trying to parse "
						+ whatsBeingParsed + ". Tried to access offset " + offset 
						+ " whereas valid index range is 0.." + Integer.toString(cs.length - 1));
			return cs[offset];
		}
		
		// return whether specified char array compares with contents of cs starting at index
		private boolean arrayCompare(int index, char[] other) {
			int len = other.length;
			if (index + len > cs.length)
				return false;
			for (int i = 0; i < len; ++i)
				if (other[i] != cs[index + i])
					return false;
			return true;
		}
		
		/**
		 * @return index of next non-whitespace character
		 * @throws JsonParsingException if no more non-whitespace characters remain
		 */
		public int indexOfNextNonWhitespaceChar(String whatsBeingParsed) throws JsonParsingException {
			for (int i = currIndex; i < cs.length; ++i) {
				char c = cs[i];
				if (WHITESPACE.indexOf(c) == -1)
					return i;
			}
			throw new JsonParsingException("Unexpectedly reached EOF when trying to parse "
					+ whatsBeingParsed + " at offset " + currIndex);
		}
	}
}
