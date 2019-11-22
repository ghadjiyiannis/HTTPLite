package org.ghi.external.HTTPLite;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * HTTPLite is the responsible for initializing everything and getting the server going
 * 1) It creates a logger so that we can log errors
 * 2) It reads the configuration and sets defaults for anything not included in the config file
 * 		NOTE: we currently read the config file from classpath for simplicity, but this could
 * 		easily be any other location (and probably should be passed in on command line)
 * 3) It then creates a server listener and starts it up. The server listener will do the rest.
 * 
 * NOTE: This is not yet a proper Unix-style daemon, in that it is still attached to stdio and
 * stderr, and will be killed by signals, including the one generated when the parent exits.
 * That can be implemented later.
 * 
 * @author George Hadjiyiannis
 *
 */
public class HTTPLite 
{
	// let's define constants for the strings used to name the config parameters
	public static final String KEY_PORT = "port";
	public static final String KEY_POOl_SIZE = "poolSize";
	public static final String KEY_DOCUMENT_ROOT = "documentRoot";
	
    public static void main( String[] args )
    {
    	// first of all create a logger so that we can log issues
		Logger logger;
    	logger = LogManager.getLogger(HTTPLite.class);
    	
    	// and let the world know we are here
    	logger.info("Starting up HTTPLite...");
 
    	try {
    		// now read our configuration
			Properties properties = getConfigPropertiesFromClasspath();

	    	// and let's extract it into variables with reasonable defaults
	    	int port = Integer.valueOf(properties.getProperty(KEY_PORT, "80"));
	    	int poolSize = Integer.valueOf(properties.getProperty(KEY_POOl_SIZE, "10"));
	    	String documentRoot = properties.getProperty(KEY_DOCUMENT_ROOT, "/var/httpd");
	    	
	    	// now let's create our injector
			IApplicationInjector injector = new ApplicationInjector(documentRoot);
			
			// get a ServerListener from the injector, and start it up
			Runnable serverListener = injector.getServerListener(logger, port, poolSize);
			serverListener.run();
			
			// note that currently this has no real way of shutting down other than error
			
		} catch (ApplicationException e) {
			logger.error("Failed to initialize correctly: ", e);
		}
    	
    	// mark the shutdown in the logs
    	logger.info("Shutting down HTTPLite...");
    }
    
    public static Properties getConfigPropertiesFromClasspath() throws ApplicationException {
    	
    	Properties properties = new Properties();
    	// open the properties file
    	InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");
    	if (null == input)
    		throw new ApplicationException("Could not find config.properties!");
    	// try to load from the file
    	try {
			properties.load(input);
		} catch (IOException e) {
			throw new ApplicationException("Failed to read config.properties: ", e);
		} finally {
			// make sure we clean up no matter which way we exit
			try {
				input.close();
			} catch (IOException e) {
				// ignore - there's nothing we can do
			}
		}
    	return properties;
    }
}
