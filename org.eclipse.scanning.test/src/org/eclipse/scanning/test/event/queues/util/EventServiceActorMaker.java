package org.eclipse.scanning.test.event.queues.util;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.core.IProcessCreator;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.event.EventServiceImpl;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

public class EventServiceActorMaker {
	
	private static IEventService evServ;
	private static URI uri;
	
	static {
		ActivemqConnectorService.setJsonMarshaller(new MarshallerService());
		evServ =  new EventServiceImpl(new ActivemqConnectorService());
		
		try {
			uri = new URI("vm://localhost?broker.persistent=false");
		} catch (URISyntaxException e) {
			System.out.println("Failed to create URI for IEventService: "
					+ "vm://localhost?broker.persistent=false");
			e.printStackTrace();
		}
	}
	
	public static IEventService getEventService() {
		return evServ;
	}
	
	public static URI getURI() {
		return uri;
	}
	
	public static <T extends StatusBean> IConsumer<T> makeConsumer(T bean, 
			boolean withRunner) {
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
	
	public static <T extends StatusBean> IConsumer<T> makeConsumer(T bean, 
			String submQ, String statQ, String statT, boolean withRunner) {
		IConsumer<T> cons;
		try {
			cons =  evServ.createConsumer(uri, submQ, statQ, statT);
			if (withRunner) cons.setRunner(makeEmptyRunner(bean));
			return cons;
		} catch (Exception ex) {
			System.out.println("Failed to create consumer.");
			ex.printStackTrace();
			return null;
		}
	}
	
	public static <T extends StatusBean> IConsumer<T> makeConsumer(T bean,
			String submQ, String statQ, String statT, String heartT, 
			String cmdT, boolean withRunner) {
		IConsumer<T> cons;
		try {
			cons = evServ.createConsumer(uri, submQ, statQ, statT, heartT, cmdT);
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
