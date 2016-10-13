package org.eclipse.scanning.test.event.queues.mocks;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.status.StatusBean;

public class MockSubmitter<T extends StatusBean> implements ISubmitter<T> {
	
	private List<T> submittedBeans;
	private String uniqueId;
	
	public MockSubmitter() {
		submittedBeans = new ArrayList<>();
	}
	
	public void resetSubmitter() {
		submittedBeans.clear();
	}

	@Override
	public String getStatusSetName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setStatusSetName(String queueName) throws EventException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getSubmitQueueName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSubmitQueueName(String queueName) throws EventException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<T> getQueue(String queueName, String fieldName) throws EventException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clearQueue(String queueName) throws EventException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cleanQueue(String queueName) throws EventException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean reorder(T bean, String queueName, int amount) throws EventException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean remove(T bean, String queueName) throws EventException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean replace(T bean, String queueName) throws EventException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Class<T> getBeanClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setBeanClass(Class<T> beanClass) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<T> getQueue() throws EventException {
		return submittedBeans;
	}

	@Override
	public void disconnect() throws EventException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public URI getUri() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IEventConnectorService getConnectorService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void submit(T bean) throws EventException {
		submit(bean, true);
	}

	@Override
	public void submit(T bean, boolean prepareBean) throws EventException {
		if (uniqueId==null) {
			uniqueId = bean.getUniqueId()!=null ? bean.getUniqueId() : UUID.randomUUID().toString();
		}
		if (prepareBean) {
			if (bean.getUniqueId()==null) bean.setUniqueId(uniqueId);
			if (getTimestamp()>0) bean.setSubmissionTime(getTimestamp());
		}
		
		submittedBeans.add(bean);
	}

	@Override
	public void blockingSubmit(T bean) throws EventException, InterruptedException, IllegalStateException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getStatusTopicName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setStatusTopicName(String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean reorder(T bean, int amount) throws EventException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean remove(T bean) throws EventException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean replace(T bean) throws EventException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getUniqueId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setUniqueId(String uniqueId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getPriority() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setPriority(int priority) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long getLifeTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setLifeTime(long lifeTime) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long getTimestamp() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setTimestamp(long timestamp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isDisconnected() {
		// TODO Auto-generated method stub
		return false;
	}

}
