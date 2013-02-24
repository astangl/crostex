/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * File saver, for performing safe file save operations, trying to keep the original
 * file around in cases of failure or partial failure.
 * This is an independent implementation of the idea of Ian Darwin for his next Java Cookbook,
 * though the core idea itself is classic.
 * 
 * Recommended usage:
 * 
 * try {
 *     FileSaver fileSaver = new FileSaver(targetFile);
 *     OutputStream out = fileSaver.getFileOutputStream();	// or use getFileWriter instead
 *     // NOTE: if exception between here and commit, temp file will be left open. You need
 *     //       to decide if it is worth the complexity to try to close tempfile w/o losing the original Throwable
 *     out.write(bytes);									// write data to file
 *     out.close();
 *     // Only performing the fileSaver commit operation if everything good up to this point
 *     fileSaver.commit();
 * } catch (IOException e) {
 *     LOG.severe("Caught IOException: " + e);
 * }
 *     
 * @see http://javacook.darwinsys.com/new_recipes/10saveuserdata.jsp
 */
public class FileSaver {
	/** target file, designates the actual file (which may already exist) that we want to ultimately write to */
	private final File targetFile;
	
	/** temp file, where new file writing tentatively occurs */
	private final File tempFile;
	
	/** flag tracking whether stream/writer has been retrieved */
	private boolean isReturnedStreamOrWriter;
	
	/**
	 * Public constructor, accepting a File argument designating file ultimately to be written.
	 * @param targetFile specifies file we actually want to ultimately write to
	 * @throws IOException if operation cannot complete successfully due to IO error
	 */
	public FileSaver(File targetFile) throws IOException {
		this.targetFile = targetFile;
		String tempFileName = this.targetFile.getAbsolutePath() + ".tmp";
		tempFile = new File(tempFileName);
		if (! tempFile.createNewFile())
			throw new IOException("Error creating temp file " + tempFileName + " because a file by that name already exists.");
		tempFile.deleteOnExit();
	}
	
	/**
	 * Return output stream, which client should probably buffer, for client to write its data to.
	 * @return output stream
	 * @throws FileNotFoundException if unable to successfully complete operation due to inability to open temp file
	 */
	public FileOutputStream getFileOutputStream() throws FileNotFoundException {
		if (isReturnedStreamOrWriter) {
			throw new IllegalStateException("Called getFileOutputStream or getFileWriter more than once");
		}
		isReturnedStreamOrWriter = true;
		return new FileOutputStream(tempFile);
	}
	
	/**
	 * Return output writer, which client should probably buffer, for client to write its data to.
	 * @return output writer
	 * @throws IOException if unable to successfully complete operation due to inability to open temp file
	 */
	public OutputStreamWriter getFileWriter() throws IOException {
		if (isReturnedStreamOrWriter) {
			throw new IllegalStateException("Called getFileOutputStream or getFileWriter more than once");
		}
		isReturnedStreamOrWriter = true;
		return new OutputStreamWriter(new FileOutputStream(tempFile), "UTF-8");
	}
	
	/**
	 * Commit new file data, after all writing has been completed.
	 * Attempts to rename existing FILE to FILE.bak and rename the FILE.tmp that client has been writing to to FILE
	 * @throws IOException if unable to successfully complete operation due to IO problems (i.e., inability to rename files)
	 */
	public void commit() throws IOException {
		if (! isReturnedStreamOrWriter) {
			throw new IllegalStateException("Called commit without ever calling getFileOutputStream or getFileWriter");
		}

		// Try to delete existing backup file, if it exists, then rename target file, if it already exists, to backup file name
		File backupFile = new File(targetFile.getAbsolutePath() + ".bak");
		backupFile.delete();
		if (targetFile.exists() && ! targetFile.renameTo(backupFile)) {
			throw new IOException("Could not rename " + targetFile + " to " + backupFile);
		}
		if (! tempFile.renameTo(targetFile)) {
			throw new IOException("Could not rename " + tempFile + " to " + targetFile);
		}
		// Not resetting state here since it seems cleaner to create a new FileSaver each time
	}
}
