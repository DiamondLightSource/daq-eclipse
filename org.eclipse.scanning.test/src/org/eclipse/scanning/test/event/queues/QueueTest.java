package org.eclipse.scanning.test.event.queues;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;

import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.alive.HeartbeatBean;
import org.eclipse.scanning.api.event.alive.IHeartbeatListener;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.queues.IQueue;
import org.eclipse.scanning.api.event.queues.QueueNameMap;
import org.eclipse.scanning.api.event.queues.QueueStatus;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.event.queues.Queue;
import org.eclipse.scanning.test.BrokerTest;
import org.eclipse.scanning.test.event.queues.mocks.DummyBean;
import org.eclipse.scanning.test.event.queues.util.EventServiceActorMaker;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

/**
 * Test of the Queue class (implementation of IQueue)
 * 
 * @author Michael Wharmby
 *
 */
public class QueueTest extends BrokerTest {
	
	protected IEventService evServ; //FIXME Remove
	
	protected IQueue<DummyBean> queue;
	protected IConsumer<DummyBean> cons;
	protected ISubscriber<IHeartbeatListener> mon;
	protected UUID consID;

	protected static String qID = "uk.ac.diamond.i15-1.test";
	protected static QueueNameMap qNames;
	
	private static String submQ, statQ, statT, heartT, cmdT;
	
	@BeforeClass
	public static void createInfrastructure() throws Exception {
		submQ = qID + ".submitQ";
		statQ = qID + ".statusQ";
		statT = qID + ".statusT";
		heartT = qID + ".heartbeat";
		cmdT = qID + ".command";
		qNames = new QueueNameMap(submQ, statQ, statT, heartT, cmdT);
		
	}
	
	/**
	 * Queue creation relies on hard coded creation of EventService. This is OK:
	 * - it's a test.
	 * - we don't test any consumer functions.
	 * 
	 * @throws Exception
	 */
	@Before
	public void createQueue() throws Exception {
		//FIXME Remove next two lines
		ActivemqConnectorService.setJsonMarshaller(new MarshallerService());
		evServ =  new EventServiceImpl(new ActivemqConnectorService());
		
		cons = EventServiceActorMaker.makeConsumer(new DummyBean(), submQ, 
				statQ, statT, heartT, cmdT, true);
		consID = cons.getConsumerId();
		createHeartMonitor();
		
		queue = new Queue<DummyBean>(qID, qNames, cons, mon);
	}
	
	@After
	public void cleanup() throws EventException {
		queue.getConsumer().stop();
		queue.getConsumer().clearQueue(submQ);
		queue.getConsumer().clearQueue(statQ);
		queue.getConsumer().disconnect();
		queue = null;
	}
	
	/**
	 * Check the status of the queue is being set correctly on creation.
	 */
	@Test
	public void testQueueStatus() {
		assertEquals("Initial queue state wrong", QueueStatus.INITIALISED, queue.getQueueStatus());
	}
	
	/**
	 * Use the heartbeat monitor to record some queue heartbeats.
	 * @throws Exception
	 */
	@Test
	public void testHeartbeatMonitors() throws Exception {
		queue.getConsumer().start();
		Thread.sleep(5000);
		
		List<HeartbeatBean> heartbeats = queue.getLatestHeartbeats();
		HeartbeatBean lastBeat = queue.getLastHeartbeat();
		
		assertEquals("Last heartbeat in latest and lastBeat differ", lastBeat, heartbeats.get(heartbeats.size()-1));
		for (HeartbeatBean hb : heartbeats) {
			assertEquals("Heartbeat for an unknown consumerID!", consID, hb.getConsumerId());
		}
	}
	
	/**
	 * Check the reported consumer ID is consistent with the ID of the consumer
	 * in the queue.
	 */
	@Test
	public void testConsumerID() {
		assertEquals("Consumer ID different", consID, queue.getConsumerID());
	}
	
	@Test
	public void testDisconnect() throws Exception {
		queue.getConsumer().start();
		Thread.sleep(3000);
		queue.getConsumer().stop();
		
		queue.disconnect();
		Thread.sleep(3000);
		assertFalse("Consumer still alive!", queue.getConsumer().isActive());
	}
	
	/**
	 * Test that queues can be cleared using the clearQueues method.
	 * @throws Exception
	 */
	@Test
	public void testClear() throws Exception {
		DummyBean beanA = new DummyBean("Yuri", 5603);
		ISubmitter<DummyBean> subm = evServ.createSubmitter(uri, submQ);
		subm.submit(beanA);
		subm.disconnect();
		
		assertEquals("More/less than one bean in the submission queue; one submitted", 1, queue.getConsumer().getSubmissionQueue().size());
		assertEquals("Non-zero number of beans in status set; none expected.", 0, queue.getConsumer().getStatusSet().size());
		assertTrue("Failed to clear queues", queue.clearQueues());
		assertEquals("Non-zero number of beans in submit queue; none expected", 0, queue.getConsumer().getSubmissionQueue().size());
		assertEquals("Non-zero number of beans in status set; none expected.", 0, queue.getConsumer().getStatusSet().size());
	}
	
	/**
	 * Test the queue can correctly identify when jobs are in the submission 
	 * queue waiting to be consumed by the hasSubmittedJobsPending() method.
	 * @throws Exception
	 */
	@Test
	public void testHasJobsPending() throws Exception {
		assertFalse("Jobs found on the consumer, but none submitted", queue.hasSubmittedJobsPending());
		
		DummyBean beanA = new DummyBean("Alfred", 5603);
		ISubmitter<DummyBean> subm = evServ.createSubmitter(uri, submQ);
		subm.submit(beanA);
		subm.disconnect();
		
		assertTrue("No jobs found, but one submitted", queue.hasSubmittedJobsPending());
		queue.getConsumer().start();
		Thread.sleep(1000);
		assertFalse("Jobs found on the consumer, but all should be consumed", queue.hasSubmittedJobsPending());
	}
	
	//FIXME Remove
	protected void createHeartMonitor() {
		mon = evServ.createSubscriber(uri, heartT);
	}

}
