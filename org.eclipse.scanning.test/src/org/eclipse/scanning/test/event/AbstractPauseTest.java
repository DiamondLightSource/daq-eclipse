package org.eclipse.scanning.test.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EventListener;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.alive.HeartbeatBean;
import org.eclipse.scanning.api.event.alive.HeartbeatEvent;
import org.eclipse.scanning.api.event.alive.IHeartbeatListener;
import org.eclipse.scanning.api.event.alive.KillBean;
import org.eclipse.scanning.api.event.alive.PauseBean;
import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.IProcessCreator;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.dry.DryRunCreator;
import org.eclipse.scanning.api.event.dry.FastRunCreator;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.points.serialization.PointsModelMarshaller;
import org.junit.After;
import org.junit.Test;

public class AbstractPauseTest {

	
	protected IEventService          eservice;
	protected ISubmitter<StatusBean> submitter;
	protected IConsumer<StatusBean>  consumer;

	
	@After
	public void dispose() throws EventException {
		submitter.disconnect();
		consumer.clearQueue(IEventService.SUBMISSION_QUEUE);
		consumer.clearQueue(IEventService.STATUS_SET);
		consumer.clearQueue(IEventService.CMD_SET);
		consumer.disconnect();
	}
	
     @Test
    public void testPausingAConsumerByID() throws Exception {
    	
		consumer.setRunner(new FastRunCreator<StatusBean>(100,false));
		consumer.start();

		StatusBean bean = doSubmit();

		Thread.sleep(200);

		IPublisher<PauseBean> pauser = eservice.createPublisher(submitter.getUri(), IEventService.CMD_TOPIC);
		pauser.setStatusSetName(IEventService.CMD_SET);
		PauseBean pbean = new PauseBean();
		pbean.setConsumerId(consumer.getConsumerId());
		pauser.broadcast(pbean);
		
		Thread.sleep(200);
		
		assertTrue(!consumer.isActive());
		
		pbean.setPause(false);
		pauser.broadcast(pbean);

		Thread.sleep(100);
	
		assertTrue(consumer.isActive());
    }

    
    @Test
    public void testPausingAConsumerByQueueName() throws Exception {
    	
		consumer.setRunner(new FastRunCreator<StatusBean>(100,false));
		consumer.start();

		StatusBean bean = doSubmit();

		Thread.sleep(200);

		IPublisher<PauseBean> pauser = eservice.createPublisher(submitter.getUri(), IEventService.CMD_TOPIC);
		pauser.setStatusSetName(IEventService.CMD_SET);
		PauseBean pbean = new PauseBean();
		pbean.setQueueName(consumer.getSubmitQueueName());
		pauser.broadcast(pbean);
		
		Thread.sleep(200);
		
		assertTrue(!consumer.isActive());
		
		pbean.setPause(false);
		pauser.broadcast(pbean);

		Thread.sleep(100);
	
		assertTrue(consumer.isActive());
    }

    @Test
    public void testReorderingAPausedQueue() throws Exception {
    	
		consumer.setRunner(new FastRunCreator<StatusBean>(200, true));
		consumer.start();

		// Bung ten things on there.
		for (int i = 0; i < 5; i++) {
			StatusBean bean = new StatusBean();
			bean.setName("Submission"+i);
			bean.setStatus(Status.SUBMITTED);
			bean.setHostName(InetAddress.getLocalHost().getHostName());
			bean.setMessage("Hello World");
			bean.setUniqueId(UUID.randomUUID().toString());
			bean.setUserName(String.valueOf(i));
			submitter.submit(bean);
		}

		Thread.sleep(100);
		IPublisher<PauseBean> pauser = eservice.createPublisher(submitter.getUri(), IEventService.CMD_TOPIC);
		pauser.setStatusSetName(IEventService.CMD_SET);
		
		PauseBean pbean = new PauseBean();
		pbean.setQueueName(consumer.getSubmitQueueName());
		pauser.broadcast(pbean);
		
		// Now we are paused. Read the submission queue
		Thread.sleep(200);
		List<StatusBean> submitQ = consumer.getSubmissionQueue();
		assertEquals(4, submitQ.size());
	
		Thread.sleep(2000); // Wait for a while and check again that nothing else is
		
		submitQ = consumer.getSubmissionQueue();
		assertEquals(4, submitQ.size()); // It really has paused has it?
		
		// Right then we will reorder it.
		consumer.cleanQueue(consumer.getSubmitQueueName());
		
		// Reverse sort
		Collections.sort(submitQ, new Comparator<StatusBean>() {
			@Override
			public int compare(StatusBean o1, StatusBean o2) {
				int y = Integer.valueOf(o1.getUserName());
				int x = Integer.valueOf(o2.getUserName());
				return (x < y) ? -1 : ((x == y) ? 0 : 1);
			}
		});
		
		// Start the consumer again
		pbean.setPause(false);
		pauser.broadcast(pbean);
		
		// Resubmit in new order 4-1
		for (StatusBean statusBean : submitQ) submitter.submit(statusBean);
		
		final Map<String, StatusBean> run = new LinkedHashMap<>(4); // Order important
		ISubscriber<EventListener> sub = eservice.createSubscriber(consumer.getUri(), consumer.getStatusTopicName());
		sub.addListener(new IBeanListener<StatusBean>() {
			@Override
			public void beanChangePerformed(BeanEvent<StatusBean> evt) {
				// Many events come through here but each scan is run in order
				StatusBean bean = evt.getBean();
				run.put(bean.getName(), bean);
			}
		});

		while(!consumer.getSubmissionQueue().isEmpty()) Thread.sleep(1000); // Wait for all to run
		
		Thread.sleep(500); // ensure last one is in the status set
		
		List<StatusBean> ordered = new ArrayList<>(run.values());
		assertEquals(4, ordered.size());
		for (int i = 0; i < ordered.size(); i++) {
			int t = Integer.valueOf(ordered.get(i).getUserName());
			if ((4-i) != t) throw new Exception("The run order was not 5-1 after reordering! Position "+i+" was "+t+" and should be "+(4-i));
		}
    }

   private StatusBean doSubmit() throws Exception {
	   return doSubmit("Test");
   }
   private StatusBean doSubmit(String name) throws Exception {

		StatusBean bean = new StatusBean();
		bean.setName(name);
		return doSubmit(bean);
   }
   private StatusBean doSubmit(StatusBean bean) throws Exception {

		bean.setStatus(Status.SUBMITTED);
		bean.setHostName(InetAddress.getLocalHost().getHostName());
		bean.setMessage("Hello World");
		bean.setUniqueId(UUID.randomUUID().toString());

		submitter.submit(bean);
		
		return bean;
	}
  }
