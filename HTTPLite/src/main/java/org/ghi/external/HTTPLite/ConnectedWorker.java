package org.ghi.external.HTTPLite;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

import org.apache.logging.log4j.Logger;

import rawhttp.core.RawHttp;
import rawhttp.core.RawHttpRequest;
import rawhttp.core.RawHttpResponse;
import rawhttp.core.body.FileBody;

/**
 * The ConnectedWorker is responsible for serving all the requests that come from
 * a connection. It currently serves only GET and HEAD requests.
 * It also respects the keep-alive flag.
 * 
 * The handling of the RawHttp requests and responses is based on the examples
 * at https://renatoathaydes.github.io/rawhttp/docs/low-level-api/
 * 
 * @author George Hadjiyiannis
 *
 */
public class ConnectedWorker implements Runnable {
	// private variables
	private IApplicationInjector injector;
	private Logger logger;
	private Socket clientSocket;
	private boolean keepalive;
	private IFileUtil fileUtil;

	public ConnectedWorker(IApplicationInjector injector, Logger logger, Socket clientSocket)
			throws ApplicationException {

		// sanity check the inputs
		if (null == injector)
			throw new ApplicationException("Injector cannot be null!");
		this.injector = injector;

		if (null == logger)
			throw new ApplicationException("Logger cannot be null!");
		this.logger = logger;

		if (null == clientSocket)
			throw new ApplicationException("Client socket is null!");
		this.clientSocket = clientSocket;

		// create an IFileUtil we can use locally
		fileUtil = this.injector.getFileUtil();
	}

	public void run() {
		logger.debug("Connected...");

		// lets create a parser for the protocol
		RawHttp http = new RawHttp();

		try {
			// loop as we may be servicing multiple requests if keep-alive is set
			while (true) {

				// serve a single request
				serveSingleRequest(http);

				// check and abide by the keep-alive flag
				if (!keepalive)
					break;
			}

		} catch (Exception e) {

			logger.error("Internal server error while processing request: ", e);

			// respond with 500 Internal Server Error
			respondInternalServerError(http, e.getMessage());

		} finally {
			logger.debug("Exiting connection...");

			// make sure we close the clientSocket no matter how we exit
			try {
				if (!clientSocket.isClosed())
					clientSocket.close();
			} catch (IOException e) {
				logger.error("Unknown error while closing client socket: ", e);
			}
		}
	}

	/*
	 * Private helper methods
	 */
	private void serveSingleRequest(RawHttp http) throws ApplicationException {

		// separate try catch blocks to be able to give informative messages

		// parse the request
		RawHttpRequest request;
		try {
			request = http.parseRequest(clientSocket.getInputStream());
		} catch (IOException e) {
			throw new ApplicationException("Error while parsing request: ", e);
		}

		// check the keep-alive flag
		keepalive = hasRequestedKeepalive(request);

		// check the requested method
		String method = request.getMethod();
		// we can only process GET and HEAD requests
		if (method.contentEquals("GET") || method.equals("HEAD")) {

			// get the path, URL decoded and with the query string removed
			String path = request.getUri().getPath();
			
			/*
			 * I was planning to remove /../ sequences to make sure that one cannot climb out
			 * of document root but it turns out RawHttp already takes care of that
			 */
			
			logger.debug("Serving from path " + path);

			// lets check the file out a bit
			File file = fileUtil.getFileFromPath(path);

			// if it does not exists respond with 404 Not Found
			if (!fileUtil.exists(file)) {
				respondNotFound(http);
				return;
			}

			// if it is not readable respond with 403 Forbidden
			if (!fileUtil.isReadable(file)) {
				respondForbidden(http);
				return;
			}

			// now send the response
			if (method.equals("GET"))
				respondOKWithContent(http, file);
			else
				respondOKWithoutContent(http, file);

		} else {
			// respond with 405 Method Not Allowed
			respondMethodNotAllowed(http);
		}
	}

	private boolean hasRequestedKeepalive(RawHttpRequest request) {
		Optional<String> connectionHeader = request.getHeaders().getFirst("Connection");
		// if the header is not there, no keep-alive
		if (!connectionHeader.isPresent())
			return false;
		// true if the header is keep-alive
		return connectionHeader.get().equals("keep-alive");
	}

