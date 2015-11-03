package org.eclipse.scanning.api.event.core;

import java.util.List;
import java.util.UUID;

import org.eclipse.scanning.api.event.EventException;

/**
 * 
 * A consumer consumes the submission queue. If a job appears on the queue, it
 * starts to run the job and moves the job to a status queue. As information is
 * run about the job, it publishes topics containing the current state of the bean
 * in the queue (JSON string).
 * 
 * This consumer is intended to replace the consumer in the DAWN command server and
 * be a generic way to run jobs with messaging.
 * 
 * @author Matthew Gerring
 *
 * @param <T> The bean class used for the Queue
 */
public interface IConsumer<T> extends IQueueConnection<T> {
	/**
	 * Get a copy of the current submission queue as a list of beans, type T
	 * @return
	 */
	public List<T> getSubmissionQueue() throws EventException ;
	
	/**
	 * Get a copy of the current status queue as a list of beans, type T
	 * @return
	 */
	public List<T> getStatusQueue() throws EventException ;
	
	/**
	 * The string to define the queue for storing status of scans.
	 * 
	 * @return
	 */
	public String getStatusQueueName();
	
	/**
	 * The string to define the queue for storing status of scans.
	 * @param topic
	 * @throws EventException
	 */
	public void setStatusQueueName(String queueName) throws EventException;
	
	/**
	 * The string to define the queue for storing status of scans.
	 * 
	 * @return
	 */
	public String getStatusTopicName();
	
	/**
	 * The string to define the queue for storing status of scans.
	 * @param topic
	 * @throws EventException
	 */
	public void setStatusTopicName(String queueName) throws EventException;

	/**
	 * The string to define the queue for storing status of scans.
	 * 
	 * @return
	 */
	public String getSubmitQueueName();
	
	/**
	 * The string to define the queue for storing status of scans.
	 * @param topic
	 * @throws EventException
	 */
	public void setSubmitQueueName(String queueName) throws EventException;

	/**
	 * Set the consumer process to run for each job.
	 * @param process
	 * @throws Exception if the alive topic cannot be sent
	 */
	void setRunner(IProcessCreator<T> process) throws EventException ;
	
	/**
	 * The consumer needs the class that it will deserialize to, should be set before starting the consumer.
	 * @param clazz
	 */
	void setBeanClass(Class<T> clazz);
	
	/**
	 * Starts the consumer in new thread and return. Similar to Thread.start()
	 * You must set the runner before calling this method
	 * @throws Exception
	 */
	void start() throws EventException;
	
	/**
	 * Ask the consumer to stop 
	 * @throws EventException
	 */
	void stop() throws EventException;


	/**
	 * Starts the consumer and block. Similar to Thread.run()
	 * You must set the runner before calling this method
	 * @throws Exception
	 */
	void run() throws EventException;
	
	/**
	 * 
	 * @return the current active process which will run jobs 
	 */
	IProcessCreator<T> getRunner();
	
	/**
	 * The topic used to terminate the running process and get the consumer to stop.
	 * @return topic name
	 */
	public String getKillTopicName();
	
	/**
	 * The topic used to terminate the running process and get the consumer to stop.
	 * @return topic name
	 */
	public void setKillTopicName(String terminateName);
	
    /**
     * The string UUID which denotes this consumer
     * @return
     */
	public UUID getConsumerId();
	
	public String getName();
	public void setName(String name);
		
	/**
	 * Call to disconnect all publishers and subscribers when the connection goes down.
	 * @throws EventException
	 */
	public void disconnect() throws EventException;

	
	public boolean isDurable();
	public void setDurable(boolean durable);

	/**
	 * This method will purge the status queue
	 */
	public void clearStatusQueue() throws EventException;

}
