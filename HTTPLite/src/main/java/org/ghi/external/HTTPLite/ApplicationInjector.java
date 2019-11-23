package org.ghi.external.HTTPLite;

import java.net.Socket;

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

	public Runnable getServerListener(int port, int poolSize) throws ApplicationException {
		return new ServerListener(this, port, poolSize);
	}

	public Runnable getWorker(Socket clientSocket) throws ApplicationException {
		return new ConnectedWorker(this, clientSocket);
	}

	public IFileUtil getFileUtil() throws ApplicationException {
		return new FileUtil(documentRoot);
	}

}
