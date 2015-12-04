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

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.scan.IHardwareConnectorService;
import org.eclipse.scanning.api.scan.IPositionListener;
import org.eclipse.scanning.api.scan.IPositioner;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.ScanningException;

class Positioner implements IPositioner{
	
	private IHardwareConnectorService     hservice;
	private IPosition                     position;
	private Collection<IPositionListener> listeners;

	Positioner(IHardwareConnectorService service) {	
		this.hservice = service;
	}

	@Override
	public boolean setPosition(IPosition position) throws ScanningException {
		
		this.position = position;
		Map<Integer, List<IScannable<?>>> positionMap = getOrderedScannables(position);
		
		try {
			Integer finalLevel = 0;
			for (int level : positionMap.keySet()) {
				finalLevel=level;
				// Each service does one level's move.
				ExecutorService eservice = createService();
				List<IScannable<?>> scannables = positionMap.get(level);
				for (IScannable<?> iScannable : scannables) {
					eservice.submit(new MoveTask(iScannable, position));
				}
				eservice.shutdown();
				eservice.awaitTermination(1, TimeUnit.MINUTES); // Does this need spring config?
				fireLevelPerformed(level, scannables, getPosition());
			}
			firePositionPerformed(finalLevel, position);
			
		} catch (InterruptedException ex) {
			throw new ScanningException("Scanning interupted while moving to new position!", ex);
		}
		
		return true;
	}

	protected void firePositionPerformed(int finalLevel, IPosition position) {
		if (listeners==null) return;
		IPositionListener[] ls = listeners.toArray(new IPositionListener[listeners.size()]);
		final PositionEvent evnt = new PositionEvent(position);
		evnt.setLevel(finalLevel);
		for (IPositionListener l : ls)  l.positionPerformed(evnt);
	}

	protected void fireLevelPerformed(int level, List<IScannable<?>> scannables, IPosition position) {
		if (listeners==null) return;
		IPositionListener[] ls = listeners.toArray(new IPositionListener[listeners.size()]);
		final PositionEvent evnt = new PositionEvent(position);
		evnt.setLevel(level);
		evnt.setScannables(scannables);
		for (IPositionListener l : ls)  l.levelPerformed(evnt);
	}

	private ExecutorService createService() {
		int processors = Runtime.getRuntime().availableProcessors();
		return new ThreadPoolExecutor(processors,             /* number of motors to move at the same time. */
                processors*2,                                 /* max size current tasks. */
                5, TimeUnit.SECONDS,                          /* timeout after - does this need spring config? */
                new ArrayBlockingQueue<Runnable>(1000, true), /* max 1000+ncores motors to a level */
                new ThreadPoolExecutor.AbortPolicy());

	}

	/**
	 * Get the scannables in the position ordered by level
	 * @param position
	 * @return
	 * @throws ScanningException 
	 */
	private Map<Integer, List<IScannable<?>>> getOrderedScannables(IPosition position) throws ScanningException {
		
		final Map<Integer, List<IScannable<?>>> ret = new TreeMap<Integer, List<IScannable<?>>>();
		final List<String> names = position.getNames();
		for (String name : names) {
			IScannable<?> scannable = hservice.getScannable(name);
			if (scannable == null) throw new ScanningException("Cannot find scannable called '"+name+"'");
			final int level = scannable.getLevel();
		
			if (!ret.containsKey(level)) ret.put(level, new ArrayList<IScannable<?>>(7));
			ret.get(level).add(scannable);
		}
		
		return ret;
	}
	
	private class MoveTask<V> implements Callable<V> {

		private IScannable<V> scannable;
		private IPosition     position;

		public MoveTask(IScannable<V> iScannable, IPosition position) {
			this.scannable = iScannable;
			this.position  = position;
		}

		@Override
		public V call() throws Exception {
			V pos = (V)position.get(scannable.getName());
			try {
			    scannable.moveTo(pos);
			} catch (Exception ne) {
				ne.printStackTrace();  // Just for testing we make sure that the stack is visible.
				throw ne;
			}
			return scannable.getPosition(); // Might not be exactly what we moved to
		}
		
	}

	@Override
	public IPosition getPosition() throws ScanningException {
		if (position==null) return null;
		MapPosition ret = new MapPosition();
		for (String name : position.getNames()) {
			try {
			    ret.put(name, hservice.getScannable(name).getPosition());
			} catch (Exception ne) {
				throw new ScanningException("Cannout read value of "+name, ne);
			}
		}
		return ret;
	}

	@Override
	public void addPositionListener(IPositionListener listener) {
		if (listeners==null) listeners = new HashSet<IPositionListener>(3);
		listeners.add(listener);
	}

	@Override
	public void removePositionListener(IPositionListener listener) {
		if (listeners==null) return;
		listeners.remove(listener);
	}
}
