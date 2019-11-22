package org.ghi.external.HTTPLite;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Optional;

import org.apache.logging.log4j.Logger;

import rawhttp.core.RawHttp;
import rawhttp.core.RawHttpRequest;

/**
 * 
 * 
 * 
 * 
 * The handling of the RawHttp requests and responses is based on the examples at
 * https://renatoathaydes.github.io/rawhttp/docs/low-level-api/
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

	public ConnectedWorker(IApplicationInjector injector, Logger logger, Socket clientSocket) throws ApplicationException {
		
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
		// TODO Auto-generated method stub
		logger.debug("Connected...");
		
		// lets create a parser for the protocol
		RawHttp http = new RawHttp();
		
		try {
			// loop as we may be servicing multiple requests if keep-alive is set
			while(true) {
				
				// serve a single request
				serveSingleRequest(http);
				
				// check and abide by the keep-alive flag
				if (!keepalive)
					break;
			}
			
		} catch (Exception e) {
			
			logger.error("Internal server error while processing request: ", e);
			
			// respond with 500 Internal Server Error
			respondInternalServerError(http);
			
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
			
			// lets check the file out a bit
			File file = fileUtil.getFileFromPath(path);
			
			// if it does not exists respond with 404 Not Found
			if (!fileUtil.exists(file))
				respondNotFound(http);
			
			// if it is not readable respond with 403 Forbidden
			respondForbidden(http);
			
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
	private void respondOKWithContent(RawHttp http, File file) {
		// TODO Auto-generated method stub
		
	}

	// respond with 200 OK (without content)
	private void respondOKWithoutContent(RawHttp http, File file) {
		// TODO Auto-generated method stub
		
	}
	
	// respond with 403 Forbidden
	private void respondForbidden(RawHttp http) {
		// TODO Auto-generated method stub
		
	}

	// respond with 404 Not Found
	private void respondNotFound(RawHttp http) {
		// TODO Auto-generated method stub
		
	}

	// respond with 405 Method Not Allowed
	private void respondMethodNotAllowed(RawHttp http) {
		// TODO Auto-generated method stub
		
	}
	
	// respond with 500 Internal Server Error
	private void respondInternalServerError(RawHttp http) {
		// TODO Auto-generated method stub
		
	}

}
