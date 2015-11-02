/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.scanning.test.event;

import java.net.InetAddress;
import java.net.URI;
import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.event.dry.DryRunCreator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

/**
 * Class to test that we can run 
 * 
 * @author Matthew Gerring
 *
 */
public class SubmissionTest {
	
	private EventServiceImpl eservice;
	private ISubmitter<StatusBean> submitter;
	private IConsumer<StatusBean> consumer;

	@Before
	public void createServices() throws Exception {
		
		eservice = new EventServiceImpl(); // Do not copy this get the service from OSGi!
		
		final URI uri = new URI("tcp://sci-serv5.diamond.ac.uk:61616");	
		
		// We use the long winded constructor because we need to pass in the connector.
		// In production we would normally 
		submitter  = eservice.createSubmitter(uri, IEventService.SUBMISSION_QUEUE, new ActivemqConnectorService());
		consumer   = eservice.createConsumer(uri, IEventService.SUBMISSION_QUEUE, IEventService.STATUS_QUEUE, IEventService.STATUS_TOPIC, IEventService.HEARTBEAT_TOPIC, IEventService.TERMINATE_TOPIC, new ActivemqConnectorService());
		consumer.setName("Test Consumer");
		consumer.clearStatusQueue();
	}
	
	@After
	public void dispose() throws EventException {
		submitter.disconnect();
		consumer.clearStatusQueue();
		consumer.disconnect();
	}
	
    @Test
	public void testSimpleConsumer() throws Exception {
    	
       
		consumer.setRunner(new DryRunCreator());
		consumer.setBeanClass(StatusBean.class);
		consumer.start();
		
		URI uri = new URI("tcp://sci-serv5.diamond.ac.uk:61616");
		StatusBean bean = doSubmit(uri);
		 	
		Thread.sleep(12000); // 10000 to do the loop, 2000 for luck
		
		List<StatusBean> stati = consumer.getStatusQueue();
		if (stati.size()!=1) throw new Exception("Unexpected status size in queue! Might not have status or have forgotten to clear at end of test!");
		
		StatusBean complete = stati.get(0);
		
       	if (complete.equals(bean)) {
       		throw new Exception("The bean from the status queue was the same as that submitted! It should have a different status. q="+complete+" submit="+bean);
       	}
        
       	if (complete.getStatus()!=Status.COMPLETE) {
       		throw new Exception("The bean in the queue is not complete!"+complete);
       	}
       	if (complete.getPercentComplete()<100) {
       		throw new Exception("The percent complete is less than 100!"+complete);
       	}
    }

    @Test
	public void testSimpleSubmission() throws Exception {
		
		URI        uri  = new URI("tcp://sci-serv5.diamond.ac.uk:61616");		
		StatusBean bean = doSubmit(uri);
		
		// Manually take the submission back of the list
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(uri);		
		Connection connection = connectionFactory.createConnection();
		
		try {
			Session   session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Queue queue = session.createQueue(IEventService.SUBMISSION_QUEUE);
	
			final MessageConsumer consumer = session.createConsumer(queue);
			connection.start();
	
			TextMessage msg = (TextMessage)consumer.receive(1000);
			
			ActivemqConnectorService cservice = new ActivemqConnectorService();
			StatusBean fromQ = cservice.unmarshal(msg.getText(), StatusBean.class);
        	
        	if (!fromQ.equals(bean)) throw new Exception("The bean from the queue was not the same as that submitted! q="+fromQ+" submit="+bean);
        	
		} finally {
			connection.close();
		}
	}

	private StatusBean doSubmit(URI uri) throws Exception {
		
		StatusBean bean = new StatusBean();
		bean.setName("Test");
		bean.setStatus(Status.SUBMITTED);
		bean.setHostName(InetAddress.getLocalHost().getHostName());
		bean.setMessage("Hello World");

		submitter.submit(bean);
		
		return bean;
	}
}
