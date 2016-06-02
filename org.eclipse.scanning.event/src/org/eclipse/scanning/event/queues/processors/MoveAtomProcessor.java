package org.eclipse.scanning.event.queues.processors;

import java.util.concurrent.CountDownLatch;

import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.queues.IQueueProcessor;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositioner;
import org.eclipse.scanning.event.queues.QueueServicesHolder;
import org.eclipse.scanning.event.queues.beans.MoveAtom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveAtomProcessor extends AbstractQueueProcessor<MoveAtom> implements IQueueProcessor<MoveAtom> {
	
	private static Logger logger = LoggerFactory.getLogger(MoveAtomProcessor.class);
	
	//Scanning infrastructure
	private final IRunnableDeviceService deviceService;
	private IPositioner positioner;
	
	//Processor operation
	private Thread moveThread;
	private CountDownLatch moveLatch = new CountDownLatch(1);
	
	public MoveAtomProcessor() {
		//Get the deviceService from the OSGi configured holder.
		deviceService = QueueServicesHolder.getDeviceService();
	}

	@Override
	public void execute() throws EventException, InterruptedException {
		setExecuted();
		process.broadcast(Status.RUNNING,"Creating position from configured values");
		
		final IPosition target = new MapPosition(queueBean.getPositionConfig());
		process.broadcast(10d);
		
		//Get the positioner
		process.broadcast(Status.RUNNING, "Getting device positioner");
		try {
			positioner = deviceService.createPositioner();
		} catch (ScanningException se) {
			logger.error("Failed to get device positioner in "+queueBean.getName()+": \""+se.getMessage()+"\"");
			process.broadcast(Status.FAILED, "Failed to get device positioner");
			throw new EventException(se);
		}
		process.broadcast(20d);
		
		//Create a new thread to call the move in
		moveThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				//Move device(s)
				try {
					process.broadcast(Status.RUNNING, "Moving device(s) to requested position");
					positioner.setPosition(target);
					
					//Completed cleanly
					setComplete();
					moveLatch.countDown();
				} catch (InterruptedException inEx) {
					if (!isTerminated()) {
						reportFail(inEx);
					}
				} catch(Exception ex) {
					reportFail(ex);
				}
			}
			
			private void reportFail(Exception ex) {
				logger.error("Moving device(s) in '"+queueBean.getName()+"' failed with: \""+ex.getMessage()+"\"");
				try{
					process.broadcast(Status.FAILED, "Moving device(s) in '"+queueBean.getName()+"' failed: \""+ex.getMessage()+"\"");
				} catch(EventException evEx) {
					logger.error("Broadcasting bean failed with: \""+evEx.getMessage()+"\"");
				}
			}
		});
		moveThread.setDaemon(true);
		moveThread.setPriority(Thread.MAX_PRIORITY);
		moveThread.start();
		
		moveLatch.await();
		
		//Post-match analysis
		if (isTerminated()) {
			moveThread.interrupt();
			positioner.abort();
			return;
		}
		
		//Clean finish
		if (isComplete()) {
			process.broadcast(Status.COMPLETE, 100d, "Device move(s) completed.");
		} else {
			process.broadcast(Status.FAILED, "Processing ended unexpectedly.");
			logger.warn("Processing of '"+queueBean.getName()+"' ended unexpectedly.");
		}
	}

	@Override
	public void pause() throws EventException {
		logger.debug("Pause/resume not possible on MoveAtom");
	}

	@Override
	public void resume() throws EventException {
		logger.debug("Pause/resume not possible on MoveAtom");
	}

	@Override
	public void terminate() throws EventException {
		setTerminated();
		//Early stop
		moveLatch.countDown();
	}

	@Override
	public Class<MoveAtom> getBeanClass() {
		return MoveAtom.class;
	}

}
