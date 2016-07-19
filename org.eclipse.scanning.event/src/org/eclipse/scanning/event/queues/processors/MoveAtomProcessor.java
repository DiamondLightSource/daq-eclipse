package org.eclipse.scanning.event.queues.processors;

import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositioner;
import org.eclipse.scanning.event.queues.QueueProcess;
import org.eclipse.scanning.event.queues.QueueServicesHolder;
import org.eclipse.scanning.event.queues.beans.MoveAtom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MoveAtomProcessor reads the values included in a {@link MoveAtom} and 
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
public class MoveAtomProcessor extends AbstractQueueProcessor<MoveAtom> {
	
	private static Logger logger = LoggerFactory.getLogger(MoveAtomProcessor.class);
	
	//Scanning infrastructure
	private final IRunnableDeviceService deviceService;
	private IPositioner positioner;
	
	//For processor operation
	private Thread moveThread;

	private long latRel1 = 0, latRel2 = 0;
	
	
	/**
	 * Create a MoveAtomProcessor which can be used by a {@link QueueProcess}. 
	 * Constructor configures the device service 
	 * ({@link IRunnableDeviceService}) using the instance specified in the 
	 * {@link QueueServicesHolder}.
	 */
	public MoveAtomProcessor() {
		//Get the deviceService from the OSGi configured holder.
		deviceService = QueueServicesHolder.getDeviceService();
	}

	@Override
	public void execute() throws EventException, InterruptedException {
		System.out.println("EXEC START\n*****************");//FIXME
		
		setExecuted();
		if (!(queueBean.equals(broadcaster.getBean()))) throw new EventException("Beans on broadcaster and processor differ");
		broadcaster.broadcast(Status.RUNNING,"Creating position from configured values.");
		
		final IPosition target = new MapPosition(queueBean.getPositionConfig());
		broadcaster.broadcast(10d);
		
		//Get the positioner
		broadcaster.broadcast(Status.RUNNING, "Getting device positioner.");
		try {
			positioner = deviceService.createPositioner();
		} catch (ScanningException se) {
			broadcaster.broadcast(Status.FAILED, "Failed to get device positioner: \""+se.getMessage()+"\".");
			logger.error("Failed to get device positioner in "+queueBean.getName()+": \""+se.getMessage()+"\".");
			throw new EventException("Failed to get device positioner", se);
		}
		broadcaster.broadcast(20d);
				
		//Create a new thread to call the move in
		moveThread = new Thread(new Runnable() {
			
			/*
			 * DO NOT SET FINAL STATUSES IN THIS THREAD - set them in the post-match analysis
			 */
			@Override
			public synchronized void run() {
				//Move device(s)
				try {
					System.out.println("RUN START");//FIXME
					broadcaster.broadcast(Status.RUNNING, "Moving device(s) to requested position.");
					positioner.setPosition(target);
					
					//Check whether we received an interrupt whilst setting the positioner
					if (Thread.currentThread().isInterrupted()) throw new InterruptedException("Move interrupted.");
					//Completed cleanly
					broadcaster.broadcast(99.5);
					processorLatch.countDown();
				} catch (Exception ex) {
					if (isTerminated()) {
						positioner.abort();
						latRel2 = System.currentTimeMillis();
						processorLatch.countDown();
					} else {
						reportFail(ex);
					}
				}
			}

			private void reportFail(Exception ex) {
				logger.error("Moving device(s) in '"+queueBean.getName()+"' failed with: \""+ex.getMessage()+"\".");
				try {
					//Bean has failed, but we don't want to set a final status here.
					broadcaster.broadcast(Status.RUNNING, "Moving device(s) in '"+queueBean.getName()+"' failed: \""+ex.getMessage()+"\".");
				} catch(EventException evEx) {
					logger.error("Broadcasting bean failed with: \""+evEx.getMessage()+"\".");
				} finally {
					processorLatch.countDown();
				}
			}
		});
		moveThread.setDaemon(true);
		moveThread.setPriority(Thread.MAX_PRIORITY);
		moveThread.start();
		System.out.println("Latch (pre-await): "+processorLatch.getCount());//FIXME
		
		processorLatch.await();
		System.out.println("Latch released. ("+processorLatch.getCount()+")");//FIXME
		
		System.out.println("\nisTerminated(): "+isTerminated()+"\n");//FIXME
		
		//Post-match analysis - set all final statuses here!
		if (isTerminated()) {
			broadcaster.broadcast("Move aborted before completion (requested).");
			System.out.println("\nI AM ABORTING:  "+queueBean.getPercentComplete()+"\n");//FIXME
			System.out.println("latRel1="+latRel1);
			System.out.println("Term latRel2="+latRel2);
			return;
		}
		
		if (queueBean.getPercentComplete() >= 99.5) {
			System.out.println("\nI AM COMPLETE: "+queueBean.getPercentComplete()+"\n");//FIXME
			//Clean finish
			broadcaster.broadcast(Status.COMPLETE, 100d, "Device move(s) completed.");
		} else {
			//Scan failed - don't set anything here as messages should have 
			//been updated elsewhere
			positioner.abort();
			broadcaster.broadcast(Status.FAILED);
		}
		System.out.println("latRel1="+latRel1);
		System.out.println("Term latRel2="+latRel2);
	}

	@Override
	public void pause() throws EventException {
		logger.debug("Pause/resume not implemented on MoveAtom");
	}

	@Override
	public void resume() throws EventException {
		logger.debug("Pause/resume not implemented on MoveAtom");
	}

	@Override
	public void terminate() throws EventException {
		moveThread.interrupt();
		setTerminated();
	}

	@Override
	public Class<MoveAtom> getBeanClass() {
		return MoveAtom.class;
	}

}
