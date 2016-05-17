package org.eclipse.scanning.test.event.queues.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.net.URI;
import java.util.List;

import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.alive.HeartbeatBean;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.event.queues.HeartbeatMonitor;
import org.eclipse.scanning.event.queues.QueueServicesHolder;
import org.eclipse.scanning.test.event.queues.mocks.DummyBean;
import org.eclipse.scanning.test.event.queues.util.EventServiceActorMaker;
import org.junit.Before;
import org.junit.Test;

public class HeartbeatMonitorTest {
	
	private IConsumer<DummyBean> cons;
	private HeartbeatMonitor hbM;
	
	private URI uri;
	
	@Before
	public void setUp() {
		cons = EventServiceActorMaker.makeConsumer(new DummyBean(), true);
		uri = EventServiceActorMaker.getURI();
		
		//This is not a plugin-test - need to supply the EventService
		QueueServicesHolder.setEventService(EventServiceActorMaker.getEventService());
	}
	
	@Test
	public void testLatestHeartbeats() throws Exception {
		hbM = new HeartbeatMonitor(uri, IEventService.HEARTBEAT_TOPIC, cons.getConsumerId());
		cons.start();
		Thread.sleep(4300);
		
		List<HeartbeatBean> heartbeats = hbM.getLatestHeartbeats();
		assertEquals("Unexpected number of heartbeats", 2, heartbeats.size());
	}
	
	@Test
	public void testLastHeartbeat() throws Exception {
		hbM = new HeartbeatMonitor(uri, IEventService.HEARTBEAT_TOPIC, cons.getConsumerId());
		cons.start();
		Thread.sleep(2300);
		
		final HeartbeatBean first = hbM.getLastHeartbeat();
		Thread.sleep(2300);
		final HeartbeatBean second = hbM.getLastHeartbeat();
		cons.start();
		List<HeartbeatBean> heartbeats = hbM.getLatestHeartbeats();
		
		assertFalse("First & second heartbeats are the same", first.equals(second));
		assertEquals("Last heartbeat in latest and lastBeat differ", second, heartbeats.get(heartbeats.size()-1));
	}
	
//	@Test
//	public void testChangingConsumerID() {
//		
//	}
//
//	@Test
//	public void testChangingQueue() {
//		//This needs a mock queue object
//	
//	@Test
//	public void testChangingQueueID() {
//		//This needs a mock queue & queue service object
//	}
//	
//	@Test
//	public void testFixedQueueMonitor() {
//		
//	}

}
