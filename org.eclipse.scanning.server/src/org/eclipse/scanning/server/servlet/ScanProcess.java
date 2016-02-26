package org.eclipse.scanning.server.servlet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.scan.AbstractRunnableDevice;
import org.eclipse.scanning.api.scan.IDeviceService;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.scanning.api.scan.IRunnableDevice;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositioner;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.api.script.IScriptService;
import org.eclipse.scanning.api.script.ScriptExecutionException;
import org.eclipse.scanning.api.script.ScriptRequest;
import org.eclipse.scanning.api.script.ScriptResponse;
import org.eclipse.scanning.api.script.UnsupportedLanguageException;

/**
 * Object for running a scan.
 * 
 * @author Matthew Gerring
 *
 */
class ScanProcess implements IConsumerProcess<ScanBean> {

	// Services
	private IPositioner                positioner;
	private IScriptService             scriptService;
	
	private ScanBean                   bean;
	private IPublisher<ScanBean>       response;
	private IRunnableDevice<ScanModel> device;
	private boolean                    blocking;

	public ScanProcess(ScanBean scanBean, IPublisher<ScanBean> response, boolean blocking) throws EventException {
		
		this.bean     = scanBean;
		this.response = response;
		this.blocking = blocking;
		
		if (bean.getScanRequest().getStart()!=null || bean.getScanRequest().getEnd()!=null) {
			try {
				this.positioner = Services.getScanService().createPositioner();
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
	public ScanBean getBean() {
		return bean;
	}

	@Override
	public IPublisher<ScanBean> getPublisher() {
		return response;
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
			bean.setPreviousStatus(Status.RUNNING);
			bean.setStatus(Status.FAILED);
			bean.setMessage(ne.getMessage());
			broadcast(bean);
			
			throw new EventException(ne);
		}
	}

	private ScriptResponse<?> runScript(ScriptRequest req) throws EventException, UnsupportedLanguageException, ScriptExecutionException {
		if (req==null) return null; // Nothing to do
		if (scriptService==null) throw new EventException("Not script service is available, cannot run script request "+req);
		return scriptService.execute(req);		
	}

	private IRunnableDevice<ScanModel> createRunnableDevice(ScanBean bean) throws ScanningException, EventException {

		ScanRequest<?> req = bean.getScanRequest();
		if (req==null) throw new ScanningException("There must be a scan request to run a new scan!");
		
		try {
			final ScanModel smodel = new ScanModel();
			smodel.setPositionIterable(getPositionIterable(req));
			smodel.setDetectors(getDetectors(req.getDetectors()));
			smodel.setMonitors(getMonitors(req.getMonitorNames()));
			smodel.setBean(bean);
			
			// the name of the scan should be set here.
			if (req.getFilePath()==null) {
				IFilePathService fservice = Services.getFilePathService();
				if (fservice!=null) {
					try {
						smodel.setFilePath(fservice.nextPath());
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
			
			return Services.getScanService().createRunnableDevice(smodel, response);
			
		} catch (Exception e) {
			bean.setStatus(Status.FAILED);
			bean.setMessage(e.getMessage());
			broadcast(bean);
			if (e instanceof EventException) throw (EventException)e;
			throw new EventException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private Iterable<IPosition> getPositionIterable(ScanRequest<?> req) throws GeneratorException {
		IPointGeneratorService service = Services.getGeneratorService();
		
		IPointGenerator<?,? extends IPosition> ret = null;
		for (IScanPathModel model : req.getModels()) {
			IPointGenerator<?,? extends IPosition> gen = service.createGenerator(model, req.getRegions(model.getUniqueKey()));
			if (ret != null) ret = service.createCompoundGenerator(ret, gen);
			if (ret==null) ret = gen;
		}
		return (Iterable<IPosition>)ret;
	}

	@Override
	public void terminate() throws EventException {
		if (bean.getStatus()==Status.COMPLETE) return; // Nothing to terminate.
		try {
			bean.setPreviousStatus(Status.RUNNING);
			bean.setStatus(Status.TERMINATED);
			broadcast(bean);
			device.abort();
			
		} catch (ScanningException e) {
			bean.setPreviousStatus(Status.RUNNING);
			bean.setStatus(Status.FAILED);
			broadcast(bean);

			throw new EventException(e);
		}
	}
	
	private List<IRunnableDevice<?>> getDetectors(Map<String, ?> detectors) throws EventException {
		if (detectors==null) return null;
		try {
			final List<IRunnableDevice<?>> ret = new ArrayList<>(3);
			
			final IDeviceService service = Services.getScanService();
			for (String name : detectors.keySet()) {
				Object dmodel = detectors.get(name);
				IRunnableDevice<?> detector = (IRunnableDevice<?>)service.createRunnableDevice(dmodel);
				ret.add(detector);
			}
			return ret;
		} catch (ScanningException ne) {
			throw new EventException(ne);
		}
	}

	private List<IScannable<?>> getMonitors(String... monitorNames) throws EventException {
		if (monitorNames==null) return null;
		try {
			final List<IScannable<?>> ret = new ArrayList<>(3);
			for (String name : monitorNames) ret.add(Services.getConnector().getScannable(name));
			return ret;
		} catch (ScanningException ne) {
			throw new EventException(ne);
		}
	}

	private void broadcast(ScanBean bean) throws EventException {
		if (response!=null && response.isAlive()) {
			response.broadcast(bean);
		}		
	}

}