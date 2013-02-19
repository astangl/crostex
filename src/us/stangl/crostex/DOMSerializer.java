/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Utility class to serialize a Document into a file.
 */
public class DOMSerializer {
	// logger
	private static final Logger LOG = Logger.getLogger(DOMSerializer.class.getName());

	// default transformer factory
	private final TransformerFactory tfactory = TransformerFactory.newInstance();

	/**
	 * Serialize DOM document into the specified output file. Uses FileSaver to safely perform operation.
	 * @param doc DOM document to write to file
	 * @param outputFile output file
	 * @throws ServiceException if unable to successfully complete operation
	 */
	public void serialize(Document doc, File outputFile) throws ServiceException {
		try {
			tfactory.setAttribute("indent-number", "2");
			Transformer serializer = tfactory.newTransformer();
			
			setProperty(serializer, "method", "xml");
			setProperty(serializer, "indent", "yes");
			setProperty(serializer, "{http://xml.apache.org/xslt}indent-amount", "2");
	
			FileSaver fileSaver = new FileSaver(outputFile);
			Writer out = new BufferedWriter(fileSaver.getFileWriter());
			
			try {
				serializer.transform(new DOMSource(doc), new StreamResult(out));
				out.close();
				out = null;
				fileSaver.commit();
			} catch (TransformerException e) {
				// Quietly try to close temp file, if still open, then rethrow original throwable
				if (out != null) {
					try {
						out.close();
					} catch (IOException ignore) {
						LOG.log(Level.WARNING, "Suppressing secondary IOException caught trying to cleanup tempfile", ignore);
					}
				}
				throw e;
			}
		} catch (TransformerConfigurationException e) {
			throw new ServiceException("TransformerConfigurationException caught", e);
		} catch (TransformerException e) {
			throw new ServiceException("TransformerException caught", e);
		} catch (IOException e) {
			throw new ServiceException("IOException caught", e);
		}
	}
	
	public static Document newDocument(String rootElementName) throws ServiceException {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			DOMImplementation domImpl = builder.getDOMImplementation();
	//		DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
	//		DOMImplementation domImpl = registry.getDOMImplementation("XML");
			return domImpl.createDocument("http://stangl.us/crossword", rootElementName, null);
		} catch (ParserConfigurationException e) {
			throw new ServiceException("Caught ParserConfigurationException", e);
		}
	}

	public static Document readDocument(String directory, String filename) throws ServiceException {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		try
		{
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			File file = new File(directory, filename);
			return builder.parse(file);
		} catch (ParserConfigurationException e) {
			throw new ServiceException("Caught ParserConfigurationException", e);
		} catch (SAXException e) {
			throw new ServiceException("Caught SAXException", e);
		} catch (IOException e) {
			throw new ServiceException("Caught IOException", e);
		}
	}
	
	/** try to set transformer property of the specified name to specified value, reporting warning if failure */
	private void setProperty(Transformer transformer, String propName, String propValue) {
		try {
			transformer.setOutputProperty(propName, propValue);
		} catch (IllegalArgumentException e) {
			LOG.log(Level.WARNING, "Transformer does not recognize property " + propName
					+ " that we tried to set to value " + propValue, e);
			System.err.println("Transformer does not recognize property " + propName
					+ " that we tried to set to value " + propValue + ", " + e);
		}
	}
}
