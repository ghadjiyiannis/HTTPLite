package org.ghi.external.HTTPLite;

import java.net.Socket;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;

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

	@BeforeClass
	public static void beforeClass() throws ApplicationException {
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
		new ConnectedWorker(null, socket);
	}
	
	@Test(expected=ApplicationException.class)
	public void test_ThrowsWhen_SocketNull() throws ApplicationException {
		new ConnectedWorker(injector, null);
	}

}
