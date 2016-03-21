package org.eclipse.scanning.api.event.core;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;

public abstract class AbstractPausableProcess<T extends StatusBean> implements IConsumerProcess<T> {
	
	protected final T                      bean;
	protected final IPublisher<T>          publisher;

	/*
	 * Concurrency design recommended by Keith Ralphs after investigating
	 * how to pause and resume a collection cycle using Reentrant locks.
	 * Design requires these three fields.
	 */
	private ReentrantLock    lock;
	private Condition        paused;
	private volatile boolean awaitPaused;

	protected AbstractPausableProcess(T bean, IPublisher<T> publisher) {
		this.bean = bean;
		this.publisher = publisher;
		this.lock      = new ReentrantLock();
		this.paused    = lock.newCondition();
	}
	
	@Override
	public T getBean() {
		return bean;
	}

	@Override
	public IPublisher<T> getPublisher() {
		return publisher;
	}

	/**
	 * Blocks until process is not paused.
	 * 
	 * @throws EventException
	 */
	protected void checkPaused() throws EventException {
		
		// Check the locking using a condition
		try {
	    	if(!lock.tryLock(1, TimeUnit.SECONDS)) {
	    		throw new EventException("Internal Error - Could not obtain lock to run device!");    		
	    	}
	    	try {
	       	    if (awaitPaused) {
	         		paused.await(); // Until unpaused
	       	    }
	    	} finally {
	    		lock.unlock();
	    	}
		} catch (InterruptedException ne) {
			if (bean.getStatus().isTerminated()) return;
			throw new EventException(ne);
		}
		
	}

	/**
	 * Implements paused using a standard design
	 */
	@Override
	public void pause() throws EventException {
		try {
			lock.lockInterruptibly();
			
			awaitPaused = true;
			
			doPause();
			bean.setPreviousStatus(Status.REQUEST_PAUSE);
			bean.setStatus(Status.PAUSED);
			publisher.broadcast(bean);
			
		} catch (EventException ne) {
			throw ne;
		} catch (Exception ne) {
			throw new EventException(ne);
		} finally {
			lock.unlock();
		}

	}
	
	/**
	 * Override this method to do work on a pause once the pause lock has been received.
	 */
	protected void doPause()  throws Exception {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Implements resume using a standard design
	 */
	@Override
	public void resume() throws EventException {
		
		try {
			lock.lockInterruptibly();
		
			try {
				awaitPaused = false;
				
				doResume();
				bean.setPreviousStatus(Status.REQUEST_RESUME);
				bean.setStatus(Status.RESUMED);
				publisher.broadcast(bean);
				
				// We don't have to actually start anything again because the getMessage(...) call reconnects automatically.
				paused.signalAll();
				
			} finally {
				lock.unlock();
			}
		} catch (EventException ne) {
			throw ne;
		} catch (Exception ne) {
			throw new EventException(ne);
		}

	}

	/**
	 * Override this method to do work on a resume once the pause lock has been received.
	 */
	protected void doResume()  throws Exception {
		// TODO Auto-generated method stub
		
	}


}
