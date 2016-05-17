package org.eclipse.scanning.test.event.queues.util;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IConsumer;
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
	
	public static <T extends StatusBean> IConsumer<T> makeConsumer(T bean) {
		try {
			return evServ.createConsumer(uri);
		} catch (Exception ex) {
			System.out.println("Failed to create consumer.");
			ex.printStackTrace();
			return null;
		}
	}
	
	public static <T extends StatusBean> IConsumer<T> makeConsumer(T bean, 
			String submQ, String statQ, String statT) {
		try {
			return evServ.createConsumer(uri, submQ, statQ, statT);
		} catch (Exception ex) {
			System.out.println("Failed to create consumer.");
			ex.printStackTrace();
			return null;
		}
	}
	
	public static <T extends StatusBean> IConsumer<T> makeConsumer(T bean,
			String submQ, String statQ, String statT, String heartT, 
			String cmdT) {
		try {
			return evServ.createConsumer(uri, submQ, statQ, statT, heartT, cmdT);
		} catch (Exception ex) {
			System.out.println("Failed to create consumer.");
			ex.printStackTrace();
			return null;
		}
	}

}
