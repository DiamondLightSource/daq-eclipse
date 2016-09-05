package org.eclipse.scanning.api;

import java.io.InputStream;
import java.util.Map;

/**
 * A parser for reading a spring file and creating objects.
 * The parser might use actual spring or its own home grown version.
 * If a home grown version is used, the parse method will throw an
 * UnsupportedOperationException. If other errors occur with the 
 * parsing any other type of exception may be thrown.
 * 
 * @author Matthew Gerring
 *
 */
public interface ISpringParser {

	/**
	 * Parse an xml 
	 * @param path
	 * @return
	 */
	Map<String, Object> parse(String path) throws UnsupportedOperationException, Exception;
	
	
	/**
	 * Parse an xml 
	 * @param in
	 * @return
	 */
	Map<String, Object> parse(InputStream in) throws UnsupportedOperationException, Exception;

}
