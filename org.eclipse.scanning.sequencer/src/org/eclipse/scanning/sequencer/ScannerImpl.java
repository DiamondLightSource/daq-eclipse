package org.eclipse.scanning.sequencer;

import org.eclipse.scanning.api.IScanner;
import org.eclipse.scanning.api.scan.ScanningException;

class ScannerImpl<T> implements IScanner<T> {

	
	private T model;
	
	@Override
	public void configure(T model) throws ScanningException {
		this.model = model;
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
