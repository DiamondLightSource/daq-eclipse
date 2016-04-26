package org.eclipse.scanning.test.scan.mock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IDeviceConnectorService;
import org.eclipse.scanning.api.device.legacy.ILegacyDeviceSupport;
import org.eclipse.scanning.api.scan.ScanningException;

public class MockScannableConnector implements IDeviceConnectorService {

	private static Map<String, INameable> cache;
	
	// Create a few random scannables with different levels.
	static {
		System.out.println("Starting up Mock IDeviceConnectorService");
		if (cache==null) cache = new HashMap<String, INameable>(3);
		register(new MockTopupMonitor("topup", 10d,  -1));
		register(new MockBeanOnMonitor("beamon", 10d, 1));
		register(new MockScannable("bpos",  0.001,  -1));
		register(new MockScannable("a", 10d, 1));
		register(new MockScannable("b", 10d, 1));
		register(new MockScannable("c", 10d, 1));
		register(new MockScannable("p", 10d, 2, "µm"));
		register(new MockScannable("q", 10d, 2, "µm"));
		register(new MockScannable("r", 10d, 2, "µm"));
		register(new MockScannable("x", 0d,  3));
		register(new MockScannable("y", 0d,  3));
		register(new MockScannable("x", 0d,  3));
		register(new MockNeXusScannable("xNex", 0d,  3));
		register(new MockNeXusScannable("yNex", 0d,  3));

		for (int i = 0; i < 10; i++) {
			register(new MockNeXusScannable("neXusScannable"+i, 0d,  3));
	    }
		for (int i = 0; i < 10; i++) {
			register(new MockNeXusScannable("monitor"+i, 0d,  3));
	    }
		for (int i = 0; i < 10; i++) {
			MockNeXusScannable metadataScannable = new MockNeXusScannable("metadataScannable"+i, 0d, 3);
			metadataScannable.setInitialPosition(i * 10.0);
			register(metadataScannable);
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
	public List<String> getScannableNames() throws ScanningException {
		return cache.keySet().stream().filter(key -> cache.get(key) instanceof IScannable).collect(Collectors.toList());
	}

	@Override
	public ILegacyDeviceSupport getLegacyDeviceSupport() {
		return null; // Not supported
	}

}
