package org.eclipse.scanning.test.scan;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.scan.IScannableConnectorService;
import org.eclipse.scanning.api.scan.ScanningException;

public class MockScannableConnector implements IScannableConnectorService {

	@Override
	public <T> IScannable<T> getScannable(String name) throws ScanningException {
		// TODO Auto-generated method stub
		return null;
	}

}
