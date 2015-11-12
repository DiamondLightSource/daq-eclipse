package org.eclipse.malcolm.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.eclipse.malcolm.api.IMalcolmConnection;
import org.eclipse.malcolm.api.IMalcolmDevice;
import org.eclipse.malcolm.api.MalcolmDeviceException;
import org.eclipse.malcolm.api.State;
import org.eclipse.malcolm.api.event.IMalcolmListener;
import org.eclipse.malcolm.api.event.MalcolmEvent;
import org.eclipse.malcolm.api.event.MalcolmEventBean;
import org.eclipse.malcolm.api.message.MalcolmUtil;
import org.eclipse.malcolm.api.message.Type;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import uk.ac.diamond.malcom.jacksonzeromq.connector.StateDeserializer;
import uk.ac.diamond.malcom.jacksonzeromq.connector.StateSerializer;
import uk.ac.diamond.malcom.jacksonzeromq.connector.TypeDeserializer;
import uk.ac.diamond.malcom.jacksonzeromq.connector.TypeSerializer;

@RunWith(Parameterized.class)
public abstract class AbstractCommunicationMalcolmTest extends AbstractMalcolmTest {
	
	private static final int REPEAT_COUNT = 1;
	private static final int MESSAGE_GRACE = 500;

	@Parameterized.Parameters
	public static List<Object[]> data() {
	    return Arrays.asList(new Object[REPEAT_COUNT][0]);
	}

	@Test
	public void testBasicRunPausableDevice() throws Exception {
		basicRun(device);
	}

	private void basicRun(IMalcolmDevice zebra) throws MalcolmDeviceException, Exception {
		
		configure(zebra, 10);
		zebra.run(); // blocks until finished
		
		final State state = zebra.getState();
		
		if (!state.isBeforeRun()) throw new Exception("Problem with state at end of test!");
	}
	
	@Test
	public void testStartAndStopEventsPausableDevice() throws Exception {
		startAndStopEvents(device);
	}

	private void startAndStopEvents(IMalcolmDevice zebra) throws MalcolmDeviceException, InterruptedException, Exception {
		
		final List<MalcolmEventBean> beans = new ArrayList<MalcolmEventBean>(IMAGE_COUNT);
		zebra.addMalcolmListener(new IMalcolmListener<MalcolmEventBean>() {
			@Override
			public void eventPerformed(MalcolmEvent<MalcolmEventBean> e) {
				MalcolmEventBean bean = e.getBean();
	   			if (bean.isScanEnd() || bean.isScanStart()) {
				    beans.add(bean);
    			}
			}
		});
		
		configure(zebra, IMAGE_COUNT);
		zebra.run(); 						// blocks until finished
		Thread.sleep(MESSAGE_GRACE);		// allow for messaging delays
		
		if (beans.size()!=2) throw new Exception("Scan start and end not encountered!");
		
		final State state = zebra.getState();
		
		if (!state.isBeforeRun()) throw new Exception("Problem with state at end of test!");
	}
	
	
	@Test
	public void testMalcolmEventsPausableDevice() throws Exception {		
		runMalcolmEvents(device);
	}
	
	private void runMalcolmEvents(IMalcolmDevice zebra) throws Exception {
		
		final boolean[] scanHasStarted = {false};
        
		final List<MalcolmEventBean> beans = new ArrayList<MalcolmEventBean>(IMAGE_COUNT);
		zebra.addMalcolmListener(new IMalcolmListener<MalcolmEventBean>() {
			@Override
			public void eventPerformed(MalcolmEvent<MalcolmEventBean> e) {				
				MalcolmEventBean bean = e.getBean();
				if (bean.isScanStart()) {
					scanHasStarted[0] = true;
				}
	   			if (MalcolmUtil.isScanning(bean) && scanHasStarted[0]) {
				    beans.add(bean);
    			}
			}
		});
		
		configure(zebra, IMAGE_COUNT);
		zebra.run(); 						// blocks until finished
		Thread.sleep(MESSAGE_GRACE);		// allow for messaging delays
		
		// There is one extra event as the state is set to Running before scan start
		if (beans.size()!=IMAGE_COUNT) {
			throw new Exception("Unexpected number of images written! Expected: "+IMAGE_COUNT+" got "+beans.size());
		}
		
		final State state = zebra.getState();
		
		if (!state.isBeforeRun()) throw new Exception("Problem with state at end of test!");
	}
	
	@Test
	public void testStartAndStopTopicPausableDevice() throws Exception {
		startAndStopTopic(device);
	}

