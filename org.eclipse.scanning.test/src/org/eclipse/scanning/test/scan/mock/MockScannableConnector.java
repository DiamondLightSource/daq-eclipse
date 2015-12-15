package org.eclipse.scanning.test.scan.mock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.scan.IDeviceConnectorService;
import org.eclipse.scanning.api.scan.IReadableDetector;
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
	public <T> IScannable<T> getScannable(String name) throws ScanningException {
		if (cache==null) cache = new HashMap<String, INameable>(3);
		if (cache.containsKey(name)) return (IScannable<T>)cache.get(name);
		register(new MockScannable(name, 0d));
		return (IScannable<T>)cache.get(name);
	}

	@Override
	public <M> IReadableDetector<M> getDetector(String name) throws ScanningException {
		if (cache==null) cache = new HashMap<String, INameable>(3);
		if (cache.containsKey(name)) return (IReadableDetector<M>)cache.get(name);
		register(new MockReadableDetector(name));
		return (IReadableDetector<M>)cache.get(name);
	}

	
	public <T> IScannable<T> createMockScannable(String name) {
		
		IScannable s = mock(MockScannable.class, name);
		when(s.getName()).thenReturn(name);

		register(s);
		return s;
	}

	public IReadableDetector<MockDetectorModel> createMockDetector(String name) {
		IReadableDetector d = mock(MockReadableDetector.class, name);
		when(d.getName()).thenReturn(name);
		register(d);
		return d;
	}
}
