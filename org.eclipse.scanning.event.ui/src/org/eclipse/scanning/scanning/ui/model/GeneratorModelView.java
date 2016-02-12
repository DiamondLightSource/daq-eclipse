package org.eclipse.scanning.scanning.ui.model;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.richbeans.widgets.table.ISeriesItemDescriptor;
import org.eclipse.scanning.scanning.ui.util.PageUtil;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

public class GeneratorModelView extends ViewPart implements ISelectionListener {

	private GeneratorModelViewer modelEditor;
	
	@Override
	public void createPartControl(Composite parent) {
		
		PageUtil.getPage(getSite()).addSelectionListener(this);

		modelEditor = new GeneratorModelViewer(PageUtil.getPage(getSite()));
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
			if (ob instanceof ISeriesItemDescriptor) {
				ISeriesItemDescriptor des = (ISeriesItemDescriptor)ob;
				final String       name = des.getLabel();
				setPartName("Model '"+name+"'");
			}
		}		
	}

}
