package org.eclipse.scanning.api.event;

import java.util.EventListener;

public interface IEventListener<T> extends EventListener {

	public Class<T> getBeanClass();
}
