package org.eclipse.scanning.device.ui.device;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.richbeans.widgets.internal.GridUtils;
import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.ISpringParser;
import org.eclipse.scanning.api.scan.ui.ControlFactory;
import org.eclipse.scanning.device.ui.Activator;
import org.eclipse.scanning.device.ui.DevicePreferenceConstants;
import org.eclipse.scanning.device.ui.ServiceHolder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlView extends ViewPart {
	
	private static final Logger logger = LoggerFactory.getLogger(ControlView.class);

	public static final String ID = "org.eclipse.scanning.device.ui.device.ControlView"; //$NON-NLS-1$
	
	private FilteredTree viewer;

	public ControlView() {
		
		Activator.getDefault().getPreferenceStore().setDefault(DevicePreferenceConstants.SHOW_CONTROL_TOOLTIPS, true);
		
		if (ControlFactory.getInstance().isEmpty()) {
			// We ensure that the xml is parsed, if any
			// Hopefully this has already been done by
			// the client spring xml configuration but
			// if not we check if there is an xml argument
			// here and attempt to load its path.
			// This step is done for testing and to make
			// the example client work.
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
		tviewer.setInput(ControlFactory.getInstance());
		tviewer.expandAll();
		
		getSite().setSelectionProvider(tviewer);
		createActions();
		setSearchVisible(false);
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
				return ((INameable)element).getName();
			}
		});
		
		var   = new TreeViewerColumn(viewer, SWT.LEFT, 1);
		var.getColumn().setText("Value");
		var.getColumn().setWidth(300);
		var.setLabelProvider(new DelegatingStyledCellLabelProvider(new ControlValueLabelProvider()));
		var.setEditingSupport(new ControlEditingSupport(viewer));

	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		
		IMenuManager    menuManager    = getViewSite().getActionBars().getMenuManager();
		IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
		
		IAction expandAll = new Action("Expand All", Activator.getImageDescriptor("icons/expand_all.png")) {
			public void run() {
				viewer.getViewer().expandAll();
			}
		};
		toolbarManager.add(expandAll);
		menuManager.add(expandAll);
		
		IAction showSearch = new Action("Expand All", IAction.AS_CHECK_BOX) {
			public void run() {
				setSearchVisible(isChecked());
			}
		};
		showSearch.setImageDescriptor(Activator.getImageDescriptor("icons/magnifier--pencil.png"));
		toolbarManager.add(showSearch);
		menuManager.add(showSearch);
	
		IAction setShowTip = new Action("Show tooltip on edit", IAction.AS_CHECK_BOX) {
			public void run() {
				Activator.getDefault().getPreferenceStore().setValue(DevicePreferenceConstants.SHOW_CONTROL_TOOLTIPS, isChecked());
			}
		};
		setShowTip.setChecked(Activator.getDefault().getPreferenceStore().getBoolean(DevicePreferenceConstants.SHOW_CONTROL_TOOLTIPS));
		menuManager.add(new Separator());
		menuManager.add(setShowTip);
		
	}
	
	@Override
	public void setFocus() {
		// Set the focus
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
}
