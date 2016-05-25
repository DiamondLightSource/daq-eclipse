package org.eclipse.scanning.api.event.dry;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.AbstractPausableProcess;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;

public class DryRunProcess<T extends StatusBean> extends AbstractPausableProcess<T> {

	
	private boolean blocking;
	private boolean terminated;

	private int stop;
	private int start;
	private int step;
	private long sleep;

	private Thread thread;

	public DryRunProcess(T bean, IPublisher<T> statusPublisher, boolean blocking) {
		this(bean,statusPublisher,blocking,0,100,1,100);
	}
	public DryRunProcess(T bean, IPublisher<T> statusPublisher, boolean blocking, int start, int stop, int step, long sleep) {
		super(bean, statusPublisher);
		this.blocking  = blocking;
		this.start = start;
		this.stop  = stop;
		this.step  = step;
		this.sleep  = sleep;
	}

	@Override
	public void execute() throws EventException {
		if (isBlocking()) {
			run(); // Block until process has run.
		} else {
			final Thread thread = new Thread(new Runnable() {
				public void run() {
					try {
						DryRunProcess.this.run();
					} catch (EventException ne) {
						System.out.println("Cannot complete dry run");
						ne.printStackTrace();
					}
				}
			});
			thread.setDaemon(true);
			thread.setPriority(Thread.MAX_PRIORITY);
			thread.start();
		}		
	}
	
	private void run()  throws EventException {
		
		this.thread = Thread.currentThread();
		getBean().setPreviousStatus(Status.QUEUED);
		getBean().setStatus(Status.RUNNING);
		getBean().setPercentComplete(0d);
		getPublisher().broadcast(getBean());

		terminated = false;
		for (int i = start; i <= stop; i+=step) {
			
			if (isTerminated()) {
				getBean().setPreviousStatus(Status.RUNNING);
				getBean().setStatus(Status.TERMINATED);
				getPublisher().broadcast(getBean());
				return;
			}
			
			checkPaused(); // Blocks if is, sends events
			
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				System.out.println("Cannot complete dry run");
				e.printStackTrace();
			}
			System.out.println("Dry run : "+getBean().getPercentComplete()+" : "+getBean().getName());
			getBean().setPercentComplete((Double.valueOf(i)/Double.valueOf(stop))*100d);
			getPublisher().broadcast(getBean());
		}

		getBean().setPreviousStatus(Status.RUNNING);
		getBean().setStatus(Status.COMPLETE);
		getBean().setPercentComplete(100);
		getBean().setMessage("Dry run complete (no software run)");
		getPublisher().broadcast(getBean());
	}

	@Override
	public void doTerminate() throws EventException {
		if (thread!=null) thread.interrupt();
		terminated = true;
	}

	public boolean isBlocking() {
		return blocking;
	}

	public void setBlocking(boolean blocking) {
		this.blocking = blocking;
	}

	public boolean isTerminated() {
		return terminated;
	}

	public void setTerminated(boolean terminated) {
		this.terminated = terminated;
	}

}
