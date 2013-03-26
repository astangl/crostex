/**
 * Copyright 2008-2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
import org.w3c.dom.DocumentType;
import org.xml.sax.SAXException;

import us.stangl.crostex.ServiceException;

/**
 * Utility class to serialize Documents to/from files and streams.
 * @author Alex Stangl
 */
public class DOMSerializer {
	// logger
	private static final Logger LOG = Logger.getLogger(DOMSerializer.class.getName());

	/**
	 * Serialize DOM document into the specified output file. Uses FileSaver to safely perform operation.
	 * @param doc DOM document to write to file
	 * @param outputFile output file
	 * @throws ServiceException if unable to successfully complete operation
	 */
	public void serialize(Document doc, File outputFile) throws ServiceException {
		try {
			FileSaver fileSaver = new FileSaver(outputFile);
			Writer out = new BufferedWriter(fileSaver.getFileWriter());
			
			try {
				serializer().transform(new DOMSource(doc), new StreamResult(out));
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
		return getDOMImplementation().createDocument("http://stangl.us/crostex", rootElementName, null);
	}
	
	public static Document newDocument(String namespaceURI, String qualifiedName, DocumentType documentType) throws ServiceException {
		return getDOMImplementation().createDocument(namespaceURI, qualifiedName, documentType);
	}
	
	public static DOMImplementation getDOMImplementation() throws ServiceException {
		return builder().getDOMImplementation();
	}

	public static Document readDocument(String directory, String filename) throws ServiceException {
		return readDocument(new File(directory, filename));
	}
	
	public static Document readDocument(File file) throws ServiceException {
		try {
			return builder().parse(file);
		} catch (SAXException e) {
			throw new ServiceException("Caught SAXException", e);
		} catch (IOException e) {
			throw new ServiceException("Caught IOException", e);
		}
	}
	
	public static Document readDocument(InputStream inputStream) throws ServiceException {
		try {
			return builder().parse(inputStream);
		} catch (SAXException e) {
			throw new ServiceException("Caught SAXException", e);
		} catch (IOException e) {
			throw new ServiceException("Caught IOException", e);
		}
	}
	
	private static DocumentBuilder builder() throws ServiceException {
		try {
			return DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new ServiceException("Caught ParserConfigurationException", e);
		}
	}
	
	private Transformer serializer() throws ServiceException {
		try {
			TransformerFactory tfactory = TransformerFactory.newInstance();
			tfactory.setAttribute("indent-number", "2");
			Transformer serializer = tfactory.newTransformer();
			
			setProperty(serializer, "method", "xml");
			setProperty(serializer, "indent", "yes");
			setProperty(serializer, "{http://xml.apache.org/xslt}indent-amount", "2");
			return serializer;
		} catch (TransformerConfigurationException e) {
			throw new ServiceException("TransformerConfigurationException caught", e);
		} catch (IllegalArgumentException e) {
			throw new ServiceException("IllegalArgumentException caught", e);
		}
	}
	
	// try to set transformer property of the specified name to specified value, reporting warning if failure
	private void setProperty(Transformer transformer, String propName, String propValue) {
		try {
			transformer.setOutputProperty(propName, propValue);
		} catch (IllegalArgumentException e) {
			LOG.log(Level.WARNING, "Transformer does not recognize property " + propName
					+ " that we tried to set to value " + propValue, e);
		}
	}
}
