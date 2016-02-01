package org.eclipse.scanning.api.event;

import java.net.URI;
import java.util.EventListener;

import org.eclipse.scanning.api.INameable;
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
 * <pre>
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
 *
 *
 *
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
 *   </code>
 *   </pre>
 * 
 * @author Matthew Gerring
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
	public static final String STATUS_SET = "org.eclipse.scanning.status.set";
	
	/**
	 * Topic used to tell UI users that a give consumer will be going down.
	 */
	public static final String ADMIN_MESSAGE_TOPIC = "org.eclipse.scanning.consumer.administratorMessage";


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
	public <T extends EventListener> ISubscriber<T> createSubscriber(URI uri, String topicName);
	

	/**
	 * Creates an IEventPublisher with the default scan event topic and no heartbeat events.
	 * 
	 * @param uri - the location of the JMS broker
	 * @return IEventManager
	 */
	public <U> IPublisher<U> createPublisher(URI uri, String topicName);


	/**
	 * Create a submitter for adding a bean of type U onto the queue.
	 * @param uri
	 * @param queueName
	 * @return
	 */
	public <U extends StatusBean> ISubmitter<U> createSubmitter(URI uri, String queueName);
	

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
						                                        String statusTName) throws EventException;

	/**
	 * Create a consumer with the submission queue, status queue, status topic and termination topic passed in.
	 * 
	 * @param uri
	 * @param submissionQName
	 * @param statusQName
	 * @param statusTName
	 * @param killTName
	 * @param service, may be null, should be null in the OSGi case
	 * @return
	 */
	public <U extends StatusBean> IConsumer<U> createConsumer(URI uri, String submissionQName, 
						                                        String statusQName,
						                                        String statusTName,
						                                        String heartbeatTName, 
						                                        String killTName) throws EventException;

	
	/**
	 * Checks the heartbeat can be found and if it cannot in the given time, throws an exception.
	 * @param uri - URI
	 * @param patientName - Name of entity we are checking the hearbeat of
	 * @param listenTime - Time to listener before giving up
	 * @return
	 * @throws EventException
	 */
	public void checkHeartbeat(URI uri, String patientName, long listenTime) throws EventException, InterruptedException;
	
	/**
	 * Checks the topic has things published on it intermittently 
	 * If it does not, in the given time, throws an exception.
	 * @param uri - URI
	 * @param patientName - Name of entity we are checking the hearbeat of
	 * @param listenTime - Time to listener before giving up
	 * @param topicName - The topic, or null to use default heartbeat topic
	 * @param beanClass - The bean class that will be broadcast or null to not specify
	 * @return
	 * @throws EventException
	 */
	public <T extends INameable> void checkTopic(URI uri, String patientName, long listenTime, String topicName, Class<T> beanClass) throws EventException, InterruptedException;

}
