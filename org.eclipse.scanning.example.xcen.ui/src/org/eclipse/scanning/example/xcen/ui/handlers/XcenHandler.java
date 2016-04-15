package org.eclipse.scanning.example.xcen.ui.handlers;

import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.ConsumerConfiguration;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.api.ui.IHandler;
import org.eclipse.scanning.example.xcen.beans.XcenBean;

public class XcenHandler implements IHandler<XcenBean> {

	protected IEventService eventService;
	protected ConsumerConfiguration conf;

	@Override
	public void init(IEventService eventService, ConsumerConfiguration conf) {
		this.eventService = eventService;
		this.conf = conf;
	}

	@Override
	public boolean isHandled(StatusBean bean) {
		return bean instanceof XcenBean;
	}

}
