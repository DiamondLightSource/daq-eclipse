package org.eclipse.scanning.event.dry;

import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.core.IProcessCreator;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.status.StatusBean;

public class DryRunCreator implements IProcessCreator<StatusBean> {

	@Override
	public IConsumerProcess<StatusBean> createProcess(StatusBean bean, IPublisher<StatusBean> statusNotifier) {
		return new DryRunProcess(bean, statusNotifier);
	}

}
