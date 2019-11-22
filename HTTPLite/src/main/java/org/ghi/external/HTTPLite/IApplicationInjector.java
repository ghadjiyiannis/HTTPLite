package org.ghi.external.HTTPLite;

import java.net.Socket;

import org.apache.logging.log4j.Logger;

public interface IApplicationInjector {
	
	public Runnable getWorker(Logger logger, Socket socket);
	
	public IFileUtil getFileUtil();

}
