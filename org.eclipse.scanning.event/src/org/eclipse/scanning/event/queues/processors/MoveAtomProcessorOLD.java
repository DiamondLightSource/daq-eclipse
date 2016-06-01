package org.eclipse.scanning.event.queues.processors;

import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.queues.IQueueProcessor;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositioner;
import org.eclipse.scanning.event.queues.beans.MoveAtom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MoveAtomProcessor takes the fields of a {@link MoveAtom} and from them makes
 * an {@link IPosition}. This is then used to set the position of referenced 
 * motors by calling to an {@link IPositioner} from the scan service. There is 
 * no monitoring of the progress of a motor move as it is happening, the 
 * processor just waits for the set call to return.
 * 
 * TODO Add test of wrong bean type before cast.
 * 
 * @author Michael Wharmby
 *
 * @param <T> Bean implementing {@link Queueable}, but must be a 
 *            {@link MoveAtom}.
 */
public class MoveAtomProcessorOLD implements IQueueProcessor {

	private static Logger logger = LoggerFactory.getLogger(MoveAtomProcess.class);

	@Override
	public <T extends Queueable> IConsumerProcess<T> makeProcess(T bean,
			IPublisher<T> publisher, boolean blocking)  throws EventException {
		return new MoveAtomProcess<T>(bean, publisher, blocking);
	}
	
	public synchronized <T extends Queueable> IConsumerProcess<T> makeProcessWithScanServ(T bean,
			IPublisher<T> publisher, boolean blocking, IRunnableDeviceService scanServ)  throws EventException {
		MoveAtomProcess<T> moveProc = new MoveAtomProcess<T>(bean, publisher, blocking);
		moveProc.setRunnableDeviceService(scanServ);
		return moveProc;
	}
	

	class MoveAtomProcess<T extends Queueable> extends AbstractQueueProcessorOLD<T> {

		private IRunnableDeviceService scanService;
		private IPositioner positioner;
		private MoveAtom atom;

		private Thread th;

		public MoveAtomProcess(T bean, IPublisher<T> publisher, boolean blocking) throws EventException {
			super(bean, publisher);
			this.blocking = blocking;

			//We know the bean is of type MoveAtom as this processor wouldn't get
			//called otherwise
			atom = (MoveAtom) bean;
		}

		/**
		 * For use in testing! 
		 * @param evServ - class implementing IEventService
		 */
		public synchronized void setRunnableDeviceService(IRunnableDeviceService scanServ) {
			scanService = scanServ;
		}

		@Override
		public void execute() throws EventException {
			broadcast(bean, Status.RUNNING);

			bean.setMessage("Creating position from configured values");
			broadcast(bean, Status.RUNNING);
			final IPosition target = new MapPosition(atom.getPositionConfig());
			broadcast(bean, 10d);

			//Get the positioner
			bean.setMessage("Getting device positioner");
			broadcast(bean, Status.RUNNING);
			try {
				positioner = scanService.createPositioner();
			} catch (ScanningException se) {
				logger.error("Failed to get device positioner: "+se.getMessage());
				bean.setMessage("Failed to get device positioner");
				broadcast(bean, Status.FAILED);
			}
			broadcast(bean, 20d);

			//Create a new thread to call the move in
			th = new Thread(new Runnable() {

				@Override
				public void run() {
					//Move device(s)
					bean.setMessage("Moving device(s) to requested position");
					try {
						broadcast(bean, Status.RUNNING);
						positioner.setPosition(target);
						runComplete = true;
					} catch(Exception e) {
						logger.error("Moving device(s) failed with: "+e.getMessage());
						bean.setMessage("Moving device(s) failed: "+e.getMessage());
						try{
							broadcast(bean, Status.FAILED);
						} catch(EventException evEx) {
							logger.error("Broadcasting bean failed with: "+evEx.getMessage());
						}
					}
				}
			});
			th.setDaemon(true);
			th.setPriority(Thread.MAX_PRIORITY);
			th.start();

			while (!runComplete) {
				try {
					Thread.sleep(loopSleepTime);
				} catch (InterruptedException e) {
					throw new EventException(e);
				}

				if (terminated) {
					th.interrupt();
					positioner.abort();
					broadcast(bean, Status.TERMINATED);
					return;
				}
			}

			bean.setMessage("Device move(s) completed.");
			broadcast(bean, Status.COMPLETE, 100d);
		}

		@Override
		public void terminate() throws EventException {
			terminated = true;	
		}

	}

}
