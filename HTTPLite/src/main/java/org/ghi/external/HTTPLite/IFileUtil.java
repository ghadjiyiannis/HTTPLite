package org.ghi.external.HTTPLite;

import java.io.File;
import java.util.Date;


/**
 * The interface to a small utility to abstract the operations against the file-system.
 * The methods should be self-explanatory.
 * 
 * Each thread gets its own copy of this to avoid synchronization.
 * 
 * @author George Hadjiyiannis
 *
 */
public interface IFileUtil {

	public File getFileFromPath(String path);
	
	public boolean exists(File file);
	
	public boolean isReadable(File file);
	
	public long getContentLength(File file);
	
	public String getContentType(File file) throws ApplicationException;
	
	public Date getLastModifiedDate(File file);
}
