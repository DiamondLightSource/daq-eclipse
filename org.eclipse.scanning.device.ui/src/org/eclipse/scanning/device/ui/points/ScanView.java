package org.eclipse.scanning.device.ui.points;

import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.richbeans.widgets.file.FileSelectionDialog;
import org.eclipse.richbeans.widgets.internal.GridUtils;
import org.eclipse.richbeans.widgets.table.ISeriesItemDescriptor;
import org.eclipse.richbeans.widgets.table.SeriesTable;
import org.eclipse.richbeans.widgets.table.event.SeriesItemEvent;
import org.eclipse.richbeans.widgets.table.event.SeriesItemListener;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IBoundingBoxModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.scan.ui.ControlTree;
import org.eclipse.scanning.device.ui.Activator;
import org.eclipse.scanning.device.ui.DevicePreferenceConstants;
import org.eclipse.scanning.device.ui.ServiceHolder;
import org.eclipse.scanning.device.ui.device.ControlTreeUtils;
import org.eclipse.scanning.device.ui.model.ModelView;
import org.eclipse.scanning.device.ui.util.PageUtil;
import org.eclipse.scanning.device.ui.util.PlotUtil;
import org.eclipse.scanning.device.ui.util.ScanRegions;
import org.eclipse.scanning.device.ui.util.Stashing;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This view allows users to build up arbitrary scans
 * and run them.
 * 
 * @author Matthew Gerring
 * 
 * TODO Convert to e4 view.
 *
 */
public class ScanView  extends ViewPart implements SeriesItemListener {
	
	public static final String ID = "org.eclipse.scanning.device.ui.scanEditor";
	
	private static final Logger logger = LoggerFactory.getLogger(ScanView.class);
	
	// Services
	private IPointGeneratorService pservice;
	private IEventService          eservice;

	// UI
	private SeriesTable  seriesTable;
	private GeneratorFilter pointsFilter;

	// Data
	private List<GeneratorDescriptor<?>> saved;
	private ControlTree startTree, endTree;

	// File
	private Stashing stash;

	// Preferences
	private IPreferenceStore store;

	
	public ScanView() {
		
		this.pservice     = ServiceHolder.getGeneratorService();
		this.eservice     = ServiceHolder.getEventService();
		this.seriesTable  = new SeriesTable();
		this.pointsFilter = new GeneratorFilter(pservice, eservice.getEventConnectorService(), seriesTable);
		this.stash = new Stashing("org.eclipse.scanning.device.ui.scan.models.json", ServiceHolder.getEventConnectorService());

		this.store        = Activator.getDefault().getPreferenceStore();
		store.setDefault(DevicePreferenceConstants.START_POSITION, false);
		store.setDefault(DevicePreferenceConstants.END_POSITION, false);
		store.setDefault(DevicePreferenceConstants.SHOW_CONTROL_TOOLTIPS, true);
	}
	
	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {

		super.init(site, memento);

		if (stash.isStashed()) {
			try {
				final List<IScanPathModel> models = stash.unstash(List.class);
				this.saved = pointsFilter.createDescriptors(models);
			} catch (Exception e) {
				logger.error("Cannot load generators to memento!", e);
			}
		}
	}
	
	@Override
    public void saveState(IMemento memento) {
		try {
			final List<Object> models = pointsFilter.getModels(seriesTable.getSeriesItems());
	    	stash.stash(models);
	    	
			Stashing tstash = new Stashing(DevicePreferenceConstants.START_POSITION+".json", ServiceHolder.getEventConnectorService());
			tstash.stash(startTree);
			tstash = new Stashing(DevicePreferenceConstants.END_POSITION+".json", ServiceHolder.getEventConnectorService());
			tstash.stash(endTree);

		} catch (Exception ne) {
			logger.error("Cannot save generators to memento!", ne);
		}
    }
    
