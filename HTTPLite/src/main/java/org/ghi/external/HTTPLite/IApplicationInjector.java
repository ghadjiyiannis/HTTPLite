package org.ghi.external.HTTPLite;

import java.net.Socket;

import org.apache.logging.log4j.Logger;

/**
 * An interface for an injector to implement Inversion of Control (IoC)
 * 
 * @author George Hadjiyiannis
 *
 */
public interface IApplicationInjector {
	
	public Runnable getServerListener(Logger logger, int port, int poolSize) throws ApplicationException;
	
	public Runnable getWorker(Logger logger, Socket socket) throws ApplicationException;
	
	public IFileUtil getFileUtil() throws ApplicationException;

}
