package org.eclipse.scanning.server.servlet;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.scan.AbstractRunnableDevice;
import org.eclipse.scanning.api.scan.IRunnableDevice;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.models.ScanModel;

/**
 * Object for running a scan.
 * 
 * @author Matthew Gerring
 *
 */
class ScanProcess implements IConsumerProcess<ScanBean> {

	private ScanBean                   bean;
	private IPublisher<ScanBean>       response;
	private IRunnableDevice<ScanModel> device;
	private boolean blocking;

	public ScanProcess(ScanBean scanBean, IPublisher<ScanBean> response, boolean blocking) {
		this.bean     = scanBean;
		this.response = response;
		this.blocking = blocking;
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
			this.device = createRunnableDevice(bean);
			if (blocking) {
			    device.run(null); // Runs until done
			} else {
				((AbstractRunnableDevice<ScanModel>)device).start(null);
			}
		} catch (ScanningException | InterruptedException ne) {
			throw new EventException(ne);
		}
	}

	private IRunnableDevice<ScanModel> createRunnableDevice(ScanBean bean) throws EventException {

		ScanRequest req = bean.getScanRequest();
		if (req==null) throw new EventException("There must be a scan request to run a new scan!");
		
		try {
			final ScanModel smodel = new ScanModel();
			smodel.setPositionIterable(Services.getGeneratorService().createGenerator(req.getModel()));
			smodel.setDetectors(getDetectors(req.getDetectorNames()));
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
			device.abort();
		} catch (ScanningException e) {
			throw new EventException(e);
		}
	}
	
	
	
	private List<IRunnableDevice<?>> getDetectors(String... detectorNames) throws EventException {
		if (detectorNames==null) return null;
		try {
			final List<IRunnableDevice<?>> ret = new ArrayList<>(3);
			for (String name : detectorNames) ret.add(Services.getConnector().getDetector(name));
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