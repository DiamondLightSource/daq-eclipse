package org.eclipse.scanning.api.scan.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scanning.api.IAncestered;
import org.eclipse.scanning.api.INameable;

/**
 * An old fashioned singleton pattern because this interacts with 
 * the objects like ScannableContol etc. being made by spring, nicely.
 * 
 * @author Matthew Gerring
 *
 */
public class ScannableControlFactory {

	private static ScannableControlFactory instance;
	
	private final List<INameable> content;

	private ScannableControlFactory() {
		instance = this;
		this.content = new ArrayList<>(37);
	}
	public static ScannableControlFactory getInstance() {
		if (instance==null) new ScannableControlFactory();
		return instance;
	}
	public static void setInstance(ScannableControlFactory instance) {
		ScannableControlFactory.instance = instance;
	}
	
	public void add(INameable object) {
		content.add(object);
	}
	
	/**
	 * A call to this method tells the factory to create a tree by reading
	 * the groups and controls created in spring. After calling this method
	 * the tree content provider may call methods such as get children.
	 * 
	 * It is done this way because at the point where spring creates the 
	 * objects, they might not yet have their names set.
	 */
	public void createTree() {
		// Parse all names into a map, if names are repeated, throw 
		// an exception. All names in the table must be unique, even scannables
		// and groups must not have colliding names.
		Set<String> names = new HashSet<>(content.size());
		for (INameable iNameable : content) {
			String name = iNameable.getName();
			if (names.contains(name)) throw new IllegalArgumentException("The name '"+name+"' is already used!");
			names.add(name);
		}
	}
	
	public Object[] getChildren(INameable item) {
		return null;
	}
	
	public INameable getParent(INameable item) {
		if (item instanceof IAncestered) return ((IAncestered)item).getParent();
		return null;
	}
	
	public boolean hasChildren(Object element) {
		// TODO Auto-generated method stub
		return false;
	}
	
 
}
