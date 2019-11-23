package org.ghi.external.HTTPLite;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.Logger;

/**
 * The ServerListener is responsible for managing incoming connections.
 * 1) It creates a connection to listen to on a configurable port
 * 2) It creates a thread pool to bee used later to service the requests
 * 3) It goes into a loop during which
 * 		it waits for incoming connections (accept)
 * 		when a connection comes in it creates a worker to service it
 * 		it passes the worker to the thread pool for execution.
 * 
 * NOTES: 
 * A) The worker is responsible for closing the client connection
 * B) Each thread receives its own worker. This minimizes the need for synchronization.
 * 
 * @author George Hadjiyiannis
 *
 */
public class ServerListener implements Runnable {
	// private variables
	private IApplicationInjector injector;
	private Logger logger;
	private int port;
	private int poolSize;
	private ServerSocket listenSocket;
	private ExecutorService threadPool;

	public ServerListener(IApplicationInjector injector, Logger logger, int port, int poolSize) throws ApplicationException {
		
		// sanity check the inputs
		if (null == injector)
			throw new ApplicationException("Injector cannot be null!");
		this.injector = injector;
		
		if (null == logger)
			throw new ApplicationException("Logger cannot be null!");
		this.logger = logger;

		if (port < 0)
			throw new ApplicationException("Invalid port " + port);
		this.port = port;
		
		if (poolSize < 1)
			throw new ApplicationException("Invalid pool size " + poolSize);
		this.poolSize = poolSize;
	}

	public void run() {
		
		try {
			
			logger.info("Listening on port " + port);

			// create a socket to listen on
			listenSocket = new ServerSocket(port);
			
			// create a Thread pool to use for the workers
			threadPool = Executors.newFixedThreadPool(poolSize);
			
			// loop waiting for connections
			while(true) {
				// this will block until a connection comes in
				Socket clientSocket = listenSocket.accept();
				
				// create a worker to service the connection
				/*
				 * NOTE: the worker is responsible for closing the clientSocket
				 */
				Runnable worker = injector.getWorker(logger, clientSocket);
				// and execute in our thread pool
				threadPool.execute(worker);
			}
			
		} catch (ApplicationException a) {
			logger.error("Unknown error while setting up connection: ", a);
		} catch (SocketException e) {
			/*
			 * ignore it - we expect this to happen when stop() is called
			 * cleanup will be handle in finally anyway
			 */
		} catch (IOException e) {
			logger.error("Unknown error while setting up connection: ", e);
		} finally {
			logger.info("Listener shutting down...");
			
			// let's make sure we clean up no matter how we exit
			try {
				if (!listenSocket.isClosed()) 
					listenSocket.close();
			} catch (IOException e) {
				logger.error("Unknown error while closing listening socket: ", e);
			}
			
			threadPool.shutdown();
		}
	}

	/*
	 * This will be called from a different thread than the one that called run (run
	 * does not exit otherwise)
	 */
	public void stop() {
		/*
		 * Note that we have no simple way of signaling through variables that the main
		 * loop should stop since most likely it is blocked waiting to accept an
		 * incoming connection Instead we close the listen socket causing the accept
		 * call to throw an exception.
		 * Its not pretty but it's better for clarity for now
		 */
		try {
			if (!listenSocket.isClosed()) 
				listenSocket.close();
		} catch (IOException e) {
			logger.error("Unknown error while closing listening socket: ", e);
		}
	}

}
