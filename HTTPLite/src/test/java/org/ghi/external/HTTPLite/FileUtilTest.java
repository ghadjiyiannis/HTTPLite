package org.ghi.external.HTTPLite;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Date;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/*
 * This implements a full unit test of FileUtil with 100% coverage
 */
public class FileUtilTest {
	// for speed, create one and use it for as many tests as possible
	private static FileUtil fileUtil;
	
	@BeforeClass
	public static void beforeClass() throws ApplicationException {
		// lets use the same config as the application
		Properties properties = HTTPLite.getConfigPropertiesFromClasspath();
		// extract Document Root
		String documentRoot = properties.getProperty(HTTPLite.KEY_DOCUMENT_ROOT);
		// create a FileUtil instance to test
		fileUtil = new FileUtil(documentRoot);
	}
	
	@Test(expected = ApplicationException.class)
	public void testThrowsWhenDocumentRootNull() throws ApplicationException {
		new FileUtil(null);
	}

	@Test
	public void testGetHTMLFileFromPath() {
		File file = fileUtil.getFileFromPath("/index.html");
		// check that we got the right file
		assertEquals(20611, fileUtil.getContentLength(file));
	}
	
	@Test
	public void testGetDeepHTMLFileFromPath() {
		File file = fileUtil.getFileFromPath("/blog/its_a_start/index.html");
		// check that we got the right file
		assertEquals(18358, fileUtil.getContentLength(file));
	}
	
	@Test
	public void testGetImplicitHTMLFileFromPath() {
		File file = fileUtil.getFileFromPath("/blog/its_a_start");
		// check that we got the right file
		assertEquals(18358, fileUtil.getContentLength(file));
	}

	@Test
	public void testExistsReturnsTrue() {
		File file = fileUtil.getFileFromPath("/index.html");
		assertTrue(fileUtil.exists(file));
	}

	@Test
	public void testExistsReturnsFalse() {
		File file = fileUtil.getFileFromPath("/no_such_file");
		assertFalse(fileUtil.exists(file));
	}

	@Test
	public void testReadableReturnsTrue() {
		File file = fileUtil.getFileFromPath("/index.html");
		assertTrue(fileUtil.isReadable(file));
	}

	@Test
	public void testReadableReturnsFalse() {
		File file = fileUtil.getFileFromPath("/not_readable.html");
		assertFalse(fileUtil.isReadable(file));
	}

	@Test
	public void testContentLength() {
		File file = fileUtil.getFileFromPath("/index.html");
		// check that we got the right length
		assertEquals(20611, fileUtil.getContentLength(file));
	}

	@Test
	public void testContentTypeHTML() throws ApplicationException {
		File file = fileUtil.getFileFromPath("/index.html");
		// check that we got the right length
		assertEquals("text/html", fileUtil.getContentType(file));
	}

	@Test
	public void testContentTypeCSS() throws ApplicationException {
		File file = fileUtil.getFileFromPath("/css/main.min.css");
		// check that we got the right length
		assertEquals("text/css", fileUtil.getContentType(file));
	}

	/*
	 * Unfortunately the detector for javascript is not installed on my system
	 */
	@Ignore
	@Test
	public void testContentTypeJS() throws ApplicationException {
		File file = fileUtil.getFileFromPath("/js/main.js");
		// check that we got the right length
		assertEquals("text/javascript", fileUtil.getContentType(file));
	}

	@Test
	public void testContentTypeJPEG() throws ApplicationException {
		File file = fileUtil.getFileFromPath("/img/main/logo.jpg");
		// check that we got the right length
		assertEquals("image/jpeg", fileUtil.getContentType(file));
	}

	@Test
	public void testLastModifiedDate() {
		File file = fileUtil.getFileFromPath("/index.html");
		// set the modifed date to something predictable so that we can check
		Date now = new Date();
		file.setLastModified(now.getTime());
		// now check
		assertEquals(now, fileUtil.getLastModifiedDate(file));
	}
	
}
