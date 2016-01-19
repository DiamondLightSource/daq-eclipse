package org.eclipse.scanning.test.event;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.event.EventServiceImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

public class AnyBeanEventTest {

	private IEventService              eservice;
	private IPublisher<AnyBean>        publisher;
	private ISubscriber<IBeanListener<AnyBean>> subscriber;

	@Before
	public void createServices() throws Exception {
		
		// We wire things together without OSGi here 
		// DO NOT COPY THIS IN NON-TEST CODE!
		eservice = new EventServiceImpl(); // Do not copy this get the service from OSGi!
		
		final URI uri = new URI("tcp://sci-serv5.diamond.ac.uk:61616");	
		
		// We use the long winded constructor because we need to pass in the connector.
		// In production we would normally 
		publisher  = eservice.createPublisher(uri,  "my.custom.topic", new ActivemqConnectorService());		
		subscriber = eservice.createSubscriber(uri, "my.custom.topic", new ActivemqConnectorService());
	}

	
	@After
	public void dispose() throws EventException {
		publisher.disconnect();
		subscriber.disconnect();
	}

	
	@Test
	public void blindBroadcastTest() throws Exception {

		final AnyBean bean = new AnyBean();
		bean.setName("fred");
		bean.setAddress("My home");
		bean.setDob(-1);
		bean.setTelephoneNumber("+44 666");
		publisher.broadcast(bean);
	}

	@Test
	public void checkedBroadcastTest() throws Exception {

		final AnyBean bean = new AnyBean();
		bean.setName("fred");
		bean.setAddress("My home");
		bean.setDob(-1);
		bean.setTelephoneNumber("+44 666");
		
		final List<AnyBean> gotBack = new ArrayList<AnyBean>(3);
		subscriber.addListener(new IBeanListener<AnyBean>() {
			@Override
			public void beanChangePerformed(BeanEvent<AnyBean> evt) {
				gotBack.add(evt.getBean());
			}
		});
		
		publisher.broadcast(bean);
		
		Thread.sleep(500); // The bean should go back and forth in ms anyway
		
		if (!bean.equals(gotBack.get(0))) throw new Exception("Bean did not come back!");
	}
	

}
