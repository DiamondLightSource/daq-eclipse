package org.eclipse.scanning.test.event.queues;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.alive.HeartbeatBean;
import org.eclipse.scanning.api.event.core.IConsumer;
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
	
	@After
	public void tearDown() throws Exception {
		cons.clearQueue(IEventService.SUBMISSION_QUEUE);
		cons.clearQueue(IEventService.STATUS_SET);
		cons.clearQueue(IEventService.CMD_SET);
		cons.disconnect();
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
	
	@Test
	public void testChangingConsumerID() throws Exception {
		UUID consID = cons.getConsumerId();
		hbM = new HeartbeatMonitor(uri, IEventService.HEARTBEAT_TOPIC, consID);
		cons.start();
		Thread.sleep(2300);
		
		final HeartbeatBean first = hbM.getLastHeartbeat();
		assertEquals("Heartbeat from wrong consumer", consID, first.getConsumerId());
		
		IConsumer<DummyBean> consTwo = EventServiceActorMaker.makeConsumer(new DummyBean(), true);
		UUID consTwoID = consTwo.getConsumerId();
		assertFalse("IDs of two consumers are identical", consTwoID.equals(cons.getConsumerId()));
		
		hbM.setConsumerID(consTwoID);
		consTwo.start();
		Thread.sleep(2300);
		
		final HeartbeatBean second = hbM.getLastHeartbeat();
		assertEquals("Heartbeat from wrong consumer", consTwoID, second.getConsumerId());
		assertFalse("HeartbeatBean consumerIDs identical", consID.equals(consTwoID));
		
		//Tear down the local consumer because someone has to!
		consTwo.clearQueue(IEventService.SUBMISSION_QUEUE);
		consTwo.clearQueue(IEventService.STATUS_SET);
		consTwo.clearQueue(IEventService.CMD_SET);
		consTwo.disconnect();
	}

	@Test
	public void testChangingQueue() throws Exception {
		IQueue<DummyBean> mockOne = new MockQueue<>("MockQueueOne", cons);
		UUID consID = cons.getConsumerId();
		
		hbM = new HeartbeatMonitor(uri, IEventService.HEARTBEAT_TOPIC, mockOne);
		assertEquals("Wrong queueID set on monitor", "MockQueueOne", hbM.getQueueID());
		assertEquals("Wrong consumerID set on monitor", consID, hbM.getConsumerID());
		cons.start();
		Thread.sleep(2300);
		
		final HeartbeatBean first = hbM.getLastHeartbeat();
		assertEquals("Heartbeat from wrong consumer", consID, first.getConsumerId());
		
		IConsumer<DummyBean> consTwo = EventServiceActorMaker.makeConsumer(new DummyBean(), true);
		UUID consTwoID = consTwo.getConsumerId();
		IQueue<DummyBean> mockTwo = new MockQueue<>("MockQueueTwo", consTwo);
		assertFalse("IDs of two consumers are identical", consTwoID.equals(cons.getConsumerId()));
		
		hbM.setQueue(mockTwo);
		assertEquals("Wrong queueID set on monitor", "MockQueueTwo", hbM.getQueueID());
		assertEquals("Wrong consumerID set on monitor", consTwoID, hbM.getConsumerID());
		consTwo.start();
		Thread.sleep(2300);
		
		final HeartbeatBean second = hbM.getLastHeartbeat();
		assertEquals("Heartbeat from wrong consumer", consTwoID, second.getConsumerId());
		assertFalse("HeartbeatBean consumerIDs identical", consID.equals(consTwoID));
		
		//Tear down the local consumer because someone has to!
		consTwo.clearQueue(IEventService.SUBMISSION_QUEUE);
		consTwo.clearQueue(IEventService.STATUS_SET);
		consTwo.clearQueue(IEventService.CMD_SET);
		consTwo.disconnect();
	}
	
	@Test
	public void testChangingQueueID() throws Exception {
		IConsumer<QueueBean> consOne = EventServiceActorMaker.makeConsumer(new DummyBean(), true);
		MockQueue<QueueBean> mockOne = new MockQueue<>("MockQueueOne", consOne);
		UUID consOneID = consOne.getConsumerId();
		
		IConsumer<QueueAtom> consTwo = EventServiceActorMaker.makeConsumer(new DummyAtom(), true);
		MockQueue<QueueAtom> mockTwo = new MockQueue<>("MockQueueTwo", consTwo);
		UUID consTwoID = consTwo.getConsumerId();
		
		assertFalse("IDs of two consumers are identical", consTwoID.equals(consOneID));
		
		MockQueueService mQServ = new MockQueueService(mockOne);
		mQServ.addActiveQueue(mockTwo);
		mQServ.start();
		
		hbM = new HeartbeatMonitor(uri, IEventService.HEARTBEAT_TOPIC, "MockQueueOne", mQServ);
		assertEquals("Wrong queueID set on monitor", "MockQueueOne", hbM.getQueueID());
		assertEquals("Wrong consumerID set on monitor", consOneID, hbM.getConsumerID());
		cons.start();
		Thread.sleep(2300);
		
		final HeartbeatBean first = hbM.getLastHeartbeat();
		assertEquals("Heartbeat from wrong consumer", consOneID, first.getConsumerId());
		
		hbM.setQueueID("MockQueueTwo", mQServ);
		assertEquals("Wrong queueID set on monitor", "MockQueueTwo", hbM.getQueueID());
		assertEquals("Wrong consumerID set on monitor", consTwoID, hbM.getConsumerID());
		consTwo.start();
		Thread.sleep(2300);
		
		final HeartbeatBean second = hbM.getLastHeartbeat();
		assertEquals("Heartbeat from wrong consumer", consTwoID, second.getConsumerId());
		assertFalse("HeartbeatBean consumerIDs identical", consOneID.equals(consTwoID));
		
		//Tear down the local consumers because someone has to!
		consOne.clearQueue(IEventService.SUBMISSION_QUEUE);
		consOne.clearQueue(IEventService.STATUS_SET);
		consOne.clearQueue(IEventService.CMD_SET);
		consOne.disconnect();
		
		consTwo.clearQueue(IEventService.SUBMISSION_QUEUE);
		consTwo.clearQueue(IEventService.STATUS_SET);
		consTwo.clearQueue(IEventService.CMD_SET);
		consTwo.disconnect();
	}
//	
//	@Test
//	public void testFixedQueueMonitor() {
//		
//	}

}
