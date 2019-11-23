package org.ghi.external.HTTPLite;

import java.net.Socket;

/**
 * An interface for an injector to implement Inversion of Control (IoC)
 * 
 * @author George Hadjiyiannis
 *
 */
public interface IApplicationInjector {
	
	public Runnable getServerListener(int port, int poolSize) throws ApplicationException;
	
	public Runnable getWorker(Socket socket) throws ApplicationException;
	
	public IFileUtil getFileUtil() throws ApplicationException;

}
