package org.eclipse.scanning.test.event.queues.dummy;

import java.util.concurrent.CountDownLatch;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.queues.IQueueProcess;
import org.eclipse.scanning.api.event.queues.IQueueProcessor;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.status.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DummyProcessor <P extends Queueable> implements IQueueProcessor<P> {

	private static final Logger logger = LoggerFactory.getLogger(DummyProcessor.class);
	
	private IQueueProcess<? extends Queueable> process;
	private boolean terminated, executed = false;
	
	protected P dummy;
	protected CountDownLatch execLatch;
	
	@Override
	public void execute() throws EventException {
		if (!(dummy.equals(process.getBean()))) throw new EventException("Beans on QueueProcess and QueueProcessor differ");
		setExecuted();
		execLatch = getLatch();
		
		final Thread thread = new Thread(new Runnable() {
			public void run() {
				try {
					DummyProcessor.this.run();
				} catch (EventException ne) {
					ne.printStackTrace(); // Only a test process!
				}
			}
		});
		thread.setDaemon(true);
		thread.setPriority(Thread.MAX_PRIORITY);
		thread.start();
	}
	
	@Override
	public void pause() throws EventException {
		// TODO Auto-generated method stub
	}

	@Override
	public void resume() throws EventException {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void terminate() throws EventException {
		terminated = true;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Queueable> void setProcessBean(T bean) throws EventException {
		if (isExecuted()) throw new EventException("Cannot change queueProcess after execution started.");
		if (bean.getClass().equals(getBeanClass())) {
			dummy = (P)bean;
		} else {
			throw new EventException("Unsupported bean type");
		}
	}

	@Override
	public void setQueueProcess(IQueueProcess<? extends Queueable> process) throws EventException {
		if (isExecuted()) throw new EventException("Cannot change queueProcess after execution started.");
		this.process = process;
	}
	
	protected abstract CountDownLatch getLatch();
	
	private void run() throws EventException {
		process.broadcast(Status.RUNNING, 0d);

		terminated = false;

		for (int i = 0; i < 100; i++) {

			if (isTerminated()) {
				process.broadcast(Status.TERMINATED);
				return;
			}

			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				logger.error("Dummy process sleeping failed", e);
			}
			System.out.println("DummyProcessor ("+dummy.getClass().getSimpleName()+" - "+dummy.getName()+"): "+dummy.getPercentComplete());
			process.broadcast(new Double(i));
		}
		process.broadcast(Status.COMPLETE, 100d, "Dummy process complete (no software run)");
		if (execLatch != null) execLatch.countDown();
	}
	
	public boolean isTerminated() {
		return terminated;
	}

	public void setTerminated(boolean terminated) {
		this.terminated = terminated;
	}
	
	@Override
	public boolean isExecuted() {
		return executed;
	}
	
	@Override
	public void setExecuted() {
		executed = true;
	}
	

//	class DummyProcess<T extends Queueable> implements IConsumerProcess<T> {
//
//		private final T bean;
//		private final IPublisher<T> statusReporter;
//		private boolean blocking;
//
//		private boolean terminated;
//
//		public DummyProcess(T bean, IPublisher<T> statusNotifier, boolean blocking) {
//			this.bean = bean;
//			this.statusReporter = statusNotifier;
//			this.blocking = blocking;
//		}
//
//		@Override
//		public T getBean() {
//			return bean;
//		}
//
//		@Override
//		public IPublisher<T> getPublisher() {
//			return statusReporter;
//		}
//
//		@Override
//		public void execute() throws EventException {
//			if (isBlocking()) {
//				run(); // Block until process has run.
//			} else {
//				final Thread thread = new Thread(new Runnable() {
//					public void run() {
//						try {
//							DummyProcess.this.run();
//						} catch (EventException ne) {
//							ne.printStackTrace(); // Only a test process!
//						}
//					}
//				});
//				thread.setDaemon(true);
//				thread.setPriority(Thread.MAX_PRIORITY);
//				thread.start();
//			}
//
//		}
//
//		@Override
//		public void terminate() throws EventException {
//			terminated = true;
//
//		}
//
//		private void run() throws EventException {
//			broadcast(bean, Status.RUNNING, 0d);
//
//			terminated = false;
//
//			for (int i = 0; i < 100; i++) {
//
//				if (isTerminated()) {
//					broadcast(bean, Status.TERMINATED);
//					return;
//				}
//
//				try {
//					Thread.sleep(50);
//				} catch (InterruptedException e) {
//					logger.error("Dummy process sleeping failed", e);
//				}
//				System.out.println("DummyProcessor ("+bean.getClass().getSimpleName()+" - "+bean.getName()+"): "+bean.getPercentComplete());
//				broadcast(bean, new Double(i));
//			}
//
//
//			bean.setMessage("Dummy process complete (no software run)");
//			broadcast(bean, Status.COMPLETE, 100d);
//		}
//
//		public boolean isBlocking() {
//			return blocking;
//		}
//
//		public void setBlocking(boolean blocking) {
//			this.blocking = blocking;
//		}
//
//		public boolean isTerminated() {
//			return terminated;
//		}
//
//		public void setTerminated(boolean terminated) {
//			this.terminated = terminated;
//		}
//		
//		
//		/*
//		 * Broadcast methods copied wholesale from {@link AbstractQueueProcessor}
//		 */
//		/**
//		 * Convenience method to call broadcast with only {@link Status} argument.
//		 * 
//		 * @param bean Bean to be broadcast.
//		 * @param newStatus Status the bean has just reached.
//		 * @throws EventException In case broadcasting fails.
//		 */
//		protected void broadcast(T bean, Status newStatus) throws EventException {
//			broadcast(bean, newStatus, null);
//		}
//		
//		/**
//		 * Convenience method to call broadcast with only percent complete 
//		 * argument.
//		 * 
//		 * @param bean Bean to be broadcast.
//		 * @param newPercent The value percent complete should be set to.
//		 * @throws EventException In case broadcasting fails.
//		 */
//		protected void broadcast(T bean, double newPercent) throws EventException {
//			broadcast(bean, null, newPercent);
//		}
//
//		/**
//		 * Broadcast the new status, updated previous status and percent complete 
//		 * of the given bean.
//		 * 
//		 * @param bean Bean to be broadcast.
//		 * @param newStatus Status the bean has just reached.
//		 * @param newPercent The value percent complete should be set to.
//		 * @throws EventException In case broadcasting fails.
//		 */
//		protected void broadcast(T bean, Status newStatus, Double newPercent) throws EventException {
//			if (statusReporter != null) {
//				if (newStatus != null) {
//					bean.setPreviousStatus(bean.getStatus());
//					bean.setStatus(newStatus);
//				}
//				if (newPercent != null) bean.setPercentComplete(newPercent);
//				
//				statusReporter.broadcast(bean);
//			}		
//		}
//
//	}

}

