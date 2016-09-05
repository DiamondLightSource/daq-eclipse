package org.eclipse.scanning.event;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.commons.lang.ClassUtils;
import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.IdBean;
import org.eclipse.scanning.api.event.alive.HeartbeatBean;
import org.eclipse.scanning.api.event.alive.HeartbeatEvent;
import org.eclipse.scanning.api.event.alive.IHeartbeatListener;
import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.event.scan.IScanListener;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanEvent;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.scan.event.ILocationListener;
import org.eclipse.scanning.api.scan.event.Location;
import org.eclipse.scanning.api.scan.event.LocationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("rawtypes")
class SubscriberImpl<T extends EventListener> extends AbstractConnection implements ISubscriber<T> {
	
	private static final Logger logger = LoggerFactory.getLogger(SubscriberImpl.class);
	
	private static String DEFAULT_KEY = UUID.randomUUID().toString(); // Does not really matter what key is used for the default collection.

	private Map<String, Collection<T>>    slisteners; // Scan listeners
	private Map<String, Collection<T>>    hlisteners; // Scan listeners
	private Map<Class, DiseminateHandler> dMap;
	private BlockingQueue<DespatchEvent>  queue;
	
	private MessageConsumer scanConsumer, hearbeatConsumer;
	
	private boolean synchronous = true;
	
	public SubscriberImpl(URI uri, String topic, IEventConnectorService service) {
		super(uri, topic, service);
		slisteners = new ConcurrentHashMap<String, Collection<T>>(31); // Concurrent overkill?
		hlisteners = new ConcurrentHashMap<String, Collection<T>>(31); // Concurrent overkill?
		dMap       = createDiseminateHandlers();
	}

	@Override
	public void addListener(T listener) throws EventException {
		addListener(DEFAULT_KEY, listener);
	}

	@Override
	public void addListener(String scanID, T listener) throws EventException{
		setConnected(true);
		if (isSynchronous()) createDespatchThread();
		registerListener(scanID, listener, slisteners);
		if (scanConsumer == null) {
			try {
				Class<?> beanClass = listener instanceof IBeanListener ? ((IBeanListener)listener).getBeanClass() : null;
				scanConsumer = createConsumer(getTopicName(), beanClass, slisteners);
			} catch (JMSException e) {
				throw new EventException("Cannot subscribe to topic "+getTopicName()+" with URI "+uri, e);
			}
		}
	}

