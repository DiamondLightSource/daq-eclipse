package org.eclipse.scanning.sequencer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scanning.api.scan.AbstractScanner;
import org.eclipse.scanning.api.scan.IDeviceConnectorService;
import org.eclipse.scanning.api.scan.IPositioner;
import org.eclipse.scanning.api.scan.IRunnableDevice;
import org.eclipse.scanning.api.scan.IScanningService;
import org.eclipse.scanning.api.scan.ScanModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.osgi.service.component.ComponentContext;

public class ScannerServiceImpl implements IScanningService {
	
	private static IDeviceConnectorService deviceService;

	private static final Map<Class<?>, Class<? extends IRunnableDevice<?>>> scanners;
	
	// Use a factory pattern to register the types.
	// This pattern can always be extended by extension points
	// to allow point generators to be dynamically registered. 
	static {
		System.out.println("Starting scanning service");
		Map<Class<?>, Class<? extends IRunnableDevice<?>>> tmp = new HashMap<>(7);
		tmp.put(ScanModel.class,         DescretePointScanner.class);
		
		// TODO FIXME Add extension points so that the mandlebrot from the examples
		// can be read by the service.
		
		scanners = Collections.unmodifiableMap(tmp);
	}
	
	

	@Override
	public IPositioner createPositioner() throws ScanningException {
		return createPositioner(null);
	}

	@Override
	public IPositioner createPositioner(IDeviceConnectorService hservice) throws ScanningException {
		if (hservice==null) hservice = ScannerServiceImpl.deviceService;
		return new Positioner(hservice);
	}

	
	@Override
	public <T> IRunnableDevice<T> createScanner(T model) throws ScanningException {
        return createScanner(model, null);
	}

	@Override
	public <T> IRunnableDevice<T> createScanner(T model, IDeviceConnectorService hservice) throws ScanningException {
		
		if (hservice==null) hservice = ScannerServiceImpl.deviceService;
		
		try {
			final IRunnableDevice<T> scanner = (IRunnableDevice<T>)scanners.get(model.getClass()).newInstance();
			if (scanner instanceof AbstractScanner) {
				AbstractScanner<T> ascanner = (AbstractScanner<T>)scanner;
				ascanner.setScanningService(this);
                ascanner.setDeviceService(hservice);
			}
			scanner.configure(model);
			return scanner;
			
		} catch (ScanningException s) {
			throw s;
		} catch (Exception ne) {
			throw new ScanningException(ne);
		}
	}

	public static IDeviceConnectorService getDeviceService() {
		return deviceService;
	}

	public static void setDeviceService(IDeviceConnectorService connectorService) {
		ScannerServiceImpl.deviceService = connectorService;
	}

	private static ComponentContext context;

	public void start(ComponentContext context) {
		ScannerServiceImpl.context = context;
	}
	
	public void stop() {
		context = null;
	}

}
