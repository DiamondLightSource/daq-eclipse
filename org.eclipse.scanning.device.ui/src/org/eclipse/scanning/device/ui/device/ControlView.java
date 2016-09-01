package org.eclipse.scanning.device.ui.device;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.EventListener;
import java.util.Iterator;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.richbeans.widgets.internal.GridUtils;
import org.eclipse.scanning.api.INamedNode;
import org.eclipse.scanning.api.ISpringParser;
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
import org.eclipse.ui.IMemento;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlView extends ViewPart {
	
	private static final Logger logger = LoggerFactory.getLogger(ControlView.class);

	public static final String ID = "org.eclipse.scanning.device.ui.device.ControlView"; //$NON-NLS-1$
	
	private FilteredTree viewer;

	private ISubscriber<EventListener> subscriber;

	public ControlView() {
		Activator.getDefault().getPreferenceStore().setDefault(DevicePreferenceConstants.SHOW_CONTROL_TOOLTIPS, true);
		if (ControlTree.getInstance()==null || ControlTree.getInstance().isEmpty()) createFactory(false);
	}

	private void createFactory(boolean resetDefault) {
		try {
			// See if we stashed the previous control factory, use
			// that if we did, otherwise see if the client had a -xml argument
			if (!resetDefault && ControlTree.isStashed()) {
				ControlTree.unstash(ServiceHolder.getEventConnectorService()); // Loads from stash
			} else {
			    parseDefaultXML(); // Spring reread
			}
		} catch (Exception ne) {
			logger.error("Problem reading control factory!", ne);
		}
	}
	
	@Override
    public void saveState(IMemento memento) {
    	super.saveState(memento);
    	try {
			ControlTree.getInstance().stash(ServiceHolder.getEventConnectorService());
		} catch (Exception e) {
			logger.error("Problem stashing control factory!", e);
		}
    }

	/** 
	 * We ensure that the xml is parsed, if any
	 * Hopefully this has already been done by
	 * the client spring xml configuration but
	 * if not we check if there is an xml argument
	 * here and attempt to load its path.
	 * This step is done for testing and to make
	 * the example client work. 
	 **/
	private void parseDefaultXML() {
		String[] args = Platform.getApplicationArgs();
		for (int i = 0; i < args.length; i++) {
			final String arg = args[i];
			if (arg.equals("-xml")) {
				String path = args[i+1];
				ISpringParser parser = ServiceHolder.getSpringParser();
				try {
					parser.parse(path);
				} catch (Exception e) {
					logger.error("Unabled to parse: "+path, e);
				}
				break;
			}
		}
	}

	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		
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
		tviewer.setInput(ControlTree.getInstance());
		tviewer.expandAll();
		
		getSite().setSelectionProvider(tviewer);
		createActions(tviewer);
		setSearchVisible(false);
		
		try {
		    registerAll();
		} catch (Exception ne) {
			logger.error("Cannot listen to motor values changing...");
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

	    Iterator<INamedNode> it = ControlTree.getInstance().iterator();
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

	private void setSearchVisible(boolean b) {
		GridUtils.setVisible(viewer.getFilterControl().getParent(), b);
		viewer.layout(new Control[]{viewer.getFilterControl().getParent()});
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
		var.setLabelProvider(new DelegatingStyledCellLabelProvider(new ControlValueLabelProvider()));
		var.setEditingSupport(new ControlEditingSupport(viewer));

	}

	/**
	 * Create the actions.
	 */
	private void createActions(final TreeViewer tviewer) {
		
		IMenuManager    menuManager    = getViewSite().getActionBars().getMenuManager();
		IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
		MenuManager     rightClick     = new MenuManager();
		
		final IAction addGroup = new Action("Add group", Activator.getImageDescriptor("icons/ui-toolbar--purpleplus.png")) {
			public void run() {
                INamedNode nnode = ControlTree.getInstance().insert(ControlTree.getInstance(), new ControlGroup());
                edit(nnode, 0);
			}
		};
		
		final IAction addNode = new Action("Add control", Activator.getImageDescriptor("icons/ui-toolbar--plus.png")) {
			public void run() {
                INamedNode     node    = getSelection();
                ControlTree factory = ControlTree.getInstance();
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
                ControlTree factory = ControlTree.getInstance();
			    INamedNode parent = factory.getNode(node.getParentName());
                if (node.getChildren()==null || node.getChildren().length<1) {
                	factory.delete(node);
                } else {
                	boolean ok = MessageDialog.openQuestion(getViewSite().getShell(), "Confirm Delete", "The item '"+node.getName()+"' is a group.\n\nAre you sure you would like to delete it?");
                	if (ok) factory.delete(node);
                }
                subscriber.removeListeners(node.getName());
                viewer.getViewer().refresh();
                setSelection(parent);
			}
		};
		remove.setEnabled(false);
			
		addGroup("add", toolbarManager, addGroup, addNode, remove);
		addGroup("add", menuManager, addGroup, addNode, remove);
		addGroup("add", rightClick, addGroup, addNode, remove);

		
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
		
		addGroup("refresh", toolbarManager, expandAll, showSearch, edit);
		addGroup("refresh", menuManager, expandAll, showSearch, edit);
		addGroup("refresh", rightClick, expandAll, showSearch, edit);
	
		IAction resetAll = new Action("Reset all controls to default", Activator.getImageDescriptor("icons/arrow-return-180-left.png")) {
			public void run() {
				boolean ok = MessageDialog.openConfirm(getViewSite().getShell(), "Confirm Reset Controls", "Are you sure that you want to reset all controls to default?");
				if (!ok) return;
				createFactory(false);
				viewer.getViewer().setInput(ControlTree.getInstance());
				expandAll.run();
			}
		};
		addGroup("reset", toolbarManager, resetAll);
		addGroup("reset", menuManager, resetAll);
		addGroup("reset", rightClick, resetAll);
				
		IAction setShowTip = new Action("Show tooltip on edit", IAction.AS_CHECK_BOX) {
			public void run() {
				Activator.getDefault().getPreferenceStore().setValue(DevicePreferenceConstants.SHOW_CONTROL_TOOLTIPS, isChecked());
			}
		};
		setShowTip.setChecked(Activator.getDefault().getPreferenceStore().getBoolean(DevicePreferenceConstants.SHOW_CONTROL_TOOLTIPS));
		menuManager.add(new Separator("tip"));
		menuManager.add(setShowTip);

		tviewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				remove.setEnabled(true);
			}
		});

		viewer.getViewer().getControl().setMenu(rightClick.createContextMenu(viewer.getViewer().getControl()));

	}
	
	private void addGroup(String id, IContributionManager manager, IAction... actions) {
		manager.add(new Separator(id));
		for (IAction action : actions) {
			manager.add(action);
		}
	}
	
	protected void edit(INamedNode node, int index) {
     	refresh();
    	viewer.getViewer().editElement(node, index);
	}

	protected void refresh() {
		viewer.getViewer().refresh();
		viewer.getViewer().expandAll();
	}

	protected INamedNode getSelection() {
		final ISelection selection = viewer.getViewer().getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection)selection;
			return (INamedNode)ssel.getFirstElement();
		}
		return null;
	}
	protected void setSelection(INamedNode node) {
		final IStructuredSelection sel = new StructuredSelection(node);
		viewer.getViewer().setSelection(sel);
	}

	@Override
	public void setFocus() {
		if (!viewer.isDisposed()) viewer.getViewer().getTree().setFocus();
	}
	
	@Override
	public void dispose() {
		super.dispose();
		try {
			if (subscriber!=null) subscriber.disconnect();
		} catch (EventException e) {
			logger.error("Unable to disconnect subscriber in "+getClass().getSimpleName());
		}
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

}
