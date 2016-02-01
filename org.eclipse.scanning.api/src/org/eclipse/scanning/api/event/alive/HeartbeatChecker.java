package org.eclipse.scanning.api.event.alive;

import java.net.URI;

import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.ISubscriber;

/**
 * Checks for the heartbeat of a named consumer.
 * 
 * @author Matthew Gerring
 *
 */
public class HeartbeatChecker {
	
	private static IEventService eventService;
	public HeartbeatChecker() {
		// Just for OSGi
	}
	
	private URI    uri;
	private String consumerName;
	private long   listenTime;
	private volatile boolean ok = false;
	
	public HeartbeatChecker(URI uri, String consumerName, long listenTime) {
		this.uri          = uri;
		this.consumerName = consumerName;
		this.listenTime   = listenTime;
	}
	
	public void checkPulse() throws Exception {
		
		ISubscriber<IHeartbeatListener>	subscriber = eventService.createSubscriber(uri, IEventService.HEARTBEAT_TOPIC);
        ok = false;
        
        try {
        	subscriber.addListener(new IHeartbeatListener() {
        		@Override
        		public void heartbeatPerformed(HeartbeatEvent evt) {
        			HeartbeatBean bean = evt.getBean();
        			if (!consumerName.equals(bean.getConsumerName())) {
        				return;
        			}
        			ok = true;
        		}
        	});

            Thread.sleep(listenTime);
            
            if (!ok) throw new Exception(consumerName+" Consumer heartbeat absent.\nIt is either stopped or unresponsive.\nPlease contact your support representative.");
        	

        } finally {
        	subscriber.disconnect();
        }
	}

	public static IEventService getEventService() {
		return eventService;
	}

	public static void setEventService(IEventService eventService) {
		HeartbeatChecker.eventService = eventService;
	}

}
