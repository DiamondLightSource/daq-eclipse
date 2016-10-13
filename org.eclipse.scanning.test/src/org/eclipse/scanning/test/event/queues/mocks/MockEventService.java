package org.eclipse.scanning.test.event.queues.mocks;

import java.net.URI;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.IdBean;
import org.eclipse.scanning.api.event.alive.ConsumerCommandBean;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.IQueueReader;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.core.IResponder;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.event.queues.ServicesHolder;

public class MockEventService implements IEventService {
	
	private Map<String, MockConsumer<? extends StatusBean>> consumers = new HashMap<>();
	
	private MockConsumer<? extends StatusBean> mockConsumer;
	private MockPublisher<?> mockPublisher;
	private MockPublisher<? extends ConsumerCommandBean> mockCmdPub;
	private MockSubmitter<? extends StatusBean> mockSubmitter;

	@Override
	public <T> IQueueReader<T> createQueueReader(URI uri, String queueName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends EventListener> ISubscriber<T> createSubscriber(URI uri, String topicName) {
		return new MockSubscriber<>(uri, topicName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U> IPublisher<U> createPublisher(URI uri, String topicName) {
		if (topicName == ServicesHolder.getQueueService().getCommandTopicName()) {
			return (IPublisher<U>) mockCmdPub;
		}
		return (IPublisher<U>) mockPublisher;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U extends StatusBean> ISubmitter<U> createSubmitter(URI uri, String queueName) {
		return (ISubmitter<U>) mockSubmitter.create(queueName);
	}

	@Override
	public <U extends StatusBean> IConsumer<U> createConsumer(URI uri) throws EventException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U extends StatusBean> IConsumer<U> createConsumer(URI uri, String submissionQName, String statusQName,
			String statusTName) throws EventException {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U extends StatusBean> IConsumer<U> createConsumer(URI uri, String submissionQName, String statusQName,
			String statusTName, String heartbeatTName, String commandTName) throws EventException {
		return (IConsumer<U>) consumers.get(submissionQName);
	}

	@Override
	public <T extends IdBean> IRequester<T> createRequestor(URI uri, String requestTopic, String responseTopic)
			throws EventException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends IdBean> IResponder<T> createResponder(URI uri, String requestTopic, String responseTopic)
			throws EventException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void checkHeartbeat(URI uri, String patientName, long listenTime)
			throws EventException, InterruptedException {
		// TODO Auto-generated method stub

	}

	@Override
	public <T extends INameable> void checkTopic(URI uri, String patientName, long listenTime, String topicName,
			Class<T> beanClass) throws EventException, InterruptedException {
		// TODO Auto-generated method stub

	}

	@Override
	public IEventConnectorService getEventConnectorService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T createRemoteService(URI uri, Class<T> serviceClass) throws EventException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setMockPublisher(MockPublisher<?> mockPublisher) {
		this.mockPublisher = mockPublisher;
	}
	
	public void setMockCmdPublisher(MockPublisher<ConsumerCommandBean> mockCmdPub) {
		this.mockCmdPub = mockCmdPub;
	}
	
	public <U extends StatusBean> void addMockConsumer(MockConsumer<U> mockCons) {
		consumers.put(mockCons.getSubmitQueueName(), mockCons);
	}
	
	public <U extends StatusBean> void setMockSubmitter(MockSubmitter<U> mockSub) {
		this.mockSubmitter = mockSub;
	}
}
