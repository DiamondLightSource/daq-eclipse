package org.eclipse.scanning.event.queues.processors;

import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositioner;
import org.eclipse.scanning.event.queues.QueueServicesHolder;
import org.eclipse.scanning.event.queues.beans.MoveAtom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveAtomProcessor extends AbstractQueueProcessor<MoveAtom> {
	
	private static Logger logger = LoggerFactory.getLogger(MoveAtomProcessor.class);
	
	//Scanning infrastructure
	private final IRunnableDeviceService deviceService;
	private IPositioner positioner;
	
	//Processor operation
	private Thread moveThread;
		
	public MoveAtomProcessor() {
		//Get the deviceService from the OSGi configured holder.
		deviceService = QueueServicesHolder.getDeviceService();
	}

	@Override
	public void execute() throws EventException, InterruptedException {
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
			public void run() {
				//Move device(s)
				try {
					broadcaster.broadcast(Status.RUNNING, "Moving device(s) to requested position.");
					positioner.setPosition(target);
					
					//Completed cleanly
					broadcaster.broadcast(99.5);
					processorLatch.countDown();
				} catch (InterruptedException inEx) {
					if (!isTerminated()) {
						reportFail(inEx);
					}
				} catch(Exception ex) {
					reportFail(ex);
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
		
		processorLatch.await();
		
		//Post-match analysis - set all final statuses here!
		if (isTerminated()) {
			moveThread.interrupt();
			positioner.abort();
			return;
		}
		
		if (queueBean.getPercentComplete() >= 99.5) {
			//Clean finish
			broadcaster.broadcast(Status.COMPLETE, 100d, "Device move(s) completed.");
		} else {
			//Scan failed - don't set anything here as messages should have 
			//been updated elsewhere
			positioner.abort();
			broadcaster.broadcast(Status.FAILED);
		}
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
		setTerminated();
	}

	@Override
	public Class<MoveAtom> getBeanClass() {
		return MoveAtom.class;
	}

}
