package org.ghi.external.HTTPLite;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.Logger;

public class HTTPLite 
{
	// let's define constants for the strings used to name the config parameters
	public static final String KEY_PORT = "port";
	public static final String KEY_POOl_SIZE = "poolSize";
	public static final String KEY_DOCUMENT_ROOT = "documentRoot";
	
	// our basic private variables
	private Logger logger;
	
    public static void main( String[] args )
    {
 
    }
    
    public static Properties getConfigPropertiesFromClasspath() throws ApplicationException, IOException {
    	
    	Properties properties = new Properties();
    	// open the properties file
    	InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");
    	if (null == input)
    		throw new ApplicationException("Could not find config.properties!");
    	properties.load(input);
    	return properties;
    }
}
