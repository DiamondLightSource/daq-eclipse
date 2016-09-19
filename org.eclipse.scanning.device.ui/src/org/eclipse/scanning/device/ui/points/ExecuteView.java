package org.eclipse.scanning.device.ui.points;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.richbeans.widgets.internal.GridUtils;
import org.eclipse.scanning.api.IValidatorService;
import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.annotation.ui.FieldValue;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.ScanRegion;
import org.eclipse.scanning.device.ui.Activator;
import org.eclipse.scanning.device.ui.DevicePreferenceConstants;
import org.eclipse.scanning.device.ui.ServiceHolder;
import org.eclipse.scanning.device.ui.util.PageUtil;
import org.eclipse.scanning.device.ui.util.PlotUtil;
import org.eclipse.scanning.device.ui.util.ScanRegions;
import org.eclipse.scanning.device.ui.util.ViewUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * A view which attempts to pick up selection events
 * from building a scan and then display the scan information.
 * 
 * @author Matthew Gerring
 *
 */
public class ExecuteView extends ViewPart implements ISelectionListener {

	public static final String ID = "org.eclipse.scanning.device.ui.scan.executeView"; //$NON-NLS-1$
	private static final Logger logger = LoggerFactory.getLogger(ExecuteView.class);
	
	// UI
	private StyledText text;

	// Services
	private IPointGeneratorService pservice; // Used to create a compound generator
	private IValidatorService      vservice; // Used to validate a selection
	private IRunnableDeviceService dservice;

	// Job
	private Job updateJob;
	private Composite run;
	