	// respond with 200 OK (plus content)
	private void respondOKWithContent(RawHttp http, File file) throws ApplicationException {
		// get the content type from the file itself
		String contentType = fileUtil.getContentType(file);
		long contentLength = fileUtil.getContentLength(file);
		// get the last modified date
		Date lastModified = fileUtil.getLastModifiedDate(file);

		// get the date and time of the response
		String dateString = RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC));

		// if keep-alive is set we need to insert the header in the response
		String connectionHeader = keepalive ? "Connection: keep-alive\r\n" : "";

		// if we have a Last Modified date we need to insert it
		String lastModifiedHeader = "Last-Modified: " + lastModified + "\r\n";

		// construct the response
		RawHttpResponse<?> response = http.parseResponse(
				"HTTP/1.1 200 OK \r\n" + 
				"Content-Type: " + contentType + "\r\n" + 
				"Content-Length: " + contentLength + "\r\n" + 
				"Server: RawHTTP\r\n" + "Date: " + dateString + "\r\n" + 
				connectionHeader + 
				lastModifiedHeader + "\r\n").withBody(new FileBody(file, contentType));
		// send it
		try {
			response.writeTo(clientSocket.getOutputStream());
		} catch (IOException e) {
			throw new ApplicationException("Error sending response: ", e);
		}
	}

	// respond with 200 OK (without content)
	private void respondOKWithoutContent(RawHttp http, File file) throws ApplicationException {
		// get the content type from the file itself
		String contentType = fileUtil.getContentType(file);
		long contentLength = 0; // we are not sending any content
		// get the last modified date
		Date lastModified = fileUtil.getLastModifiedDate(file);

		// get the date and time of the response
		String dateString = RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC));

		// if keep-alive is set we need to insert the header in the response
		String connectionHeader = keepalive ? "Connection: keep-alive\r\n" : "";

		// if we have a Last Modified date we need to insert it
		String lastModifiedHeader = "Last-Modified: " + lastModified + "\r\n";

		// construct the response
		RawHttpResponse<?> response = http.parseResponse(
				"HTTP/1.1 200 OK \r\n" + 
				"Content-Type: " + contentType + "\r\n" + 
				"Content-Length: " + contentLength + "\r\n" + 
				"Server: RawHTTP\r\n" + "Date: " + dateString + "\r\n" + 
				connectionHeader + 
				lastModifiedHeader + "\r\n");
		// send it
		try {
			response.writeTo(clientSocket.getOutputStream());
		} catch (IOException e) {
			throw new ApplicationException("Error sending response: ", e);
		}
	}

	// respond with 403 Forbidden
	private void respondForbidden(RawHttp http) throws ApplicationException {
		// get the content type from the file itself
		String contentType = "text/plain";
		long contentLength = 0; 

		// get the date and time of the response
		String dateString = RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC));

		// if keep-alive is set we need to insert the header in the response
		String connectionHeader = keepalive ? "Connection: keep-alive\r\n" : "";

		// construct the response
		RawHttpResponse<?> response = http.parseResponse(
				"HTTP/1.1 403 Forbidden \r\n" + 
				"Content-Type: " + contentType + "\r\n" + 
				"Content-Length: " + contentLength + "\r\n" + 
				"Server: RawHTTP\r\n" + "Date: " + dateString + "\r\n" + 
				connectionHeader + "\r\n");
		// send it
		try {
			response.writeTo(clientSocket.getOutputStream());
		} catch (IOException e) {
			throw new ApplicationException("Error sending response: ", e);
		}
	}

	// respond with 404 Not Found
	private void respondNotFound(RawHttp http) throws ApplicationException {
		// get the content type from the file itself
		String contentType = "text/plain";
		long contentLength = 0; 

		// get the date and time of the response
		String dateString = RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC));

		// if keep-alive is set we need to insert the header in the response
		String connectionHeader = keepalive ? "Connection: keep-alive\r\n" : "";

		// construct the response
		RawHttpResponse<?> response = http.parseResponse(
				"HTTP/1.1 404 Not Found \r\n" + 
				"Content-Type: " + contentType + "\r\n" + 
				"Content-Length: " + contentLength + "\r\n" + 
				"Server: RawHTTP\r\n" + "Date: " + dateString + "\r\n" + 
				connectionHeader + "\r\n");
		// send it
		try {
			response.writeTo(clientSocket.getOutputStream());
		} catch (IOException e) {
			throw new ApplicationException("Error sending response: ", e);
		}
	}

	// respond with 405 Method Not Allowed
	private void respondMethodNotAllowed(RawHttp http) throws ApplicationException {
		// get the content type from the file itself
		String contentType = "text/plain";
		long contentLength = 0; 

		// get the date and time of the response
		String dateString = RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC));

		// if keep-alive is set we need to insert the header in the response
		String connectionHeader = keepalive ? "Connection: keep-alive\r\n" : "";

		// construct the response
		RawHttpResponse<?> response = http.parseResponse(
				"HTTP/1.1 405 Method Not Allowed \r\n" + 
				"Content-Type: " + contentType + "\r\n" + 
				"Content-Length: " + contentLength + "\r\n" + 
				"Server: RawHTTP\r\n" + "Date: " + dateString + "\r\n" + 
				connectionHeader + "\r\n");
		// send it
		try {
			response.writeTo(clientSocket.getOutputStream());
		} catch (IOException e) {
			throw new ApplicationException("Error sending response: ", e);
		}
	}

	// respond with 500 Internal Server Error
	private void respondInternalServerError(RawHttp http, String msg) {
		// the message can be null - fix it
		msg = (null == msg) ? "" : msg;
		// get the content type from the file itself
		String contentType = "text/plain";
		long contentLength = msg.length(); 

		// get the date and time of the response
		String dateString = RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC));

		// if keep-alive is set we need to insert the header in the response
		String connectionHeader = keepalive ? "Connection: keep-alive\r\n" : "";

		// construct the response
		RawHttpResponse<?> response = http.parseResponse(
				"HTTP/1.1 500 Internal Server Error \r\n" + 
				"Content-Type: " + contentType + "\r\n" + 
				"Content-Length: " + contentLength + "\r\n" + 
				"Server: RawHTTP\r\n" + "Date: " + dateString + "\r\n" + 
				connectionHeader + "\r\n");
		// send it
		try {
			response.writeTo(clientSocket.getOutputStream());
		} catch (IOException e) {
			// ignore - we were already sending a 500 and exiting!
		}
	}
}
