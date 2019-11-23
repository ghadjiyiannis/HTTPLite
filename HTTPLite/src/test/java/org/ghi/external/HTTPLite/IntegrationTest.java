package org.ghi.external.HTTPLite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Optional;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import rawhttp.core.RawHttp;
import rawhttp.core.RawHttpRequest;
import rawhttp.core.RawHttpResponse;

public class IntegrationTest {
	// instances we will be using for all the tests 
	private static IApplicationInjector injector;
	private static ServerListener server;
	private static RawHttp http;
	private static IFileUtil fileUtil;

	@BeforeClass
	public static void beforeClass() throws ApplicationException {
		// lets use the same config as the application
		Properties properties = HTTPLite.getConfigPropertiesFromClasspath();
		// extract Document Root
		String documentRoot = properties.getProperty(HTTPLite.KEY_DOCUMENT_ROOT);
		
		// create an injector
		injector = new ApplicationInjector(documentRoot);
		server = (ServerListener)injector.getServerListener(8080, 10);
		
		// start the server on a different thread (we need this one to go on)
		Thread t = new Thread(server);
		t.start();
		
		// create an IFileUtil
		fileUtil = injector.getFileUtil();
		
		// create a RawHttp parser
		http = new RawHttp();
	}
	
	@AfterClass
	public static void afterClass() {
		server.stop();
	}

	@Test(expected=ApplicationException.class)
	public void test_ThrowsWhen_InjectorNull() throws ApplicationException {
		new ServerListener(null, 8081, 10);
	}

	@Test(expected=ApplicationException.class)
	public void test_ThrowsWhen_PortInvalid() throws ApplicationException {
		new ServerListener(injector, -1, 10);
	}

	@Test(expected=ApplicationException.class)
	public void test_ThrowsWhen_PoolSizeInvalid() throws ApplicationException {
		new ServerListener(null, 8081, 0);
	}

	@Test
	public void test_GetOK_HTMLFile() throws IOException {
		RawHttpResponse<?> response = executeRequest("GET", "/index.html");
		// check the response code
		assertEquals(200, response.getStatusCode());
		// check content length
		Optional<String> contentLength = response.getHeaders().getFirst("Content-Length");
		assertTrue(contentLength.isPresent());
		assertEquals("19919", contentLength.get());
		// check content type
		Optional<String> contentType = response.getHeaders().getFirst("Content-Type");
		assertTrue(contentType.isPresent());
		assertEquals("text/html", contentType.get());
	}

	@Test
	public void test_GetOK_DeepHTMLFile() throws IOException {
		RawHttpResponse<?> response = executeRequest("GET", "/blog/its_a_start/index.html");
		// check the response code
		assertEquals(200, response.getStatusCode());
		// check content length
		Optional<String> contentLength = response.getHeaders().getFirst("Content-Length");
		assertTrue(contentLength.isPresent());
		assertEquals("18358", contentLength.get());
		// check content type
		Optional<String> contentType = response.getHeaders().getFirst("Content-Type");
		assertTrue(contentType.isPresent());
		assertEquals("text/html", contentType.get());
	}
	
	@Test
	public void test_GetOK_ImplicitHTMLFile() throws IOException {
		RawHttpResponse<?> response = executeRequest("GET", "/");
		// check the response code
		assertEquals(200, response.getStatusCode());
		// check content length
		Optional<String> contentLength = response.getHeaders().getFirst("Content-Length");
		assertTrue(contentLength.isPresent());
		assertEquals("19919", contentLength.get());
		// check content type
		Optional<String> contentType = response.getHeaders().getFirst("Content-Type");
		assertTrue(contentType.isPresent());
		assertEquals("text/html", contentType.get());
	}

	@Test
	public void test_GetOK_HeadHTMLFile() throws IOException {
		RawHttpResponse<?> response = executeRequest("HEAD", "/index.html");
		// check the response code
		assertEquals(200, response.getStatusCode());
		// check content length
		Optional<String> contentLength = response.getHeaders().getFirst("Content-Length");
		assertTrue(contentLength.isPresent());
		assertEquals("0", contentLength.get());
		// check content type
		Optional<String> contentType = response.getHeaders().getFirst("Content-Type");
		assertTrue(contentType.isPresent());
		assertEquals("text/html", contentType.get());
	}

