/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.scanning.test.event;

import java.net.URI;

import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.scanning.api.device.DeviceResponse;
import org.eclipse.scanning.api.device.IDeviceService;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.IResponseCreator;
import org.eclipse.scanning.api.event.core.IResponseProcess;
import org.eclipse.scanning.api.event.scan.DeviceRequest;
import org.eclipse.scanning.points.serialization.PointsModelMarshaller;
import org.junit.Before;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

/**
 * Class to test that we can run 
 * 
 * @author Matthew Gerring
 *
 */
public class RequesterPluginTest extends AbstractRequesterTest {
	
    private static IEventService  eventService;
    private static IDeviceService deviceService;

	public static IEventService getEventService() {
		return eventService;
	}

	public static void setEventService(IEventService service) {
		RequesterPluginTest.eventService = service;
	}

	public static IDeviceService getDeviceService() {
		return deviceService;
	}

	public static void setDeviceService(IDeviceService deviceService) {
		RequesterPluginTest.deviceService = deviceService;
	}

	@Before
	public void createServices() throws Exception {
		
		
		// Set up stuff because we are not in OSGi with a test
		// DO NOT COPY TESTING ONLY
		dservice = getDeviceService();
		
		// We wire things together without OSGi here 
		// DO NOT COPY THIS IN NON-TEST CODE!
		ActivemqConnectorService.setJsonMarshaller(new MarshallerService(new PointsModelMarshaller()));
		eservice = getEventService();
		
		// Use in memory broker removes requirement on network and external ActiveMQ process
		// http://activemq.apache.org/how-to-unit-test-jms-code.html
		final URI uri = new URI("vm://localhost?broker.persistent=false");
		
		// We use the long winded constructor because we need to pass in the connector.
		// In production we would normally 
		requester  = eservice.createRequestor(uri, IEventService.REQUEST_TOPIC, IEventService.RESPONSE_TOPIC);
		
		// This object sits on the server.
		responder = eservice.createResponder(uri, IEventService.REQUEST_TOPIC, IEventService.RESPONSE_TOPIC);
		responder.setResponseCreator(new IResponseCreator<DeviceRequest>() {		
			@Override
			public IResponseProcess<DeviceRequest> createResponder(DeviceRequest bean, IPublisher<DeviceRequest> statusNotifier) throws EventException {
				return new DeviceResponse(deviceService, bean, statusNotifier);
			}
		});
	}
	
}
