package org.eclipse.scanning.test.event.queues.dummy;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.event.queues.processors.AbstractQueueProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public abstract class DummyProcessor <P extends Queueable> extends AbstractQueueProcessor<P> {

	private static final Logger logger = LoggerFactory.getLogger(DummyProcessor.class);

	@Override
	public void execute() throws EventException {
		setExecuted();
		if (!(queueBean.equals(broadcaster.getBean()))) throw new EventException("Beans on broadcaster and processor differ");

		broadcaster.broadcast(Status.RUNNING, 0d);

		for (int i = 0; i < 10; i++) {
			if (isTerminated()) {
				broadcaster.broadcast(Status.TERMINATED);
				return;
			}
			logger.debug("DummyProcessor ("+queueBean.getClass().getSimpleName()+" - "+queueBean.getName()+"): "+queueBean.getPercentComplete());
			System.out.println("DummyProcessor ("+queueBean.getClass().getSimpleName()+" - "+queueBean.getName()+"): "+queueBean.getPercentComplete());
			broadcaster.broadcast(new Double(i*10));

			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				logger.error("Dummy process sleeping failed", e);
				throw new EventException(e);
			}
		}
		broadcaster.broadcast(Status.COMPLETE, 100d, "Dummy process complete (no software run)");
	}

	@Override
	public void pause() throws EventException {
		// TODO Auto-generated method stub
	}

	@Override
	public void resume() throws EventException {
		// TODO Auto-generated method stub
	}

	@Override
	public void terminate() throws EventException {
		setTerminated();
	}

}

