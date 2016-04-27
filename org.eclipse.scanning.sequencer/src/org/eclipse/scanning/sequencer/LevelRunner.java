package org.eclipse.scanning.sequencer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.eclipse.scanning.api.scan.event.PositionDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
	
	private static Logger logger = LoggerFactory.getLogger(LevelRunner.class);

    protected IPosition        position;
    private ExecutorService    eservice;
	private ScanningException  abortException;
	private PositionDelegate   pDelegate;
	
	protected LevelRunner() {
		pDelegate = new PositionDelegate();
	}

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
	 * @param block - set to true to block until the parallel tasks have completed. Set to false to
	 * return after the last level is told to execute. In this case more work can be done on the calling 
	 * thread. Use the latch() method to come back to the last level's ExecutorService and 
	 * @return
	 * @throws ScanningException
	 */
	protected boolean run(IPosition position, boolean block) throws ScanningException, InterruptedException {
		
		if (abortException!=null) throw abortException;

		this.position = position;
		boolean ok = pDelegate.firePositionWillPerform(position);
        if (!ok) return false;
		
		Map<Integer, List<L>> positionMap = getLevelOrderedObjects(getObjects());
		
		try {
			// TODO Should we actually create the service size to the size
			// of the largest level population? This would mean that you try to 
			// start everything at the same time.
			if (eservice==null) this.eservice = createService();

			Integer finalLevel = 0;
			for (Iterator<Integer> it = positionMap.keySet().iterator(); it.hasNext();) {
			    
				if (abortException!=null) throw abortException;
				
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
					// Blocks until level has run
				    List<Future<IPosition>> pos = eservice.invokeAll(tasks, getTimeout(lobjects), TimeUnit.SECONDS);
				    
				    // If timed out, some isDone will be false.
				    for (Future<IPosition> future : pos) {
						if (!future.isDone()) throw new ScanningException("The timeout of "+timeout+"s has been reached waiting for level "+level+" objects "+toString(lobjects));
					}
				    pDelegate.fireLevelPerformed(level, lobjects, getPosition(pos));
				}
			}
			
			pDelegate.firePositionPerformed(finalLevel, position);
			
		} catch (ScanningException s) {
			throw s;
		} catch (InterruptedException i) {
			throw i;
		} catch (Exception ne) {
			if (abortException!=null) throw abortException;
			throw new ScanningException("Scanning interupted while moving to new position!", ne);
			
		} finally {
			if (block) {
				if (eservice!=null) eservice.shutdownNow();
				eservice = null;
			}
		}
		
		return true;
	}
	
	protected String toString(List<L> lobjects) {
		final  StringBuilder buf = new StringBuilder("[");
		for (L l : lobjects) {
			buf.append(l);
			buf.append(", ");
		}
		return buf.toString();
	}

	/**
	 * The timeout is overridden by some subclasses.
	 */
	private long timeout = 10;

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
	protected void await() throws InterruptedException, ScanningException {
        await(getTimeout(null));
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
	protected void await(long time) throws InterruptedException, ScanningException{
		try {
			if (eservice==null)          return;
			if (eservice.isTerminated()) return;
			eservice.shutdown();
			eservice.awaitTermination(time, TimeUnit.SECONDS); 
			if (!eservice.isTerminated()) {
				eservice.shutdownNow();
			    throw new ScanningException("The timeout of "+timeout+"s has been reached, scan aborting. Please implement ITimeoutable to define how long your device needs to write.");
			}
		} finally {
		    eservice = null;
		}
	}
	
	public void abort() {
		if (eservice==null) return; // We are already finished
		eservice.shutdownNow();
		eservice = null;
	}
	
	/**
	 * 
	 * @param device
	 * @param value
	 * @param pos
	 * @param ne
	 */
	protected void abort(INameable device, Object value, IPosition pos, Exception ne) {
		
		String message = "Cannot run device named '"+device.getName()+"' value is '"+value+"' (may be null) and position is '"+pos+"'\nMessage: "+ne.getMessage();
		logger.error(message, ne); // Just for testing we make sure that the stack is visible.
        System.err.println(message);
        abortException = ne instanceof ScanningException 
        		       ? (ScanningException)ne
        		       : new ScanningException(ne.getMessage(), ne);
		eservice.shutdownNow();
		eservice = null;
	}
	
	public void reset() {
		abortException = null;
	}

	/**
	 * Get the scannables, ordered by level, lowest first
	 * @param position
	 * @return
	 * @throws ScanningException 
	 */
	protected Map<Integer, List<L>> getLevelOrderedObjects(final Collection<L> objects) throws ScanningException {
		
		if (objects==null) return Collections.emptyMap();
		
		// TODO It is necessary to cache this map for speed?
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
		return new ThreadPoolExecutor(processors,                                   /* number of motors to move at the same time. */
						              processors*2,                                 /* max size current tasks. */
						              1, TimeUnit.SECONDS,                          /* timeout after - does this need spring config? */
						              new ArrayBlockingQueue<Runnable>(1000, true), /* max 1000+ncores motors to a level */
						              new ThreadPoolExecutor.AbortPolicy());

	}

	public void addPositionListener(IPositionListener listener) {
		pDelegate.addPositionListener(listener);
	}

	public void removePositionListener(IPositionListener listener) {
		pDelegate.removePositionListener(listener);
	}

	public IPosition getPosition()  throws ScanningException {
		return position;
	}

	private IPosition getPosition(List<Future<IPosition>> futures) throws InterruptedException, ExecutionException {
		MapPosition ret = new MapPosition();
	    for (Future<IPosition> future : futures) {
	    	// Faster than using composite
	    	IPosition pos = future.get();
	    	ret.putAll(pos);
	    	ret.putAllIndices(pos);
		}
	    return ret;
	}

	public static <T extends ILevel> LevelRunner<T> createEmptyRunner() {
		return new LevelRunner<T>() {
			
			@Override
			protected boolean run(IPosition position, boolean block) {
				return true;
			}
			@Override
			protected void await(long time) throws InterruptedException {
				return;
			}

			@Override
			protected Collection<T> getObjects() throws ScanningException {
				return null;
			}

			@Override
			protected Callable<IPosition> create(T levelObject, IPosition position) throws ScanningException {
				return null;
			}
			
		};
	}

	/**
	 * The await and maximum run time in seconds.
	 * @param the objects at this level. If null then the maximum of all possible objects in the runner should be used.
	 * @return
	 */
	public long getTimeout(List<L> objects) {
		return timeout;
	}

	/**
	 * The await time in sceonds.
	 * @return time
	 */
	public void setTimeout(long time) {
		this.timeout = time;
	}

}
