package org.eclipse.scanning.server.servlet;

import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.servlet.ISubscriberServlet;


/**

    Spring config started servlets, for instance:
    <pre>
    
    {@literal <bean id="myEventServlet" class="org.eclipse.scanning.server.servlet.MyEventServlet">}
    {@literal    <property name="broker"   value="tcp://p45-control:61616" />}
    {@literal    <property name="topic" value="uk.ac.diamond.p45.myActionTopic" />}
    {@literal </bean>}
     
    </pre>
 * 
 * @author Matthew Gerring
 *
 * @param <T>
 */
public abstract class AbstractSubscriberServlet<T> implements ISubscriberServlet<T> {
	
	
	protected IEventService eventService;
	protected String        broker;
	protected String        topic;
	protected String        responseTopic;

	protected ISubscriber<IBeanListener<T>> subscriber;
	protected IPublisher<T> publisher;
	
	protected AbstractSubscriberServlet() {
		this.eventService = Services.getEventService();
	}
	
	@PostConstruct    // Requires spring 3 or better
    public void connect() throws EventException, URISyntaxException {	
    	
    	if (getResponseTopic()!=null) {
    		publisher = eventService.createPublisher(new URI(getBroker()), getResponseTopic());
    	}
    	
    	subscriber = eventService.createSubscriber(new URI(getBroker()), getTopic());
    	subscriber.addListener(new IBeanListener<T>() {
			@Override
			public void beanChangePerformed(BeanEvent<T> evt) {
				try {
					doObject(evt.getBean(), publisher);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
    }
    
	@PreDestroy
	public void disconnect() throws EventException {
    	subscriber.disconnect();
    	if (publisher!=null) publisher.disconnect();
    }

	public String getBroker() {
		return broker;
	}

	public void setBroker(String uri) {
		this.broker = uri;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getResponseTopic() {
		return responseTopic;
	}

	public void setResponseTopic(String responseTopic) {
		this.responseTopic = responseTopic;
	}

}
