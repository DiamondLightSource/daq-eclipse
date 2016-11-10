package org.eclipse.scanning.server.servlet;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.annotation.scan.AnnotationManager;
import org.eclipse.scanning.api.annotation.scan.PostConfigure;
import org.eclipse.scanning.api.annotation.scan.PreConfigure;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IPausableDevice;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.AbstractMalcolmModel;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.AbstractPausableProcess;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IDeviceDependentIterable;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.scanning.api.scan.ScanEstimator;
import org.eclipse.scanning.api.scan.ScanInformation;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositioner;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.api.script.IScriptService;
import org.eclipse.scanning.api.script.ScriptExecutionException;
import org.eclipse.scanning.api.script.ScriptRequest;
import org.eclipse.scanning.api.script.ScriptResponse;
import org.eclipse.scanning.api.script.UnsupportedLanguageException;
import org.eclipse.scanning.server.application.Activator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Object for running a scan.
 * 
 * @author Matthew Gerring
 *
 */
public class ScanProcess extends AbstractPausableProcess<ScanBean> {
	
	private static final Logger logger = LoggerFactory.getLogger(ScanProcess.class);

	// Services
	private IPositioner                positioner;
	private IScriptService             scriptService;
	
	private IPausableDevice<ScanModel> device;
	private boolean                    blocking;

	public ScanProcess(ScanBean scanBean, IPublisher<ScanBean> response, boolean blocking) throws EventException {
		
		super(scanBean, response);
		this.blocking = blocking;
		
		if (bean.getScanRequest().getStart()!=null || bean.getScanRequest().getEnd()!=null) {
			try {
				this.positioner = Services.getRunnableDeviceService().createPositioner();
			} catch (ScanningException e) {
				throw new EventException(e);
			}
		}
		
		this.scriptService = Services.getScriptService();
		
		bean.setPreviousStatus(Status.SUBMITTED);
		bean.setStatus(Status.QUEUED);
		broadcast(bean);
	}
	
	@Override
	public void doPause() throws Exception {
		device.pause();
	}
	
	@Override
	public void doResume() throws Exception  {
		device.resume();
	}

	@Override
	public void execute() throws EventException {
		
		try {		
			// Move to a position if they set one
			if (bean.getScanRequest().getStart()!=null) {
				positioner.setPosition(bean.getScanRequest().getStart());
			}
			
			// Run a script, if any has been requested
			ScriptResponse<?> res = runScript(bean.getScanRequest().getBefore());
			bean.getScanRequest().setBeforeResponse(res);
			
			this.device = createRunnableDevice(bean);
			
			if (blocking) {
			    device.run(null); // Runs until done
			    
				// Run a script, if any has been requested
			    res = runScript(bean.getScanRequest().getAfter());
				bean.getScanRequest().setAfterResponse(res);

				if (bean.getScanRequest().getEnd()!=null) {
					positioner.setPosition(bean.getScanRequest().getEnd());
				}

			} else {
				((AbstractRunnableDevice<ScanModel>)device).start(null);
				if (bean.getScanRequest().getAfter()!=null) throw new EventException("Cannot run end script when scan is async.");
				if (bean.getScanRequest().getEnd()!=null) throw new EventException("Cannot perform end position when scan is async.");
			}
			
			bean.setPreviousStatus(Status.RUNNING);
			bean.setStatus(Status.COMPLETE);
			bean.setPercentComplete(100);
			broadcast(bean);
			
	        // Intentionally do not catch EventException, that passes straight up.
			
		} catch (ScanningException | InterruptedException | UnsupportedLanguageException | ScriptExecutionException ne) {
			ne.printStackTrace();
			logger.error("Cannot execute run "+getBean().getName()+" "+getBean().getUniqueId(), ne);
			bean.setPreviousStatus(Status.RUNNING);
			bean.setStatus(Status.FAILED);
			bean.setMessage(ne.getMessage());
			broadcast(bean);
			
			throw new EventException(ne);
		}
	}

	private ScriptResponse<?> runScript(ScriptRequest req) throws EventException, UnsupportedLanguageException, ScriptExecutionException {
		if (req==null) return null; // Nothing to do
		if (scriptService==null) throw new ScriptExecutionException("No script service is available, cannot run script request "+req);
		return scriptService.execute(req);		
	}

