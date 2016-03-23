package org.eclipse.scanning.api.event.core;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IdBean;

public interface IResponseCreator<T extends IdBean> {

	IResponseProcess<T> createResponder(T bean, IPublisher<T> statusNotifier) throws EventException;

}
