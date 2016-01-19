package org.eclipse.scanning.test.scan.real;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.event.EventServiceImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

public class RunTest {

	private IEventService            eservice;
	private IPublisher<TestScanBean> publisher;
	
	@Before
	public void before() throws URISyntaxException {
		eservice = new EventServiceImpl();
		publisher = eservice.createPublisher(new URI("tcp://sci-serv5.diamond.ac.uk:61616"), "org.eclipse.scanning.test.scan.real.test", new ActivemqConnectorService());
		
	}
	
	@After
	public void after() throws EventException {
		publisher.disconnect();
	}
	
	@Test
	public void testSendScan() throws Exception {
		TestScanBean info = new TestScanBean();
		info.setName("fred");
		publisher.broadcast(info);
	}
}
