package org.eclipse.scanning.api.scan.ui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scanning.api.INamedNode;
import org.eclipse.scanning.api.event.IEventConnectorService;

/**
 * An old fashioned singleton pattern because this interacts with 
 * the objects like ScannableContol etc. being made by spring, nicely.
 * 
 * @author Matthew Gerring
 *
 */
public class ControlTree extends AbstractControl {

	private static ControlTree instance;
	
	private Map<String, INamedNode> content;

	public ControlTree() {
		this.content = new LinkedHashMap<>(37); // Ordered by insertion
	}
	
	public static ControlTree getInstance() {
		return instance;
	}
	
	public void globalize() { // Called by spring
		ControlTree.instance = this;
	}
	
	/**
	 * Used to register a control such that a subsequent call
	 * to 'build' will create a legal tree.
	 * @param object
	 * @return Returns true if this collection changed as a result of the call.
	 */
	public INamedNode add(INamedNode object) {
		if (content.containsKey(object.getName())) throw new RuntimeException("The name '"+object.getName()+"' is already used!");
		return content.put(object.getName(), object);
	}
	
	public INamedNode insert(INamedNode parent, INamedNode child) {
		
		child.setParentName(parent.getName());
		
		final INamedNode[] onodes = parent.getChildren();
		final INamedNode[] nnodes = new INamedNode[onodes!=null ? onodes.length+1 : 1];
		if (onodes!=null) System.arraycopy(onodes, 0, nnodes, 0, onodes.length);
		nnodes[nnodes.length-1] = child;
		parent.setChildren(nnodes);
		
		return child;
	}

	public void delete(INamedNode node) {
		if (node.getParentName()==null) return;
		INamedNode parent = getNode(node.getParentName());
		List<INamedNode> nodes = new ArrayList<INamedNode>(Arrays.asList(parent.getChildren()));
		nodes.remove(node);
		parent.setChildren(nodes.toArray(new INamedNode[nodes.size()]));
		content.remove(node.getName());
	}
	
	public boolean isEmpty() {
		return content.isEmpty();
	}
	public Iterator<INamedNode> iterator() {
		return content.values().iterator();
	}

	/**
	 * A call to this method tells the factory to validate by reading
	 * the groups and controls created in spring. 
	 * 
	 * It is done this way because at the point where spring creates the 
	 * objects, they might not yet have their names and other details set.
	 */
	public boolean build() {
		
		if (getChildren()!=null) return false;
		
		final List<INamedNode> children = new ArrayList<>();
		
		for (String name : content.keySet()) {
			
			INamedNode iNameable = content.get(name);
			
			if (iNameable instanceof ControlGroup) {
				children.add(iNameable);
				iNameable.setParentName(getName());
			}
		}
		setChildren(children.toArray(new INamedNode[children.size()]));
		return true;
	}
	

	public Map<String, INamedNode> getContent() {
		return content;
	}

	public void setContent(Map<String, INamedNode> content) {
		this.content = content;
	}

	public INamedNode getNode(String name) {
		if (getName().equals(name)) return this;
		return content.get(name);
	}

	public boolean contains(String name) {
		if (getName().equals(name)) return true;
		return content.containsKey(name);
	}

	
	private static File getStashFile() {
		final File stash = new File(System.getProperty("user.name")+"/.solstice/org.eclipse.scanning.device.ui.device.controls.json");
        return stash;
	}
	
	public static boolean isStashed() {
		return getStashFile().exists();
	}
	
	public void stash(IEventConnectorService marshallerService) throws Exception {
		final String json = marshallerService.marshal(this);
		write(getStashFile(), json);
	}
	
	public static void unstash(IEventConnectorService marshallerService) throws Exception {
		
		final String json = readFile(getStashFile()).toString();
		final ControlTree factory = marshallerService.unmarshal(json, ControlTree.class);
		factory.build();
		factory.globalize();
	}
	
	private static void write(final File file, final String text) throws Exception {
		
		file.getParentFile().mkdirs();
		BufferedWriter b = null;
		try {
			final OutputStream out = new FileOutputStream(file);
			final OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
			b = new BufferedWriter(writer);
			b.write(text.toCharArray());
		} finally {
			if (b != null) {
				b.close();
			}
		}
	}

	private static final StringBuffer readFile(final File file) throws Exception {

		final String charsetName = "UTF-8";
		final InputStream in = new FileInputStream(file);
		BufferedReader ir = null;
		try {
			ir = new BufferedReader(new InputStreamReader(in, charsetName));

			// deliberately do not remove BOM here
			int c;
			StringBuffer currentStrBuffer = new StringBuffer();
			final char[] buf = new char[4096];
			while ((c = ir.read(buf, 0, 4096)) > 0) {
				currentStrBuffer.append(buf, 0, c);
			}
			return currentStrBuffer;

		} finally {
			if (ir != null) {
				ir.close();
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ControlTree other = (ControlTree) obj;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		return true;
	}

	public void setName(INamedNode node, String name) {
		node.setName(name);
		content.put(name, node);
	}

}
