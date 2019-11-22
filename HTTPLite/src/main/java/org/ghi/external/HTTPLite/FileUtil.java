package org.ghi.external.HTTPLite;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;

/**
 * A small utility to abstract the operations against the file-system.
 * The methods should be self-explanatory.
 * 
 * Each thread gets its own copy of this to avoid synchronization.
 * 
 * @author George Hadjiyiannis
 *
 */
public class FileUtil implements IFileUtil {
	// we need to remember the document root
	private String documentRoot;

	public FileUtil(String documentRoot) throws ApplicationException {
		// sanity check input
		if (null == documentRoot)
			throw new ApplicationException("Document root cannot be null!");
		// remove trailing slashes and save
		this.documentRoot = documentRoot.replaceAll("/+$", "");
	}

	public File getFileFromPath(String path) {
		File file = new File(documentRoot + path);
		// if it is a directory, look for an "idex.html" in the directory
		if (file.isDirectory())
			file = new File(documentRoot + path.replace("/+$", "") + "/index.html");
		return file;
	}

	public boolean exists(File file) {
		return file.exists();
	}

	public boolean isReadable(File file) {
		// it seems that File.canRead() is broken
		return Files.isReadable(file.toPath());
	}

	public long getContentLength(File file) {
		return file.length();
	}

	/*
	 * This uses the detectors already installed on the system 
	 */
	public String getContentType(File file) throws IOException {
		return Files.probeContentType(file.toPath());
	}

	public Date getLastModifiedDate(File file) {
		return new Date(file.lastModified());
	}

}
