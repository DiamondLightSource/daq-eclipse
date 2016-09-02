package org.eclipse.scanning.device.ui.device.scannable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.richbeans.widgets.internal.GridUtils;
import org.eclipse.scanning.api.INamedNode;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.scan.event.ILocationListener;
import org.eclipse.scanning.api.scan.event.Location.LocationType;
import org.eclipse.scanning.api.scan.event.LocationEvent;
import org.eclipse.scanning.api.scan.ui.ControlGroup;
import org.eclipse.scanning.api.scan.ui.ControlNode;
import org.eclipse.scanning.api.scan.ui.ControlTree;
import org.eclipse.scanning.device.ui.Activator;
import org.eclipse.scanning.device.ui.DevicePreferenceConstants;
import org.eclipse.scanning.device.ui.ServiceHolder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.dialogs.FilteredTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Widget to edit a tree of scannables.
 * 
 * @author fcp94556
 *
 */
public class ControlTreeViewer {
	
	private static final Logger logger = LoggerFactory.getLogger(ControlTreeViewer.class);

	// Services
	private IScannableDeviceService cservice;

	// Events
	private ISubscriber<EventListener> subscriber;
	
	// UI
	private FilteredTree viewer;

	private ControlTree defaultTree;
	
	/**
	 * 
	 * @param cservice
	 */
	public ControlTreeViewer(IScannableDeviceService cservice) {
		this(null, cservice);
	}

    /**
     * 
     * @param defaultTree may be null
     * @param cservice
     */
	public ControlTreeViewer(ControlTree defaultTree, IScannableDeviceService cservice) {
		this.cservice = cservice;
		this.defaultTree = defaultTree;
	}
	
	/**
	 * Creates an editor for the tree passed in
	 * @param parent
	 * @param tree which is edited. If this ControlTreeViewer was created without specifiying a default, this is cloned for the default.
	 * @param managers
	 * @throws Exception 
	 */
	public void createPartControl(Composite parent, ControlTree tree, IContributionManager... managers) throws Exception {
		
		if (viewer!=null) throw new IllegalArgumentException("The createPartControl() method must only be called once!");

		if (defaultTree==null && tree==null) throw new IllegalArgumentException("No control tree has been defined!");
		
		// Clone this tree so that they can reset it!
		if (defaultTree==null) defaultTree = clone(tree);
        if (tree == null)      tree        = clone(defaultTree);
		

    	viewer = new FilteredTree(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE, new NamedNodeFilter(), true);
		
		TreeViewer tviewer = viewer.getViewer();
		tviewer.getTree().setLinesVisible(true);
		tviewer.getTree().setHeaderVisible(false);
		setItemHeight(tviewer.getTree(), 30);
		viewer.setLayoutData(new GridData(GridData.FILL_BOTH));

		createColumns(tviewer);
		
		try {
			tviewer.setContentProvider(new ControlContentProvider());
		} catch (Exception e) {
			logger.error("Cannot create content provider", e);
		}
		tviewer.setInput(tree);
		tviewer.expandAll();
		
		createActions(tviewer, managers);
		setSearchVisible(false);
		
		try {
		    registerAll();
		} catch (Exception ne) {
			logger.error("Cannot listen to motor values changing...");
		}

	}
	
	private ControlTree clone(ControlTree tree) throws Exception {
		IMarshallerService mservice = ServiceHolder.getMarshallerService();
		String      json = mservice.marshal(tree);
		ControlTree clone = mservice.unmarshal(json, ControlTree.class);
		return clone;
	}

