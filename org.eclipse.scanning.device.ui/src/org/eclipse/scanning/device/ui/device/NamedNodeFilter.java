package org.eclipse.scanning.device.ui.device;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.scanning.api.INamedNode;
import org.eclipse.ui.dialogs.PatternFilter;

class NamedNodeFilter extends PatternFilter {

	@Override
    protected boolean isLeafMatch(Viewer viewer, Object element){
		try {
			INamedNode ob = (INamedNode)element;
			boolean matched = super.isLeafMatch(viewer, ob.getName());
			if (!matched && ob.getParent()!=null) {
				return isLeafMatch(viewer, ob.getParent());
			}
			return matched;
		} catch (Exception ne) {
			return super.isLeafMatch(viewer, element);
		}
	}
}