	public ExecuteView() {
		
		Activator.getDefault().getPreferenceStore().setDefault(DevicePreferenceConstants.SHOW_SCAN_INFO, true);
		this.pservice = ServiceHolder.getGeneratorService();
		this.vservice = ServiceHolder.getValidatorService();
		try {
			this.dservice = ServiceHolder.getEventService().createRemoteService(new URI(Activator.getJmsUri()), IRunnableDeviceService.class);
		} catch (EventException  | URISyntaxException e) {
			logger.error("Unable to get remote device service!", e);
		}
		updateJob = new Job("Update Scna Information") {
			public IStatus run(IProgressMonitor monitor) {
				update(monitor);
				return Status.OK_STATUS;
			}
		};
		updateJob.setUser(false);
		updateJob.setSystem(true);
		updateJob.setPriority(Job.INTERACTIVE);
	}

	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		
		this.text = new StyledText(container, SWT.NONE);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		text.setBackground(text.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		GridUtils.setVisible(text, Activator.getDefault().getPreferenceStore().getBoolean(DevicePreferenceConstants.SHOW_SCAN_INFO));
		text.getParent().layout(new Control[]{text});
		
		run = new Composite(container, SWT.NONE);
		run.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		run.setLayout(new GridLayout(2, false));
		
		final Button execute = new Button(run, SWT.PUSH);
		execute.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		execute.setText("Execute");
		execute.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				execute();
			}
		});
		execute.setImage(Activator.getImageDescriptor("icons/shoe--arrow.png").createImage());
		execute.setToolTipText("Submits scan to the queue of scans to be run.");

		createActions();
		PageUtil.getPage(getSite()).addSelectionListener(this);
		
		// We force the scan view to exist. It might be the one to return the compound model
		// that we will use.
		IViewReference ref = PageUtil.getPage().findViewReference(ScanView.ID);
		if (ref!=null) ref.getView(true);

		updateJob.schedule();
	}
	
	protected void execute() {
		System.out.println("TODO!");
	}

	@Override
	public void dispose() {
		if (PageUtil.getPage()!=null) PageUtil.getPage().removeSelectionListener(this);
		super.dispose();
	}
	
	private IAdaptable modelAdaptable;
	
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			Object ob = ((IStructuredSelection)selection).getFirstElement();
			
			// This slightly funny alg or assign and sometimes update
			// is correct. Do not change unless sure that UI is working afterwards.
			if (ob instanceof IAdaptable) {
                CompoundModel<IROI> cm = ((IAdaptable)ob).getAdapter(CompoundModel.class);
                if (cm !=null) modelAdaptable = (IAdaptable)ob;
			}
			if (isUpdatableSelection(ob)) updateJob.schedule();

		}
	}

	private boolean isUpdatableSelection(Object ob) {
		if (ob == null)                        return true; // Something deleted.
		if (ob instanceof GeneratorDescriptor) return true; // Generator changed.
		if (ob instanceof FieldValue)          return true; // Model changed.
		if (ob instanceof DeviceInformation)   return true; // Device changed.
		if (ob instanceof ScanRegion)          return true; // Region changed.
		if (ob instanceof IROI)                return true; // Region changed.
		return false;
	}

	/**
	 * Thread safe method for getting the string with should be shown to the user about the scan.
	 * @param monitor
	 */
	private void update(IProgressMonitor monitor) {
		if (modelAdaptable==null) {
			// We see if there is a view with a compound model adaptable
			IViewReference[] refs = PageUtil.getPage().getViewReferences();
			for (IViewReference iViewReference : refs) {
				IViewPart part = iViewReference.getView(false);
				if (part==null) continue;
                CompoundModel<IROI> cm = part.getAdapter(CompoundModel.class);
                if (cm !=null) {
                	modelAdaptable = part;
                }
			}
		}
		if (modelAdaptable==null) return; // Nothing to update, no view gives us a CompoundModel!
		try {
			if (monitor.isCanceled()) return;
	        CompoundModel<IROI> cm = modelAdaptable.getAdapter(CompoundModel.class);
	        if (cm != null) {
	        	// Validate
	        	vservice.validate(cm);
	        	
	    		StyledString styledString = new StyledString();
		        	
	        	// Create generator for points
				if (monitor.isCanceled()) return;
	        	final IPointGenerator<?> gen = pservice.createCompoundGenerator(cm);
	        	styledString.append("A scan of ");
	        	styledString.append((new DecimalFormat()).format(gen.size()), StyledString.COUNTER_STYLER);
	        	styledString.append(" points, scanning motors: ");
	        	styledString.append(getMotorNames(gen), FontStyler.BOLD);

				if (monitor.isCanceled()) return;
	        	styledString.append("\nUsing detectors: ");
	        	styledString.append(getDetectorNames(), FontStyler.BOLD);
	        	
				if (monitor.isCanceled()) return;
	        	styledString.append("\nRegions: ");
	        	styledString.append(getScanRegions(), StyledString.QUALIFIER_STYLER);
	        	
                setThreadSafeText(text, styledString);
	        }
		} catch (ModelValidationException ne) {
			setThreadSafeText(text, ne.getMessage());
			 
		} catch (Exception ne) {
			logger.error("Cannot create summary of scan!", ne);
			if (ne.getMessage()!=null) {
				setThreadSafeText(text, ne.getMessage());
			} else {
				setThreadSafeText(text, ne.toString());
			}
		}
	}

	private void setThreadSafeText(StyledText text, String string) {
		setThreadSafeText(text, new StyledString(string));
	}
	private void setThreadSafeText(StyledText text, StyledString styledString) {
		if (text.isDisposed()) return;
    	text.getDisplay().syncExec(new Runnable() {
    		public void run() {
    			if (text.isDisposed()) return;
	        	text.setText(styledString.toString());
	        	text.setStyleRanges(styledString.getStyleRanges());
    		}
    	});	
    }

	private String getScanRegions() {
		
		final StringBuilder buf = new StringBuilder();
		final List<ScanRegion<IROI>> regions = ScanRegions.getScanRegions(PlotUtil.getRegionSystem());
		if (regions==null) return "None";
     	for (Iterator<ScanRegion<IROI>> it = regions.iterator(); it.hasNext();) {
    		ScanRegion<IROI> region = it.next();
    		buf.append(region);
    		if(it.hasNext()) buf.append(",");
    		buf.append(" ");
    	}
    	if (buf.length()>0) return buf.toString();
    	return "None";
 	}

	private String getDetectorNames() throws Exception {
		
		final StringBuilder buf = new StringBuilder();
		Collection<DeviceInformation<?>> infos = dservice.getDeviceInformation();
		Collection<DeviceInformation<?>> activated = new ArrayList<>();
    	for (Iterator<DeviceInformation<?>> it = infos.iterator(); it.hasNext();) {
			DeviceInformation<?> deviceInformation = it.next();
			if (deviceInformation.isActivated()) activated.add(deviceInformation);
    	}
    	for (Iterator<DeviceInformation<?>> it = activated.iterator(); it.hasNext();) {
    		DeviceInformation<?> info = it.next();
    		IRunnableDevice<Object> device = dservice.getRunnableDevice(info.getName());
    		device.validate(info.getModel());
    		buf.append(info.getName());
    		if(it.hasNext()) buf.append(",");
    		buf.append(" ");
    	}

    	if (buf.length()>0) return buf.toString();
    	return "None";
	}

	private String getMotorNames(IPointGenerator<?> gen) {

		StringBuilder buf = new StringBuilder();
		IPosition first = gen.iterator().next();
		for (Iterator<String> it = first.getNames().iterator(); it.hasNext();) {
			String name = it.next();
			buf.append(name);
			if(it.hasNext()) buf.append(",");
			buf.append(" ");
		}
		return buf.toString();
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		List<IContributionManager> mans = new ArrayList<>(Arrays.asList(getViewSite().getActionBars().getToolBarManager(), getViewSite().getActionBars().getMenuManager()));
		MenuManager     rightClick     = new MenuManager();
		mans.add(rightClick);
		
		IAction showInfo = new Action("Show scan information", IAction.AS_CHECK_BOX) {
			public void run() {
				Activator.getDefault().getPreferenceStore().setValue(DevicePreferenceConstants.SHOW_SCAN_INFO, isChecked());
				GridUtils.setVisible(text, isChecked());
				text.getParent().layout(new Control[]{text});
			}
		};
		showInfo.setImageDescriptor(Activator.getImageDescriptor("icons/information-white.png"));
		showInfo.setChecked(Activator.getDefault().getPreferenceStore().getBoolean(DevicePreferenceConstants.SHOW_SCAN_INFO));
	
		ViewUtil.addGroups("show", mans, showInfo);
		
		IAction run = new Action("Execute current scan\n(Submits it to the queue of scans to be run.)", Activator.getImageDescriptor("icons/shoe--arrow.png")) {
			public void run() {
				execute();
			}
		};
	
		ViewUtil.addGroups("execute", mans, run);

		
		text.setMenu(rightClick.createContextMenu(text));

	}

	@Override
	public void setFocus() {
		if (text!=null && !text.isDisposed()) text.setFocus();
	}

	private static class FontStyler extends Styler {
		
		public static Styler BOLD = new FontStyler(new Font(null, "Dialog", 10, SWT.BOLD));

		private Font font;

		public FontStyler(Font toSet) {
			this.font = toSet;
		}

		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.font  = font;
		}
	}

}
