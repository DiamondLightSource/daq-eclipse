package org.eclipse.scanning.api;

/**
 * 
 * An interface for declaring parents and possibly
 * one day more distant ancestors.
 * 
 * This is designed to be used as a node in an RCP tree content provider.
 * 
 * @author Matthew Gerring
 *
 */
public interface INamedNode extends INameable {

	/**
	 * 
	 * @return
	 */
	INamedNode getParent();
	
	/**
	 * 
	 * @param parent
	 */
	void setParent(INamedNode parent);

	/**
	 * 
	 * @return the children or null if there are none.
	 */
	INamedNode[] getChildren();
	
	/**
	 * Set the children.
	 * @param children
	 */
	void setChildren(INamedNode[] children);

	/**
	 * 
	 * @return true if there are children.
	 */
	boolean hasChildren();
	
	/**
	 * Used to provide a label in the UI
	 */
	default String getDisplayName() {
		return getName();
	}
}
