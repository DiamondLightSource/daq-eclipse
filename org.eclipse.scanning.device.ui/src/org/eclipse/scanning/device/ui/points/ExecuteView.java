package org.eclipse.scanning.device.ui.points;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import org.eclipse.jface.dialogs.ErrorDialog;
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
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.AbstractPointsModel;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.ScanRegion;
import org.eclipse.scanning.api.scan.ui.AbstractControl;
import org.eclipse.scanning.api.script.ScriptRequest;
import org.eclipse.scanning.api.ui.CommandConstants;
import org.eclipse.scanning.device.ui.Activator;
import org.eclipse.scanning.device.ui.DevicePreferenceConstants;
import org.eclipse.scanning.device.ui.ScanningPerspective;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
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
			this.dservice = ServiceHolder.getEventService().createRemoteService(new URI(CommandConstants.getScanningBrokerUri()), IRunnableDeviceService.class);
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
				submit();
			}
		});
		execute.setImage(Activator.getImageDescriptor("icons/shoe--arrow.png").createImage());
		execute.setToolTipText("Submits scan to the queue of scans to be run.");

		createActions();
		PageUtil.getPage(getSite()).addSelectionListener(this);
		
		// We force the scan view to exist. It might be the one to return the compound model
		// that we will use.
		ScanningPerspective.createKeyPlayers();

		updateJob.schedule();
	}
	
	protected void submit() {
		try {

			// Send it off
			ScanBean bean=null;
			try {
				bean = new ScanBean(createScanRequest());
			} catch (Exception ne) {
				ErrorDialog.openError(getViewSite().getShell(), "Cannot Create Scan Request", "Unable to create a legal scan request.\nThere is something invalid in your current configuration of the scan.\n\nPlease contact your support representative.", new Status(IStatus.ERROR, Activator.PLUGIN_ID, ne.getMessage(), ne));
				logger.error("Unable to create a legal scan request!", ne);
				return;
			}
			bean.setStatus(org.eclipse.scanning.api.event.status.Status.SUBMITTED);

			ISubmitter<ScanBean> submitter = ServiceHolder.getEventService().createSubmitter(new URI(CommandConstants.getScanningBrokerUri()), EventConstants.SUBMISSION_QUEUE);
			submitter.submit(bean);

			
			// Show the Queue
			showQueue();
			
		} catch (Exception ne) {
			ErrorDialog.openError(getViewSite().getShell(), "Cannot Submit Scan", "There was a problem submitting the scan.\n\nPlease contact your support representative.", new Status(IStatus.ERROR, Activator.PLUGIN_ID, ne.getMessage(), ne));
		    logger.error("Unable to submit scan", ne);
		}
	}

	private void showQueue() {
		// Make sure a view is opened that looks at it.
		try {
			ViewUtil.openQueueMonitor(ScanBean.class, "Scans");
		} catch (PartInitException | UnsupportedEncodingException ne) {
			ErrorDialog.openError(getViewSite().getShell(), "Cannot Show Queue", "There was a problem showing the queue.\n\nPlease contact your support representative.", new Status(IStatus.ERROR, Activator.PLUGIN_ID, ne.getMessage(), ne));
		    logger.error("Unable to show scan queue", ne);
		}
	}

	private ScanRequest<IROI> createScanRequest() throws Exception {
		
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
		if (modelAdaptable==null) return null; // Nothing to update, no view gives us a CompoundModel!

		ScanRequest<IROI> ret = new ScanRequest<IROI>();
		CompoundModel<IROI> cm = modelAdaptable.getAdapter(CompoundModel.class);
		if (cm!=null && cm.getModels()!=null && !cm.getModels().isEmpty()) vservice.validate(cm);
		ret.setCompoundModel(cm);

		IPosition[] pos = modelAdaptable.getAdapter(IPosition[].class);
		ret.setStart(pos[0]);
		ret.setEnd(pos[1]);

		ScriptRequest[] req = modelAdaptable.getAdapter(ScriptRequest[].class);
		ret.setBefore(req[0]);
		ret.setAfter(req[1]);

		ret.setDetectors(getDetectors());

		return ret;
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
		if (ob instanceof AbstractControl)     return true; // Position changed.
		return false;
	}

	/**
	 * Thread safe method for getting the string with should be shown to the user about the scan.
	 * @param monitor
	 */
	private void update(IProgressMonitor monitor) {
		
		try {
			ScanRequest<IROI> req = createScanRequest();
			if (monitor.isCanceled()) return;
			if (req==null) {
	    		StyledString styledString = new StyledString();
	    		String name = modelAdaptable != null && modelAdaptable instanceof IWorkbenchPart ? ((IWorkbenchPart)modelAdaptable).getTitle() : "Scan Editor";
	        	styledString.append("Please create a model using '"+name+"'", StyledString.COUNTER_STYLER);
	            setThreadSafeText(text, styledString);
	            return;
			}
	        CompoundModel<IROI> cm = req.getCompoundModel();
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

		        IPosition start = req.getStart();
		        if (start!=null) {
					if (monitor.isCanceled()) return;
		        	styledString.append("\nStart: "+start);
		        }
		        
		        ScriptRequest before = req.getBefore();
		        if (before!=null) {
					if (monitor.isCanceled()) return;
		        	styledString.append("\nBefore: ");
		        	styledString.append(before.toString(), StyledString.DECORATIONS_STYLER);
		        }
	        	
				if (monitor.isCanceled()) return;
	        	styledString.append("\nScan: ");
	        	styledString.append(getModelNames(cm), StyledString.DECORATIONS_STYLER);

		        ScriptRequest after = req.getAfter();
		        if (after!=null) {
					if (monitor.isCanceled()) return;
		        	styledString.append("\nAfter: ");
		        	styledString.append(after.toString(), StyledString.DECORATIONS_STYLER);
		        }
		        IPosition end = req.getEnd();
		        if (end!=null) {
					if (monitor.isCanceled()) return;
		        	styledString.append("\nEnd: "+end);
		        }

				if (monitor.isCanceled()) return;
	        	styledString.append("\nDetectors: ");
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

	private String getModelNames(CompoundModel<IROI> compound) {
		StringBuilder buf = new StringBuilder();
		for (Iterator<Object> it = compound.getModels().iterator(); it.hasNext();) {
			Object model = it.next();
			if (model instanceof AbstractPointsModel) {
				buf.append(((AbstractPointsModel)model).getSummary());
			} else {
				buf.append(model);
			}
			if (it.hasNext()) buf.append(", ");
		}
		return buf.toString();
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
	
	private Map<String,Object> getDetectors() throws Exception {
		
		Map<String,Object> detectors = new HashMap<>();
		Collection<DeviceInformation<?>> infos = dservice.getDeviceInformation();
		Collection<DeviceInformation<?>> activated = new ArrayList<>();
    	for (Iterator<DeviceInformation<?>> it = infos.iterator(); it.hasNext();) {
			DeviceInformation<?> deviceInformation = it.next();
			if (deviceInformation.isActivated()) activated.add(deviceInformation);
    	}
    	for (Iterator<DeviceInformation<?>> it = activated.iterator(); it.hasNext();) {
    		DeviceInformation<?> info = it.next();
    		detectors.put(info.getName(), info.getModel());
    	}

    	return detectors;
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
				submit();
			}
		};
		IAction showQueue = new Action("Show the scan queue", Activator.getImageDescriptor("icons/cards-stack.png")) {
			public void run() {
				showQueue();
			}
		};
	
		ViewUtil.addGroups("execute", mans, run, showQueue);

		
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