	private void startAndStopTopic(IMalcolmDevice zebra) throws JMSException, MalcolmDeviceException, InterruptedException, Exception {
		
		Connection      send     = null;
		try {
			
			// Add a topic consumer which deserliazes to 
			QueueConnectionFactory connectionFactory = (QueueConnectionFactory)connectorService.createConnectionFactory(zebra.getURI());
			send = connectionFactory.createConnection();

			final Session session = send.createSession(false, Session.AUTO_ACKNOWLEDGE);
			final Topic   topic   = session.createTopic(zebra.getTopicName());		
            final List<MalcolmEventBean> beans = new ArrayList<MalcolmEventBean>(IMAGE_COUNT);
		   	
			
        	final MessageConsumer consumer = session.createConsumer(topic);
	    	MessageListener listener = new MessageListener() {
	    		public void onMessage(Message message) {		 
	    			TextMessage txt = (TextMessage)message;
	    			MalcolmEventBean bean;
					try {
						bean = connectorService.unmarshal(txt.getText(), MalcolmEventBean.class);
			   			if (bean.isScanEnd() || bean.isScanStart()) {
						    beans.add(bean);
		    			}
					} catch (Exception e) {
						e.printStackTrace();
					}
	    		}
	    	};
	    	consumer.setMessageListener(listener);
	    	send.start();
	    	
	    	configure(zebra, IMAGE_COUNT);
			zebra.run(); 						// blocks until finished
			Thread.sleep(MESSAGE_GRACE);		// allow for messaging delays			
			
			if (beans.size()!=2) throw new Exception("Scan start and end not encountered!");
						
			final State state = zebra.getState();
			
			if (!state.isBeforeRun()) throw new Exception("Problem with state at end of test!");

		} finally {
			if (send!=null)     send.close();
		}
	}

	@Test
	public void testMalcolmTopicPausableDevice() throws Exception {
		runMalcolmTopic(device);
	}

	private void runMalcolmTopic(IMalcolmDevice zebra) throws JMSException, MalcolmDeviceException, InterruptedException, Exception {
		
		final boolean[] scanHasStarted = {false};
        
		Connection      send     = null;
		try {
			
			// Add a topic consumer which deserializes to 
			QueueConnectionFactory connectionFactory = (QueueConnectionFactory)connectorService.createConnectionFactory(zebra.getURI());
			send = connectionFactory.createConnection();

			final Session session = send.createSession(false, Session.AUTO_ACKNOWLEDGE);
			final Topic   topic   = session.createTopic(zebra.getTopicName());		
            final List<MalcolmEventBean> beans = new ArrayList<MalcolmEventBean>(IMAGE_COUNT);
			
        	final MessageConsumer consumer = session.createConsumer(topic);
	    	MessageListener listener = new MessageListener() {
	    		public void onMessage(Message message) {		 
	    			TextMessage txt = (TextMessage)message;
	    			MalcolmEventBean bean;
					try {
						bean = connectorService.unmarshal(txt.getText(), MalcolmEventBean.class);
						if (bean.isScanStart()) {
							scanHasStarted[0] = true;
						}
			   			if (MalcolmUtil.isScanning(bean) && scanHasStarted[0]) {						
						    beans.add(bean);
		    			}
					} catch (Exception e) {
						e.printStackTrace();
					}
	    		}
	    	};
	    	consumer.setMessageListener(listener);
	    	send.start();
	    	
	    	configure(zebra, IMAGE_COUNT);
			zebra.run(); 						// blocks until finished
			Thread.sleep(MESSAGE_GRACE);		// allow for messaging delays
			
			if (beans.size()!=IMAGE_COUNT) {
				throw new Exception("Unexpected number of images written! Expected: "+IMAGE_COUNT+" got "+beans.size());
			}
			
			final State state = zebra.getState();
			
			if (!state.isBeforeRun()) throw new Exception("Problem with state at end of test!");

		} finally {
			if (send!=null)     send.close();
		}
	}

	@Test
	public void testAbortIdleRunnableDevice() throws Throwable {

		try {
			IMalcolmConnection  connection = service.createConnection(null);
			final IMalcolmDevice     zebra =  connection.getDevice("zebra");
			zebra.abort();
		} catch (Exception expected) {
			return;
		}
		throw new Exception(State.IDLE+" did not throw an exception on aborting!");
	}
	
	@Test
	public void testAbortIdlePausableDevice() throws Throwable {

		try {
			IMalcolmConnection  connection = service.createConnection(PAUSABLE);
			final IMalcolmDevice     zebra =  connection.getDevice("zebra");
			zebra.abort();
		} catch (Exception expected) {
			return;
		}
		throw new Exception(State.IDLE+" did not throw an exception on aborting!");
	}

}

