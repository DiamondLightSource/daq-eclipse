package org.eclipse.scanning.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.scan.event.IPositionListenable;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.eclipse.scanning.api.scan.event.Location;
import org.eclipse.scanning.api.scan.event.PositionDelegate;

/**
 * 
 * Convenience class using inheritance to contain some of the general 
 * things a scannable does that are the same for all scannables.
 * 
 * NOTE: Inheritance is designed to have three levels only
 * IScannable->AbstractScannable->A device
 * 
 * The preferred alternative if more complex behaviour is required would
 * be to create delegates for these interfaces which are then aggregated
 * in the device.
 * 
 * @author Matthew Gerring
 *
 * @param <T>
 */
public abstract class AbstractScannable<T> implements IScannable<T>, IScanAttributeContainer, IPositionListenable {

	private T                   max;
	private T                   min;
	private Map<String, Object> attributes;
	private int                 level;
	private String              name;
	
	/**
	 * Implementors should use the delegate to notify of position.
	 */
	protected PositionDelegate  delegate;
	
	protected AbstractScannable() {
		this(null);
	}
	/**
	 * 
	 * @param publisher used to notify of positions externally.
	 */
	protected AbstractScannable(IPublisher<Location> publisher) {
		this.attributes = new HashMap<>(7);
		this.delegate   = new PositionDelegate(publisher);
	}
	
	@Override
	public void addPositionListener(IPositionListener listener) {
		delegate.addPositionListener(listener);
	}
	@Override
	public void removePositionListener(IPositionListener listener) {
		delegate.removePositionListener(listener);
	}
	
	public void setPublisher(IPublisher<Location> publisher) {
		delegate.setPublisher(publisher);
	}
	
	/**
	 * 
	 * @return null if no attributes, otherwise collection of the names of the attributes set
	 */
	@Override
	public Set<String> getScanAttributeNames() {
		return attributes.keySet();
	}

	/**
	 * Set any attribute the implementing classes may provide
	 * 
	 * @param attributeName
	 *            is the name of the attribute
	 * @param value
	 *            is the value of the attribute
	 * @throws DeviceException
	 *             if an attribute cannot be set
	 */
	@Override
	public <A> void setScanAttribute(String attributeName, A value) throws Exception {
		attributes.put(attributeName, (A)value);
	}

	/**
	 * Get the value of the specified attribute
	 * 
	 * @param attributeName
	 *            is the name of the attribute
	 * @return the value of the attribute
	 * @throws DeviceException
	 *             if an attribute cannot be retrieved
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <A> A getScanAttribute(String attributeName) throws Exception {
		return (A)attributes.get(attributeName);
	}
	
	public int getLevel() {
		return level;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}


	@Override
	public T getMaximum() {
		return max;
	}


	public T setMaximum(T upper) {
		T ret = this.max;
		this.max = upper;
		return ret;
	}

    @Override
	public T getMinimum() {
		return min;
	}


	public T setMinimum(T lower) {
		T ret = this.min;
		this.min = lower;
		return ret;
	}
	
}