	private IPausableDevice<ScanModel> createRunnableDevice(ScanBean bean) throws ScanningException, EventException {

		ScanRequest<?> req = bean.getScanRequest();
		if (req==null) throw new ScanningException("There must be a scan request to run a scan!");
		
		try {
			final ScanModel scanModel = new ScanModel();
			IPointGenerator<?> generator = getGenerator(req);
			scanModel.setPositionIterable(generator);
			
			ScanEstimator estimator = new ScanEstimator(Services.getGeneratorService(), bean.getScanRequest());
			bean.setSize(estimator.getSize());

			setFilePath(scanModel, bean);
			
			scanModel.setDetectors(getDetectors(bean, req.getDetectors()));
			scanModel.setMonitors(getScannables(req.getMonitorNames()));
			scanModel.setMetadataScannables(getScannables(req.getMetadataScannableNames()));
			scanModel.setBean(bean);
			
			configureDetectors(req.getDetectors(), scanModel, estimator, generator);
			
			return (IPausableDevice<ScanModel>) Services.getRunnableDeviceService().createRunnableDevice(scanModel, publisher);
			
		} catch (Exception e) {
			bean.setStatus(Status.FAILED);
			bean.setMessage(e.getMessage());
			broadcast(bean);
			if (e instanceof EventException) throw (EventException)e;
			throw new EventException(e);
		}
	}

	private void configureDetectors(Map<String, Object> dmodels, ScanModel model, ScanEstimator estimator, IPointGenerator<?> generator) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, ScanningException {
		
		ScanInformation info = new ScanInformation(estimator);
		info.setScannableNames(getScannableNames(model.getPositionIterable()));
		
		for (IRunnableDevice<?> device : model.getDetectors()) {
			
			AnnotationManager manager = new AnnotationManager(Activator.createResolver());
			manager.addDevices(device);
			manager.addContext(info);
			
			IRunnableDevice<Object> odevice = (IRunnableDevice<Object>)device;
			Object dmodel = dmodels.get(odevice.getName());
			if (dmodel instanceof AbstractMalcolmModel) {
				AbstractMalcolmModel mmodel = (AbstractMalcolmModel)dmodel;
				mmodel.setGenerator(generator);
			}
			manager.invoke(PreConfigure.class, dmodel);
			odevice.configure(dmodel);
			manager.invoke(PostConfigure.class, dmodel);
		}
	}

	private Collection<String> getScannableNames(Iterable<IPosition> gen) {
		
		Collection<String> names = null;
		if (gen instanceof IDeviceDependentIterable) {
			names = ((IDeviceDependentIterable)gen).getScannableNames();	
		}
		if (names==null) {
			names = gen.iterator().next().getNames();
		}
		return names;   		
	}

	private void setFilePath(ScanModel smodel, ScanBean bean) throws EventException, GeneratorException {
		
		ScanRequest<?> req = bean.getScanRequest();
		
		// Set the file path to the next scan file path from the service
		// which manages scan names.
		if (req.getFilePath()==null) {
			IFilePathService fservice = Services.getFilePathService();
			if (fservice!=null) {
				try {
					final String template = req.getSampleData()!=null ? req.getSampleData().getName() : null;
					smodel.setFilePath(fservice.getNextPath(template));
				} catch (Exception e) {
					throw new EventException(e);
				}
			} else {
				smodel.setFilePath(null); // It is allowable to run a scan without a nexus file.
			}
		} else {
		    smodel.setFilePath(req.getFilePath());
		}
		bean.setFilePath(smodel.getFilePath());		
	}

	@SuppressWarnings("unchecked")
	private IPointGenerator<?> getGenerator(ScanRequest<?> req) throws GeneratorException {
		IPointGeneratorService service = Services.getGeneratorService();
		return service.createCompoundGenerator(req.getCompoundModel());
	}

	@Override
	public void doTerminate() throws Exception {
		
		if (bean.getStatus()==Status.COMPLETE) return; // Nothing to terminate.
		device.abort();
	}
	
	private List<IRunnableDevice<?>> getDetectors(ScanBean bean, Map<String, ?> detectors) throws EventException {
		
		if (detectors==null) return null;
		try {
			
			final List<IRunnableDevice<?>> ret = new ArrayList<>(3);
			
			final IRunnableDeviceService service = Services.getRunnableDeviceService();
			
			for (String name : detectors.keySet()) {
				Object dmodel = detectors.get(name);
				IRunnableDevice<Object> detector = service.getRunnableDevice(name);
				if (detector==null) {
					detector = (IRunnableDevice<Object>)service.createRunnableDevice(dmodel, false);
					detector.setName(name); // Not sure if this is ok. For now name must match that in table
				}
				ret.add(detector);
			}
			
			return ret;
			
		} catch (ScanningException ne) {
			throw new EventException(ne);
		}
	}

	private List<IScannable<?>> getScannables(Collection<String> scannableNames) throws EventException {
		// used to get the monitors and the metadata scannables
		if (scannableNames==null) return null;
		try {
			final List<IScannable<?>> ret = new ArrayList<>(3);
			for (String name : scannableNames) ret.add(Services.getConnector().getScannable(name));
			return ret;
		} catch (ScanningException ne) {
			throw new EventException(ne);
		}
	}
	
	private void broadcast(ScanBean bean) throws EventException {
		if (publisher!=null) {
			publisher.broadcast(bean);
		}		
	}

}