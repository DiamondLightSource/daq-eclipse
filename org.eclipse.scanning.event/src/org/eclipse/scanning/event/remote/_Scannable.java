package org.eclipse.scanning.event.remote;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.annotation.ui.DeviceType;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.core.ResponseConfiguration.ResponseWaiter;
import org.eclipse.scanning.api.event.scan.DeviceAction;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.DeviceRequest;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.event.ILocationListener;
import org.eclipse.scanning.api.scan.event.IPositionListenable;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.eclipse.scanning.api.scan.event.Location;
import org.eclipse.scanning.api.scan.event.LocationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class _Scannable<T> extends _AbstractRemoteDevice<T> implements IScannable<T>, IPositionListenable {

	private final static Logger logger = LoggerFactory.getLogger(_Scannable.class);

	private ISubscriber<ILocationListener> subscriber;
	
	_Scannable(DeviceRequest req, URI uri, IEventService eservice) throws EventException, InterruptedException {
		super(req, uri, eservice);
	    requester.setTimeout(250, TimeUnit.MILLISECONDS); // TODO This is how often the response is checked for setPosition()
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getPosition() throws Exception {
		DeviceRequest req = update();
		return (T)req.getDeviceValue();
	}

	/**
	 * Calls setPosition and waits for up to five minutes.
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void setPosition(T value, IPosition position) throws Exception {
		try {
			// Will tell us that the value is changing by recording the time of the change 
			if (this.subscriber == null) createSubscriber(); 

			DeviceRequest req = new DeviceRequest(info.getName(), DeviceType.SCANNABLE);
			req.setDeviceAction(DeviceAction.SET);
			req.setDeviceValue(value);
			req.setPosition(position);
			req = requester.post(req, createResponseWaiter());
			info = (DeviceInformation<T>)req.getDeviceInformation();
			
		} catch (Exception ne) {
			logger.error("Cannot update device info for "+info.getName(), ne);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected DeviceRequest update() {
		try {
			DeviceRequest req = requester.post(new DeviceRequest(info.getName(), DeviceType.SCANNABLE));
			info = (DeviceInformation<T>)req.getDeviceInformation();
			return req;
		} catch (Exception ne) {
			logger.error("Cannot update device info for "+info.getName(), ne);
			return null;
		}
	}

	public String getUnit() {
		if (info==null) update();  // We assume that they do not change unit.
		return info.getUnit();
	}
	
	
	/**
	 * Set the current upper limit, returning the previous value, if any.
	 * @param upper
	 * @return
	 */
	@Override
	@SuppressWarnings("unchecked")
	public T getMaximum() {
		if (info==null) update();
		return (T)info.getUpper();
	}

	/**
	 * Set the current lower limit, returning the previous value, if any.
	 * @param lower
	 * @return
	 */
	@Override
	@SuppressWarnings("unchecked")
	public T getMinimum() {
		if (info==null) update();
		return (T)info.getLower();
	}

	private Collection<IPositionListener> listeners;
	
	@Override
	public void addPositionListener(IPositionListener listener) {
		if (this.subscriber == null) createSubscriber();
		if (listeners==null) listeners = new LinkedHashSet<IPositionListener>(3);
		listeners.add(listener);
	}

	@Override
	public void removePositionListener(IPositionListener listener) {
		if (listeners==null) return;
		listeners.remove(listener);
	}
	
	private long lastActive = System.currentTimeMillis();
	/**
	 * A subscriber that notifies position and also resets
	 * timeouts for calls to set position, because it knows
	 * that the link is not dead.
	 */
	private void createSubscriber() {

		subscriber = eservice.createSubscriber(uri, EventConstants.POSITION_TOPIC);
		// We must use the name as the key to avoid too many events.
		try {
			subscriber.addListener(getName(), new ILocationListener() {
				@Override
				public void locationPerformed(LocationEvent evt) {
					
					lastActive = System.currentTimeMillis();
					final Location      loc  = evt.getLocation();
					if (loc.getType()==null) return;
					
					final PositionEvent evnt = new PositionEvent(loc.getPosition());
					evnt.setLevel(loc.getLevel());

					IPositionListener[] ls = listeners.toArray(new IPositionListener[listeners.size()]);
					try {
						final Method method = IPositionListener.class.getMethod(loc.getType().toString(), PositionEvent.class);
						for (IPositionListener l : ls)  {
							method.invoke(l, evnt);
						}
					} catch (Exception ne) {
						logger.error("Cannot diseminate event "+loc, ne);
					}

				}
			});
		} catch (EventException e) {
			logger.error("Problem creating subscriber!", e);
		}
	}


	private ResponseWaiter createResponseWaiter() {
		return new ResponseWaiter() {
			@Override
			public boolean waitAgain() {
				long since = System.currentTimeMillis()-lastActive;
				return since < 1000*60*2; // If a last value update was within two minutes, we wait some more.
			}
		};
	}

}
