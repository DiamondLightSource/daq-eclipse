package org.eclipse.scanning.test.event;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.alive.HeartbeatBean;
import org.eclipse.scanning.api.event.alive.HeartbeatEvent;
import org.eclipse.scanning.api.event.alive.IHeartbeatListener;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.event.EventServiceImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

public class HeartbeatTest {

	private IEventService                   eservice;
	private IPublisher<HeartbeatBean>       publisher;
	private ISubscriber<IHeartbeatListener> subscriber;

	@Before
	public void createServices() throws Exception {
		
		eservice = new EventServiceImpl(); // Do not copy this get the service from OSGi!
		
		final URI uri = new URI("tcp://sci-serv5.diamond.ac.uk:61616");	
		
		// We use the long winded constructor because we need to pass in the connector.
		// In production we would normally 
		publisher  = eservice.createPublisher(uri, IEventService.HEARTBEAT_TOPIC, new ActivemqConnectorService());		
		subscriber = eservice.createSubscriber(uri, IEventService.HEARTBEAT_TOPIC, new ActivemqConnectorService());
	}
	
	@After
	public void dispose() throws EventException {
		publisher.disconnect();
	}


	@Test
	public void blindHeartbeatTest() throws Exception {

		System.setProperty("org.eclipse.scanning.event.heartbeat.freq",    String.valueOf(1000));
		System.setProperty("org.eclipse.scanning.event.heartbeat.timeout", String.valueOf(5000));
		publisher.setAlive(true);

		Thread.sleep(10000);

		if (!publisher.isAlive()) throw new Exception("Heartbeat should still be being published!");

		publisher.disconnect();

		if (publisher.isAlive()) throw new Exception("Heartbeat should be stopped and the patient should require resusitation! Are they a vampire?");

	}


	@Test
	public void checkedHeartbeatTest() throws Exception {

		System.setProperty("org.eclipse.scanning.event.heartbeat.freq",    String.valueOf(1000));
		System.setProperty("org.eclipse.scanning.event.heartbeat.timeout", String.valueOf(5000));
		publisher.setAlive(true);
		
		final List<HeartbeatBean> gotBack = new ArrayList<>(3);
		subscriber.addListener(new IHeartbeatListener.Stub() {
			@Override
			public void heartbeatPerformed(HeartbeatEvent evt) {
				gotBack.add(evt.getBean());
				System.out.println("The heart beated at "+((new SimpleDateFormat()).format(new Date(evt.getBean().getPublishTime()))));
			}
		});

		Thread.sleep(10000);

		if (!publisher.isAlive()) throw new Exception("Heartbeat should still be being published!");

		publisher.disconnect();

		if (publisher.isAlive()) throw new Exception("Heartbeat should be stopped and the patient should require resusitation! Are they a vampire?");

		if (gotBack.size()<6) throw new Exception("Not enough heartbeats were detected!");
		System.out.println("Encountered "+gotBack.size()+" beats");
	}
	
	@Test
	public void timeoutHeartbeatTest() throws Exception {

		System.setProperty("org.eclipse.scanning.event.heartbeat.freq",    String.valueOf(1000));
		System.setProperty("org.eclipse.scanning.event.heartbeat.timeout", String.valueOf(5000));
		try {
			final URI uri = new URI("tcp://rubbish:5600");	
			publisher = eservice.createPublisher(uri, IEventService.HEARTBEAT_TOPIC, new ActivemqConnectorService());
			publisher.setAlive(true);

		} catch (EventException required) {
			return;
		}
		throw new Exception("Able to connect to tcp://rubbish:5600 without an error!");

	}

}
