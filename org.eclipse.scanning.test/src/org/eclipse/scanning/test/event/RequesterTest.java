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

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.IResponseCreator;
import org.eclipse.scanning.api.event.core.IResponseProcess;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.DeviceRequest;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.example.detector.MandelbrotDetector;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.sequencer.DeviceResponse;
import org.eclipse.scanning.sequencer.DeviceServiceImpl;
import org.eclipse.scanning.test.scan.mock.MockScannableConnector;
import org.junit.Before;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;
import uk.ac.diamond.json.JsonMarshaller;

/**
 * Class to test that we can run 
 * 
 * @author Matthew Gerring
 *
 */
public class RequesterTest extends AbstractRequesterTest {
	

	@Before
	public void createServices() throws Exception {
		
		
		// Set up stuff because we are not in OSGi with a test
		// DO NOT COPY TESTING ONLY
		dservice = new DeviceServiceImpl(new MockScannableConnector());
		MandelbrotDetector mandy = new MandelbrotDetector();
		final DeviceInformation info = new DeviceInformation(); // This comes from extension point or spring in the real world.
		info.setName("mandelbrot");
		info.setLabel("Example Mandelbrot");
		info.setDescription("Example mandelbrot device");
		info.setId("org.eclipse.scanning.example.detector.mandelbrotDetector");
		info.setIcon("org.eclipse.scanning.example/icon/mandelbrot.png");
		mandy.setDeviceInformation(info);
		((DeviceServiceImpl)dservice)._register("mandelbrot", mandy);
		
		// We wire things together without OSGi here 
		// DO NOT COPY THIS IN NON-TEST CODE!
		ActivemqConnectorService.setJsonMarshaller(new JsonMarshaller());
		eservice = new EventServiceImpl(new ActivemqConnectorService()); // Do not copy this get the service from OSGi!
		
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
				return new DeviceResponse(dservice, bean, statusNotifier);
			}
		});
	}
	
}
