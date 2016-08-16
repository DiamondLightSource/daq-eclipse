package org.eclipse.scanning.device.ui.device;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.scan.ui.ScannableControlFactory;

public class ScannableControlContentProvider implements ITreeContentProvider {

	
	private ScannableControlFactory factory;

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.factory = (ScannableControlFactory)newInput;
		factory.createTree();
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
	public Object[] getChildren(Object parentElement) {
		INameable item = (INameable)parentElement;
		return factory.getChildren(item);
	}

	@Override
	public Object getParent(Object element) {
		INameable item = (INameable)element;
		return factory.getParent(item);
	}

	@Override
	public boolean hasChildren(Object element) {
		return factory.hasChildren(element);
	}

}
