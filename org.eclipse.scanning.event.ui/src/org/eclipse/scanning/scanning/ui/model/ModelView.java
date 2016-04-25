package org.eclipse.scanning.scanning.ui.model;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.richbeans.widgets.table.ISeriesItemDescriptor;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.scanning.ui.util.PageUtil;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

public class ModelView extends ViewPart implements ISelectionListener {

	private ModelViewer modelEditor;
	
	@Override
	public void createPartControl(Composite parent) {
		
		PageUtil.getPage(getSite()).addSelectionListener(this);

		modelEditor = new ModelViewer(PageUtil.getPage(getSite()));
		modelEditor.createPartControl(parent);
		
		getSite().setSelectionProvider(modelEditor);
		
	}

	@Override
	public void setFocus() {
		modelEditor.setFocus();
	}
	
	@Override
	public void dispose() {
		if (modelEditor!=null) modelEditor.dispose();
		if (PageUtil.getPage()!=null) PageUtil.getPage().removeSelectionListener(this);
		super.dispose();
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			Object ob = ((IStructuredSelection)selection).getFirstElement();
			String       name = null;
			if (ob instanceof ISeriesItemDescriptor) {
				ISeriesItemDescriptor des = (ISeriesItemDescriptor)ob;
				name = des.getLabel();
			} else if (ob instanceof DeviceInformation) {
				DeviceInformation info = (DeviceInformation)ob;
				name = info.getLabel();
				if (name == null) name = info.getName();
				if (name == null) name = info.getId();
			}
			if (name!=null) setPartName("Model '"+name+"'");

		}		
	}

}