	private MessageConsumer createConsumer(final String    topicName, 
			                               final Class<?>  beanClass, // Maybe null
			                               final Map<String, Collection<T>> listeners) throws JMSException {
		
		Topic topic = super.createTopic(topicName);
		

       	final MessageConsumer consumer = session.createConsumer(topic);
    	MessageListener listener = new MessageListener() {
    		public void onMessage(Message message) {
    			
    			try {
	    			TextMessage txt   = (TextMessage)message;
	    			String      json  = txt.getText(); 
	    			
	    			Object bean = service.unmarshal(json, beanClass);
	    			diseminate(bean, listeners); // We simply use the event thread from JMS for this.
	    			
    			} catch (Exception ne) {
    				
    				if (ne.getClass().getName().contains("com.fasterxml.jackson")) {
        				logger.error("JSON Serialization Error!", ne);
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
	
	private void diseminate(Object bean, Map<String, Collection<T>> listeners) throws EventException {
		diseminate(bean, listeners.get(DEFAULT_KEY));  // general listeners
		if (bean instanceof IdBean) {
			IdBean idBean = (IdBean)bean;
		    diseminate(bean, listeners.get(idBean.getUniqueId())); // scan specific listeners, if any
		} else if (bean instanceof INameable) {
			INameable namedBean = (INameable)bean;
		    diseminate(bean, listeners.get(namedBean.getName())); // scan specific listeners, if any
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

	private Map<Class, DiseminateHandler> createDiseminateHandlers() {
		
		Map<Class, DiseminateHandler> ret = Collections.synchronizedMap(new HashMap<Class, DiseminateHandler>(3));
		
		ret.put(IScanListener.class, new DiseminateHandler() {
			public void diseminate(Object bean, EventListener e) {
				
				if (!(bean instanceof ScanBean)) return; 
				// This listener must be used with events publishing ScanBean
				// If your scan does not publish ScanBean events then you
				// may listen to it with a standard IBeanListener.
				
				// Used casting because generics got silly
				ScanBean sbean  = (ScanBean)bean;
				IScanListener l = (IScanListener)e;
				
				DeviceState now = sbean.getDeviceState();
				DeviceState was = sbean.getPreviousDeviceState();
				if (now!=null && now!=was) {
					schedule(new DespatchEvent(l, new ScanEvent(sbean), true));
					return;
				} else {
					Status snow = sbean.getStatus();
					Status swas = sbean.getPreviousStatus();
					if (snow!=null && snow!=swas && swas!=null) {
						schedule(new DespatchEvent(l, new ScanEvent(sbean), true));
						return;
					}
				}		
				schedule(new DespatchEvent(l, new ScanEvent(sbean), false));
			}
		});
		ret.put(IHeartbeatListener.class, new DiseminateHandler() {
			public void diseminate(Object bean, EventListener e) {
				// Used casting because generics got silly
				HeartbeatBean hbean = (HeartbeatBean)bean;
				IHeartbeatListener l= (IHeartbeatListener)e;
				schedule(new DespatchEvent(l, new HeartbeatEvent(hbean)));
			}
		});
		ret.put(IBeanListener.class, new DiseminateHandler() {
			public void diseminate(Object bean, EventListener e) {
				// Used casting because generics got silly
				@SuppressWarnings("unchecked")
				IBeanListener<Object> l = (IBeanListener<Object>)e;
				schedule(new DespatchEvent(l, new BeanEvent<Object>(bean)));
			}
		});
		ret.put(ILocationListener.class, new DiseminateHandler() {
			public void diseminate(Object bean, EventListener e) {
				// Used casting because generics got silly
				ILocationListener l = (ILocationListener)e;
				schedule(new DespatchEvent(l, new LocationEvent((Location)bean)));
			}
		});



		return ret;
	}
	
	private void schedule(DespatchEvent event) {
		if (isSynchronous()) {
		    if (queue!=null) queue.add(event);
		} else {
			// TODO FIXME Might not be right...
			final Thread thread = new Thread("Execute event "+getTopicName()) {
				public void run() {
					execute(event); // Use this JMS thread directly to do work.
				}
			};
			thread.setDaemon(true);
			thread.setPriority(Thread.NORM_PRIORITY+1);
			thread.start();
		}
	}


	private interface DiseminateHandler {
		public void diseminate(Object bean, EventListener listener) throws ClassCastException;
	}


	private void registerListener(String key, T listener, Map<String, Collection<T>> listeners) {
		Collection<T> ls = listeners.get(key);
		if (ls == null) {
			ls = new ArrayList<T>(3);
			listeners.put(key.toString(), ls);
		}
		ls.add(listener);
	}

	@Override
	public void removeListener(T listener) {
		removeListener(DEFAULT_KEY, listener);
	}

	@Override
	public void removeListener(String id, T listener) {
		if (slisteners.containsKey(id)) {
			slisteners.get(id).remove(listener);
		}
	}
	
	@Override
	public void removeListeners(String id) {
		slisteners.remove(id);
	}
	
	@Override
	public void clear() {
		slisteners.clear();
		hlisteners.clear();
	}

	@Override
	public void disconnect() throws EventException {
		try {
			clear();
			if (scanConsumer!=null)     scanConsumer.close();
			if (hearbeatConsumer!=null) hearbeatConsumer.close();
			
			super.disconnect();
			
		} catch (JMSException ne) {
			throw new EventException("Internal error - unable to close connection!", ne);
		
		} finally {
			scanConsumer = null;
			hearbeatConsumer = null;
			setConnected(false);
		}
		super.disconnect();
		schedule(DespatchEvent.STOP);
	}
	
	protected boolean isListenersEmpty() {
		return slisteners.isEmpty() && hlisteners.isEmpty();
	}
	
	private boolean connected; 

	private void createDespatchThread() {
		
		if (!isSynchronous()) return; // If asynch we do not run events in order and wait until they return.
		if (queue!=null) return;
		queue      = new LinkedBlockingQueue<>(); // Small, if they do work and things back-up, exceptions will occur.
		
		final Thread despachter = new Thread(new Runnable() {
			public void run() {
				while(isConnected()) {
					try {
						DespatchEvent event = queue.take();
						if (event==DespatchEvent.STOP) return;					
						execute(event);
						
					} catch (RuntimeException e) {
						logger.error("RuntimeException occured despatching event", e);
						continue;
						
					} catch (InterruptedException e) {
						logger.error("Stopping event despatch thread ", e);
						return;
					}
				}
				System.out.println(Thread.currentThread().getName()+" disconnecting events.");
			}
		}, "Submitter despatch thread "+getSubmitQueueName());
		despachter.setDaemon(true);
		despachter.setPriority(Thread.NORM_PRIORITY+1);
		despachter.start();
	}
	
	private void execute(DespatchEvent event) {
		
		if (event==DespatchEvent.STOP) return; // Nothing to do in execute!
		if (event.listener instanceof IHeartbeatListener) ((IHeartbeatListener)event.listener).heartbeatPerformed((HeartbeatEvent)event.object);
		if (event.listener instanceof IBeanListener)      ((IBeanListener)event.listener).beanChangePerformed((BeanEvent)event.object);
		if (event.listener instanceof ILocationListener)  ((ILocationListener)event.listener).locationPerformed((LocationEvent)event.object);
		if (event.listener instanceof IScanListener){
			IScanListener l = (IScanListener)event.listener;
			ScanEvent     e = (ScanEvent)event.object;
			if (event.isStateChange()) {
				l.scanStateChanged(e);
			} else {
				l.scanEventPerformed(e);
			}
		}
	}

	/**
	 * Immutable event for queue.
	 * 
	 * @author fcp94556
	 *
	 */
	private static class DespatchEvent {
		
		public static final DespatchEvent STOP = new DespatchEvent(null, new EventObject(DespatchEvent.class));
		
		protected final EventListener listener;
		protected final EventObject   object;
		protected final boolean       isStateChange;
		
		public DespatchEvent(EventListener listener, EventObject object) {
			this(listener, object, false);
		}
		public DespatchEvent(EventListener listener2, EventObject object2, boolean b) {
			this.listener = listener2;
			this.object   = object2;
			this.isStateChange = b;
		}
		public EventListener getListener() {
			return listener;
		}
		public EventObject getObject() {
			return object;
		}
		public boolean isStateChange() {
			return isStateChange;
		}

	}


	public boolean isConnected() {
		return connected;
	}

	private void setConnected(boolean connected) {
		this.connected = connected;
	}

	public boolean isSynchronous() {
		return synchronous;
	}

	public void setSynchronous(boolean synchronous) {
		this.synchronous = synchronous;
	}
}
