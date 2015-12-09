package org.eclipse.scanning.sequencer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.scan.IDeviceConnectorService;
import org.eclipse.scanning.api.scan.IPositioner;
import org.eclipse.scanning.api.scan.ScanningException;

/**
 * Positions scannables by level, returning after all the blocking moveTo
 * methods have returned.
 * 
 * @author Matthew Gerring
 *
 */
final class ScannablePositioner extends LevelRunner<IScannable<?>> implements IPositioner{
	
	private IDeviceConnectorService     hservice;

	ScannablePositioner(IDeviceConnectorService service) {	
		this.hservice = service;
	}
	
	@Override
	public boolean setPosition(IPosition position) throws ScanningException {
		run(position);
		return true;
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
	protected Collection<IScannable<?>> getObjects() throws ScanningException {
		final List<IScannable<?>> ret = new ArrayList<>(position.getNames().size());
		for (String name : position.getNames()) ret.add(hservice.getScannable(name));
		return ret;
	}

	@Override
	protected Callable<IPosition> create(IScannable<?> scannable, IPosition position) throws ScanningException {
		return new MoveTask(scannable, position);
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

}
