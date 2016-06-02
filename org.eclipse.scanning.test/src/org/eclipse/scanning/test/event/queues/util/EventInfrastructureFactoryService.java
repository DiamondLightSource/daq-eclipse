package org.eclipse.scanning.test.event.queues.util;

import java.net.URI;

import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.core.IProcessCreator;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.points.serialization.PointsModelMarshaller;
import org.eclipse.scanning.test.BrokerTest;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

/**
 * Manages Broker service through the BrokerTest class and allows 
 * instantiation of a "Dummy" consumer.
 *
 * @author Michael Wharmby
 *
 */
public class EventInfrastructureFactoryService extends BrokerTest {
	
	private boolean active = false, unitTest = false;
	private IEventService evServ;
	
	/**
	 * Start the broker service & optionally set up the Event Service too.
	 * 
	 * @param unitTest true if this is a unit test, will set up EventService.
	 * @throws Exception
	 */
	public void start(boolean unitTest) throws Exception {
		startBroker();
		this.unitTest = unitTest;
		
		if (unitTest) {
			ActivemqConnectorService.setJsonMarshaller(new MarshallerService(new PointsModelMarshaller()));
			evServ =  new EventServiceImpl(new ActivemqConnectorService());
		}
		
		active = true;
	}
	
	/**
	 * Stop the current BrokerService instance.
	 * @throws Exception
	 */
	public void stop() throws Exception {
		if (unitTest) {
			evServ = null;
		}
		
		stopBroker();
		active = false;
	}
	
	/**
	 * Is this service started?
	 * @return
	 */
	public boolean isActive() {
		return active;
	}
	
	/**
	 * The URI of the currently active BrokerService
	 * @return
	 * @throws Exception
	 */
	public URI getURI() throws Exception {
		if (!isActive()) {
			System.out.println("TestBroker not started; starting (is unit test?: "+unitTest+")...");
			start(unitTest);
		}
		return uri;
	}
	
	/**
	 * EventService configured for the currently active BrokerService.
	 * @return
	 */
	public IEventService getEventService() {
		return evServ;
	}
	
	/**
	 * Create a consumer with generic configuration
	 * 
	 * @param bean only needed if creating a runner.
	 * @param withRunner true for a non-functional fake/
	 * @return
	 * @throws Exception
	 */
	public <T extends StatusBean> IConsumer<T> makeConsumer(T bean, 
			boolean withRunner) throws Exception {
		if (!isActive()) {
			System.out.println("TestBroker not started; starting (is unit test?: "+unitTest+")...");
			start(unitTest);
		}
		
		IConsumer<T> cons;
		try {
			cons = evServ.createConsumer(uri);
			if (withRunner) cons.setRunner(makeEmptyRunner(bean));
			return cons;
		} catch (Exception ex) {
			System.out.println("Failed to create consumer.");
			ex.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Create a fake, non-functional runner.
	 * @param bean
	 * @return
	 */
	private static <T extends StatusBean> IProcessCreator<T> makeEmptyRunner(T bean) {
		return new IProcessCreator<T>() {

			@Override
			public IConsumerProcess<T> createProcess(T bean,
					IPublisher<T> statusNotifier)
					throws EventException {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}
	
	/**
	 * Create a publisher configured for the running BrokerService
	 * @param topic
	 * @return
	 */
	public <T extends StatusBean> IPublisher<T> makePublisher(String topic) {
		if (topic == null) topic = IEventService.STATUS_TOPIC;
		return evServ.createPublisher(uri, topic);
	}

}
