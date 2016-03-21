package org.eclipse.scanning.api.event.core;

import org.eclipse.scanning.api.event.EventException;


/**
 * A consumer process is the process run when a consumer starts a job.
 * ProgressibleProcess in the DAWN command server plugin is the inspiration for this plugin.
 * 
 * @author Matthew Gerring
 *
 */
public interface IConsumerProcess<T> {

	/**
	 *  
	 * @return the bean which this process is currently running.
	 */
	T getBean();
	
	/**
	 *  
	 * @return the bean which this process is currently running.
	 */
	IPublisher<T> getPublisher();
	
	/**
	 * Execute the process, if an exception is thrown the process is set to 
	 * failed and the message is the message of the exception.
	 * 
	 * This is the blocking method to run the process. 
	 * The start method will be called by the consumer running the process
	 * and by default terminate is called in the same thread. If blocking is
	 * set to 
	 * 
	 * @throws Exception
	 */
	void execute() throws EventException;
	
	/**
	 * If the process is non-blocking this method will start a thread
	 * which calls execute (and the method will return).
	 * 
	 * By default a process blocks until it is done. isBlocking() and start()
	 * may be overridden to redefine this.
	 * 
	 * @throws EventException
	 */
	default void start() throws EventException {
		
		if (isBlocking()) {
			execute(); // Block until process has run.
		} else {
			final Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						execute();
					} catch (EventException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}, "Run "+getBean());
			thread.setDaemon(true);
			thread.setPriority(Thread.MAX_PRIORITY);
			thread.start();
		}
	}
	
	default boolean isBlocking() {
		return true;
	}
	
	/**
	 * Please provide a termination for the process by implementing this method.
	 * If the process has a stop file, write it now; if it needs to be killed,
	 * get its pid and kill it; if it is running on a cluster, use the qdel or dramaa api.
	 * 
	 * @throws Exception
	 */
	void terminate() throws EventException;

	/**
	 * Call to pause the running process.
	 */
	default void pause() throws EventException {
		
	}
	
	/**
	 * Call to resume the process.
	 */
	default void resume() throws EventException {

	}

}
