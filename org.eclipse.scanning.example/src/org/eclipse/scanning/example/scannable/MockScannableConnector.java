package org.eclipse.scanning.example.scannable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.scanning.api.AbstractScannable;
import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.Location;

public class MockScannableConnector implements IScannableDeviceService {

	private Map<String, INameable> cache;
	private IPublisher<Location> positionPublisher;
	
	// Create a few random scannables with different levels.
	public MockScannableConnector(IPublisher<Location> positionPublisher) {
		
		System.out.println("Starting up Mock IScannableDeviceService");
		this.positionPublisher = positionPublisher;
		
		if (cache==null) cache = new HashMap<String, INameable>(3);
		register(new MockTopupMonitor("topup", 10d,  -1));
		register(new MockBeanOnMonitor("beamon", 10d, 1));
		register(new MockScannable("bpos",  0.001,  -1));
		register(new MockScannable("a", 10d, 1, "mm"));
		register(new MockScannable("b", 10d, 1, "mm"));
		register(new MockScannable("c", 10d, 1, "mm"));
		register(new MockScannable("p", 10d, 2, "µm"));
		register(new MockScannable("q", 10d, 2, "µm"));
		register(new MockScannable("r", 10d, 2, "µm"));
		
		MockScannable x = new MockScannable("x", 0d,  3, "�m");
		x.setRealisticMove(true);
		x.setMoveRate(10000); // �m/s or 1 cm/s
		register(x);
		
		MockScannable y = new MockScannable("y", 0d,  3, "�m");
		y.setRealisticMove(true);
		y.setMoveRate(100); // �m/s, faster than real?
		register(y);
		
		register(new MockScannable("z", 2d,  3, "�m"));
		register(new MockNeXusScannable("xNex", 0d,  3, "�m"));
		register(new MockNeXusScannable("yNex", 0d,  3, "�m"));
		register(new MockScannable("benchmark1",  0.0,  -1, false));
		
		MockNeXusScannable temp= new MockNeXusScannable("T", 295,  3, "K");
		temp.setRealisticMove(true);
		temp.setMoveRate(10); // K/s much faster than real but device used in tests.
		register(temp);
		for (int i = 0; i < 10; i++) {
			MockScannable t = new MockScannable("T"+i, 0d,  0, "K");
			t.setRequireSleep(false);
			register(t);

		}
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

	public void register(INameable mockScannable) {
		cache.put(mockScannable.getName(), mockScannable);
		if (mockScannable instanceof AbstractScannable) {
			((AbstractScannable)mockScannable).setPublisher(positionPublisher);
		}
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

}
