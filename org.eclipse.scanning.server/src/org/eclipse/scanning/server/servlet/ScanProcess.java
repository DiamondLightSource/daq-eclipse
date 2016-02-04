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
import org.eclipse.scanning.api.scan.AbstractRunnableDevice;
import org.eclipse.scanning.api.scan.IRunnableDevice;
import org.eclipse.scanning.api.scan.IWritableDetector;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositioner;
import org.eclipse.scanning.api.scan.models.ScanModel;

/**
 * Object for running a scan.
 * 
 * @author Matthew Gerring
 *
 */
class ScanProcess implements IConsumerProcess<ScanBean> {

	private IPositioner                positioner;
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
		
		bean.setPreviousStatus(Status.SUBMITTED);
		bean.setStatus(Status.QUEUED);
		response.broadcast(bean);
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
			if (bean.getScanRequest().getStart()!=null) {
				positioner.setPosition(bean.getScanRequest().getStart());
			}
			
			this.device = createRunnableDevice(bean);
			
			if (blocking) {
			    device.run(null); // Runs until done
			    
				if (bean.getScanRequest().getEnd()!=null) {
					positioner.setPosition(bean.getScanRequest().getEnd());
				}

			} else {
				((AbstractRunnableDevice<ScanModel>)device).start(null);
				
				if (bean.getScanRequest().getEnd()!=null) throw new EventException("Cannot perform end position when scan is async.");
			}
			
			bean.setPreviousStatus(Status.RUNNING);
			bean.setStatus(Status.COMPLETE);
			response.broadcast(bean);

		} catch (ScanningException | InterruptedException ne) {
			bean.setPreviousStatus(Status.RUNNING);
			bean.setStatus(Status.FAILED);
			bean.setMessage(ne.getMessage());
			response.broadcast(bean);
			throw new EventException(ne);
		}
	}

	private IRunnableDevice<ScanModel> createRunnableDevice(ScanBean bean) throws EventException {

		ScanRequest req = bean.getScanRequest();
		if (req==null) throw new EventException("There must be a scan request to run a new scan!");
		
		try {
			final ScanModel smodel = new ScanModel();
			smodel.setPositionIterable(Services.getGeneratorService().createGenerator(req.getModel()));
			smodel.setDetectors(getDetectors(req.getDetectors()));
			smodel.setMonitors(getMonitors(req.getMonitorNames()));
			smodel.setBean(bean);
			smodel.setFilePath(req.getFilePath()); // FIXME The file path should come from a service
			                                       // that gives a proper location for the next collection.
			
			return Services.getScanService().createRunnableDevice(smodel, response);
			
		} catch (GeneratorException | ScanningException e) {
			throw new EventException(e);
		}
	}

	@Override
	public void terminate() throws EventException {
		try {
			bean.setPreviousStatus(Status.RUNNING);
			bean.setStatus(Status.TERMINATED);
			response.broadcast(bean);
			device.abort();
			
		} catch (ScanningException e) {
			bean.setPreviousStatus(Status.RUNNING);
			bean.setStatus(Status.FAILED);
			response.broadcast(bean);

			throw new EventException(e);
		}
	}
	
	
	
	@SuppressWarnings("unchecked")
	private List<IRunnableDevice<?>> getDetectors(Map<String, ?> detectors) throws EventException {
		if (detectors==null) return null;
		try {
			final List<IRunnableDevice<?>> ret = new ArrayList<>(3);
			for (String name : detectors.keySet()) {
				@SuppressWarnings("rawtypes")
				Object dmodel = detectors.get(name);
				IRunnableDevice<?> detector = (IWritableDetector<?>)Services.getScanService().createRunnableDevice(dmodel);
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

}