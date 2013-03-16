/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Basic file read functionality.
 * @author Alex Stangl
 */
public class FileReader {

	public static byte[] getFileBytes(File file) throws IOException {

		ByteArrayOutputStream baos = null;
		InputStream is = null;
		try {
			baos = new ByteArrayOutputStream((int) file.length());
			is = new FileInputStream(file);
			byte[] buffer = new byte[8192];
			int read = 0;
			while ((read = is.read(buffer)) != -1)
				baos.write(buffer, 0, read);
		} finally {
			try {
				if (baos != null)
					baos.close();
			} catch (IOException ignore) {
			}
			try {
				if (is != null)
					is.close();
			} catch (IOException ignore) {
			}
		}
		return baos.toByteArray();
	}
}
