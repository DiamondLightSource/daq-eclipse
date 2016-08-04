package org.eclipse.scanning.device.ui.points;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
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
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.device.ui.Activator;
import org.eclipse.scanning.device.ui.ServiceHolder;
import org.eclipse.scanning.device.ui.util.MarginUtils;
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
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.part.ViewPart;

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
	
	// Thankyou OSGi
	private IPointGeneratorService pservice;
	private IEventService          eservice;

	private SeriesTable  seriesTable;
	private GeneratorFilter pointsFilter;

	private List<GeneratorDescriptor> saved;
	
	public ScanView() {
		this.pservice     = ServiceHolder.getGeneratorService();
		this.eservice     = ServiceHolder.getEventService();
		this.seriesTable  = new SeriesTable();
		this.pointsFilter = new GeneratorFilter(pservice);
	}
	
	@Override
    public void init(IViewSite site, IMemento memento) throws PartInitException {
		
		super.init(site, memento);

		final String key = memento!=null ? memento.getString(GeneratorConstants.GENERATOR_IDS) : null;
        if (key!=null && !"".equals(key)) {
			List<String> ids = getList(key);
			try {
				this.saved = pointsFilter.createDescriptors(ids);
			} catch (GeneratorException e) {
				e.printStackTrace();
			}
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
		
		createToobarActions(site);
		final MenuManager rightClick = new MenuManager("#PopupMenu");
		rightClick.setRemoveAllWhenShown(true);
		//createActions(rightClick);
		rightClick.addMenuListener(new IMenuListener() {
			
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				setDynamicMenuOptions(manager);
			}
		});
		createColumns();
		
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
							readScans(file.getLocation().toOSString(), site);
							return;
						}
					}
				} else if (dropData instanceof String[]) {
					for (String path : (String[])dropData){
						readScans(path, site);
						return;
					}
				}
				
			}
		});

	}
	
	private void createColumns() {
		
		// Do we need extra columns for the scans?
		
	}

	@Override
	public Object getAdapter(Class clazz) {
		if (clazz==IPointGenerator.class) {
			return getGenerators();
		}
		return null;
	}

	private String lastPath = null;
	private IAction add;
	private IAction delete;
	private IAction clear;
	
	private final static String[] extensions = new String[]{"scan"};
	private final static String[] files = new String[]{"Scan files"};

	private void createToobarActions(final IViewSite site) {
		
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
		
		site.getActionBars().getToolBarManager().add(add);
		site.getActionBars().getMenuManager().add(add);
		site.getActionBars().getToolBarManager().add(delete);
		site.getActionBars().getMenuManager().add(delete);
		site.getActionBars().getToolBarManager().add(clear);
		site.getActionBars().getMenuManager().add(clear);
		site.getActionBars().getToolBarManager().add(new Separator());
		site.getActionBars().getMenuManager().add(new Separator());
		
		final IAction save = new Action("Save configured scan", IAction.AS_PUSH_BUTTON) {
			public void run() {
				
				IPointGenerator<?>[] gens = getGenerators();
				
				if (gens == null) return;
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
				saveOperationsToFile(path, gens, site);
				lastPath = path;
			}
		};
		
		final IAction load = new Action("Load configured pipeline", IAction.AS_PUSH_BUTTON) {
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
				readScans(path, site);
				lastPath = path;
			}
		};
		save.setImageDescriptor(Activator.getImageDescriptor("icons/mask-import-wiz.png"));
		load.setImageDescriptor(Activator.getImageDescriptor("icons/mask-export-wiz.png"));
	
		site.getActionBars().getToolBarManager().add(save);
		site.getActionBars().getMenuManager().add(save);
		site.getActionBars().getToolBarManager().add(load);
		site.getActionBars().getMenuManager().add(load);
		
		site.getActionBars().getToolBarManager().add(new Separator());
		site.getActionBars().getMenuManager().add(new Separator());
		
		final IAction lock = new Action("Lock scan editing", IAction.AS_CHECK_BOX) {
			public void run() {
				Activator.getDefault().getPreferenceStore().setValue(GeneratorConstants.LOCK_PIPELINE, isChecked());
				seriesTable.setLockEditing(isChecked());
				add.setEnabled(!isChecked());
				delete.setEnabled(!isChecked());
				clear.setEnabled(!isChecked());
			}
		};
		lock.setImageDescriptor(Activator.getImageDescriptor("icons/lock.png"));

		lock.setChecked(Activator.getDefault().getPreferenceStore().getBoolean(GeneratorConstants.LOCK_PIPELINE));
		add.setEnabled(!lock.isChecked());
		delete.setEnabled(!lock.isChecked());
		clear.setEnabled(!lock.isChecked());
		seriesTable.setLockEditing(lock.isChecked());
		
		site.getActionBars().getToolBarManager().add(lock);
		site.getActionBars().getMenuManager().add(lock);

	}
	
	private void saveOperationsToFile(String filename, IPointGenerator[] gens, IViewSite site) {
		try {
			
			if (new File(filename).exists()) {
				MessageDialog.openInformation(site.getShell(), "File error", "Overwriting of processing information forbidden, please select a new file.");
				return;
			}
			
			System.out.println("TODO Create a way of persisting scans to file!");
		} catch (Exception e) {
			MessageDialog.openInformation(site.getShell(), "Exception while writing scans to file", "An exception occurred while writing the scans to a file.\n" + e.getMessage());
		}
	}
	private void readScans(String filename, IViewSite site) {
		try {
			System.out.println("TODO Create way of reading scans from file!");
			
		} catch (Exception e) {
			MessageDialog.openInformation(site.getShell(), "Exception while reading scans from file", "An exception occurred while reading scans from a file.\n" + e.getMessage());
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

	@Override
    public void saveState(IMemento memento) {
    	memento.putString(GeneratorConstants.GENERATOR_IDS, createIdList(seriesTable.getSeriesItems()));
    }

	private String createIdList(Collection<ISeriesItemDescriptor> seriesItems) {
		if (seriesItems==null || seriesItems.isEmpty()) return null;
		final StringBuilder buf = new StringBuilder();
		for (Iterator<ISeriesItemDescriptor> iterator = seriesItems.iterator(); iterator.hasNext();) {
			ISeriesItemDescriptor des = iterator.next();
			if (!(des instanceof GeneratorDescriptor)) continue;
			GeneratorDescriptor  odes = (GeneratorDescriptor)des;
			buf.append(odes.getId());
			if(iterator.hasNext()) buf.append(",");
		}
		return buf.toString();
	}

	/**
	 * 
	 * @param value
	 * @return v
	 */
	public static List<String> getList(final String value) {
		if (value == null)           return null;
		if ("".equals(value.trim())) return null;
		final String[]    vals = value.split(",");
		final List<String> ret = new ArrayList<String>(vals.length);
		for (int i = 0; i < vals.length; i++) ret.add(vals[i].trim());
		return ret;
	}

}
