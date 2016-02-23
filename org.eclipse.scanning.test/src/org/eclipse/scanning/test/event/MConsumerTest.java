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
import org.eclipse.scanning.event.EventServiceImpl;
import org.junit.After;
import org.junit.Before;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;
import uk.ac.diamond.json.JsonMarshaller;

/**
 * Class to test that we can run 
 * 
 * @author Matthew Gerring
 *
 */
public class MConsumerTest extends AbstractMConsumerTest{
	

	@Before
	public void createServices() throws Exception {
		
		// We wire things together without OSGi here 
		// DO NOT COPY THIS IN NON-TEST CODE!
		ActivemqConnectorService.setJsonMarshaller(new JsonMarshaller());
		eservice = new EventServiceImpl(new ActivemqConnectorService()); // Do not copy this get the service from OSGi!
		
		// Use in memory broker removes requirement on network and external ActiveMQ process
		// http://activemq.apache.org/how-to-unit-test-jms-code.html
		final URI uri = new URI("vm://localhost?broker.persistent=false");
		
		// We use the long winded constructor because we need to pass in the connector.
		// In production we would normally 
		submitter  = eservice.createSubmitter(uri, IEventService.SUBMISSION_QUEUE);
		consumer   = eservice.createConsumer(uri, IEventService.SUBMISSION_QUEUE, IEventService.STATUS_SET, IEventService.STATUS_TOPIC, IEventService.HEARTBEAT_TOPIC, IEventService.KILL_TOPIC);
		consumer.setName("Test Consumer 1");
		consumer.clearQueue(IEventService.SUBMISSION_QUEUE);
		consumer.clearQueue(IEventService.STATUS_SET);
	}
	
	@After
	public void dispose() throws EventException {
		submitter.disconnect();
		consumer.clearQueue(IEventService.SUBMISSION_QUEUE);
		consumer.clearQueue(IEventService.STATUS_SET);
		consumer.disconnect();
	}
}
