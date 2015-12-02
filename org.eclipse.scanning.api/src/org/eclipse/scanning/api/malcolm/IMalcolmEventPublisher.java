package org.eclipse.scanning.api.malcolm;

import java.net.URI;

import org.eclipse.scanning.api.malcolm.event.IMalcolmListener;

/**
 * This is an object that can be configured to 
 * send events in topics which can be listened to in other processes.
 * It also allows events to be directly listened to in this VM.
 * 
 * In the case of Malcolm, the publisher receives events from Malcolm 
 * and publishes them to ActiveMQ. These events can then be picked up 
 * and the SWMR file read to get the data.
 * 
 * @author fcp94556
 *
 */
public interface IMalcolmEventPublisher {

	/**
	 * The URI used for the topic connection to the JMS messaging system used.
	 * @return
	 */
	public URI getURI();
	
	/**
	 * The URI used for the topic connection to the JMS messaging system used.
	 * @param uri
	 * @return the old URI, if any.
	 * @throws Exception if the device is already connected to the previous URI and has not been closed.
	 */
	public URI setURI(URI uri) throws MalcolmDeviceException;

	
	/**
	 * The name of the publisher, used in the auto-generated 
	 * topic name but not the full topic name!.
	 * 
	 * @return
	 */
	public String getName();
	
	/**
	 * The name of the publisher, used in the auto-generated 
	 * topic name but not the full topic name!.
	 * 
	 * @param name
	 */
	public void setName(String name);

	/**
	 * The topic string to publish events using JMS/ActiveMQ
	 * Each device will default a topic anyway.
	 * 
	 * NOTE: This is not the topic with with we talk to Malcolm. It is
	 * the topic which we publish events from Malcolm.
	 * 
	 * A topic should be unique for Diamond in case the same broker is used.
	 * 
	 * @return
	 */
	public String getTopicName();
	
	/**
	 * Set the topic name to publish events to. Most devices will
	 * start with a default event based on their name and beamline,
	 * therefore there is normally no need to override the topic.
	 * 
	 * A topic should be unique for Diamond in case the same broker is used.
	 * 
	 * @param topicName
	 */
	public void setTopicName(String topicName);

	/**
	 * Add a listener which can be used instead of monitoring
	 * JMS topics. Useful if the device connection to Malcolm
	 * is running in the same VM.
	 * 
	 * @param l
	 */
	public void addMalcolmListener(IMalcolmListener l);
	
	/**
	 * Remove a listener which can be used instead of monitoring
	 * JMS topics.
     *
	 * @param l
	 */
	public void removeMalcolmListener(IMalcolmListener l);
	
	
	/**
	 * Call to dispose of the resources used send events, clear listener
	 * lists and any other connections.
	 */
	public void dispose() throws MalcolmDeviceException ;


}
