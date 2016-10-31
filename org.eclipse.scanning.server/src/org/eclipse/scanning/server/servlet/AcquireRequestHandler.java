package org.eclipse.scanning.server.servlet;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.scanning.api.annotation.AnnotationManager;
import org.eclipse.scanning.api.annotation.scan.PostConfigure;
import org.eclipse.scanning.api.annotation.scan.PreConfigure;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.IResponseProcess;
import org.eclipse.scanning.api.event.scan.AcquireRequest;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.StaticModel;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.server.application.Activator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AcquireRequestHandler implements IResponseProcess<AcquireRequest> {
	
	private static final Logger logger = LoggerFactory.getLogger(AcquireRequestHandler.class);
	
	private final AcquireRequest bean;
	private final IPublisher<AcquireRequest> publisher;
	
	public AcquireRequestHandler(AcquireRequest bean, IPublisher<AcquireRequest> publisher) {
		this.bean = bean;
		this.publisher = publisher;
	}
	
	@Override
	public AcquireRequest getBean() {
		return bean;
	}

	@Override
	public IPublisher<AcquireRequest> getPublisher() {
		return publisher;
	}

	@Override
	public AcquireRequest process(AcquireRequest bean) throws EventException {
		try {
			bean.setStatus(Status.RUNNING);
			IRunnableDevice<?> device = createRunnableDevice(bean);
			device.run(null);

			bean.setStatus(Status.COMPLETE);
			bean.setMessage(null);
		} catch (ScanningException | InterruptedException e) {
			e.printStackTrace();
			bean.setStatus(Status.FAILED);
			bean.setMessage(e.getMessage());
			logger.error("Cannot acquire data for detector " + getBean().getDetectorName(), e);
			throw new EventException(e);
		}
		
		return bean;
	}
	
	private IRunnableDevice<?> createRunnableDevice(AcquireRequest request) throws ScanningException, EventException {
		final ScanModel scanModel = new ScanModel();

		try {
			IPointGeneratorService pointGenService = Services.getGeneratorService();
			IPointGenerator<?> gen = pointGenService.createGenerator(new StaticModel());
			scanModel.setPositionIterable(gen);
			
			scanModel.setFilePath(getOutputFilePath(request));
			IRunnableDeviceService deviceService = Services.getRunnableDeviceService();
			IRunnableDevice<?> detector = deviceService.getRunnableDevice(bean.getDetectorName());
			scanModel.setDetectors(detector);
			
			configureDetector(detector, request.getDetectorModel());
			return deviceService.createRunnableDevice(scanModel, null);
		} catch (Exception e) {
			if (e instanceof EventException) throw (EventException) e;
			throw new EventException(e);
		}
	}

	private String getOutputFilePath(AcquireRequest request) throws EventException {
		if (request.getFilePath() == null) {
			IFilePathService filePathService = Services.getFilePathService();
			try {
				request.setFilePath(filePathService.getNextPath(request.getDetectorName() + "-acquire"));
			} catch (Exception e) {
				throw new EventException(e);
			}
		}
		
		return request.getFilePath();
	}
	
	@SuppressWarnings("unchecked")
	private void configureDetector(IRunnableDevice<?> detector, Object detectorModel) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, ScanningException {
		AnnotationManager manager = new AnnotationManager(Activator.createResolver());
		manager.addDevices(detector);
		
		manager.invoke(PreConfigure.class, detectorModel);
		((IRunnableDevice<Object>) detector).configure(detectorModel);
		manager.invoke(PostConfigure.class, detectorModel);
	}
	
}
