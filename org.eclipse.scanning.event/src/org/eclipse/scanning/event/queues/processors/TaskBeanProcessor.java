package org.eclipse.scanning.event.queues.processors;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.event.queues.beans.TaskBean;

public class TaskBeanProcessor extends AbstractQueueProcessor<TaskBean> {

	@Override
	public void execute() throws EventException, InterruptedException {
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub

	}

	@Override
	public Class<TaskBean> getBeanClass() {
		return TaskBean.class;
	}

}
