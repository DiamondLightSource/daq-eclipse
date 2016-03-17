package org.eclipse.scanning.api.scan.event;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.eclipse.scanning.api.ILevel;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.ScanningException;

/**
 * Manages position listeners.
 * 
 * @author Matthew Gerring
 *
 */
public class PositionDelegate {

	private Collection<IPositionListener> listeners;

	public boolean firePositionWillPerform(IPosition position) throws ScanningException {
		if (listeners==null) return true;
		IPositionListener[] ls = listeners.toArray(new IPositionListener[listeners.size()]);
		final PositionEvent evnt = new PositionEvent(position);
		for (IPositionListener l : ls)  {
			boolean ok = l.positionWillPerform(evnt);
			if (!ok) return false;
		}
		return true;
	}

	public void firePositionPerformed(int finalLevel, IPosition position) throws ScanningException {
		if (listeners==null) return;
		IPositionListener[] ls = listeners.toArray(new IPositionListener[listeners.size()]);
		final PositionEvent evnt = new PositionEvent(position);
		evnt.setLevel(finalLevel);
		for (IPositionListener l : ls)  l.positionPerformed(evnt);
	}

	public void fireLevelPerformed(int level, List<? extends ILevel> levels, IPosition position) throws ScanningException {
		if (listeners==null) return;
		IPositionListener[] ls = listeners.toArray(new IPositionListener[listeners.size()]);
		final PositionEvent evnt = new PositionEvent(position);
		evnt.setLevel(level);
	    evnt.setLevelObjects(levels);
		
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

}
