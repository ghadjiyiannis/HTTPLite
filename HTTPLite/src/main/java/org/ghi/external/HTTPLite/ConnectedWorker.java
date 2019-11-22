package org.ghi.external.HTTPLite;

import java.io.IOException;
import java.net.Socket;

import org.apache.logging.log4j.Logger;

public class ConnectedWorker implements Runnable {
	// private variables
	private IApplicationInjector injector;
	private Logger logger;
	private Socket clientSocket;

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

	}

	public void run() {
		// TODO Auto-generated method stub
		logger.info("Connected...");
		
		try {
			if (!clientSocket.isClosed())
				clientSocket.close();
		} catch (IOException e) {
			logger.error("Unknown error while closing client socket: ", e);
		}
		
		logger.info("Exiting connection...");
	}

}
