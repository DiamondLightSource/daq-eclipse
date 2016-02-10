package org.eclipse.scanning.malcolm.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnectorService;
import org.eclipse.scanning.api.malcolm.connector.MessageGenerator;
import org.eclipse.scanning.api.malcolm.event.IMalcolmListener;
import org.eclipse.scanning.api.malcolm.event.MalcolmEventBean;
import org.eclipse.scanning.api.malcolm.message.JsonMessage;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.AbstractRunnableDevice;
import org.eclipse.scanning.api.scan.ScanningException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Base class for Malcolm devices
 *
 */
public abstract class AbstractMalcolmDevice<T> extends AbstractRunnableDevice<T> implements IMalcolmDevice<T> {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractMalcolmDevice.class);
	
	// Events
	protected MalcolmEventDelegate eventDelegate;
	
	// Connection to serilization to talk to the remote object
	protected MessageGenerator<JsonMessage> connectionDelegate;
	
	public AbstractMalcolmDevice(IMalcolmConnectorService<JsonMessage> connector) throws MalcolmDeviceException {
   		this.connectionDelegate = connector.createDeviceConnection(this);
   		this.eventDelegate = new MalcolmEventDelegate(getName(), connector);
	}
		
	/**
	 * Enacts any pre-actions or conditions before the device attempts to run the task block.
	 *  
	 * @throws Exception
	 */
	protected void beforeExecute() throws Exception {
        logger.debug("Entering beforeExecute, state is " + getDeviceState());	
	}
	
	
	/**
	 * Enacts any post-actions or conditions after the device completes a run of the task block.
	 *  
	 * @throws Exception
	 */
	protected void afterExecute() throws Exception {
        logger.debug("Entering afterExecute, state is " + getDeviceState());	
	}
	
	protected void setTemplateBean(MalcolmEventBean bean) {
		eventDelegate.setTemplateBean(bean);
	}
	
	public void start(final IPosition pos) throws ScanningException, InterruptedException {
		
		final List<Throwable> exceptions = new ArrayList<>(1);
		final Thread thread = new Thread(new Runnable() {
			public void run() {
				try {
					AbstractMalcolmDevice.this.run(pos);
				} catch (ScanningException|InterruptedException e) {
					e.printStackTrace();
					exceptions.add(e);
				}
			}
		}, "Device Runner Thread "+getName());
		thread.start();
		
		// We delay by 500ms just so that we can 
		// immediately throw any connection exceptions
		Thread.sleep(500);
		
		if (!exceptions.isEmpty()) throw new ScanningException(exceptions.get(0));
	}

	protected void close() throws Exception {
		eventDelegate.close();
	}
	
	@Override
	public void dispose() throws MalcolmDeviceException {
		try {
			try {
			    if (getDeviceState().isRunning()) abort();
			} finally {
			   close();
			}
		} catch (Exception e) {
			throw new MalcolmDeviceException(this, "Cannot dispose of '"+getName()+"'!", e);
		}
	}

	@Override
	public void addMalcolmListener(IMalcolmListener l) {
		eventDelegate.addMalcolmListener(l);
	}

	@Override
	public void removeMalcolmListener(IMalcolmListener l) {
		eventDelegate.removeMalcolmListener(l);
	}

	protected void sendEvent(MalcolmEventBean event) throws Exception {
		eventDelegate.sendEvent(event);
	}

}
