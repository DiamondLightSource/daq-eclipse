package org.eclipse.scanning.malcolm.core;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.malcolm.State;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnectorService;
import org.eclipse.scanning.api.malcolm.connector.MessageGenerator;
import org.eclipse.scanning.api.malcolm.event.IMalcolmListener;
import org.eclipse.scanning.api.malcolm.event.MalcolmEventBean;
import org.eclipse.scanning.api.malcolm.message.JsonMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO FIXME This class contains some things that will be dealt with by Malcolm like the ReentrantLock
// They will be removed eventually... For now this is part of the Mock and it is not clear if the real
// Malcolm connection will need to deal with locking (depending on wether it passes the tests or not!)


/**
 * Base class for non-pausable Malcolm devices
 *
 */
public abstract class AbstractMalcolmDevice<T> implements IMalcolmDevice<T> {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractMalcolmDevice.class);

	// Fields
	protected String              name;
	
	// Events
	protected MalcolmEventDelegate eventDelegate;
	
	// Connection to serilization to talk to the remote object
	protected MessageGenerator<JsonMessage> connectionDelegate;
	
	public AbstractMalcolmDevice(IMalcolmConnectorService<JsonMessage> connector) throws MalcolmDeviceException {
		
        try {
    		this.connectionDelegate = connector.createDeviceConnection(this);
			this.eventDelegate = new MalcolmEventDelegate(new URI("tcp://sci-serv5.diamond.ac.uk:61616"), name, connector);
		} catch (URISyntaxException e) {
			throw new MalcolmDeviceException(this, "Internal error, cannot create event delegate!", e);
		}
	}
		
	/**
	 * Enacts any pre-actions or conditions before the device attempts to run the task block.
	 *  
	 * @throws Exception
	 */
	protected void beforeExecute() throws Exception {
        logger.debug("Entering beforeExecute, state is " + getState());	
	}
	
	
	/**
	 * Enacts any post-actions or conditions after the device completes a run of the task block.
	 *  
	 * @throws Exception
	 */
	protected void afterExecute() throws Exception {
        logger.debug("Entering afterExecute, state is " + getState());	
	}
	
	protected void setTemplateBean(MalcolmEventBean bean) {
		eventDelegate.setTemplateBean(bean);
	}
	
	/**
	 * Not supported in non-pausable device
	 */
	@Override
	public void pause() throws MalcolmDeviceException {
		throw new MalcolmDeviceException(this, "This device is not pausable");
	}

	/**
	 * Not supported in non-pausable device
	 */
	@Override
	public void resume() throws MalcolmDeviceException {
		throw new MalcolmDeviceException(this, "This device is not resumable");
	}


	protected void close() throws Exception {
		eventDelegate.close();
	}
	
	@Override
	public void dispose() throws MalcolmDeviceException {
		try {
			try {
			    if (getState().isRunning()) abort();
			} finally {
			   close();
			}
		} catch (Exception e) {
			throw new MalcolmDeviceException(this, "Cannot dispose of '"+getName()+"'!", e);
		}
	}


	@Override
	public URI getURI() {
		return eventDelegate.getURI();
	}

	@Override
	public URI setURI(URI uri) throws MalcolmDeviceException {
		return eventDelegate.setURI(uri);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getTopicName() {
		return eventDelegate.getTopicName();
	}

	@Override
	public void setTopicName(String topicName) {
		eventDelegate.setTopicName(topicName);
	}


	@Override
	public void addMalcolmListener(IMalcolmListener l) {
		eventDelegate.addMalcolmListener(l);
	}

	@Override
	public void removeMalcolmListener(IMalcolmListener l) {
		eventDelegate.removeMalcolmListener(l);
	}

	@Override
	public void configure(T params) throws MalcolmDeviceException {
		throw new MalcolmDeviceException(this, "Configure has not been implemented!");
	}

	protected void sendEvent(MalcolmEventBean event) throws Exception {
		eventDelegate.sendEvent(event);
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractMalcolmDevice other = (AbstractMalcolmDevice) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
