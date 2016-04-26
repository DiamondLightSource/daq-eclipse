package org.eclipse.scanning.sequencer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IDeviceConnectorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositioner;

/**
 * Positions several scannables by level, returning after all the blocking IScannable.setPosition(...)
 * methods have returned.
 * 
 * @author Matthew Gerring
 *
 */
final class ScannablePositioner extends LevelRunner<IScannable<?>> implements IPositioner {
		
	private IDeviceConnectorService     connectorService;
	private List<IScannable<?>>         monitors;

	ScannablePositioner(IDeviceConnectorService service) {	
		this.connectorService = service;
	}
	
	@Override
	public boolean setPosition(IPosition position) throws ScanningException, InterruptedException {
		run(position);
		return true;
	}

	@Override
	public IPosition getPosition() throws ScanningException {
		if (position==null) return null;
		MapPosition ret = new MapPosition();
		for (String name : position.getNames()) {
			try {
				IScannable<?> scannable = connectorService.getScannable(name);
			    ret.put(name, scannable.getPosition());
			} catch (Exception ne) {
				throw new ScanningException("Cannout read value of "+name, ne);
			}
		}
		return ret;
	}
  

	@Override
	protected Collection<IScannable<?>> getObjects() throws ScanningException {
		List<String> names = position.getNames();
		if (names==null) return null;
		final List<IScannable<?>> ret = new ArrayList<>(names.size());
		for (String name : position.getNames()) ret.add(connectorService.getScannable(name));
		if (monitors!=null) for(IScannable<?> mon : monitors) ret.add(mon);
		return ret;
	}

	@Override
	protected Callable<IPosition> create(IScannable<?> scannable, IPosition position) throws ScanningException {
		return new MoveTask(scannable, position);
	}

	private final class MoveTask implements Callable<IPosition> {

		@SuppressWarnings("rawtypes")
		private IScannable scannable;
		private IPosition  position;

		public MoveTask(IScannable<?> iScannable, IPosition position) {
			this.scannable = iScannable;
			this.position  = position;
		}

		@SuppressWarnings("unchecked")
		@Override
		public IPosition call() throws Exception {
			
			// Get the value in this position, may be null for monitors.
			Object value = position.get(scannable.getName());
			try {
			    scannable.setPosition(value, position);
			    
			} catch (Exception ne) {
				abort(scannable, value, position, ne);
				throw ne;
			}
			return new MapPosition(scannable.getName(), position.getIndex(scannable.getName()), scannable.getPosition()); // Might not be exactly what we moved to
		}
		
	}

	public List<IScannable<?>> getMonitors() {
		return monitors;
	}

	public void setMonitors(List<IScannable<?>> monitors) {
		this.monitors = monitors;
	}
	
	public void setMonitors(IScannable<?>... monitors) {
		this.monitors = Arrays.asList(monitors);
	}

}
