package org.eclipse.scanning.event.queues;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.queues.IQueueControllerService;
import org.eclipse.scanning.api.event.queues.beans.IQueueable;

public class QueueControllerService implements IQueueControllerService {

	@Override
	public void start() throws EventException {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop(boolean force) throws EventException {
		// TODO Auto-generated method stub

	}

	@Override
	public <T extends IQueueable> void submit(T bean, String queueID) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T extends IQueueable> void remove(T bean, String queueID) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T extends IQueueable> void reorder(T bean, int move, String queueID) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T extends IQueueable> void pause(T bean, String queueID) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T extends IQueueable> void resume(T bean, String queueID) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T extends IQueueable> void terminate(T bean, String queueID) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pauseQueue(String queueID) {
		// TODO Auto-generated method stub

	}

	@Override
	public void resumeQueue(String queueID) {
		// TODO Auto-generated method stub

	}

	@Override
	public void killQueue(String queueID) {
		// TODO Auto-generated method stub

	}

}
