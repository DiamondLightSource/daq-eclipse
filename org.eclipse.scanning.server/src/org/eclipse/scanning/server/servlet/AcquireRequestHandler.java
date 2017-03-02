/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.server.servlet;

import org.eclipse.scanning.api.annotation.scan.AnnotationManager;
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
import org.eclipse.scanning.api.scan.ScanInformation;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.server.application.Activator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A handler for {@link AcquireRequest}. Performs a scan with a single empty point
 * with the detector as specified in the request.
 * 
 * @author Matthew Dickie
 */
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
		} catch (Exception e) {
			// if an exception occurs set the bean status to failed and add the
			// exception message as the bean's message.
			e.printStackTrace();
			logger.error("Cannot acquire data for detector " + getBean().getDetectorName(), e);
			bean.setStatus(Status.FAILED);
			bean.setMessage(e.getMessage());
		}
		
		return bean;
	}
	
	private IRunnableDevice<?> createRunnableDevice(AcquireRequest request) throws Exception {
		final ScanModel scanModel = new ScanModel();

		IPointGeneratorService pointGenService = Services.getGeneratorService();
		IPointGenerator<?> gen = pointGenService.createGenerator(new StaticModel());
		scanModel.setPositionIterable(gen);
		
		scanModel.setFilePath(getOutputFilePath(request));
		IRunnableDeviceService deviceService = Services.getRunnableDeviceService();
		IRunnableDevice<?> detector = deviceService.getRunnableDevice(bean.getDetectorName());
		if (detector == null) {
			throw new ScanningException("No such detector: "+ bean.getDetectorName());
		}
		
		scanModel.setDetectors(detector);
		
		configureDetector(detector, request.getDetectorModel(), scanModel);
		return deviceService.createRunnableDevice(scanModel, null);
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
	private void configureDetector(IRunnableDevice<?> detector, Object detectorModel,
			ScanModel scanModel) throws Exception {
		
		ScanInformation info = new ScanInformation();
		info.setRank(0);
		info.setShape(new int[0]);
		info.setSize(1);
		
		info.setFilePath(scanModel.getFilePath());
		
		AnnotationManager manager = new AnnotationManager(Activator.createResolver());
		manager.addContext(info);
		manager.addDevices(detector);
		
		manager.invoke(PreConfigure.class, detectorModel);
		((IRunnableDevice<Object>) detector).configure(detectorModel);
		manager.invoke(PostConfigure.class, detectorModel);
	}
	
}
