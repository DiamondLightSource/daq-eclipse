package org.eclipse.scanning.event.queues.processors;

import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.queues.beans.MoveAtom;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositioner;
import org.eclipse.scanning.event.queues.QueueProcess;
import org.eclipse.scanning.event.queues.ServicesHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MoveAtomProcess reads the values included in a {@link MoveAtom} and 
 * instructs the motors detailed in the atom to move to these positions.
 * 
 * It uses the server's {@link IRunnableDeviceService} to create an 
 * {@link IPositioner} which it passes the target positions to.
 * 
 * TODO Implement reporting back of the percent complete (when it might 
 *      become available).
 * TODO Implement pausing/resuming when implemented in the IPositioner system.
 * 
 * @author Michael Wharmby
 *
 */
public class MoveAtomProcess<T extends Queueable> extends QueueProcess<MoveAtom, T> {
	
	public static final String BEAN_CLASS_NAME = MoveAtom.class.getName();
	
	private static Logger logger = LoggerFactory.getLogger(MoveAtomProcess.class);
	
	//Scanning infrastructure
	private final IRunnableDeviceService deviceService;
	private IPositioner positioner;
	
	//For processor operation
	private Thread moveThread;
	
	/**
	 * Create a MoveAtomProcess to position motors on the beamline. 
	 * deviceService ({@link IRunnableDeviceService}) is configured using OSGi 
	 * through {@link ServicesHolder}.
	 */
	public MoveAtomProcess(T bean, IPublisher<T> publisher, Boolean blocking) throws EventException {
		super(bean, publisher, blocking);
		//Get the deviceService from the OSGi configured holder.
		deviceService = ServicesHolder.getDeviceService();
	}

	@Override
	public Class<MoveAtom> getBeanClass() {
		return MoveAtom.class;
	}

	@Override
	public void run() throws EventException, InterruptedException {
		executed = true;
		broadcast(Status.RUNNING,"Creating position from configured values.");
		
		final IPosition target = new MapPosition(queueBean.getPositionConfig());
		broadcast(10d);
		
		//Get the positioner
		broadcast(Status.RUNNING, "Getting device positioner.");
		try {
			positioner = deviceService.createPositioner();
		} catch (ScanningException se) {
			broadcast(Status.FAILED, "Failed to get device positioner: \""+se.getMessage()+"\".");
			logger.error("Failed to get device positioner in "+queueBean.getName()+": \""+se.getMessage()+"\".");
			throw new EventException("Failed to get device positioner", se);
		}
		broadcast(20d);
				
		//Create a new thread to call the move in
		moveThread = new Thread(new Runnable() {
			
			/*
			 * DO NOT SET FINAL STATUSES IN THIS THREAD - set them in the post-match analysis
			 */
			@Override
			public synchronized void run() {
				//Move device(s)
				try {
					broadcast(Status.RUNNING, "Moving device(s) to requested position.");
					positioner.setPosition(target);
					
					//Check whether we received an interrupt whilst setting the positioner
					if (Thread.currentThread().isInterrupted()) throw new InterruptedException("Move interrupted.");
					//Completed cleanly
					broadcast(99.5);
					processLatch.countDown();
				} catch (Exception ex) {
					if (isTerminated()) {
						positioner.abort();
						processLatch.countDown();
					} else {
						reportFail(ex);
					}
				}
			}

			private void reportFail(Exception ex) {
				logger.error("Moving device(s) in '"+queueBean.getName()+"' failed with: \""+ex.getMessage()+"\".");
				try {
					//Bean has failed, but we don't want to set a final status here.
					broadcast(Status.RUNNING, "Moving device(s) in '"+queueBean.getName()+"' failed: \""+ex.getMessage()+"\".");
				} catch(EventException evEx) {
					logger.error("Broadcasting bean failed with: \""+evEx.getMessage()+"\".");
				} finally {
					processLatch.countDown();
				}
			}
		});
		moveThread.setDaemon(true);
		moveThread.setPriority(Thread.MAX_PRIORITY);
		moveThread.start();
	}
	
	@Override
	public void terminate() throws EventException {
		moveThread.interrupt();
		terminated = true;
		super.terminate();
	}
	
	@Override
	public void pause() throws EventException {
		logger.error("Pause/resume not implemented on MoveAtom");
		throw new EventException("Pause/resume not implemented on MoveAtom");
	}

	@Override
	public void resume() throws EventException {
		logger.error("Pause/resume not implemented on MoveAtom");
		throw new EventException("Pause/resume not implemented on MoveAtom");
	}

		
	@Override
	public void postMatchAnalysis() throws EventException {
		//Post-match analysis - set all final statuses here!
		if (isTerminated()) {
			broadcast("Move aborted before completion (requested).");
			return;
		}
		
		if (queueBean.getPercentComplete() >= 99.5) {
			//Clean finish
			broadcast(Status.COMPLETE, 100d, "Device move(s) completed.");
		} else {
			//Scan failed - don't set anything here as messages should have 
			//been updated elsewhere
			positioner.abort();
			broadcast(Status.FAILED);
		}
	}
}
