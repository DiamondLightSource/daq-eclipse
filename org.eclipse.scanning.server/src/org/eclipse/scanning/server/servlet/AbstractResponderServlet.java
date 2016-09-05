package org.eclipse.scanning.server.servlet;

import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.IdBean;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.core.IProcessCreator;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.IResponder;
import org.eclipse.scanning.api.event.core.IResponseCreator;
import org.eclipse.scanning.api.event.core.IResponseProcess;
import org.eclipse.scanning.api.event.servlet.IConsumerServlet;
import org.eclipse.scanning.api.event.servlet.IResponderServlet;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**

    Class used to register a servlet 

    S
 * 
 * @author Matthew Gerring
 *
 * @param <T>
 */
public abstract class AbstractResponderServlet<B extends IdBean> implements IResponderServlet<B> {
	
	protected static final Logger logger = LoggerFactory.getLogger(AbstractResponderServlet.class);

	protected IEventService eventService;
	protected String        broker;
	
	
	// Recommended to configure these as
	protected String        requestTopic  = IEventService.DEVICE_REQUEST_TOPIC;
	protected String        responseTopic = IEventService.DEVICE_RESPONSE_TOPIC;
	
	// The responder for requests to this servlet.
	protected IResponder<B>   responder;

	
	protected AbstractResponderServlet() {
		this.eventService = Services.getEventService();
	}
	
	protected AbstractResponderServlet(String requestTopic, String responseTopic) {
		this();
		this.requestTopic  = requestTopic;
		this.responseTopic = responseTopic;
	}

	@PostConstruct  // Requires spring 3 or better
    public void connect() throws EventException, URISyntaxException {	
    	
		responder = eventService.createResponder(new URI(broker), requestTopic, responseTopic);
		responder.setResponseCreator(new DoResponseCreator());
     	logger.info("Started "+getClass().getSimpleName());
    }
    
	class DoResponseCreator implements IResponseCreator<B> {
		@Override
		public IResponseProcess<B> createResponder(B bean, IPublisher<B> response) throws EventException {
			return AbstractResponderServlet.this.createResponder(bean, response);
		}
	}
   
	@PreDestroy
    public void disconnect() throws EventException {
		responder.disconnect();
    }

	public String getBroker() {
		return broker;
	}

	public void setBroker(String uri) {
		this.broker = uri;
	}

	public String getRequestTopic() {
		return requestTopic;
	}

	public void setRequestTopic(String requestTopic) {
		this.requestTopic = requestTopic;
	}

	public String getResponseTopic() {
		return responseTopic;
	}

	public void setResponseTopic(String responseTopic) {
		this.responseTopic = responseTopic;
	}


}
