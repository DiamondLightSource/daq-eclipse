package org.eclipse.scanning.api.scan.event;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.eclipse.scanning.api.ILevel;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.Location.LocationType;

/**
 * Manages position listeners.
 * 
 * @author Matthew Gerring
 *
 */
public class PositionDelegate {
		
	private Collection<IPositionListener> listeners;
	private IPublisher<Location>          publisher;

	public PositionDelegate() {
		this(null);
	}

	/**
	 * 
	 * @param publisher to send events to, may be null.
	 */
	public PositionDelegate(IPublisher<Location> publisher) {
		this.publisher = publisher;
	}

	public boolean firePositionWillPerform(IPosition position) throws ScanningException {
		final PositionEvent evnt = new PositionEvent(position);
		broadcast(LocationType.positionWillPerform, evnt);
		if (listeners==null) return true;
		IPositionListener[] ls = listeners.toArray(new IPositionListener[listeners.size()]);
		for (IPositionListener l : ls)  {
			boolean ok = l.positionWillPerform(evnt);
			if (!ok) return false;
		}
		return true;
	}

	private void broadcast(LocationType type, PositionEvent evnt) {
		if (publisher!=null) {
			try {
				publisher.broadcast(new Location(type, evnt));
			} catch (EventException e) {
				// We swallow this without a logger because 
				// there is no logger dependency on the API.
				e.printStackTrace();
			}
		}
	}

	public void firePositionChanged(int finalLevel, IPosition position) throws ScanningException {
		final PositionEvent evnt = new PositionEvent(position);
		evnt.setLevel(finalLevel);
		broadcast(LocationType.positionChanged, evnt);
		
		if (listeners==null) return;
		IPositionListener[] ls = listeners.toArray(new IPositionListener[listeners.size()]);
		for (IPositionListener l : ls)  l.positionChanged(evnt);
	}

	public void firePositionPerformed(int finalLevel, IPosition position) throws ScanningException {
		final PositionEvent evnt = new PositionEvent(position);
		evnt.setLevel(finalLevel);
		broadcast(LocationType.positionPerformed, evnt);

		if (listeners==null) return;
		IPositionListener[] ls = listeners.toArray(new IPositionListener[listeners.size()]);
		for (IPositionListener l : ls)  l.positionPerformed(evnt);
	}

	public void fireLevelPerformed(int level, List<? extends ILevel> levels, IPosition position) throws ScanningException {
		final PositionEvent evnt = new PositionEvent(position);
		evnt.setLevel(level);
	    evnt.setLevelObjects(levels);
		broadcast(LocationType.levelPerformed, evnt);
		
		if (listeners==null) return;
		IPositionListener[] ls = listeners.toArray(new IPositionListener[listeners.size()]);
		for (IPositionListener l : ls)  l.levelPerformed(evnt);
	}

	public void addPositionListener(IPositionListener listener) {
		if (listeners==null) listeners = new HashSet<IPositionListener>(3);
		listeners.add(listener);
	}

	public void removePositionListener(IPositionListener listener) {
		if (listeners==null) return;
		listeners.remove(listener);
	}

	public IPublisher<Location> getPublisher() {
		return publisher;
	}

	public void setPublisher(IPublisher<Location> publisher) {
		this.publisher = publisher;
	}
}