	@Override
	public void createPartControl(Composite parent) {
		
		final Composite content = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		content.setLayout(layout);
		GridUtils.removeMargins(content);
		layout.marginTop        = 10;
	
		Composite startButton = createPositionButton(content, DevicePreferenceConstants.START_POSITION, "Start Position", "icons/position-start.png");
		
		final GeneratorLabelProvider prov = new GeneratorLabelProvider(0);
		seriesTable.createControl(content, prov, SWT.FULL_SELECTION | SWT.SINGLE);
		seriesTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		seriesTable.setHeaderVisible(false);
		
		Composite endButton = createPositionButton(content, DevicePreferenceConstants.END_POSITION, "End Position", "icons/position-end.png");
		
		final IViewSite site = getViewSite();
		
		final DelegatingSelectionProvider selectionProvider = new DelegatingSelectionProvider(seriesTable.getSelectionProvider());
		site.setSelectionProvider(selectionProvider);
		
		this.startTree = createControlTree(DevicePreferenceConstants.START_POSITION, "Start Position");
		this.endTree   = createControlTree(DevicePreferenceConstants.END_POSITION, "End Position");

        createListeners(startButton, endButton, DevicePreferenceConstants.START_POSITION, selectionProvider, startTree);
        createListeners(endButton, startButton, DevicePreferenceConstants.END_POSITION, selectionProvider, endTree);
		
		createActions(site);
		final MenuManager rightClick = new MenuManager("#PopupMenu");
		rightClick.setRemoveAllWhenShown(true);
		//createActions(rightClick);
		rightClick.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				setDynamicMenuOptions(manager);
			}
		});
		
		// Here's the data, lets show it
		seriesTable.setMenuManager(rightClick);
		seriesTable.setInput(saved, pointsFilter);
		
		DropTarget dt = seriesTable.getDropTarget();
		dt.setTransfer(new Transfer[] { TextTransfer.getInstance(),
				FileTransfer.getInstance(), ResourceTransfer.getInstance(),
				LocalSelectionTransfer.getTransfer() });
		dt.addDropListener(new DropTargetAdapter() {
			
			@Override
			public void drop(DropTargetEvent event) {
				Object dropData = event.data;
				if (dropData instanceof TreeSelection) {
					TreeSelection selectedNode = (TreeSelection) dropData;
					Object obj[] = selectedNode.toArray();
					for (int i = 0; i < obj.length; i++) {
						if (obj[i] instanceof IFile) {
							IFile file = (IFile) obj[i];
							readScans(file.getLocation().toOSString());
							return;
						}
					}
				} else if (dropData instanceof String[]) {
					for (String path : (String[])dropData){
						readScans(path);
						return;
					}
				}
				
			}
		});
		
		// Try to ensure that the model view and regions view are initialized
		IViewReference ref = PageUtil.getPage().findViewReference(ScanRegionView.ID);
		if (ref!=null) ref.getView(true);
		
		ref = PageUtil.getPage().findViewReference(ModelView.ID);
		if (ref!=null) ref.getView(true);
		
		seriesTable.addSeriesEventListener(this);
		
		final List<ISeriesItemDescriptor> desi = seriesTable.getSeriesItems();
        if (desi!=null && desi.size()>0) seriesTable.setSelection(desi.get(desi.size()-1));
      
	}
	

	private ControlTree createControlTree(String id, String name) {
		
		// TODO FIXME The default control tree for the start and end positions should have their own definitions
		// or the ability to create them. This code remembers what the user sets for start/end but
		// the initial fields simply come from the same as the ControlView ones.
		Stashing stash = new Stashing(id+".json", ServiceHolder.getEventConnectorService());
		
		ControlTree tree = null;
		try {
			if (stash.isStashed()) tree = stash.unstash(ControlTree.class);
		} catch (Exception ne) {
			logger.warn("Getting tree from "+stash, ne);
			tree = null;
		}
		if (tree == null) {
			tree = ControlTreeUtils.parseDefaultXML();
			try {
				tree = ControlTreeUtils.clone(tree);
			} catch (Exception e) {
				logger.warn("Getting tree from default XML", e);
			}
		}
		
		if (tree==null) return null;
		tree.setName(id);
		tree.setDisplayName(name);
		tree.build();
		return tree;
	}

	private void createListeners(Composite position, Composite otherPosition, String propName, DelegatingSelectionProvider prov, ControlTree tree) {
		
		position.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				setPositionSelected(position, otherPosition, prov, tree);
			}
		});	
		
		store.addPropertyChangeListener(new IPropertyChangeListener() {		
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (position.isDisposed()) {
					store.removePropertyChangeListener(this);
					return;
				}
				if (!event.getProperty().equals(propName)) return;
				boolean show = store.getBoolean(propName);
				GridUtils.setVisible(position, show);
				position.getParent().layout(new Control[]{position});
				
				if (show) {
					setPositionSelected(position, otherPosition, prov, tree);
				}
			}
		});

	}

	protected void setPositionSelected(Composite position, Composite otherPosition, DelegatingSelectionProvider prov, ControlTree tree) {
		
		position.setFocus();
		seriesTable.deselectAll();
		position.setBackground(position.getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND));
		otherPosition.setBackground(position.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		
		seriesTable.addSelectionListener(new ISelectionChangedListener() {	
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				position.setBackground(position.getDisplay().getSystemColor(SWT.COLOR_WHITE));
				seriesTable.removeSelectionListener(this);
			}
		});
		
		prov.fireSelection(new StructuredSelection(tree));
	}

	private Composite createPositionButton(final Composite content, final String propName, String label, String iconPath) {
		
		final CLabel position = new CLabel(content, SWT.LEFT);
		position.setBackground(content.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		position.setImage(Activator.getImageDescriptor(iconPath).createImage());
		position.setText(label);
		position.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		GridUtils.setVisible(position, store.getBoolean(propName));
			
		return position;
	}

	@Override
	public Object getAdapter(Class clazz) {
		
		if (CompoundModel.class == clazz) return new CompoundModel(getModels());
		if (clazz==IScanPathModel.class) {
			ISeriesItemDescriptor selected = seriesTable.getSelected();
			if (!(selected instanceof GeneratorDescriptor)) return null;
			return ((GeneratorDescriptor)selected).getModel();
		} else if (clazz==IPointGenerator.class || clazz==IPointGenerator[].class) {
			return getGenerators();
		}else if (clazz==Object[].class) {
			return getModels();
		}else if (clazz==List.class) {
			return getModels();
		}
		return null;
	}

	private IAction add;
	private IAction delete;
	private IAction clear;
	
	private String lastPath = null;
	private final static String[] extensions = new String[]{"json", "*.*"};
	private final static String[] files = new String[]{"Scan files (json)", "All Files"};

	private void createActions(final IViewSite site) {
		
		
		IToolBarManager tmanager = site.getActionBars().getToolBarManager();
		IMenuManager    mmanager = site.getActionBars().getMenuManager();
		
		IAction start = new Action("Set start position\nThis is the position before a scan", IAction.AS_CHECK_BOX) {
			public void run() {
				store.setValue(DevicePreferenceConstants.START_POSITION, isChecked());
			}
		};
		start.setChecked(store.getBoolean(DevicePreferenceConstants.START_POSITION));
		start.setImageDescriptor(Activator.getImageDescriptor("icons/position-start.png"));
		
		IAction end = new Action("Set end position\nThe position after a scan", IAction.AS_CHECK_BOX) {
			public void run() {
				store.setValue(DevicePreferenceConstants.END_POSITION, isChecked());
			}
		};
		end.setChecked(store.getBoolean(DevicePreferenceConstants.END_POSITION));
		end.setImageDescriptor(Activator.getImageDescriptor("icons/position-end.png"));
       
		addGroup("location", tmanager, start, end);
		addGroup("location", mmanager, start, end);
		
		add = new Action("Insert", Activator.getImageDescriptor("icons/clipboard-list.png")) {
			public void run() {
				seriesTable.addNew();
			}
		};

		delete = new Action("Delete", Activator.getImageDescriptor("icons/clipboard--minus.png")) {
			public void run() {
				seriesTable.delete();
			}
		};

		clear = new Action("Clear", Activator.getImageDescriptor("icons/clipboard-empty.png")) {
			public void run() {
			    boolean ok = MessageDialog.openQuestion(site.getShell(), "Confirm Clear Scan", "Do you want to clear the scan?");
			    if (!ok) return;
				seriesTable.clear();
			}
		};
		
		addGroup("manage", tmanager, add, delete, clear);
		addGroup("manage", mmanager, add, delete, clear);
		
		final IAction save = new Action("Save scan", IAction.AS_PUSH_BUTTON) {
			public void run() {
				
				List<IScanPathModel> models = getModels();
				
				if (models == null) return;
				FileSelectionDialog dialog = new FileSelectionDialog(site.getShell());
				if (lastPath != null) dialog.setPath(lastPath);
				dialog.setExtensions(extensions);
				dialog.setNewFile(true);
				dialog.setFolderSelector(false);
				
				dialog.create();
				if (dialog.open() == Dialog.CANCEL) return;
				String path = dialog.getPath();
				if (!path.endsWith(extensions[0])) { //pipeline should always be saved to .nxs
					path = path.concat("." + extensions[0]);
				}
				saveScans(path, models);
				lastPath = path;
			}
		};
		
		final IAction load = new Action("Load scan", IAction.AS_PUSH_BUTTON) {
			public void run() {
				
				FileSelectionDialog dialog = new FileSelectionDialog(site.getShell());
				dialog.setExtensions(extensions);
				dialog.setFiles(files);
				dialog.setNewFile(false);
				dialog.setFolderSelector(false);
				if (lastPath != null) dialog.setPath(lastPath);
				
				dialog.create();
				if (dialog.open() == Dialog.CANCEL) return;
				String path = dialog.getPath();
				readScans(path);
				lastPath = path;
			}
		};
		save.setImageDescriptor(Activator.getImageDescriptor("icons/mask-import-wiz.png"));
		load.setImageDescriptor(Activator.getImageDescriptor("icons/mask-export-wiz.png"));
	
		addGroup("file", tmanager, save, load);
		addGroup("file", mmanager, save, load);
		
		final IAction lock = new Action("Lock scan editing", IAction.AS_CHECK_BOX) {
			public void run() {
				store.setValue(DevicePreferenceConstants.LOCK_SCAN_SEQUENCE, isChecked());
				seriesTable.setLockEditing(isChecked());
				add.setEnabled(!isChecked());
				delete.setEnabled(!isChecked());
				clear.setEnabled(!isChecked());
			}
		};
		lock.setImageDescriptor(Activator.getImageDescriptor("icons/lock.png"));

		lock.setChecked(store.getBoolean(DevicePreferenceConstants.LOCK_SCAN_SEQUENCE));
		add.setEnabled(!lock.isChecked());
		delete.setEnabled(!lock.isChecked());
		clear.setEnabled(!lock.isChecked());
		seriesTable.setLockEditing(lock.isChecked());
		
		addGroup("lock", tmanager, lock);
		addGroup("lock", mmanager, lock);

	}
	
	private void addGroup(String id, IContributionManager manager, IAction... actions) {
		manager.add(new Separator(id));
		for (IAction action : actions) {
			manager.add(action);
		}
	}
	
	private void saveScans(String filename, List<IScanPathModel> models) {	
		Stashing stash = new Stashing(new File(filename), ServiceHolder.getEventConnectorService());
		stash.save(models, getViewSite().getShell());
	}
	
	private void readScans(String filePath) {
		Stashing stash = new Stashing(new File(filePath), ServiceHolder.getEventConnectorService());
		List<IScanPathModel> models = stash.load(List.class, getViewSite().getShell());
		try {
			this.saved = pointsFilter.createDescriptors(models);
			this.seriesTable.setInput(saved, pointsFilter);
		} catch (Exception e) {
			logger.error("Unexpected error refreshing saved models in "+getClass().getSimpleName(), e);
		}
	}
	
	private IPointGenerator<?>[] getGenerators() {
		
		final List<ISeriesItemDescriptor> desi = seriesTable.getSeriesItems();
		
		if (desi != null) {
			Iterator<ISeriesItemDescriptor> it = desi.iterator();
			while (it.hasNext()) if ((!(it.next() instanceof GeneratorDescriptor))) it.remove();
		}
		
		if (desi==null || desi.isEmpty()) return null;
		final IPointGenerator<?>[] pipeline = new IPointGenerator<?>[desi.size()];
		for (int i = 0; i < desi.size(); i++) {
			try {
				pipeline[i] = (IPointGenerator<?>)desi.get(i).getSeriesObject();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return pipeline;
	}
	private List<IScanPathModel> getModels() {
		
		IPointGenerator<?>[] gens = getGenerators();
		List<IScanPathModel> mods = new ArrayList<>(gens.length);
		for (int i = 0; i < gens.length; i++) mods.add((IScanPathModel)gens[i].getModel());
		return mods;
	}


	private void setDynamicMenuOptions(IMenuManager mm) {
		
		mm.add(add);
		mm.add(delete);
		mm.add(clear);
		mm.add(new Separator());
		
		IPointGenerator<?> gen = null;
		
		try {
			ISeriesItemDescriptor selected = seriesTable.getSelected();
			if (!(selected instanceof GeneratorDescriptor)) return;
			gen = ((GeneratorDescriptor)selected).getSeriesObject();
		} catch (Exception e1) {
			
		}
		
		final IAction passUnMod = new Action("Enabled", IAction.AS_CHECK_BOX) {
			public void run() {
				ISeriesItemDescriptor current = seriesTable.getSelected();
				if (current instanceof GeneratorDescriptor) {
					try {
						((GeneratorDescriptor)current).getSeriesObject().setEnabled(isChecked());
						seriesTable.refreshTable();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		
		if (gen != null && !gen.isEnabled()) passUnMod.setChecked(true);
		mm.add(passUnMod);
	}


	@Override
	public void dispose() {
		if (tip!=null) tip.dispose();
		seriesTable.removeSeriesEventListener(this);
		seriesTable.dispose();
    }
	
	@Override
	public void setFocus() {
		seriesTable.setFocus();
	}

    
   	@Override
	public void itemAdded(SeriesItemEvent evt) {
   		
		final IPlottingSystem<?> system = PlotUtil.getRegionSystem();
		if (system==null) return;
		
 		if (ScanRegions.getScanRegions(system)!=null) {
 			IViewReference ref = PageUtil.getPage().findViewReference(ScanRegionView.ID);
 			String name = ref!=null ? ref.getPartName() : "regions";
            showTip("There are already scan regions defined.\nGo to '"+name+"' to edit and create others.");
 			return; // They already have some
 		}
		
		try {
			final IPointGenerator<?> generator = (IPointGenerator<?>)evt.getDescriptor().getSeriesObject();
			final Object model     = generator.getModel();
			if (model instanceof IBoundingBoxModel) {
                IRegion created = ScanRegions.createRegion(system, RegionType.BOX, system.getPlotName(), null);
                if (created!=null) showTip("Click and drag in '"+system.getPlotName()+"' to add a region for '"+generator.getLabel()+"'");
			}
		} catch (Exception e) {
			logger.error("Problem creating a plotted region!", e);
		}

	}
   	
   	private ToolTip         tip;

   	private void showTip(String message) {
		if (!store.getBoolean(DevicePreferenceConstants.SHOW_CONTROL_TOOLTIPS)) return;
        if (tip==null) this.tip = new ToolTip(seriesTable.getControl().getShell(), SWT.BALLOON);
        tip.setMessage(message);
        PointerInfo a = MouseInfo.getPointerInfo();
        java.awt.Point loc = a.getLocation();

        tip.setLocation(loc.x, loc.y+20);
        tip.setVisible(true);
   	}

	@Override
	public void itemRemoved(SeriesItemEvent evt) {
		// TODO Auto-generated method stub
		
	}

}
