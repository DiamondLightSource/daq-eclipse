package org.eclipse.scanning.api.event.dry;

import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.core.IProcessCreator;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.status.StatusBean;

public class FastRunCreator<T extends StatusBean> implements IProcessCreator<T> {
	
	private boolean blocking;
	private long sleep;
	private int start, stop, step;
	
	public FastRunCreator() {
		this(true);
	}

	public FastRunCreator(boolean blocking) {
		this(50, blocking);
	}
	public FastRunCreator(long sleep, boolean blocking) {
		this(0, 100, 10, sleep, blocking);
	}
	public FastRunCreator(int start, int stop, int step, long sleep, boolean blocking) {
		this.sleep = sleep;
		this.blocking = blocking;
		this.start = start;
		this.stop = stop;
		this.step = step;
	}

	@Override
	public IConsumerProcess<T> createProcess(T bean, IPublisher<T> statusNotifier) {
		System.out.println("Creating process for name = "+bean.getName()+" id = "+bean.getUniqueId());
		return new DryRunProcess<T>(bean, statusNotifier, blocking, start, stop, step, sleep);
	}

	public boolean isBlocking() {
		return blocking;
	}

	public void setBlocking(boolean blocking) {
		this.blocking = blocking;
	}

}
