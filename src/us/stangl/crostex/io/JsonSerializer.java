/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.io;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import us.stangl.crostex.util.IdentityHashSet;


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
	
	public String parseBytesToString(byte[] bytes) throws JsonSerializationException {
		UtfEncoding encoding = detectUtfEncoding(bytes);
		if (encoding == UtfEncoding.UTF_32BE || encoding == UtfEncoding.UTF_32LE)
			throw new JsonSerializationException("Cannot handle UTF-32 encoding: " + encoding);
		if (bytes.length < 4)
			throw new JsonSerializationException("Expected JSON to be at least 4 bytes. Was "
					+ bytes.length + " bytes.");
		
		// use enum name with _ replaced with - as the Charset name
		try {
			return new String(bytes, encoding.name().replace('_', '-'));
		} catch (UnsupportedEncodingException e) {
			throw new JsonSerializationException("UnsupportedEncodingException unexpectedly caught", e);
		}
	}
	
	public Object parseJsonBytes(byte[] bytes) throws JsonSerializationException {
		return parseJsonString(parseBytesToString(bytes));
	}
	
	public Object parseJsonString(String string) throws JsonSerializationException {
		return new ParserImpl(string).parseObjectOrArray();
	}
	
	/**
	 * Serialize object to JSON string. Object is expected to be structured equivalent
	 * to that produced by this class' parser, e.g., consist of nested Map<String, ?>,
	 * Lists, and Boolean/String/Double/null "primitives".
	 * If input object graph contains back-references, this is considered an error and will
	 * cause JsonSerializationException to be thrown. 
	 * @param object object to serialize to JSON, should be either a Map (JSON object) or List (JSON array)
	 * @return JSON string equivalent of object
	 * @throws JsonSerializationException if unable to successfully encode object as JSON
	 */
	public String toJsonString(Object object) throws JsonSerializationException {
		if (! (object instanceof Map) && ! (object instanceof List))
			throw new JsonSerializationException("Illegal attempt to JSON encode top-level object that is not a Map or List");
		JsonEncoder encoder = new JsonEncoder();
		encoder.encodeAny(object);
		return encoder.toString();
	}

	// looking at first 4 bytes, determine type of UTF encoding, from RFC4627
	private UtfEncoding detectUtfEncoding(byte[] bytes) throws JsonSerializationException {
		if (bytes.length < 4)
			throw new JsonSerializationException("Expected JSON to be at least 4 bytes. Was "
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
		
		public Object parseObjectOrArray() throws JsonSerializationException {
			currIndex = indexOfNextNonWhitespaceChar("object or array");
			char c = getCharAtOffset(currIndex, "object or array");
			if (c == '[')
				return parseArray();
			else if (c == '{')
				return parseObject();
			else
				throw new JsonSerializationException("Parsed unexpected character '" + c + "' when expecting object or array at offset " + currIndex);
		}
		
		public List<Object> parseArray() throws JsonSerializationException {
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
		
		public Map<String, Object> parseObject() throws JsonSerializationException {
			assertStartCharacter('{', "object");
			Map<String, Object> object = new HashMap<String, Object>();
			while (true) {
				currIndex = indexOfNextNonWhitespaceChar("object");
				char c = getCharAtOffset(currIndex++, "object");
				if (! object.isEmpty() && c != '}') {
					if (c != ',')
						throw new JsonSerializationException("Unexpectedly encountered '" + c
								+ "' when expecting name string when parsing object at offset " + (currIndex - 1));
					currIndex = indexOfNextNonWhitespaceChar("object");
					c = getCharAtOffset(currIndex++, "object");
				}
				if (c == '}') {
					return object;
				}
				--currIndex;
				if (c != '"')
					throw new JsonSerializationException("Unexpectedly encountered '" + c
							+ "' when expecting name string when parsing object at offset " + currIndex);
				String name = parseString();
				currIndex = indexOfNextNonWhitespaceChar("object");
				c = getCharAtOffset(currIndex, "object");
				if (c != ':')
					throw new JsonSerializationException("Encountered unexpected character " + c 
							+ " when expecting ':' while parsing object at offset " + currIndex);
				++currIndex;
				
				// RFC4627 says names in object SHOULD be unique, meaning it's possible they will not be.
				// Standard handling for this appears to be last one wins, so we quietly do that here.

				object.put(name, parseValue());
			}
		}
		
		public String parseString() throws JsonSerializationException {
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
								throw new JsonSerializationException("Unexpected character '" + c
										+ "' encountered during hex escape sequence while parsing string at offset " + startString);
						}
						int codePoint = Integer.parseInt(string.substring(currIndex-4, currIndex), 16);
						builder.append(Character.toChars(codePoint));
					} else {
						throw new JsonSerializationException("Unexpectedly encountered '" + c 
								+ "' after escape when parsing string at offset " + startString);
					}
				}
				else {
					builder.append(c);
				}
			}
		}
		
		public Double parseNumber() throws JsonSerializationException {
			int i = indexOfNextNonWhitespaceChar("number");
			int startNumber = i;

			char c = getCharAtOffset(i, "number");
			if (c == '-')
				c = getCharAtOffset(++i, "number");

			if (c == '0') {
				c = getCharAtOffset(++i, "number");
				if (c >= '0' && c <= '9')
					throw new JsonSerializationException("Number with leading zero at offset " + startNumber);
			} else if (c >= '1' && c <= '9') {
				while (c >= '0' && c <= '9')
					c = getCharAtOffset(++i, "number");
			} else {
				throw new JsonSerializationException("Unexpected character " + c + " encountered trying to parse number at offset " + startNumber);
			}
			
			if (c == '.') {
				c = getCharAtOffset(++i, "number");
				if (! (c >= '0' && c <= '9'))
					throw new JsonSerializationException("Number with unexpected character '" + c + "' following decimal point at offset " + startNumber);
				while (c >= '0' && c <= '9')
					c = getCharAtOffset(++i, "number");
			}
			
			if (c == 'e' || c == 'E') {
				c = getCharAtOffset(++i, "number");
				if (c == '-' || c == '+')
					c = getCharAtOffset(++i, "number");
				if (! (c >= '0' && c <= '9'))
					throw new JsonSerializationException("Number with unexpected character '" + c + "' following exponent indicator at offset " + startNumber);
				while (c >= '0' && c <= '9')
					c = getCharAtOffset(++i, "number");
			}
			
			currIndex = i;
			try {
				return Double.parseDouble(string.substring(startNumber, i));
			} catch (NumberFormatException e) {
				throw new JsonSerializationException("Unexpectedly caught NumberFormatException trying to parse number at offset " + startNumber, e);
			}
		}
		
		/**
		 * @return next value, parsed starting at currIndex, leaving currIndex pointing after value
		 * @throws RuntimeException if value could not be successfully parsed
		 */
		public Object parseValue() throws JsonSerializationException {
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
			throw new JsonSerializationException("Unexpected character '" + c + "' encountered when trying to parse value at offset " + currIndex);
		}
		
		// assert expected start character for expected type, throwing appropriate exception otherwise
		// If successful, leaves currIndex set one past character
		private void assertStartCharacter(char c, String whatsBeingParsed) throws JsonSerializationException {
			currIndex = indexOfNextNonWhitespaceChar(whatsBeingParsed);
			char peek = getCharAtOffset(currIndex, whatsBeingParsed);
			if (peek != c)
				throw new JsonSerializationException("Encountered unexpected character '"
						+ peek + "' when was expecting '" + c + "' when trying to parse "
						+ whatsBeingParsed + " at offset " + currIndex);
			++currIndex;
		}
		
		// get character at specified offset, throwing parse exception if EOF reached
		private char getCharAtOffset(int offset, String whatsBeingParsed) throws JsonSerializationException {
			if (offset < 0)
				throw new JsonSerializationException("Tried to access bogus offset " + offset
						+ ", trying to parse " + whatsBeingParsed + ". Actual range 0.." + Integer.toString(cs.length - 1));
			if (offset >= cs.length)
				throw new JsonSerializationException("Unexpectedly reached EOF when trying to parse "
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
		 * @throws JsonSerializationException if no more non-whitespace characters remain
		 */
		public int indexOfNextNonWhitespaceChar(String whatsBeingParsed) throws JsonSerializationException {
			for (int i = currIndex; i < cs.length; ++i) {
				char c = cs[i];
				if (WHITESPACE.indexOf(c) == -1)
					return i;
			}
			throw new JsonSerializationException("Unexpectedly reached EOF when trying to parse "
					+ whatsBeingParsed + " at offset " + currIndex);
		}
	}
	
	private static class JsonEncoder {
		private final StringBuilder builder = new StringBuilder();
		
		// IdentityHashSet of objects which we have already encoded or started encoding, to detect backrefs
		private final Set<Object> visitedSet = new IdentityHashSet<Object>();
		
		@SuppressWarnings("unchecked")
		public void encodeAny(Object object) throws JsonSerializationException {
			if (object == null)
				builder.append("null");
			else if (object instanceof String)
				encodeString((String)object);
			else if (object instanceof Double)
				encodeDouble((Double)object);
			else if (object instanceof Boolean)
				builder.append(object.toString());
			else {
				if (visitedSet.contains(object))
					throw new JsonSerializationException("Backref detected in JSON encoding");
				visitedSet.add(object);
						
				if (object instanceof Map)
					encodeMap((Map<String, ?>)object);
				else if (object instanceof List)
					encodeList((List<?>)object);
				else
					throw new JsonSerializationException("Unhandled object " + object);
			}
		}
		
		private void encodeMap(Map<String, ?> map) throws JsonSerializationException {
			builder.append('{');
			boolean needComma = false;
			for (Map.Entry<String, ?> entry : map.entrySet()) {
				if (needComma)
					builder.append(',');
				encodeString(entry.getKey());
				builder.append(':');
				encodeAny(entry.getValue());
				needComma = true;
			}
			builder.append('}');
		}
		
		private void encodeList(List<?> list) throws JsonSerializationException {
			builder.append('[');
			boolean needComma = false;
			for (Object element : list) {
				if (needComma)
					builder.append(',');
				encodeAny(element);
				needComma = true;
			}
			builder.append(']');
		}
		
		private void encodeString(String string) {
			builder.append('"');
			for (char c : string.toCharArray()) {
				if (c >= '\u0000' && c <= '\u001f') {
					String hexString = "000" + Integer.toHexString(c);
					builder.append("\\u").append(hexString.substring(hexString.length() - 4));
				} else if (c == '"' || c == '\\') {
					builder.append('\\').append(c);
				} else {
					builder.append(c);
				}
			}
			builder.append('"');
		}
		
		private void encodeDouble(Double number) {
			String string = number.toString();
			String unneededSuffix = ".0";
			if (string.endsWith(unneededSuffix))
				string = string.substring(0, string.length() - unneededSuffix.length());
			builder.append(string);
		}
		
		public String toString() {
			return builder.toString();
		}
	}
}
