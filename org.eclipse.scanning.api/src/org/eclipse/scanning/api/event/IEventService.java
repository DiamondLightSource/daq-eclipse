package org.eclipse.scanning.api.event;

import java.net.URI;

import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.status.StatusBean;

/**
 * 
 * The scanning event service allows one to subscribe to 
 * and broadcast events. It may be backed by the EventBus or
 * plain JMS queues and topics depending on the service implementor.
 * 
 * <code>
 *   IEventService service = ... // OSGi
 *   final IEventSubscriber subscriber = service.createSubscriber(...);
 *   
 *   IScanListener listener = new IScanListener.Stub() { // Listen to any scan
 *       void scanEventPerformed(ScanEvent evt) {
 *           ScanBean scan = evt.getBean();
 *           System.out.println(scan.getName()+" @ "+scan.getPercentComplete());
 *       }    
 *   };
 *   
 *   subscriber.addScanListener(listener);
 *   // Subscribe to anything
 *   </code>
 *   
 *   <code>
 *   IEventService service = ... // OSGi
 *   
 *   final IPublisher publisher = service.createPublisher(...);
 *   final ScanBean scan = new ScanBean(...);
 *   
 *   publisher.broadcast(scan);
 *   
 *   // An event comes internally that the scan has changed state, so we notify like this:
 *   scan.setPercentComplete(3.14);
 *   publisher.broadcast(scan);
 *   
 *   </code>
 * 
 * @author fcp94556
 *
 */
public interface IEventService {

    /**
     * The default topic used for scan events
     */
	public static final String SCAN_TOPIC      = "org.eclipse.scanning.scan.topic";
    /**
     * The default topic used for scan events
     */
	public static final String STATUS_TOPIC      = "org.eclipse.scanning.status.topic";
	
	/**
	 * The default topic used for heartbeat events.
	 */
	public static final String HEARTBEAT_TOPIC = "org.eclipse.scanning.alive.topic";
	
	/**
	 * The default topic used for terminate events.
	 */
	public static final String KILL_TOPIC = "org.eclipse.scanning.terminate.topic";

	/**
	 * The default queue used for holding status events.
	 */
	public static final String SUBMISSION_QUEUE = "org.eclipse.scanning.submission.queue";

	/**
	 * The default queue used for holding status events.
	 */
	public static final String STATUS_QUEUE = "org.eclipse.scanning.status.queue";


	/**
	 * Creates an ISubscriber with the default scan event topic and default heartbeat topic.
	 * Useful on the client for adding event listeners to be notified.
	 * 
	 *  Normally this method is good enough to connect to the acquisition server
	 *  and receive its heartbeat and scan events. 
	 *  
	 *  Scan events have a unique id with which to assertain if a given scan event
	 *  came from given scan.
	 * 
	 * @param uri - the location of the JMS broker
	 * @return IEventManager
	 */
	@SuppressWarnings("rawtypes")
	public <T extends IEventListener> ISubscriber<T> createSubscriber(URI uri, String topicName);
	
	/**
	 * Creates an IEventManager with the default scan event topic and heartbeat topic.
     *
	 * @param service - override default IEventConnectorService which is provided by OSGi. May be null, in 
	 *        which case the default OSGi service will be used or an exception thrown.
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public <T extends IEventListener> ISubscriber<T> createSubscriber(URI uri, String topicName, IEventConnectorService service);

	/**
	 * Creates an IEventPublisher with the default scan event topic and no heartbeat events.
	 * 
	 * @param uri - the location of the JMS broker
	 * @return IEventManager
	 */
	public <U> IPublisher<U> createPublisher(URI uri, String topicName);

	
	/**
	 * Creates an IPublisher which may publish any true bean (serializable to JSON reliably)
	 * 
	 * @param uri - the location of the JMS broker
	 * @param service - override default IEventConnectorService which is provided by OSGi. May be null, in 
	 *        which case the default OSGi service will be used or an exception thrown.
	 * @return IEventManager
	 */
	public <U> IPublisher<U> createPublisher(URI uri, String topicName, IEventConnectorService service);

	/**
	 * Create a submitter for adding a bean of type U onto the queue.
	 * @param uri
	 * @param queueName
	 * @return
	 */
	public <U extends StatusBean> ISubmitter<U> createSubmitter(URI uri, String queueName);
	
	
	/**
	 * Create a submitter for adding a bean of type U onto the queue.
	 * @param uri
	 * @param queueName
	 * @param service, may be null
	 * @return
	 */
	public <U extends StatusBean> ISubmitter<U> createSubmitter(URI uri, String queueName, IEventConnectorService service);

	/**
	 * Create a consumer with the default, status topic, submission queue, status queue and termination topic.
	 * @param uri
	 * @param service
	 * @return
	 */
	public <U extends StatusBean> IConsumer<U> createConsumer(URI uri) throws EventException;
	

	/**
	 * Create a consumer with the submission queue, status queue, status topic and termination topic passed in.
	 * 
	 * @param uri
	 * @param submissionQName
	 * @param statusQName
	 * @param statusTName
	 * @param terminateTName
	 * @param service, may be null, should be null in the OSGi case
	 * @return
	 */
	public <U extends StatusBean> IConsumer<U> createConsumer(URI uri, String submissionQName, 
						                                        String statusQName,
						                                        String statusTName,
						                                        String heartbeatTName, 
						                                        String killTName, 
						                                        IEventConnectorService service) throws EventException;

}
