package org.ghi.external.HTTPLite;

import java.net.Socket;

import org.apache.logging.log4j.Logger;

/**
 * An implementation of an injector to implement Inversion of Control (IoC)
 * 
 * @author George Hadjiyiannis
 *
 */
public class ApplicationInjector implements IApplicationInjector {
	// keep track of document root
	private String documentRoot;

	public ApplicationInjector(String documentRoot) throws ApplicationException {
		// sanity check the input
		if (null == documentRoot)
			throw new ApplicationException("Document root cannot be null!");
		this.documentRoot = documentRoot;
	}

	public Runnable getServerListener(Logger logger, int port, int poolSize) throws ApplicationException {
		// TODO Auto-generated method stub
		return null;
	}

	public Runnable getWorker(Logger logger, Socket socket) throws ApplicationException {
		// TODO Auto-generated method stub
		return null;
	}

	public IFileUtil getFileUtil() throws ApplicationException {
		return new FileUtil(documentRoot);
	}

}
