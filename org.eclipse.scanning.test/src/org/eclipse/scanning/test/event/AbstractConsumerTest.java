package org.eclipse.scanning.test.event;

import java.net.InetAddress;
import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.alive.KillBean;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.IProcessCreator;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.event.dry.DryRunCreator;
import org.junit.Test;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

public class AbstractConsumerTest {

	
	protected IEventService          eservice;
	protected ISubmitter<StatusBean> submitter;
	protected IConsumer<StatusBean>  consumer;

	
    @Test
	public void testSimpleSubmission() throws Exception {
		
		StatusBean bean = doSubmit();
		
		// Manually take the submission from the list not using event service for isolated test
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(submitter.getUri());		
		Connection connection = connectionFactory.createConnection();
		
		try {
			Session   session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Queue queue = session.createQueue(IEventService.SUBMISSION_QUEUE);
	
			final MessageConsumer consumer = session.createConsumer(queue);
			connection.start();
	
			TextMessage msg = (TextMessage)consumer.receive(1000);
			
			ActivemqConnectorService cservice = new ActivemqConnectorService();
			StatusBean fromQ = cservice.unmarshal(msg.getText(), StatusBean.class);
        	
        	if (!fromQ.equals(bean)) throw new Exception("The bean from the queue was not the same as that submitted! q="+fromQ+" submit="+bean);
        	
		} finally {
			connection.close();
		}
	}

    @Test
	public void testSimpleConsumer() throws Exception {
    	
		consumer.setRunner(new DryRunCreator());
		consumer.setBeanClass(StatusBean.class);
		consumer.start();
		
		StatusBean bean = doSubmit();
		 	
		Thread.sleep(14000); // 10000 to do the loop, 4000 for luck
		
		List<StatusBean> stati = consumer.getStatusQueue();
		if (stati.size()!=1) throw new Exception("Unexpected status size in queue! Might not have status or have forgotten to clear at end of test!");
		
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
    
    @Test
	public void testConsumerStop() throws Exception {
        testStop(new DryRunCreator());
    }
    @Test
	public void testConsumerStopSeparateThread() throws Exception {
        testStop(new DryRunCreator(false));
    }

    private void testStop(IProcessCreator dryRunCreator) throws Exception {
    	
		consumer.setRunner(dryRunCreator);
		consumer.setBeanClass(StatusBean.class);
		consumer.start();

		StatusBean bean = doSubmit();

		Thread.sleep(2000);
		
		consumer.stop();
		
		Thread.sleep(2000);
		checkTerminatedProcess(bean);

	}
    
	
    @Test
    public void testKillingAConsumer() throws Exception {
    	
		consumer.setRunner(new DryRunCreator());
		consumer.setBeanClass(StatusBean.class);
		consumer.start();

		StatusBean bean = doSubmit();

		Thread.sleep(2000);

		IPublisher<KillBean> killer = eservice.createPublisher(submitter.getUri(), IEventService.KILL_TOPIC, new ActivemqConnectorService());
		KillBean kbean = new KillBean();
		kbean.setConsumerId(consumer.getConsumerId());
		kbean.setExitProcess(false); // Or tests would exit!
		kbean.setDisconnect(false);  // Or we cannot ask for the list of what's left
		killer.broadcast(kbean);
		
		Thread.sleep(2000);
		checkTerminatedProcess(bean);
		
    }


	@Test
	public void testAbortingAJobRemotely() throws Exception {

		consumer.setRunner(new DryRunCreator());
		consumer.setBeanClass(StatusBean.class);
		consumer.start();

		StatusBean bean = doSubmit();

		Thread.sleep(2000);
		
		IPublisher<StatusBean> terminator = eservice.createPublisher(submitter.getUri(), IEventService.STATUS_TOPIC, new ActivemqConnectorService());
        bean.setStatus(Status.REQUEST_TERMINATE);
        terminator.broadcast(bean);
        
        Thread.sleep(2000);
		checkTerminatedProcess(bean);
	}
    
	private void checkTerminatedProcess(StatusBean bean) throws Exception {
		List<StatusBean> stati = consumer.getStatusQueue();
		if (stati.size()!=1) throw new Exception("Unexpected status size in queue! Might not have status or have forgotten to clear at end of test!");
		
		StatusBean complete = stati.get(0);
		
       	if (complete.equals(bean)) {
       		throw new Exception("The bean from the status queue was the same as that submitted! It should have a different status. q="+complete+" submit="+bean);
       	}
        
       	if (complete.getStatus()!=Status.TERMINATED) {
       		throw new Exception("The bean in the queue should be terminated after a stop!"+complete);
       	}
       	if (complete.getPercentComplete()==100) {
       		throw new Exception("The percent complete should not be 100!"+complete);
       	}
	}
    
    @Test
    public void testHeartbeat() throws Exception {
    	throw new Exception("Patient dead!");
    }

	private StatusBean doSubmit() throws Exception {
		
		StatusBean bean = new StatusBean();
		bean.setName("Test");
		bean.setStatus(Status.SUBMITTED);
		bean.setHostName(InetAddress.getLocalHost().getHostName());
		bean.setMessage("Hello World");

		submitter.submit(bean);
		
		return bean;
	}
}