	@Test
	public void test_GetOK_CSSFile() throws IOException {
		RawHttpResponse<?> response = executeRequest("GET", "/css/main.min.css");
		// check the response code
		assertEquals(200, response.getStatusCode());
		// check content length
		Optional<String> contentLength = response.getHeaders().getFirst("Content-Length");
		assertTrue(contentLength.isPresent());
		assertEquals("32618", contentLength.get());
		// check content type
		Optional<String> contentType = response.getHeaders().getFirst("Content-Type");
		assertTrue(contentType.isPresent());
		assertEquals("text/css", contentType.get());
	}

	// no type detector for JS installed on my system so this test fails
	@Ignore
	@Test
	public void test_GetOK_JSFile() throws IOException {
		RawHttpResponse<?> response = executeRequest("GET", "/js/main.js");
		// check the response code
		assertEquals(200, response.getStatusCode());
		// check content length
		Optional<String> contentLength = response.getHeaders().getFirst("Content-Length");
		assertTrue(contentLength.isPresent());
		assertEquals("1328", contentLength.get());
		// check content type
		Optional<String> contentType = response.getHeaders().getFirst("Content-Type");
		assertTrue(contentType.isPresent());
		assertEquals("text/javascript", contentType.get());
	}

	@Test
	public void test_GetOK_JpegFile() throws IOException {
		RawHttpResponse<?> response = executeRequest("GET", "/img/main/logo.jpg");
		// check the response code
		assertEquals(200, response.getStatusCode());
		// check content length
		Optional<String> contentLength = response.getHeaders().getFirst("Content-Length");
		assertTrue(contentLength.isPresent());
		assertEquals("60747", contentLength.get());
		// check content type
		Optional<String> contentType = response.getHeaders().getFirst("Content-Type");
		assertTrue(contentType.isPresent());
		assertEquals("image/jpeg", contentType.get());
	}

	@Test
	public void test_LastModified_GetOK() throws IOException {
		// set the modified date so that it is predictable
		Date now = new Date();
		File file = fileUtil.getFileFromPath("/index.html");
		file.setLastModified(now.getTime());
		
		RawHttpResponse<?> response = executeRequest("GET", "/index.html");
		// check the response code
		assertEquals(200, response.getStatusCode());
		// check content type
		Optional<String> lastModified = response.getHeaders().getFirst("Last-Modified");
		assertTrue(lastModified.isPresent());
		assertEquals(now.toString(), lastModified.get());
	}

	@Test
	public void test_LastModified_HeadOK() throws IOException {
		// set the modified date so that it is predictable
		Date now = new Date();
		File file = fileUtil.getFileFromPath("/index.html");
		file.setLastModified(now.getTime());
		
		RawHttpResponse<?> response = executeRequest("HEAD", "/index.html");
		// check the response code
		assertEquals(200, response.getStatusCode());
		// check content type
		Optional<String> lastModified = response.getHeaders().getFirst("Last-Modified");
		assertTrue(lastModified.isPresent());
		assertEquals(now.toString(), lastModified.get());
	}

	@Test
	public void test_GetForbidden_NonReadableFile() throws IOException {
		RawHttpResponse<?> response = executeRequest("GET", "/not_readable.html");
		// check the response code
		assertEquals(403, response.getStatusCode());
		// check content length
		Optional<String> contentLength = response.getHeaders().getFirst("Content-Length");
		assertTrue(contentLength.isPresent());
		assertEquals("0", contentLength.get());
		// check content type
		Optional<String> contentType = response.getHeaders().getFirst("Content-Type");
		assertTrue(contentType.isPresent());
		assertEquals("text/plain", contentType.get());
	}

	@Test
	public void test_GetNotFound_NoSuchFile() throws IOException {
		RawHttpResponse<?> response = executeRequest("GET", "/no_such_file");
		// check the response code
		assertEquals(404, response.getStatusCode());
		// check content length
		Optional<String> contentLength = response.getHeaders().getFirst("Content-Length");
		assertTrue(contentLength.isPresent());
		assertEquals("0", contentLength.get());
		// check content type
		Optional<String> contentType = response.getHeaders().getFirst("Content-Type");
		assertTrue(contentType.isPresent());
		assertEquals("text/plain", contentType.get());
	}

