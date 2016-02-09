package org.eclipse.scanning.api.malcolm.models;

import java.net.URI;

/**
 * 
 * Bean for requesting a malcolm device. May be set in a ScanRequest to provide
 * the information to connect to malcolm.
 * 
 * @author Matthew Gerring
 *
 */
public class MalcolRequest {
	
	/**
	 * The model for the detector.
	 */
	private Object deviceModel;
	
	/**
	 * URI to create a malcolm connection with
	 */
	private URI uri;

	/**
	 * Name of the malcolm device which we would like to run.
	 */
	private String deviceName;
}
