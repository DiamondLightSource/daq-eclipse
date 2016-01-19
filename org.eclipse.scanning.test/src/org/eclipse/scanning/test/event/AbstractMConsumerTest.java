package org.eclipse.scanning.test.event;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.event.dry.DryRunCreator;
import org.junit.Test;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

public class AbstractMConsumerTest {

	protected IEventService          eservice;
	protected ISubmitter<StatusBean> submitter;
	protected IConsumer<StatusBean>  consumer;

    @Test
    public void testTwoConsumersOneSubmit() throws Exception {
    	
		consumer.setRunner(new DryRunCreator<StatusBean>(false));
		consumer.start();
 
		IConsumer<StatusBean> consumer2   = eservice.createConsumer(consumer.getUri(), IEventService.SUBMISSION_QUEUE, IEventService.STATUS_SET, IEventService.STATUS_TOPIC, IEventService.HEARTBEAT_TOPIC, IEventService.KILL_TOPIC, new ActivemqConnectorService());
		try {
			consumer2.setName("Test Consumer "+2);
			consumer2.clearQueue(IEventService.SUBMISSION_QUEUE);
			consumer2.clearQueue(IEventService.STATUS_SET);
			consumer2.setRunner(new DryRunCreator<StatusBean>(false));
			consumer2.start();
			
			checkSubmission();
			
		} finally {

			consumer2.clearQueue(IEventService.SUBMISSION_QUEUE);
			consumer2.clearQueue(IEventService.STATUS_SET);
			consumer2.disconnect();
		}
    }
    
    @Test
    public void testTwoConsumersTenSubmits() throws Exception {
    	
		consumer.setRunner(new DryRunCreator<StatusBean>(false));
		consumer.start();
 
		IConsumer<StatusBean> consumer2   = eservice.createConsumer(consumer.getUri(), IEventService.SUBMISSION_QUEUE, IEventService.STATUS_SET, IEventService.STATUS_TOPIC, IEventService.HEARTBEAT_TOPIC, IEventService.KILL_TOPIC, new ActivemqConnectorService());
		try {
			consumer2.setName("Test Consumer "+2);
			consumer2.clearQueue(IEventService.SUBMISSION_QUEUE);
			consumer2.clearQueue(IEventService.STATUS_SET);
			consumer2.setRunner(new DryRunCreator<StatusBean>(false));
			consumer2.start();
			
			List<StatusBean> submissions = new ArrayList<StatusBean>(10);
			for (int i = 0; i < 10; i++) {
				submissions.add(doSubmit("Test "+i));
				System.out.println("Submitted: Test "+i);
				Thread.sleep(10);
			}
			 	
			Thread.sleep(16000); // 10000 to do the loop, 6000 for luck
			
			checkStatus(submissions);
			
		} finally {
			consumer2.clearQueue(IEventService.SUBMISSION_QUEUE);
			consumer2.clearQueue(IEventService.STATUS_SET);
			consumer2.disconnect();
		}
    }

