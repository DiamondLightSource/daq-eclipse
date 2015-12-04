package org.eclipse.scanning.sequencer;

import java.util.concurrent.Callable;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.scan.IDeviceConnectorService;
import org.eclipse.scanning.api.scan.IPositioner;
import org.eclipse.scanning.api.scan.ScanningException;

class Positioner extends LevelRunner<IScannable<?>> implements IPositioner{
	
	private IDeviceConnectorService     hservice;

	Positioner(IDeviceConnectorService service) {	
		this.hservice = service;
	}
	
	protected IScannable<?> getNamedObject(String name) throws ScanningException {
		return hservice.getScannable(name);
	}

	@Override
	protected Callable<IPosition> createTask(IScannable<?> scannable, IPosition position) throws ScanningException {
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
}
