package org.eclipse.scanning.event.dry;

import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.core.IProcessCreator;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.status.StatusBean;

public class DryRunCreator implements IProcessCreator<StatusBean> {
	
	private boolean blocking;
	
	public DryRunCreator() {
		this(true);
	}

	public DryRunCreator(boolean blocking) {
		this.blocking = blocking;
	}

	@Override
	public IConsumerProcess<StatusBean> createProcess(StatusBean bean, IPublisher<StatusBean> statusNotifier) {
		System.out.println("Creating process for name = "+bean.getName()+" id = "+bean.getUniqueId());
		return new DryRunProcess(bean, statusNotifier, blocking);
	}

	public boolean isBlocking() {
		return blocking;
	}

	public void setBlocking(boolean blocking) {
		this.blocking = blocking;
	}

}
