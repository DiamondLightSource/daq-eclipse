package org.eclipse.scanning.test.scan.mock;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.scan.IDeviceConnectorService;
import org.eclipse.scanning.api.scan.IRunnableDevice;
import org.eclipse.scanning.api.scan.ScanningException;

public class MockScannableConnector implements IDeviceConnectorService {
	
	private static Map<String, INameable> cache;
	
	// Create a few random scannables with different levels.
	static {
		if (cache==null) cache = new HashMap<String, INameable>(3);
		register(new MockScannable("a", 10d, 1));
		register(new MockScannable("b", 10d, 1));
		register(new MockScannable("c", 10d, 1));
		register(new MockScannable("p", 10d, 2));
		register(new MockScannable("q", 10d, 2));
		register(new MockScannable("r", 10d, 2));
		register(new MockScannable("x", 0d,  3));
		register(new MockScannable("y", 0d,  3));
		register(new MockScannable("x", 0d,  3));
	}

	private static void register(INameable mockScannable) {
		cache.put(mockScannable.getName(), mockScannable);
	}

	@Override
	public IScannable<?> getScannable(String name) throws ScanningException {
		if (cache==null) cache = new HashMap<String, INameable>(3);
		if (cache.containsKey(name)) return (IScannable<?>)cache.get(name);
		register(new MockScannable(name, 0d));
		return (IScannable<?>)cache.get(name);
	}

	@Override
	public IRunnableDevice<?> getDetector(String name) throws ScanningException {
		if (cache==null) cache = new HashMap<String, INameable>(3);
		if (cache.containsKey(name)) return (IRunnableDevice<?>)cache.get(name);
		register(new MockReadableDetector(name));
		return (IRunnableDevice<?>)cache.get(name);
	}

}
