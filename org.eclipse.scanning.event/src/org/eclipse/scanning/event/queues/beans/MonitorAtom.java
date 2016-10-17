package org.eclipse.scanning.event.queues.beans;

import org.eclipse.scanning.api.event.queues.IQueueService;
import org.eclipse.scanning.api.event.queues.beans.QueueAtom;

/**
 * MonitorAtom is a type of {@link QueueAtom} which may be processed within an 
 * active-queue of an {@link IQueueService}. It contains name of a monitor 
 * which the current value needs to be recorded at a particular point in an 
 * experiment.
 * 
 * @author Michael Wharmby
 *
 */
public class MonitorAtom extends QueueAtom {
	
	/**
	 * Version ID for serialization. Should be updated when class changed. 
	 */
	private static final long serialVersionUID = 20161017L;
	
	private String monitor;
	
	/**
	 * No arg constructor for JSON
	 */
	public MonitorAtom() {
		super();
	}
	
	/**
	 * Constructor with arguments required to fully configure this atom
	 * @param monName - name for atom
	 * @param dev - name of monitor
	 */
	public MonitorAtom(String monName, String dev, long time) {
		super();
		setName(monName);
		monitor = dev;
		runTime = time;
	}

	@Override
	public long getRunTime() {
		return runTime;
	}

	@Override
	public void setRunTime(long runTime) {
		this.runTime = runTime;
	}

	/**
	 * Return the monitor which will be polled by this atom 
	 * @return the monitor to be polled
	 */
	public String getMonitor() {
		return monitor;
	}

	/**
	 * Set the monitor which will be polled by this atom
	 * @param monitor - new monitor to be polled
	 */
	public void setMonitor(String monitor) {
		this.monitor = monitor;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((monitor == null) ? 0 : monitor.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		MonitorAtom other = (MonitorAtom) obj;
		if (monitor == null) {
			if (other.monitor != null)
				return false;
		} else if (!monitor.equals(other.monitor))
			return false;
		return true;
	}
	
}
