package org.eclipse.scanning.api.ui;

import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.ConsumerConfiguration;
import org.eclipse.scanning.api.event.status.StatusBean;

public interface IHandler<T extends StatusBean> {
	
	default void init(IEventService eventService, ConsumerConfiguration conf) {
		
	}
	
	/**
	 * Defines if this handler can open the result in this bean.
	 * @param bean
	 * @return
	 */
	boolean isHandled(StatusBean bean);

}
