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
	
	public void stop() throws Exception {
		if (unitTest) {
			evServ = null;
		}
		
		stopBroker();
		active = false;
	}
	
	public boolean isActive() {
		return active;
	}
	
	public URI getURI() throws Exception {
		if (!isActive()) {
			System.out.println("TestBroker not started; starting (is unit test?: "+unitTest+")...");
			start(unitTest);
		}
		return uri;
	}
	
	public IEventService getEventService() {
		return evServ;
	}
	
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

}
