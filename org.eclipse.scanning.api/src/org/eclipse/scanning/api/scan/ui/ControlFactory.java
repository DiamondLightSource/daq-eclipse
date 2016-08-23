package org.eclipse.scanning.api.scan.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scanning.api.INamedNode;

/**
 * An old fashioned singleton pattern because this interacts with 
 * the objects like ScannableContol etc. being made by spring, nicely.
 * 
 * @author Matthew Gerring
 *
 */
public class ControlFactory extends AbstractControl {

	private static ControlFactory instance;
	
	private final Collection<INamedNode> content;

	private ControlFactory() {
		instance = this;
		this.content = new HashSet<>(37);
	}
	public static ControlFactory getInstance() {
		if (instance==null) new ControlFactory();
		return instance;
	}
	
	public static void setInstance(ControlFactory instance) {
		ControlFactory.instance = instance;
	}
	
	/**
	 * Used to register a control such that a subsequent call
	 * to 'build' will create a legal tree.
	 * @param object
	 * @return Returns true if this collection changed as a result of the call.
	 */
	public boolean add(INamedNode object) {
		return content.add(object);
	}
	
	public INamedNode insert(INamedNode parent, INamedNode child) {
		
		child.setParent(parent);
		
		final INamedNode[] onodes = parent.getChildren();
		final INamedNode[] nnodes = new INamedNode[onodes!=null ? onodes.length+1 : 1];
		if (onodes!=null) System.arraycopy(onodes, 0, nnodes, 0, onodes.length);
		nnodes[nnodes.length-1] = child;
		parent.setChildren(nnodes);
		
		return child;
	}

	public void delete(INamedNode node) {
		if (node.getParent()==null) return;
		INamedNode parent = node.getParent();
		node.setParent(null);
		List<INamedNode> nodes = new ArrayList<INamedNode>(Arrays.asList(parent.getChildren()));
		nodes.remove(node);
		parent.setChildren(nodes.toArray(new INamedNode[nodes.size()]));
		content.remove(node);
	}
	
	public boolean isEmpty() {
		return content.isEmpty();
	}
	
	/**
	 * A call to this method tells the factory to validate by reading
	 * the groups and controls created in spring. 
	 * 
	 * It is done this way because at the point where spring creates the 
	 * objects, they might not yet have their names and other details set.
	 */
	public void build() {
		
		if (getChildren()!=null) throw new IllegalArgumentException("The factory is already build after spring, it cannot be built again. Use insert(...) to add controls!");
		
		final List<INamedNode> children = new ArrayList<>();
		// Parse all names into a map, if names are repeated, throw 
		// an exception. All names in the table must be unique, even scannables
		// and groups must not have colliding names.
		Set<String> names = new HashSet<>(content.size());
		for (INamedNode iNameable : content) {
			String name = iNameable.getName();
			if (name==null || "".equals(name)) continue;
			if (names.contains(name)) throw new IllegalArgumentException("The name '"+name+"' is already used!");
			names.add(name);
			
			if (iNameable instanceof ControlGroup) {
				children.add(iNameable);
				iNameable.setParent(this);
			}
		}
		setName("Controls"); 
		setChildren(children.toArray(new INamedNode[children.size()]));
	}

}
