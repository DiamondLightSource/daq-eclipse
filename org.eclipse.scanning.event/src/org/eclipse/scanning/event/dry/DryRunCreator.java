package org.eclipse.scanning.event.dry;

import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.core.IProcessCreator;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.status.StatusBean;

public class DryRunCreator<T extends StatusBean> implements IProcessCreator<T> {
	
	private boolean blocking;
	
	public DryRunCreator() {
		this(true);
	}

	public DryRunCreator(boolean blocking) {
		this.blocking = blocking;
	}

	@Override
	public IConsumerProcess<T> createProcess(T bean, IPublisher<T> statusNotifier) {
		System.out.println("Creating process for name = "+bean.getName()+" id = "+bean.getUniqueId());
		return new DryRunProcess<T>(bean, statusNotifier, blocking);
	}

	public boolean isBlocking() {
		return blocking;
	}

	public void setBlocking(boolean blocking) {
		this.blocking = blocking;
	}

}
