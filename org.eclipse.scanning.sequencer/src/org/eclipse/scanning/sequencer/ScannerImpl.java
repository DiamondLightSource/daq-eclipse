package org.eclipse.scanning.sequencer;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.IParser;
import org.eclipse.scanning.api.scan.IScanner;
import org.eclipse.scanning.api.scan.ScanningException;

class ScannerImpl implements IScanner {

	@Override
	public Status getState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void configure(Iterable<IPosition> list, IParser<?> parser)
			throws ScanningException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void run() throws ScanningException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void abort() throws ScanningException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() throws ScanningException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() throws ScanningException {
		// TODO Auto-generated method stub
		
	}

}
