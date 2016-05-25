package org.eclipse.scanning.test.event.queues;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.alive.HeartbeatBean;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.queues.IHeartbeatMonitor;
import org.eclipse.scanning.api.event.queues.IQueue;
import org.eclipse.scanning.api.event.queues.beans.QueueAtom;
import org.eclipse.scanning.api.event.queues.beans.QueueBean;
import org.eclipse.scanning.event.queues.HeartbeatMonitor;
import org.eclipse.scanning.event.queues.QueueServicesHolder;
import org.eclipse.scanning.test.event.queues.mocks.DummyAtom;
import org.eclipse.scanning.test.event.queues.mocks.DummyBean;
import org.eclipse.scanning.test.event.queues.mocks.MockQueue;
import org.eclipse.scanning.test.event.queues.mocks.MockQueueService;
import org.eclipse.scanning.test.event.queues.util.EventServiceActorMaker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HeartbeatMonitorTest {
	
	private IConsumer<QueueBean> consOne;
	private IConsumer<QueueAtom >consTwo;
	private IQueue<QueueBean> mockOne;
	private IQueue<QueueAtom> mockTwo;
	private MockQueueService mQServ;
	private UUID consOneID, consTwoID;
	private HeartbeatMonitor hbM;
	
	private URI uri;
	
	@Before
	public void setUp() {
		consOne = EventServiceActorMaker.makeConsumer(new DummyBean(), true);
		consOneID = consOne.getConsumerId();
		uri = EventServiceActorMaker.getURI();
		
		//This is not a plugin-test - need to supply the EventService
		QueueServicesHolder.setEventService(EventServiceActorMaker.getEventService());
	}
	
	@After
	public void tearDown() throws Exception {
		hbM = null;
		
		consOne.clearQueue(IEventService.SUBMISSION_QUEUE);
		consOne.clearQueue(IEventService.STATUS_SET);
		consOne.clearQueue(IEventService.CMD_SET);
		consOne.disconnect();
		
		if (consTwo != null) {
			consTwo.clearQueue(IEventService.SUBMISSION_QUEUE);
			consTwo.clearQueue(IEventService.STATUS_SET);
			consTwo.clearQueue(IEventService.CMD_SET);
			consTwo.disconnect();
		}
		
		mockOne = null;
		mockTwo = null;
		
		consOneID = null;
		consTwoID = null;
	}
	
	@Test
	public void testLatestHeartbeats() throws Exception {
		hbM = new HeartbeatMonitor(uri, IEventService.HEARTBEAT_TOPIC, consOne.getConsumerId());
		consOne.start();
		Thread.sleep(4300);
		
		List<HeartbeatBean> heartbeats = hbM.getLatestHeartbeats();
		assertEquals("Unexpected number of heartbeats", 2, heartbeats.size());
	}
	
	@Test
	public void testLastHeartbeat() throws Exception {
		hbM = new HeartbeatMonitor(uri, IEventService.HEARTBEAT_TOPIC, consOne.getConsumerId());
		consOne.start();
		waitForHeartbeat(3000);
		
		final HeartbeatBean first = hbM.getLastHeartbeat();
		Thread.sleep(2300); //Heartbeat every 2000ms
		final HeartbeatBean second = hbM.getLastHeartbeat();
		consOne.start();
		List<HeartbeatBean> heartbeats = hbM.getLatestHeartbeats();
		
		assertFalse("First & second heartbeats are the same", first.equals(second));
		assertEquals("Last heartbeat in latest and lastBeat differ", second, heartbeats.get(heartbeats.size()-1));
	}
	
	@Test
	public void testChangingConsumerID() throws Exception {
		hbM = new HeartbeatMonitor(uri, IEventService.HEARTBEAT_TOPIC, consOneID);
		consOne.start();
		waitForHeartbeat(3000);
		
		final HeartbeatBean first = hbM.getLastHeartbeat();
		assertEquals("Heartbeat from wrong consumer", consOneID, first.getConsumerId());
		
		createSecondConsumer();
		assertFalse("IDs of two consumers are identical", consTwoID.equals(consOne.getConsumerId()));
		
		hbM.setConsumerID(consTwoID);
		consTwo.start();
		waitForHeartbeat(3000);
		
		final HeartbeatBean second = hbM.getLastHeartbeat();
		assertEquals("Heartbeat from wrong consumer", consTwoID, second.getConsumerId());
		assertFalse("HeartbeatBean consumerIDs identical", consOneID.equals(consTwoID));
	}

	@Test
	public void testChangingQueue() throws Exception {
		createTwoQueues();
		
		hbM = new HeartbeatMonitor(uri, IEventService.HEARTBEAT_TOPIC, mockOne);
		assertEquals("Wrong queueID set on monitor", "MockQueueOne", hbM.getQueueID());
		assertEquals("Wrong consumerID set on monitor", consOneID, hbM.getConsumerID());
		consOne.start();
		waitForHeartbeat(3000);
		
		final HeartbeatBean first = hbM.getLastHeartbeat();
		assertEquals("Heartbeat from wrong consumer", consOneID, first.getConsumerId());
		
		hbM.setQueue(mockTwo);
		assertEquals("Wrong queueID set on monitor", "MockQueueTwo", hbM.getQueueID());
		assertEquals("Wrong consumerID set on monitor", consTwoID, hbM.getConsumerID());
		consTwo.start();
		waitForHeartbeat(3000);
		
		final HeartbeatBean second = hbM.getLastHeartbeat();
		assertEquals("Heartbeat from wrong consumer", consTwoID, second.getConsumerId());
		assertFalse("HeartbeatBean consumerIDs identical", consOneID.equals(consTwoID));
	}
	
	@Test
	public void testChangingQueueID() throws Exception {
		createQueueService();
		mQServ.start();
		
		hbM = new HeartbeatMonitor(uri, IEventService.HEARTBEAT_TOPIC, "MockQueueOne", mQServ);
		assertEquals("Wrong queueID set on monitor", "MockQueueOne", hbM.getQueueID());
		assertEquals("Wrong consumerID set on monitor", consOneID, hbM.getConsumerID());
		waitForHeartbeat(3000);
		
		final HeartbeatBean first = hbM.getLastHeartbeat();
		assertEquals("Heartbeat from wrong consumer", consOneID, first.getConsumerId());
		
		hbM.setQueueID("MockQueueTwo", mQServ);
		assertEquals("Wrong queueID set on monitor", "MockQueueTwo", hbM.getQueueID());
		assertEquals("Wrong consumerID set on monitor", consTwoID, hbM.getConsumerID());
		waitForHeartbeat(3000);
		
		final HeartbeatBean second = hbM.getLastHeartbeat();
		assertEquals("Heartbeat from wrong consumer", consTwoID, second.getConsumerId());
		assertFalse("HeartbeatBean consumerIDs identical", consOneID.equals(consTwoID));
	}
	
	@Test
	public void testFixedQueueMonitor() throws Exception {
		createQueueService();
		
		IHeartbeatMonitor[] hbms = new IHeartbeatMonitor[]{
				new HeartbeatMonitor(uri, IEventService.HEARTBEAT_TOPIC, consOneID, true),
				new HeartbeatMonitor(uri, mockOne, true),
				new HeartbeatMonitor(uri, IEventService.HEARTBEAT_TOPIC, "MockQueueOne", mQServ, true)
		};
		
		for (IHeartbeatMonitor hbm : hbms) {
			try {
				hbm.setConsumerID(consTwoID);
				fail("Able to change consumerID when monitor locked");
			} catch (EventException ex) {
				//expected
			}
			try {
				hbm.setQueue(mockTwo);
				fail("Able to change Queue when monitor locked");
			} catch (EventException ex) {
				//expected
			}
			try {
				hbm.setQueueID(mockTwo.getQueueID(), mQServ);
				fail("Able to change queueID when monitor locked");
			} catch (EventException ex) {
				//expected
			}
		}
		
		
	}
	
	private void createSecondConsumer() {
		consTwo = EventServiceActorMaker.makeConsumer(new DummyAtom(), true);
		consTwoID = consTwo.getConsumerId();
		assertFalse("IDs of two consumers are identical", consTwoID.equals(consOneID));
	}
	
	private void createTwoQueues() {
		createSecondConsumer();
		mockOne = new MockQueue<>("MockQueueOne", consOne);
		mockTwo = new MockQueue<>("MockQueueTwo", consTwo);
	}
	
	private void createQueueService() throws Exception {
		createTwoQueues();
		
		mQServ = new MockQueueService(mockOne);
		mQServ.addActiveQueue(mockTwo);
		mQServ.start();
	}
	
	private void waitForHeartbeat(long timeout) throws Exception {
		while (timeout > 0) {
			if (hbM.getLatestHeartbeats().size() > 0) break;
			timeout -= 100;
			Thread.sleep(100);
		}
	}

}
