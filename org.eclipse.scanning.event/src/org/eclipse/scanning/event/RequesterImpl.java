package org.eclipse.scanning.event;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.IdBean;
import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.core.ResponseConfiguration;
import org.eclipse.scanning.api.event.core.ResponseConfiguration.ResponseType;

class RequesterImpl<T extends IdBean> extends AbstractRequestResponseConnection implements IRequester<T> {
	
	private ResponseConfiguration responseConfiguration;
	

	RequesterImpl(URI uri, String reqTopic, String resTopic, IEventService eservice) {
		super(uri, reqTopic, resTopic, eservice);
		responseConfiguration = new ResponseConfiguration(ResponseType.ONE, 100, TimeUnit.MILLISECONDS);
	}
	
	@Override
	public void setTimeout(long time, TimeUnit unit) {
		responseConfiguration.setTimeout(time, unit);
	}

	@Override
	public T post(final T request) throws EventException, InterruptedException {

		// Something to listen
        final ISubscriber<IBeanListener<T>>  receive = eservice.createSubscriber(getUri(), getResponseTopic());
        
        // Something to send
        final IPublisher<T>  send    = eservice.createPublisher(getUri(), getRequestTopic());
        
        try {
        	// Just listen to our id changing.
	        receive.addListener(request.getUniqueId(), new IBeanListener<T>() {
				@Override
				public void beanChangePerformed(BeanEvent<T> evt) {
					T response = evt.getBean();
					request.merge(response);  // The bean must implement merge, for instance DeviceRequest.
					responseConfiguration.countDown();
				}
			});
	        
	        // Send the request
	        send.broadcast(request);
	        
	        responseConfiguration.latch(); // Wait or die trying
	        
	        return request;
	        
        } finally {
        	
	        receive.disconnect();
	        send.disconnect();
        }
	}

	public ResponseConfiguration getResponseConfiguration() {
		return responseConfiguration;
	}

	public void setResponseConfiguration(ResponseConfiguration responseConfiguration) {
		this.responseConfiguration = responseConfiguration;
	}

}
