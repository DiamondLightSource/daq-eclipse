package org.eclipse.scanning.api;

/**
 * 
 * An interface for declaring parents and possibly
 * one day more distant ancestors.
 * 
 * @author Matthew Gerring
 *
 */
public interface IAncestered {

	/**
	 * 
	 * @return
	 */
	INameable getParent();
	
	/**
	 * 
	 * @param parent
	 */
	void setParent(INameable parent);
}
