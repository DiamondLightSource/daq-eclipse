package org.eclipse.scanning.device.ui.device;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.INamedNode;
import org.eclipse.scanning.api.scan.ui.ControlFactory;

public class ControlContentProvider implements ITreeContentProvider {

	
	private ControlFactory factory;

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.factory = (ControlFactory)newInput;
		factory.build();
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public Object[] getChildren(Object element) {
		INamedNode node = (INamedNode)element;
		return node.getChildren();
	}

	@Override
	public Object getParent(Object element) {
		INamedNode node = (INamedNode)element;
		return node.getParent();
	}

	@Override
	public boolean hasChildren(Object element) {
		INamedNode node = (INamedNode)element;
		return node.hasChildren();
	}

}