	@Test
	public void test_GetMethodNotAlllowed_OtherMethods() throws IOException {
		RawHttpResponse<?> response = executeRequest("POST", "/index.html");
		// check the response code
		assertEquals(405, response.getStatusCode());
		// check content length
		Optional<String> contentLength = response.getHeaders().getFirst("Content-Length");
		assertTrue(contentLength.isPresent());
		assertEquals("0", contentLength.get());
		// check content type
		Optional<String> contentType = response.getHeaders().getFirst("Content-Type");
		assertTrue(contentType.isPresent());
		assertEquals("text/plain", contentType.get());
		
		// check the others as well
		response = executeRequest("PUT", "/index.html");
		// check the response code
		assertEquals(405, response.getStatusCode());
		response = executeRequest("PATCH", "/index.html");
		// check the response code
		assertEquals(405, response.getStatusCode());
		response = executeRequest("DELETE", "/index.html");
		// check the response code
		assertEquals(405, response.getStatusCode());
	}
	
	@Test
	public void test_KeepAlive() throws UnknownHostException, IOException {
		// create our own connection, which we will use for two requests
		Socket socket = new Socket("localhost", 8080);
		
		// first request
		RawHttpResponse<?> response = executeRequest("GET", "/index.html", "Connection: keep-alive", socket);
		// check the response code
		assertEquals(200, response.getStatusCode());
		// check for keep-alive responsse
		Optional<String> connectionHeader = response.getHeaders().getFirst("Connection");
		assertTrue(connectionHeader.isPresent());
		assertEquals("keep-alive", connectionHeader.get());

		// second request
		response = executeRequest("GET", "/img/main/logo.jpg", "", socket);
		// check the response code
		assertEquals(200, response.getStatusCode());
		// check for keep-alive responsse
		connectionHeader = response.getHeaders().getFirst("Connection");
		assertFalse(connectionHeader.isPresent());
		
		socket.close();
	}
	
	@Test
	public void test_InternalServerError_WithMock() throws ApplicationException, IOException {
		// first let's get a new Server on a new port
		injector = new MockInjector();
		ServerListener s = (ServerListener)injector.getServerListener(8085, 10);
		
		// start the server on a different thread (we need this one to go on)
		Thread t = new Thread(s);
		t.start();
		
		// create a connection to the new server
		Socket socket = new Socket("localhost", 8085);
		
		// now let's send a normal request
		RawHttpResponse<?> response = executeRequest("GET", "/index.html", "", socket);
		// check the response code
		assertEquals(500, response.getStatusCode());
		// check content length
		Optional<String> contentLength = response.getHeaders().getFirst("Content-Length");
		assertTrue(contentLength.isPresent());
		assertEquals("0", contentLength.get());
		// check content type
		Optional<String> contentType = response.getHeaders().getFirst("Content-Type");
		assertTrue(contentType.isPresent());
		assertEquals("text/plain", contentType.get());
		
		socket.close();
		s.stop();
	}
	
	/*
	 * Private helper methods
	 */
	private RawHttpResponse<?> executeRequest(String method, String path) throws IOException {
		Socket socket = new Socket("localhost", 8080);
		return executeRequest(method, path, "", socket);
	}
	
	private RawHttpResponse<?> executeRequest(String method, String path, String additionalHeaders, Socket socket) throws IOException {
		RawHttpRequest request = http.parseRequest(
				method + " " + path + " HTTP/1.1\r\n" +
			    "Host: localhost\r\n" +
			    "User-Agent: RawHTTP\r\n" +
			    additionalHeaders);
		request.writeTo(socket.getOutputStream());
		return http.parseResponse(socket.getInputStream()).eagerly();
	}
	
	/*
	 * Mocking classes to help test errors
	 */
	public class MockInjector implements IApplicationInjector {

		public Runnable getServerListener(int port, int poolSize) throws ApplicationException {
			return new ServerListener(this, port, poolSize);
		}

		public Runnable getWorker(Socket socket) throws ApplicationException {
			return new ConnectedWorker(this, socket);
		}

		public IFileUtil getFileUtil() throws ApplicationException {
			return new MockFileUtil();
		}
		
	}
	
	public class MockFileUtil implements IFileUtil {

		public File getFileFromPath(String path) {
			causeException();
			return null;
		}

		public boolean exists(File file) {
			causeException();
			return false;
		}

		public boolean isReadable(File file) {
			causeException();
			return false;
		}

		public long getContentLength(File file) {
			causeException();
			return 0;
		}

		public String getContentType(File file) throws ApplicationException {
			causeException();
			return null;
		}

		public Date getLastModifiedDate(File file) {
			causeException();
			return null;
		}
		
		private void causeException() {
			String s = null;
			s.length();
		}
		
	}
}
