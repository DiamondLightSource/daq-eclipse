package org.eclipse.scanning.device.ui.device;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.scanning.api.INameable;
import org.eclipse.ui.dialogs.PatternFilter;

class NamedObjectFilter extends PatternFilter {

	@Override
    protected boolean isLeafMatch(Viewer viewer, Object element){
		try {
			INameable ob = (INameable)element;
			return super.isLeafMatch(viewer, ob.getName());
		} catch (Exception ne) {
			return super.isLeafMatch(viewer, element);
		}
	}
}
