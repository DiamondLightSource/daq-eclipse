package org.eclipse.scanning.sequencer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.ILevel;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.scan.IPositionListener;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.ScanningException;


/**
 * Runs any collection of objects using an executor service
 * by reading their levels. On service runs all the devices
 * at each level and waits for them to finish.
 * 
 * The implementing class provides the Callable which runs the
 * actual task. For instance setting a position.
 * 
 * @author Matthew Gerring
 *
 */
abstract class LevelRunner<L extends ILevel> {
	

    protected IPosition position;
	private ExecutorService eservice;

	/**
	 * Get a list of the objects which we would like to order by level.
	 * @return
	 */
	protected abstract Collection<L> getObjects() throws ScanningException ;

	/**
	 * Implement this method to create a callable which willbe run by the executor service.
	 * If a given level object and position return null, no work will be done for that object at that level.
	 * 
	 * @param levelObject
	 * @param position
	 * @return a callable that returns the position reached once it has finished running. May return null to
	 * do no work for a given create level.
	 * 
	 * @throws ScanningException
	 */
	protected abstract Callable<IPosition> create(L levelObject, IPosition position)  throws ScanningException;
	
	/**
	 * Call to set the value at the location specified
	 * Same as calling run(position, true)
	 * 
	 * @param position
	 * @return
	 * @throws ScanningException
	 */
	protected boolean run(IPosition position) throws ScanningException, InterruptedException {
        return run(position, true);
	}
	
	/**
	 * Call to set the value at the location specified
	 * @param position
	 * @param block - set to true to block until the parallel tasks have compeleted. Set to false to
	 * return after the last level is told to execute. In this case more work can be done on the calling 
	 * thread. Use the latch() method to come back to the last level's ExecutorService and 
	 * @return
	 * @throws ScanningException
	 */
	protected boolean run(IPosition position, boolean block) throws ScanningException, InterruptedException {
		
		this.position = position;
		Map<Integer, List<L>> positionMap = getLevelOrderedObjects(getObjects());
		
		try {
			this.eservice = createService();

			Integer finalLevel = 0;
			for (Iterator<Integer> it = positionMap.keySet().iterator(); it.hasNext();) {
			    
				int level = it.next();
				List<L> lobjects = positionMap.get(level);
				Collection<Callable<IPosition>> tasks = new ArrayList<>(lobjects.size());
				for (L lobject : lobjects) {
					Callable<IPosition> c = create(lobject, position);
					if (c==null) continue; // legal to say that there is nothing to do for a given object.
					tasks.add(c);
				}
				if (!it.hasNext() && !block) { 
					// The last one and we are non-blocking
					for (Callable<IPosition> callable : tasks) eservice.submit(callable);
				} else {
					// Normally we block until done.
				    List<Future<IPosition>> pos = eservice.invokeAll(tasks); // blocks until level has run
					fireLevelPerformed(level, lobjects, getPosition(pos));
				}
			}
			
			firePositionPerformed(finalLevel, position);
			
		} catch (ExecutionException ex) {
			throw new ScanningException("Scanning interupted while moving to new position!", ex);
		}
		
		return true;
	}

	/** 
	 * Blocks until all the tasks have complete. In order for this call to be worth
	 * using run(position, false) should have been used to run the service.
	 * 
	 * If executor does not shutdown within 1 minute, throws an exception.
	 * 
	 * If nothing has been run by the runner, there will be no executor service
	 * created and latch() will directly return.
	 * 
	 * @throws InterruptedException 
	 */
	protected void await() throws InterruptedException {
        await(1, TimeUnit.MINUTES);// FIXME Does this need spring config?
	}
	
	/** 
	 * Blocks until all the tasks have complete. In order for this call to be worth
	 * using run(position, false) should have been used to run the service.
	 * 
	 * If nothing has been run by the runner, there will be no executor service
	 * created and latch() will directly return.
	 * 
	 * @throws InterruptedException 
	 */
	protected void await(long time, TimeUnit unit) throws InterruptedException {
		if (eservice==null)          return;
		if (eservice.isTerminated()) return;
		eservice.shutdown();
		eservice.awaitTermination(time, unit); 
	}

	/**
	 * Get the scannables, ordered by level, lowest first
	 * @param position
	 * @return
	 * @throws ScanningException 
	 */
	protected Map<Integer, List<L>> getLevelOrderedObjects(final Collection<L> objects) throws ScanningException {
		
		final Map<Integer, List<L>> ret = new TreeMap<>();
		for (L object : objects) {
			final int level = object.getLevel();
		
			if (!ret.containsKey(level)) ret.put(level, new ArrayList<L>(7));
			ret.get(level).add(object);
		}
		
		return ret;
	}

	protected ExecutorService createService() {
		// TODO Need spring config for this.
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

	private IPosition getPosition(List<Future<IPosition>> pos) throws InterruptedException, ExecutionException {
	    IPosition ret = new MapPosition();
	    for (Future<IPosition> future : pos) {
	    	ret = ret.composite(future.get());
		}
	    return ret;
	}

}
