package org.eclipse.scanning.api.device;

import java.util.List;

import org.eclipse.scanning.api.scan.ScanningException;

/**
 * An interface for devices that have attributes that can be got and set.
 * 
 * @author Matt Taylor
 * 
 */
public interface IAttributableDevice {

	/**
	 * Gets the an attribute on the device
	 */
	public Object getAttribute(String attribute) throws ScanningException;
	
	/**
	 * Gets a list of all attributes on the device
	 */
	public <A> List<A> getAllAttributes() throws ScanningException;
	
	/**
	 * Gets the value of an attribute on the device
	 */
	public <A> A getAttributeValue(String attribute) throws ScanningException;

}
