package org.eclipse.scanning.event;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.commons.lang.ClassUtils;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.IEventListener;
import org.eclipse.scanning.api.event.IdBean;
import org.eclipse.scanning.api.event.alive.HeartbeatBean;
import org.eclipse.scanning.api.event.alive.HeartbeatEvent;
import org.eclipse.scanning.api.event.alive.IHeartbeatListener;
import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.IScanListener;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanEvent;
import org.eclipse.scanning.api.event.scan.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("rawtypes")
class SubscriberImpl<T extends IEventListener> extends AbstractConnection implements ISubscriber<T> {
	
	private static final Logger logger = LoggerFactory.getLogger(SubscriberImpl.class);
	
	private static UUID DEFAULT_KEY = UUID.randomUUID(); // Does not really matter what key is used for the default collection.

	private Map<UUID, Collection<T>>  slisteners; // Scan listeners
	private Map<UUID, Collection<T>>  hlisteners; // Scan listeners
	@SuppressWarnings("rawtypes")
	private Map<Class, DiseminateHandler> dMap;
	
	private MessageConsumer scanConsumer, hearbeatConsumer;
	
	public SubscriberImpl(URI uri, String topic, IEventConnectorService service) {
		super(uri, topic, service);
		slisteners = new ConcurrentHashMap<UUID, Collection<T>>(31); // Concurrent overkill?
		hlisteners = new ConcurrentHashMap<UUID, Collection<T>>(31); // Concurrent overkill?
		
		dMap       = createDiseminateHandlers();
	}

	@Override
	public void addListener(T listener) throws EventException {
		addListener(DEFAULT_KEY, listener);
	}

	@Override
	public void addListener(UUID scanID, T listener) throws EventException{
		registerListener(scanID, listener, slisteners);
		if (scanConsumer == null) {
			try {
				scanConsumer = createConsumer(getTopicName(), listener.getBeanClass(), slisteners);
			} catch (JMSException e) {
				throw new EventException("Cannot subscribe to topic "+getTopicName()+" with URI "+uri, e);
			}
		}
	}

	private MessageConsumer createConsumer(final String    topicName, 
			                               final Class<?>  staticClass, 
			                               final Map<UUID, Collection<T>> listeners) throws JMSException {
		
		Topic topic = super.createTopic(topicName);
		

       	final MessageConsumer consumer = session.createConsumer(topic);
    	MessageListener listener = new MessageListener() {
    		public void onMessage(Message message) {
    			
    			try {
	    			TextMessage txt   = (TextMessage)message;
	    			String      json  = txt.getText(); 
	    			Class<?>    clazz = staticClass !=null
			                          ? staticClass
			                          : EventServiceImpl.getClassFromJson(json);
	    			
	    			Object bean = service.unmarshal(json, clazz);
	    			diseminate(bean, listeners); // We simply use the event thread from JMS for this.
	    			
    			} catch (Exception ne) {
    				
    				if (ne.getClass().getName().contains("com.fasterxml.jackson")) {
        				logger.error("JSON Serilization Error! Have you set the bean class on the "+getClass().getSimpleName(), ne);     
        				System.out.println("Bean class is "+staticClass);
	   				} else {    				
	    				logger.error("Internal error! - Unable to process an event!", ne);
	   				}
    				ne.printStackTrace(); // Unit tests without log4j config show this one.
    			}
    		}
    	};
    	consumer.setMessageListener(listener);
        return consumer;
	}
	
	private void diseminate(Object bean, Map<UUID, Collection<T>> listeners) throws EventException {
		diseminate(bean, listeners.get(DEFAULT_KEY));  // general listeners
		if (bean instanceof IdBean) {
			IdBean idBean = (IdBean)bean;
		    diseminate(bean, listeners.get(idBean.getId())); // scan specific listeners, if any
		}
	}

	private void diseminate(Object bean, Collection<T> listeners) throws EventException {
		
		if (listeners==null)     return;
		if (listeners.isEmpty()) return;
		final EventListener[] ls = listeners.toArray(new EventListener[listeners.size()]);
		for (EventListener listener : ls) {
			@SuppressWarnings("unchecked")
			List<Class<?>> types = ClassUtils.getAllInterfaces(listener.getClass());
			boolean diseminated = false;
			for (Class<?> type : types) {
				DiseminateHandler handler = dMap.get(type);
				if (handler==null) continue;
				handler.diseminate(bean, listener);
				diseminated = true;
			}
			if (!diseminated) throw new EventException("The handler for listener type "+listener.getClass()+" does not exist!");

		}
	}

	@SuppressWarnings("rawtypes")
	private Map<Class, DiseminateHandler> createDiseminateHandlers() {
		
		Map<Class, DiseminateHandler> ret = Collections.synchronizedMap(new HashMap<Class, DiseminateHandler>(3));
		
		ret.put(IScanListener.class, new DiseminateHandler() {
			public void diseminate(Object bean, EventListener e) {
				// Used casting because generics got silly
				ScanBean sbean  = (ScanBean)bean;
				IScanListener l = (IScanListener)e;
				
				State now = sbean.getState();
				State was = sbean.getPreviousState();
				if (now!=null && now!=was) {
					l.scanStateChanged(new ScanEvent(sbean));
				}
				
				l.scanEventPerformed(new ScanEvent(sbean));
			}
		});
		ret.put(IHeartbeatListener.class, new DiseminateHandler() {
			public void diseminate(Object bean, EventListener e) {
				// Used casting because generics got silly
				HeartbeatBean hbean = (HeartbeatBean)bean;
				IHeartbeatListener l= (IHeartbeatListener)e;
				l.heartbeatPerformed(new HeartbeatEvent(hbean));
			}
		});
		ret.put(IBeanListener.class, new DiseminateHandler() {
			public void diseminate(Object bean, EventListener e) {
				// Used casting because generics got silly
				IBeanListener<Object> l = (IBeanListener<Object>)e;
				l.beanChangePerformed(new BeanEvent<Object>(bean));
			}
		});


		return ret;
	}
	

	private interface DiseminateHandler {
		public void diseminate(Object bean, EventListener listener) throws ClassCastException;
	}


	private void registerListener(UUID key, T listener, Map<UUID, Collection<T>> listeners) {
		Collection<T> ls = listeners.get(key);
		if (ls == null) {
			ls = new ArrayList<T>(3);
			listeners.put(key, ls);
		}
		ls.add(listener);
	}

	@Override
	public void removeListener(T listener) {
		removeListener(DEFAULT_KEY, listener);
	}

	@Override
	public void removeListener(UUID id, T listener) {
		if (slisteners.containsKey(id)) {
			slisteners.get(id).remove(listener);
		}
	}

	@Override
	public void disconnect() throws EventException {
		try {
			slisteners.clear();
			hlisteners.clear();
			if (scanConsumer!=null)     scanConsumer.close();
			if (hearbeatConsumer!=null) hearbeatConsumer.close();
			
			super.disconnect();
			
		} catch (JMSException ne) {
			throw new EventException("Internal error - unable to close connection!", ne);
		
		} finally {
			scanConsumer = null;
			hearbeatConsumer = null;
		}
		super.disconnect();
	}
	
	protected boolean isListenersEmpty() {
		return slisteners.isEmpty() && hlisteners.isEmpty();
	}
}
