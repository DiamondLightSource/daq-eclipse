package org.eclipse.scanning.test.event.queues;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.queues.IQueueControllerService;
import org.eclipse.scanning.api.event.queues.IQueueService;
import org.eclipse.scanning.api.event.queues.beans.QueueBean;
import org.eclipse.scanning.api.event.queues.beans.SubTaskAtom;
import org.eclipse.scanning.api.event.queues.beans.TaskBean;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.event.queues.QueueProcessorFactory;
import org.eclipse.scanning.event.queues.ServicesHolder;
import org.eclipse.scanning.test.BrokerTest;
import org.eclipse.scanning.test.event.queues.dummy.DummyAtom;
import org.eclipse.scanning.test.event.queues.dummy.DummyAtomProcessor;
import org.eclipse.scanning.test.event.queues.dummy.DummyBean;
import org.eclipse.scanning.test.event.queues.dummy.DummyBeanProcessor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class QueueServiceIntegrationPluginTest extends BrokerTest {
	
	private static IQueueService queueService;
	private static IQueueControllerService queueControl;
	private String qRoot = "fake-queue-root";
	
	private DummyBean dummyB;
	private DummyAtom dummyA;
	
	@Before
	public void setup() throws Exception {
		//FOR TESTS ONLY
		QueueProcessorFactory.registerProcessor(DummyAtomProcessor.class);
		QueueProcessorFactory.registerProcessor(DummyBeanProcessor.class);
		
		//Configure the queue service
		queueService = ServicesHolder.getQueueService();
		queueService.setUri(uri);
		queueService.setQueueRoot(qRoot);
		queueService.init();
		
		//Configure the queue controller service
		queueControl = ServicesHolder.getQueueControllerService();
		queueControl.init();
		
		//Above here - spring will make the calls
		queueControl.startQueueService();
	}
	
	@After
	public void tearDown() throws EventException {
		queueControl.stopQueueService(false);
	}
	
	@Test
	public void testRunningBean() throws EventException {
		dummyB = new DummyBean("Bob", 50);
		
		queueControl.submit(dummyB, queueService.getJobQueueID());
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<QueueBean> statusSet = queueService.getJobQueue().getConsumer().getStatusSet();
		assertEquals(1, statusSet.size());
		assertEquals(Status.COMPLETE, statusSet.get(0).getStatus());
		assertEquals(dummyB.getUniqueId(), statusSet.get(0).getUniqueId());
	}
	
	@Test
	public void testTaskBean() throws EventException {
		TaskBean task = new TaskBean();
		task.setName("Test Task");
		
		SubTaskAtom subTask = new SubTaskAtom();
		subTask.setName("Test SubTask");
		
		dummyA = new DummyAtom("Gregor", 70);
		subTask.addAtom(dummyA);
		task.addAtom(subTask);
		
		queueControl.submit(task, queueService.getJobQueueID());
		
		try {
			Thread.sleep(1000000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<QueueBean> statusSet = queueService.getJobQueue().getConsumer().getStatusSet();
		assertEquals(1, statusSet.size());
		assertEquals(Status.COMPLETE, statusSet.get(0).getStatus());
		assertEquals(task.getUniqueId(), statusSet.get(0).getUniqueId());
	}

}
