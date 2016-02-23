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
import uk.ac.diamond.json.JsonMarshaller;

public class RunTest {

	private IEventService            eservice;
	private IPublisher<TestScanBean> publisher;
	
	@Before
	public void before() throws URISyntaxException {
		ActivemqConnectorService.setJsonMarshaller(new JsonMarshaller());
		eservice = new EventServiceImpl(new ActivemqConnectorService());
		// Use in memory broker removes requirement on network and external ActiveMQ process
		// http://activemq.apache.org/how-to-unit-test-jms-code.html
		publisher = eservice.createPublisher(new URI("vm://localhost?broker.persistent=false"), "org.eclipse.scanning.test.scan.real.test");
		
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
