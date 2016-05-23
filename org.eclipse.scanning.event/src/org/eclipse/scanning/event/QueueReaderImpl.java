package org.eclipse.scanning.event;

import java.net.URI;
import java.util.Comparator;
import java.util.List;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IQueueReader;

public class QueueReaderImpl<T> extends AbstractConnection implements IQueueReader<T> {

	private IEventService eservice;
	private Class<T> beanClass;

	QueueReaderImpl(URI uri, String qName, IEventService service) {
		super(uri, null, service.getEventConnectorService());
		setSubmitQueueName(qName);
		this.eservice = service;
	}

	@Override
	public List<T> getQueue() throws EventException {
					
		QueueReader<T> reader = new QueueReader<T>(getConnectorService(), null);
		try {
			return reader.getBeans(uri, getSubmitQueueName(), beanClass);
		} catch (Exception e) {
			throw new EventException("Cannot get the beans for queue " + getSubmitQueueName(), e);
		}
	}

	@Override
	public Class<T> getBeanClass() {
		return beanClass;
	}

	@Override
	public void setBeanClass(Class<T> beanClass) {
		this.beanClass = beanClass;
	}
	

}
