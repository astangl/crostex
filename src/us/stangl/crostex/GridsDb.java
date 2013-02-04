/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Grids template database.
 */
public class GridsDb {
	/** logger */
	private static final Logger LOG = Logger.getLogger(GridsDb.class.getName());
	
	/** grids template filename */
	private static final String GRIDSDB_FILENAME = "gridsdb.xml";

	/** grids in collection */
	private Collection<Grid> grids_;
	
	/**
	 * Private constructor. Clients should use factory method below.
	 * @param grids
	 */
	private GridsDb(Collection<Grid> grids) {
		grids_ = grids;
	}
	
	public static GridsDb read(String dataDirectory) throws ServiceException {
		Collection<Grid> grids = new ArrayList<Grid>();
		Document doc = DOMSerializer.readDocument(dataDirectory, GRIDSDB_FILENAME);
//		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
//		try
//		{
//			DocumentBuilder builder = builderFactory.newDocumentBuilder();
//			File file = new File(dataDirectory, GRIDSDB_FILENAME);
//			Document document = builder.parse(file);
		Element documentElement = doc.getDocumentElement();
		if (! documentElement.getNodeName().equals("grids")) {
			throw new RuntimeException("Expected top-level gridsdb.xml element to be grids, not " + documentElement.getNodeName());
		}
		String version = documentElement.getAttribute("schemaVersion");
		LOG.finest("gridsdb schema version = " + version);
		NodeList nodeList = documentElement.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); ++i) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				String elementName = node.getNodeName();
				Element element = (Element)node;
				if (elementName.equals("version")) {
					if (version != null) {
						throw new RuntimeException("Error, multiple version elements found, first with " + version
								+ ", then with " + node.getTextContent());
					}
					version = node.getTextContent();
					if (! version.equals("0.1")) {
						throw new RuntimeException("Incompatible gridsdb version " + version + "; expecting 0.1");
					}
					System.out.println("Version = " + version);
				} else if (elementName.equals("grid")) {
					String gridName = element.getAttribute("name");
					System.out.println("Parsing grid " + gridName);
					String description = element.getAttribute("description");
					NodeList rowElements = element.getElementsByTagName("row");
					int firstRowLength = -1;
					
					// Do first pass to get width and make sure all rows are the same width
					for (int j = 0; j < rowElements.getLength(); ++j) {
						Element row = (Element)rowElements.item(j);
						String rowText = row.getTextContent();
						if (j == 0) {
							firstRowLength = rowText.length();
						} else if (rowText.length() != firstRowLength) {
							throw new RuntimeException("Error, row '" + rowText + " is " + rowText.length()
									+ " characters long whereas first row was " + firstRowLength + " characters long.");
						}
						System.out.println("Row text = '" + rowText + "'.");
					}

					Grid newGrid = new Grid(firstRowLength, rowElements.getLength(), gridName, description);

					// Now again, actually creating Grid
					for (int j = 0; j < rowElements.getLength(); ++j) {
						Element row = (Element)rowElements.item(j);
						String rowText = row.getTextContent();
						for (int k = 0; k < firstRowLength; ++k) {
							char nextChar = rowText.charAt(k);
							Cell nextCell = newGrid.getCell(j, k);
							if (nextChar == '.') {
								nextCell.setContents("");
							} else if (nextChar == 'X') {
								nextCell.setBlack();
							} else {
								throw new RuntimeException("Unexpected character " + nextChar
										+ " found in gridsdb row value " + rowText);
							}
						}
					}
					grids.add(newGrid);
//						retval.put(gridName, newGrid);
				}
			}
		}
		Node firstChild = documentElement.getFirstChild();
		System.out.println("First child = " + firstChild.toString());
//		} catch (SAXException e) {
//			System.err.println("Caught SAXException: " + e);
//		} catch (ParserConfigurationException e) {
//			System.err.println("Caught ParserConfigurationException: " + e);
//		} catch (FileNotFoundException e) {
//			System.err.println("Caught FileNotFoundException: " + e);
//		} catch (IOException e) {
//			System.err.println("Caught IOException: " + e);
//		}
		return new GridsDb(grids);
	}
	
	public void write(String dataDirectory) throws ServiceException {
		Document doc = DOMSerializer.newDocument("grids");
		Element rootElement = doc.getDocumentElement();
		rootElement.setAttribute("schemaVersion", "0.1");
		for (Grid grid : grids_) {
			Element gridElement = doc.createElement("grid");
			gridElement.setAttribute("name", grid.getName());
			gridElement.setAttribute("description", grid.getDescription());
			int height = grid.getHeight();
			int width = grid.getWidth();
			for (int row = 0; row < height; ++row) {
				Element rowElement = doc.createElement("row");
				StringBuilder stringBuilder = new StringBuilder(width);
				for (int col = 0; col < width; ++col) {
					stringBuilder.append(grid.getCell(row, col).isBlack() ? 'X' : '.');
				}
				rowElement.appendChild(doc.createTextNode(stringBuilder.toString()));
				gridElement.appendChild(rowElement);
			}
			rootElement.appendChild(gridElement);
		}
		
		new DOMSerializer().serialize(doc, new File(dataDirectory, GRIDSDB_FILENAME));
	}

	/**
	 * @return the grids
	 */
	public Collection<Grid> getGrids() {
		return grids_;
	}
}
