package org.eclipse.scanning.device.ui.model;

import java.net.URI;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.richbeans.widgets.internal.GridUtils;
import org.eclipse.richbeans.widgets.table.ISeriesItemDescriptor;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.scan.ui.ControlTree;
import org.eclipse.scanning.device.ui.Activator;
import org.eclipse.scanning.device.ui.ServiceHolder;
import org.eclipse.scanning.device.ui.device.scannable.ControlTreeViewer;
import org.eclipse.scanning.device.ui.device.scannable.ControlViewerMode;
import org.eclipse.scanning.device.ui.util.PageUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelView extends ViewPart implements ISelectionListener {

	public static final String ID = "org.eclipse.scanning.device.ui.modelEditor";
	
	private static Logger logger = LoggerFactory.getLogger(ModelView.class);

	// UI
	private Composite         parent;
	private ModelViewer       modelEditor;
	private ControlTreeViewer treeViewer;

	
	@Override
	public void createPartControl(Composite parent) {	

		this.parent = parent;
		try {
			final Composite content = new Composite(parent, SWT.NONE);
			content.setLayout(new GridLayout(1, false));
			GridUtils.removeMargins(content);
			
			modelEditor = new ModelViewer(PageUtil.getPage(getSite()));
			modelEditor.createPartControl(content);
			GridUtils.setVisible(modelEditor.getControl(), true);
			getSite().setSelectionProvider(modelEditor);
			
			IScannableDeviceService cservice = ServiceHolder.getEventService().createRemoteService(new URI(Activator.getJmsUri()), IScannableDeviceService.class);			
			treeViewer = new ControlTreeViewer(cservice, ControlViewerMode.INDIRECT_NO_SET_VALUE);
			treeViewer.createPartControl(content, new ControlTree(), getViewSite().getActionBars().getMenuManager(), getViewSite().getActionBars().getToolBarManager());
			GridUtils.setVisible(treeViewer.getControl(), false);
				
			setActionsVisible(false);
			
			PageUtil.getPage(getSite()).addSelectionListener(this);

		} catch (Exception ne) {
			logger.error("Unable to create model table!", ne);
		}
		
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
				GridUtils.setVisible(modelEditor.getControl(), true);
				GridUtils.setVisible(treeViewer.getControl(), false);
				getSite().setSelectionProvider(modelEditor);
				setActionsVisible(false);
	
			} else if (ob instanceof DeviceInformation) {
				DeviceInformation info = (DeviceInformation)ob;
				name = info.getLabel();
				if (name == null) name = info.getName();
				if (name == null) name = info.getId();
				GridUtils.setVisible(modelEditor.getControl(), true);
				GridUtils.setVisible(treeViewer.getControl(), false);
				getSite().setSelectionProvider(modelEditor);
				setActionsVisible(false);
				
			} else if (ob instanceof ControlTree) {
				ControlTree tree = (ControlTree)ob;
				treeViewer.setControlTree(tree);
				treeViewer.refresh();
				GridUtils.setVisible(modelEditor.getControl(), false);
				GridUtils.setVisible(treeViewer.getControl(), true);
			    getSite().setSelectionProvider(treeViewer.getSelectionProvider());
			    name = tree.getDisplayName();
				setActionsVisible(true);
			} else {
				// Other selections will come in, we ignore these as we cannot edit them.
			}
			if (name!=null) setPartName(name);

		}		
	}

	private void setActionsVisible(boolean vis) {
		setActionsVisible(getViewSite().getActionBars().getToolBarManager(), vis);
		setActionsVisible(getViewSite().getActionBars().getMenuManager(), vis);
		getViewSite().getActionBars().updateActionBars();
		parent.getParent().layout(new Control[]{parent});
		parent.layout(true);
	}

	private void setActionsVisible(IContributionManager man, boolean vis) {
		for (IContributionItem item : man.getItems()) {
			item.setVisible(vis);
		}
	}

}
