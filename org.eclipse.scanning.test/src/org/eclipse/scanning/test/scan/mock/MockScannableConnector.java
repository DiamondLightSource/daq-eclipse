package org.eclipse.scanning.test.scan.mock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.scan.IDeviceConnectorService;
import org.eclipse.scanning.api.scan.IWritableDetector;
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
		register(new MockNeXusScannable("xNex", 0d,  3));
		register(new MockNeXusScannable("yNex", 0d,  3));
		
		for (int i = 1; i < 10; i++) {
			register(new MockNeXusScannable("neXusScannable"+i, 0d,  3));
	    }
		for (int i = 1; i < 10; i++) {
			register(new MockNeXusScannable("monitor"+i, 0d,  3));
	    }

	}

	private static void register(INameable mockScannable) {
		cache.put(mockScannable.getName(), mockScannable);
	}

	@Override
	public <T> IScannable<T> getScannable(String name) throws ScanningException {
		if (cache==null) cache = new HashMap<String, INameable>(3);
		if (cache.containsKey(name)) return (IScannable<T>)cache.get(name);
		register(new MockScannable(name, 0d));
		return (IScannable<T>)cache.get(name);
	}

	@Override
	public <M> IWritableDetector<M> getDetector(String name) throws ScanningException {
		if (cache==null) cache = new HashMap<String, INameable>(3);
		if (cache.containsKey(name)) return (IWritableDetector<M>)cache.get(name);
		register(new MockWritableDetector(name));
		return (IWritableDetector<M>)cache.get(name);
	}

	@Override
	public List<String> getScannableNames() throws ScanningException {
		return cache.keySet().stream().filter(key -> cache.get(key) instanceof IScannable).collect(Collectors.toList());
	}

	@Override
	public List<String> getDetectorNames() throws ScanningException {
		return cache.keySet().stream().filter(key -> cache.get(key) instanceof IWritableDetector).collect(Collectors.toList());
	}
}
