package org.eclipse.scanning.event;

import java.net.URI;

import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.ISubscriber;

/**
 * Checks for the heartbeat of a named consumer.
 * 
 * @author Matthew Gerring
 *
 */
class TopicChecker<T extends INameable> {
	
	private URI    uri;
	private String consumerName;
	private long   listenTime;
	private volatile boolean ok = false;
	private String topicName;
	private Class<T> beanClass;
	private IEventService eventService;
	
	public TopicChecker(IEventService eventService, URI uri, String consumerName, long listenTime, String topicName, Class<T> beanClass) {
		this.eventService = eventService;
		this.uri          = uri;
		this.consumerName = consumerName;
		this.listenTime   = listenTime;	
	    this.topicName    = topicName;	
	    this.beanClass    = beanClass;	
	}

	public void checkPulse() throws EventException, InterruptedException {
		
    	ISubscriber<IBeanListener<T>>	subscriber = eventService.createSubscriber(uri, topicName);
        ok = false;
        
        try {
             subscriber.addListener(new IBeanListener<T>() {
        		public void beanChangePerformed(BeanEvent<T> evt) {
        			T bean = evt.getBean();
        			if (!consumerName.equals(bean.getName())) {
        				return;
        			}
        			ok = true;
        		}
        		public Class<T> getBeanClass() {
        			return beanClass;
        		}
        	});
        	

            Thread.sleep(listenTime);
            
            if (!ok) throw new EventException(consumerName+" Consumer heartbeat absent.\nIt is either stopped or unresponsive.\nPlease contact your support representative.");
        	

        } finally {
        	subscriber.disconnect();
        }
	}

}
