package org.eclipse.scanning.device.ui.device.scannable;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.scanning.api.INamedNode;
import org.eclipse.scanning.api.scan.ui.ControlTree;
import org.eclipse.ui.dialogs.PatternFilter;

class NamedNodeFilter extends PatternFilter {

	@Override
    protected boolean isLeafMatch(Viewer viewer, Object element){
		try {
			INamedNode ob = (INamedNode)element;
			boolean matched = super.isLeafMatch(viewer, ob.getName());
			if (!matched && ob.getParentName()!=null) {
				final ControlTree tree = (ControlTree)viewer.getInput();
				return isLeafMatch(viewer, tree.getNode(ob.getParentName()));
			}
			return matched;
		} catch (Exception ne) {
			return super.isLeafMatch(viewer, element);
		}
	}
}
