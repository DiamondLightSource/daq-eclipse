package org.eclipse.scanning.test.event;

import static org.junit.Assert.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.event.scan.IScanListener;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanEvent;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.event.EventServiceImpl;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

public class MappingScanTest {

	protected IEventService              eservice;
	protected IPublisher<ScanBean>       publisher;
	protected ISubscriber<IScanListener> subscriber;

	@Before
	public void createServices() throws Exception {
		
		eservice = new EventServiceImpl(); // Do not copy this get the service from OSGi!
		
		final URI uri = new URI("tcp://sci-serv5.diamond.ac.uk:61616");	
		
		// We use the long winded constructor because we need to pass in the connector.
		// In production we would normally 
		publisher  = eservice.createPublisher(uri, IEventService.SCAN_TOPIC, new ActivemqConnectorService());		
		subscriber = eservice.createSubscriber(uri, IEventService.SCAN_TOPIC, new ActivemqConnectorService());
	}

	/**
	 * This test mimics a scan being run
	 * 
	 * Eventually we will need a test running the sequencing system.
	 * @throws Exception 
	 */
	@Test
	public void testSimpleMappingScan() throws Exception {
		
		// Listen to events sent
		final List<ScanBean> gotBack = new ArrayList<ScanBean>(3);
		subscriber.addListener(new IScanListener.Stub() {
			@Override
			public void scanStateChanged(ScanEvent evt) {
				gotBack.add(evt.getBean());
			}
		});

		// Simulate queueing it
		final ScanBean bean = new ScanBean();
		bean.setName("Test Mapping Scan");
		bean.setDeviceState(DeviceState.IDLE);
		bean.setBeamline("I05-1");
		bean.setUserName("Joe Bloggs");
		bean.setStatus(Status.QUEUED);
		publisher.broadcast(bean);
        
		// Tell them we started it.
		bean.setStatus(Status.RUNNING);
		publisher.broadcast(bean);
		
		bean.setSize(10);
		int ipoint = 0;
		
		// Outer loop temperature, will be scan command driven when sequencer exists.
		for (double temp = 273; temp < 283; temp++) {
			bean.setPoint(ipoint);
			bean.putValue("temperature", temp);
			testDeviceScan(bean);
			++ipoint;
		}

		bean.setStatus(Status.COMPLETE);
		publisher.broadcast(bean);

		Thread.sleep(1000); // Just to make sure all the message events come in
		
		assertTrue(gotBack.size()>10);
		assertTrue(gotBack.get(1).scanStart());
		assertTrue(gotBack.get(gotBack.size()-1).scanEnd());
	}
	
	/**
	 * Mimic the running of a mapping scan inside the outer scan
	 * @param bean
	 * @throws Exception
	 */
	private void testDeviceScan(ScanBean bean) throws Exception {
		
		// Mimic a scan
		bean.setDeviceState(DeviceState.CONFIGURING);
		publisher.broadcast(bean);

		bean.setDeviceState(DeviceState.READY);
		publisher.broadcast(bean);

		for (int i = 0; i < 10; i++) {
			bean.setDeviceState(DeviceState.RUNNING);
			publisher.broadcast(bean);
		}
		
		bean.setDeviceState(DeviceState.IDLE);
		publisher.broadcast(bean);

	}
	
	
}
