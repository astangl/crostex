/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.swing.RepaintManager;

/**
 * Helper to print any arbitrary Component within Swing. Currently assumes Component prints entirely on one page.
 * Usage: new PrintHelper(component).print();
 * @author Alex Stangl
 */
public class PrintHelper implements Printable {
	// Component which will be printed
	private Component component;
	
	/**
	 * Construct new PrintHelper for Component which is to be printed.
	 * @param component component to print
	 */
	public PrintHelper(Component component) {
		this.component = component;
	}
	
	/**
	 * Print this wrapped component.
	 * @throws PrinterException if error occurs during print attempt
	 */
	public void print() throws PrinterException {
		PrinterJob printerJob = PrinterJob.getPrinterJob();
		printerJob.setPrintable(this);
		if (printerJob.printDialog())
			printerJob.print();
	}

	@Override
	public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
		// For this helper, assume only 1 page for now
		if (pageIndex > 0)
			return NO_SUCH_PAGE;

		((Graphics2D)g).translate(pageFormat.getImageableX(), pageFormat.getImageableY());
		// turn off double-buffering (worsens speed/quality of printig) during paint, then reenable
		RepaintManager currentManager = RepaintManager.currentManager(component);
		currentManager.setDoubleBufferingEnabled(false);
		component.paint(g);
		currentManager.setDoubleBufferingEnabled(true);
		return PAGE_EXISTS;
	}
}