	private void createColumns(TreeViewer viewer) {
		
		viewer.setColumnProperties(new String[] { "Name", "Value"});
		ColumnViewerToolTipSupport.enableFor(viewer);
		
        TreeViewerColumn var   = new TreeViewerColumn(viewer, SWT.LEFT, 0);
		var.getColumn().setText("Name");
		var.getColumn().setWidth(200);
		var.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				INamedNode node = (INamedNode)element;
				return node.getDisplayName();
			}
		});
		var.setEditingSupport(new ScannableEditingSupport(this));
		
		var   = new TreeViewerColumn(viewer, SWT.LEFT, 1);
		var.getColumn().setText("Value");
		var.getColumn().setWidth(300);
		var.setLabelProvider(new DelegatingStyledCellLabelProvider(new ControlValueLabelProvider(cservice)));
		var.setEditingSupport(new ControlEditingSupport(viewer, cservice));

	}


	/**
	 * Create the actions.
	 */
	private void createActions(final TreeViewer tviewer, IContributionManager... managers) {
		
		List<IContributionManager> mans = new ArrayList<>(Arrays.asList(managers));
		MenuManager     rightClick     = new MenuManager();
		mans.add(rightClick);
		
		final IAction addGroup = new Action("Add group", Activator.getImageDescriptor("icons/ui-toolbar--purpleplus.png")) {
			public void run() {
                INamedNode nnode = getControlTree().insert(getControlTree(), new ControlGroup());
                edit(nnode, 0);
			}
		};
		
		final IAction addNode = new Action("Add control", Activator.getImageDescriptor("icons/ui-toolbar--plus.png")) {
			public void run() {
                INamedNode     node    = getSelection();
                ControlTree factory = getControlTree();
                if (!(node instanceof ControlGroup)) node = factory.getNode(node.getParentName());
                if (node instanceof ControlGroup) {
                 	INamedNode control = factory.insert(node, new org.eclipse.scanning.api.scan.ui.ControlNode("", 0.1));
                 	edit(control, 0);
                }
			}
		};
		
		final IAction remove = new Action("Remove", Activator.getImageDescriptor("icons/ui-toolbar--minus.png")) {
			public void run() {
				final INamedNode node = getSelection();
                ControlTree factory = getControlTree();
			    INamedNode parent = factory.getNode(node.getParentName());
                if (node.getChildren()==null || node.getChildren().length<1) {
                	factory.delete(node);
                } else {
                	boolean ok = MessageDialog.openQuestion(viewer.getShell(), "Confirm Delete", "The item '"+node.getName()+"' is a group.\n\nAre you sure you would like to delete it?");
                	if (ok) factory.delete(node);
                }
                if (subscriber!=null) subscriber.removeListeners(node.getName());
                viewer.getViewer().refresh();
                if (parent.hasChildren()) {
                	setSelection(parent.getChildren()[parent.getChildren().length-1]);
                } else {
                    setSelection(parent);
                }
			}
		};
		remove.setEnabled(false);
			
		addGroups("add", mans, addGroup, addNode, remove);

		
		IAction expandAll = new Action("Expand All", Activator.getImageDescriptor("icons/expand_all.png")) {
			public void run() {
				try {
					registerAll();
				} catch (Exception e) {
					logger.error("Unable to reconnect all listeners!", e);
				}
				refresh();
			}
		};
		
		IAction showSearch = new Action("Show search", IAction.AS_CHECK_BOX) {
			public void run() {
				setSearchVisible(isChecked());
			}
		};
		showSearch.setImageDescriptor(Activator.getImageDescriptor("icons/magnifier--pencil.png"));
		
		IAction edit = new Action("Edit", Activator.getImageDescriptor("icons/pencil.png")) {
			public void run() {
				try {
					setEditNode(true);
					INamedNode node = getSelection();
					if (node!=null) {
						viewer.getViewer().editElement(node, 0); // edit name of control
					}
				} finally {
					setEditNode(false);
				}
			}
		};
		
		addGroups("refresh", mans, expandAll, showSearch, edit);
	
		IAction resetAll = new Action("Reset all controls to default", Activator.getImageDescriptor("icons/arrow-return-180-left.png")) {
			public void run() {
				boolean ok = MessageDialog.openConfirm(viewer.getShell(), "Confirm Reset Controls", "Are you sure that you want to reset all controls to default?");
				if (!ok) return;
				viewer.getViewer().setInput(defaultTree);
				expandAll.run();
			}
		};
		addGroups("reset", mans, resetAll);
				
		IAction setShowTip = new Action("Show tooltip on edit", IAction.AS_CHECK_BOX) {
			public void run() {
				Activator.getDefault().getPreferenceStore().setValue(DevicePreferenceConstants.SHOW_CONTROL_TOOLTIPS, isChecked());
			}
		};
		setShowTip.setChecked(Activator.getDefault().getPreferenceStore().getBoolean(DevicePreferenceConstants.SHOW_CONTROL_TOOLTIPS));
		addGroups("tip", mans, setShowTip);

		tviewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				remove.setEnabled(true);
			}
		});

		viewer.getViewer().getControl().setMenu(rightClick.createContextMenu(viewer.getViewer().getControl()));

	}
	
	private void addGroups(String id, List<IContributionManager> managers, IAction... actions) {
		for (IContributionManager man : managers) addGroup(id, man, actions);
	}
	private void addGroup(String id, IContributionManager manager, IAction... actions) {
		manager.add(new Separator(id));
		for (IAction action : actions) {
			manager.add(action);
		}
	}

	public void setSearchVisible(boolean b) {
		GridUtils.setVisible(viewer.getFilterControl().getParent(), b);
		viewer.layout(new Control[]{viewer.getFilterControl().getParent()});
	}

	public ISelectionProvider getSelectionProvider() {
		return viewer.getViewer();
	}

	public void setFocus() {
		if (!viewer.getViewer().getTree().isDisposed()) viewer.getViewer().getTree().setFocus();
	}
	
	public void edit(INamedNode node, int index) {
     	refresh();
    	viewer.getViewer().editElement(node, index);
	}

	public void refresh() {
		viewer.getViewer().refresh();
		viewer.getViewer().expandAll();
	}

	public INamedNode getSelection() {
		final ISelection selection = viewer.getViewer().getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection)selection;
			return (INamedNode)ssel.getFirstElement();
		}
		return null;
	}
	
	public void setSelection(INamedNode node) {
		final IStructuredSelection sel = new StructuredSelection(node);
		viewer.getViewer().setSelection(sel);
	}


	static void setItemHeight(Tree tree, int height) {
		try {
			Method method = null;
			
			Method[] methods = tree.getClass().getDeclaredMethods();
			method = findMethod(methods, "setItemHeight", 1); //$NON-NLS-1$
			if (method != null) {
				boolean accessible = method.isAccessible();
				method.setAccessible(true);
				method.invoke(tree, Integer.valueOf(height));
				method.setAccessible(accessible);
			}
		} catch (SecurityException e) {
			// ignore
		} catch (IllegalArgumentException e) {
			// ignore
		} catch (IllegalAccessException e) {
			// ignore
		} catch (InvocationTargetException e) {
			// ignore
		}
	}
	/**
	 * Finds the method with the given name and parameter count from the specified methods.
	 * @param methods the methods to search through
	 * @param name the name of the method to find
	 * @param parameterCount the count of parameters of the method to find
	 * @return the method or <code>null</code> if not found
	 */
	private static Method findMethod(Method[] methods, String name, int parameterCount) {
		for (Method method : methods) {
			if (method.getName().equals(name) && method.getParameterTypes().length == parameterCount) {
				return method;
			}
		}
		return null;
	}
	
	private boolean editNode = false; // Can be set to true when UI wants to edit
	public boolean isEditNode() {
		return editNode;
	}

	public void setEditNode(boolean editNode) {
		this.editNode = editNode;
	}

	ColumnViewer getViewer() {
		return viewer.getViewer();
	}

	public ControlTree getControlTree() {
		return (ControlTree)viewer.getViewer().getInput();
	}

	public void setControlTree(ControlTree controlTree) {
		viewer.getViewer().setInput(controlTree);
	}

	public void dispose() {
		try {
			if (subscriber!=null) subscriber.disconnect();
		} catch (EventException e) {
			logger.error("Unable to disconnect subscriber in "+getClass().getSimpleName());
		}
	}


	/**
	 * Clears all the current listeners and registers all the new ones.
	 * 
	 * @throws EventException
	 * @throws URISyntaxException 
	 */
	private void registerAll() throws EventException, URISyntaxException {
		
		if (subscriber!=null) subscriber.disconnect();
		
	    IEventService eservice  = ServiceHolder.getEventService();
	    this.subscriber = eservice.createSubscriber(new URI(Activator.getJmsUri()), EventConstants.POSITION_TOPIC);

	    Iterator<INamedNode> it = getControlTree().iterator();
	    while(it.hasNext()) register(it.next());
	}

	private void register(final INamedNode node) throws EventException {
		if (node instanceof ControlNode) {
			subscriber.addListener(node.getName(), new ILocationListener() {
				@Override
				public void locationPerformed(LocationEvent evt) {
					if (evt.getLocation().getType()==LocationType.positionChanged) {
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								if (viewer.getViewer().isCellEditorActive()) {
									final Object sel = viewer.getViewer().getStructuredSelection().getFirstElement(); // editing object
									if (sel!=node) viewer.getViewer().update(node, new String[]{"Value"});
								} else {
								    viewer.getViewer().refresh(node);
								}
							}
						});
					}
				}
			});
		}
	}

}
