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
import org.eclipse.scanning.api.device.IRunnableDeviceService;
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
    private static IRunnableDeviceService deviceService;

	public static IEventService getEventService() {
		return eventService;
	}

	public static void setEventService(IEventService service) {
		RequesterPluginTest.eventService = service;
	}

	public static IRunnableDeviceService getDeviceService() {
		return deviceService;
	}

	public static void setDeviceService(IRunnableDeviceService deviceService) {
		RequesterPluginTest.deviceService = deviceService;
	}

	@Before
	public void createServices() throws Exception {
		connect(eventService, deviceService);
	}
	
}
