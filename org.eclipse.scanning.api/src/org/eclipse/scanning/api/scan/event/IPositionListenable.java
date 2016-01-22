package org.eclipse.scanning.api.scan.event;

public interface IPositionListenable {
	/**
	 * Use to be notified as levels / positions are reached.
	 * Not usually necessary as setPosition is blocking until
	 * the position is reached but useful for other objects 
	 * which need to change when new positions are reached.
	 * 
	 * @param listener
	 */
	void addPositionListener(IPositionListener listener);
	
	/**
	 * Use to be notified as levels / positions are reached.
	 * @param listener
	 */
	void removePositionListener(IPositionListener listener);

}
