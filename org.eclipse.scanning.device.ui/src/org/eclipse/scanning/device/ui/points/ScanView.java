package org.eclipse.scanning.device.ui.points;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
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
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.richbeans.widgets.file.FileSelectionDialog;
import org.eclipse.richbeans.widgets.table.ISeriesItemDescriptor;
import org.eclipse.richbeans.widgets.table.SeriesTable;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.device.ui.Activator;
import org.eclipse.scanning.device.ui.DevicePreferenceConstants;
import org.eclipse.scanning.device.ui.ServiceHolder;
import org.eclipse.scanning.device.ui.model.ModelView;
import org.eclipse.scanning.device.ui.util.MarginUtils;
import org.eclipse.scanning.device.ui.util.PageUtil;
import org.eclipse.scanning.device.ui.util.Stashing;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
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
public class ScanView  extends ViewPart {
	
	public static final String ID = "org.eclipse.scanning.device.ui.scanEditor";
	
	private static final Logger logger = LoggerFactory.getLogger(ScanView.class);
	
	// Thankyou OSGi
	private IPointGeneratorService pservice;
	private IEventService          eservice;

	private SeriesTable  seriesTable;
	private GeneratorFilter pointsFilter;

	private List<GeneratorDescriptor<?>> saved;

	
	public ScanView() {
		this.pservice     = ServiceHolder.getGeneratorService();
		this.eservice     = ServiceHolder.getEventService();
		this.seriesTable  = new SeriesTable();
		this.pointsFilter = new GeneratorFilter(pservice, eservice.getEventConnectorService(), seriesTable);
	}
	
	@Override
    public void init(IViewSite site, IMemento memento) throws PartInitException {
		
		super.init(site, memento);

		final String key = memento!=null ? memento.getString(GeneratorConstants.GENERATOR_IDS) : null;
        if (key!=null && !"".equals(key)) {
			try {
				this.saved = pointsFilter.createDescriptors(key);
			} catch (Exception e) {
				logger.error("Cannot load generators to memento!", e);
			}
		}
	}
	
	@Override
    public void saveState(IMemento memento) {
		try {
			final String json = pointsFilter.createKey(seriesTable.getSeriesItems());
	    	memento.putString(GeneratorConstants.GENERATOR_IDS, json);
		} catch (Exception ne) {
			logger.error("Cannot save generators to memento!", ne);
		}
    }
    
	@Override
	public void createPartControl(Composite parent) {
		
		final Composite content = new Composite(parent, SWT.NONE);
		content.setLayout(new GridLayout(1, false));
		MarginUtils.removeMargins(content);
		
		final GeneratorLabelProvider prov = new GeneratorLabelProvider(0);
		seriesTable.createControl(content, prov);
		
		final IViewSite site = getViewSite();
		site.setSelectionProvider(seriesTable.getSelectionProvider());
		
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
		
	}

	@Override
	public Object getAdapter(Class clazz) {
		
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
				Activator.getDefault().getPreferenceStore().setValue(DevicePreferenceConstants.LOCK_SCAN_SEQUENCE, isChecked());
				seriesTable.setLockEditing(isChecked());
				add.setEnabled(!isChecked());
				delete.setEnabled(!isChecked());
				clear.setEnabled(!isChecked());
			}
		};
		lock.setImageDescriptor(Activator.getImageDescriptor("icons/lock.png"));

		lock.setChecked(Activator.getDefault().getPreferenceStore().getBoolean(DevicePreferenceConstants.LOCK_SCAN_SEQUENCE));
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
		seriesTable.dispose();
    }
	
	@Override
	public void setFocus() {
		seriesTable.setFocus();
	}

}
