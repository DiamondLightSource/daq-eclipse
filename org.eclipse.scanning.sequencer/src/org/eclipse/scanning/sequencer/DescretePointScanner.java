package org.eclipse.scanning.sequencer;

import org.eclipse.scanning.api.scan.IScanner;
import org.eclipse.scanning.api.scan.ScanModel;
import org.eclipse.scanning.api.scan.ScanningException;

/**
 * This scan does a standard GDA scan at each point. If a given point is a 
 * MalcolmDevice, that device will be configured and run for its given point.
 * Otherwise the levels of the scannables at the position will be taken into
 * accout and the 
 * 
 * @author Matthew Gerring
 *
 * @param <T>
 */
class DescretePointScanner implements IScanner<ScanModel> {

	
	private ScanModel model;
	
	@Override
	public void configure(ScanModel model) throws ScanningException {
		this.model = model;
	}

	@Override
	public void run() throws ScanningException {
		throw new ScanningException("Not implemented!");
	}

	@Override
	public void abort() throws ScanningException {
		throw new ScanningException("Not implemented!");
	}

	@Override
	public void pause() throws ScanningException {
		throw new ScanningException("Not implemented!");
	}

	@Override
	public void resume() throws ScanningException {
		throw new ScanningException("Not implemented!");
	}

}
