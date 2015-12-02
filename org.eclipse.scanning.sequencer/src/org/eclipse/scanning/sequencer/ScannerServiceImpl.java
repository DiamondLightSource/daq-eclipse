package org.eclipse.scanning.sequencer;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.scan.IScanner;
import org.eclipse.scanning.api.scan.IScanningService;
import org.eclipse.scanning.api.scan.ScanningException;

public class ScannerServiceImpl implements IScanningService {

	@Override
	public IScanner createScanner() throws ScanningException {
		ScannerImpl impl = new ScannerImpl();
		return impl;
	}

	@Override
	public <T> IScannable<T> getScannable(String name) throws ScanningException {
		return null;
	}
}
