package org.ghi.external.HTTPLite;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import rawhttp.core.RawHttp;
/**
 * This test only really tests the constructor input sanity checks.
 * Most of the rest of the code is covered by the IntegrationTest. 
 * Strictly speaking it should also be covered here to make it faster
 * to isolate faults in this unit, but in the interest of time I skipped
 * those tests.
 * 
 * @author George Hadjiyiannis 
 *
 */
public class ConnectedWorkerTest {
	// instances we will be using for all the tests 
	private static IApplicationInjector injector;
	private static Logger logger;

	@BeforeClass
	public static void beforeClass() throws ApplicationException {
		// the server will need a valid logger
		logger = LogManager.getLogger(IntegrationTest.class);
		// lets use the same config as the application
		Properties properties = HTTPLite.getConfigPropertiesFromClasspath();
		// extract Document Root
		String documentRoot = properties.getProperty(HTTPLite.KEY_DOCUMENT_ROOT);
		
		// create an injector
		injector = new ApplicationInjector(documentRoot);
	}
	
	@Test(expected=ApplicationException.class)
	public void test_ThrowsWhen_InjectorNull() throws ApplicationException {
		Socket socket = new Socket();
		new ConnectedWorker(null, logger, socket);
	}
	
	@Test(expected=ApplicationException.class)
	public void test_ThrowsWhen_LoggerNull() throws ApplicationException {
		Socket socket = new Socket();
		new ConnectedWorker(injector, null, socket);
	}
	
	@Test(expected=ApplicationException.class)
	public void test_ThrowsWhen_SocketNull() throws ApplicationException {
		new ConnectedWorker(injector, logger, null);
	}

}