    @Test
    public void testTwoConsumersTenSubmitsThreads() throws Exception {
    	
		consumer.setRunner(new DryRunCreator<StatusBean>(false));
		consumer.start();
 
		IConsumer<StatusBean> consumer2   = eservice.createConsumer(consumer.getUri(), IEventService.SUBMISSION_QUEUE, IEventService.STATUS_SET, IEventService.STATUS_TOPIC, IEventService.HEARTBEAT_TOPIC, IEventService.KILL_TOPIC, new ActivemqConnectorService());
		try {
			consumer2.setName("Test Consumer "+2);
			consumer2.clearQueue(IEventService.SUBMISSION_QUEUE);
			consumer2.clearQueue(IEventService.STATUS_SET);
			consumer2.setRunner(new DryRunCreator<StatusBean>(false));
			consumer2.start();
			
			final List<StatusBean> submissions = new ArrayList<StatusBean>(10);
			for (int i = 0; i < 10; i++) {
				final int finalI = i;
				final Thread thread = new Thread(new Runnable() {
					public void run () {
						try {
							submissions.add(doSubmit("Test "+finalI));
							System.out.println("Submitted: Thread Test "+finalI);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				thread.setName("Thread "+i);
				thread.setDaemon(true);
				thread.start();
				
				Thread.sleep(100);
			}

			Thread.sleep(16000); // 10000 to do the loop, 6000 for luck
			
			checkStatus(submissions);
			
		} finally {
			consumer2.clearQueue(IEventService.SUBMISSION_QUEUE);
			consumer2.clearQueue(IEventService.STATUS_SET);
			consumer2.disconnect();
		}
    }
    
    
    
    @Test
    public void testTenConsumersTenSubmits() throws Exception {
    	
		consumer.setRunner(new DryRunCreator<StatusBean>(false));
		consumer.start();
 
		List<IConsumer<StatusBean>> consumers   = new ArrayList<>(9);
		try {
			for (int i = 2; i < 11; i++) {
				IConsumer<StatusBean> c = eservice.createConsumer(consumer.getUri(), IEventService.SUBMISSION_QUEUE, IEventService.STATUS_SET, IEventService.STATUS_TOPIC, IEventService.HEARTBEAT_TOPIC, IEventService.KILL_TOPIC, new ActivemqConnectorService());
				c.setName("Test Consumer "+i);
				c.clearQueue(IEventService.SUBMISSION_QUEUE);
				c.clearQueue(IEventService.STATUS_SET);
				c.setRunner(new DryRunCreator<StatusBean>(false));
				c.start();
				consumers.add(c);
			}
			
			List<StatusBean> submissions = new ArrayList<StatusBean>(10);
			for (int i = 0; i < 10; i++) {
				submissions.add(doSubmit("Test "+i));
				System.out.println("Submitted: Test "+i);
				Thread.sleep(10);
			}
			 	
			Thread.sleep(16000); // 10000 to do the loop, 6000 for luck
			
			checkStatus(submissions);
			
		} finally {
			for (IConsumer<StatusBean> c : consumers) {
				c.clearQueue(IEventService.SUBMISSION_QUEUE);
				c.clearQueue(IEventService.STATUS_SET);
				c.disconnect();
			}
		}
    }

   
    
    @Test
    public void testTenConsumersTenSubmitsThreads() throws Exception {
    	
		consumer.setRunner(new DryRunCreator<StatusBean>(false));
		consumer.start();
 
		List<IConsumer<StatusBean>> consumers   = new ArrayList<>(9);
		try {
			for (int i = 2; i < 11; i++) {
				IConsumer<StatusBean> c = eservice.createConsumer(consumer.getUri(), IEventService.SUBMISSION_QUEUE, IEventService.STATUS_SET, IEventService.STATUS_TOPIC, IEventService.HEARTBEAT_TOPIC, IEventService.KILL_TOPIC, new ActivemqConnectorService());
				c.setName("Test Consumer "+i);
				c.clearQueue(IEventService.SUBMISSION_QUEUE);
				c.clearQueue(IEventService.STATUS_SET);
				c.setRunner(new DryRunCreator<StatusBean>(false));
				c.start();
				consumers.add(c);
			}
			
			final List<StatusBean> submissions = new ArrayList<StatusBean>(10);
			for (int i = 0; i < 10; i++) {
				final int finalI = i;
				final Thread thread = new Thread(new Runnable() {
					public void run () {
						try {
							submissions.add(doSubmit("Test "+finalI));
							System.out.println("Submitted: Thread Test "+finalI);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				thread.setName("Thread "+i);
				thread.setDaemon(true);
				thread.start();
				
				Thread.sleep(100);
			}
			 	
			Thread.sleep(16000); // 10000 to do the loop, 6000 for luck
			
			checkStatus(submissions);
			
		} finally {
			for (IConsumer<StatusBean> c : consumers) {
				c.clearQueue(IEventService.SUBMISSION_QUEUE);
				c.clearQueue(IEventService.STATUS_SET);
				c.disconnect();
			}
		}
    }


    private void checkStatus(List<StatusBean> submissions) throws Exception {
    	
    	List<StatusBean> stati = consumer.getStatusSet();
		if (stati.size()!=10) throw new Exception("Unexpected status size in queue! Should be 10 size is "+stati.size());
		
		for (int i = 0; i < 10; i++) {
			
			StatusBean complete = stati.get(i);
			if (!complete.getName().equals("Test "+(9-i))) {
				throw new Exception("Unexpected run order detected! bean is named "+complete.getName()+" and should be 'Test "+(9-i)+"'");
			}
			
			StatusBean bean     = submissions.get(i);
	       	if (complete.equals(bean)) {
	       		throw new Exception("The bean from the status queue was the same as that submitted! It should have a different status. q="+complete+" submit="+bean);
	       	}
	        
	       	if (complete.getStatus()!=Status.COMPLETE) {
	       		throw new Exception("The bean in the queue is not complete!"+complete);
	       	}
	       	if (complete.getPercentComplete()<100) {
	       		throw new Exception("The percent complete is less than 100!"+complete);
	       	}
		}		
	}
 

    private void checkSubmission() throws Exception {
    	
        StatusBean bean = doSubmit();
	 	
		Thread.sleep(14000); // 10000 to do the loop, 4000 for luck
		
		List<StatusBean> stati = consumer.getStatusSet();
		if (stati.size()!=1) throw new Exception("Unexpected status size in queue! size = "+stati.size());
		
		StatusBean complete = stati.get(0);
		
       	if (complete.equals(bean)) {
       		throw new Exception("The bean from the status queue was the same as that submitted! It should have a different status. q="+complete+" submit="+bean);
       	}
        
       	if (complete.getStatus()!=Status.COMPLETE) {
       		throw new Exception("The bean in the queue is not complete!"+complete);
       	}
       	if (complete.getPercentComplete()<100) {
       		throw new Exception("The percent complete is less than 100!"+complete);
       	}
	}



	private StatusBean doSubmit() throws Exception {
 	   return doSubmit("Test");
    }
    private StatusBean doSubmit(String name) throws Exception {

 		StatusBean bean = new StatusBean();
 		bean.setName(name);
 		bean.setStatus(Status.SUBMITTED);
 		bean.setHostName(InetAddress.getLocalHost().getHostName());
 		bean.setMessage("Hello World");
 		bean.setUniqueId(UUID.randomUUID().toString());

 		submitter.submit(bean);
 		
 		return bean;
 	}

}
