package org.eclipse.scanning.sequencer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.ILevel;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.IPositionListener;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.ScanningException;


/**
 * Runs any collection of objects using an executor service
 * by reading their levels.
 * 
 * @author Matthew Gerring
 *
 */
abstract class LevelRunner<L extends ILevel> {
	

    protected IPosition position;

	/**
     * Get the object with a name
     * @param name
     * @return
     * @throws ScanningException
     */
	protected abstract L getNamedObject(String name)  throws ScanningException;

	/**
	 * 
	 * @param levelObject
	 * @param position
	 * @return
	 * @throws ScanningException
	 */
	protected abstract Callable<IPosition> createTask(L levelObject, IPosition position)  throws ScanningException;

	/**
	 * Call to set the value at the location specified
	 * @param position
	 * @return
	 * @throws ScanningException
	 */
	public boolean setPosition(IPosition position) throws ScanningException {
		
		this.position = position;
		Map<Integer, List<L>> positionMap = getOrderedObjects(position.getNames());
		
		try {
			Integer finalLevel = 0;
			for (int level : positionMap.keySet()) {
				finalLevel=level;
				// Each service does one level's move.
				ExecutorService eservice = createService();
				List<L> levels = positionMap.get(level);
				for (L ilevel : levels) {
					eservice.submit(createTask(ilevel, position));
				}
				eservice.shutdown();
				eservice.awaitTermination(1, TimeUnit.MINUTES); // Does this need spring config?
				fireLevelPerformed(level, levels, getPosition());
			}
			firePositionPerformed(finalLevel, position);
			
		} catch (InterruptedException ex) {
			throw new ScanningException("Scanning interupted while moving to new position!", ex);
		}
		
		return true;
	}

	/**
	 * Get the scannables in the position ordered by level
	 * @param position
	 * @return
	 * @throws ScanningException 
	 */
	protected Map<Integer, List<L>> getOrderedObjects(final List<String> names) throws ScanningException {
		
		final Map<Integer, List<L>> ret = new TreeMap<>();
		for (String name : names) {
			L object = getNamedObject(name);
			if (object == null) throw new ScanningException("Cannot find object called '"+name+"'");
			final int level = object.getLevel();
		
			if (!ret.containsKey(level)) ret.put(level, new ArrayList<L>(7));
			ret.get(level).add(object);
		}
		
		return ret;
	}

	protected ExecutorService createService() {
		int processors = Runtime.getRuntime().availableProcessors();
		return new ThreadPoolExecutor(processors,             /* number of motors to move at the same time. */
                processors*2,                                 /* max size current tasks. */
                5, TimeUnit.SECONDS,                          /* timeout after - does this need spring config? */
                new ArrayBlockingQueue<Runnable>(1000, true), /* max 1000+ncores motors to a level */
                new ThreadPoolExecutor.AbortPolicy());

	}

	private Collection<IPositionListener> listeners;

	protected void firePositionPerformed(int finalLevel, IPosition position) {
		if (listeners==null) return;
		IPositionListener[] ls = listeners.toArray(new IPositionListener[listeners.size()]);
		final PositionEvent evnt = new PositionEvent(position);
		evnt.setLevel(finalLevel);
		for (IPositionListener l : ls)  l.positionPerformed(evnt);
	}

	protected void fireLevelPerformed(int level, List<L> levels, IPosition position) {
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

	public IPosition getPosition()  throws ScanningException {
		return position;
	}

}
