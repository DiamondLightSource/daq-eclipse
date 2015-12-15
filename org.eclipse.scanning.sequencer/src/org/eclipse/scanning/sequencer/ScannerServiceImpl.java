package org.eclipse.scanning.sequencer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.scan.AbstractRunnableDevice;
import org.eclipse.scanning.api.scan.IDeviceConnectorService;
import org.eclipse.scanning.api.scan.IPositioner;
import org.eclipse.scanning.api.scan.IRunnableDevice;
import org.eclipse.scanning.api.scan.IScanningService;
import org.eclipse.scanning.api.scan.ScanModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.osgi.service.component.ComponentContext;

public final class ScannerServiceImpl implements IScanningService {
	
	private static IDeviceConnectorService deviceService;

	private static final Map<Class<?>, Class<? extends IRunnableDevice<?>>> scanners;
	
	// Use a factory pattern to register the types.
	// This pattern can always be extended by extension points
	// to allow point generators to be dynamically registered. 
	static {
		System.out.println("Starting scanning service");
		Map<Class<?>, Class<? extends IRunnableDevice<?>>> tmp = new HashMap<>(7);
		tmp.put(ScanModel.class,         AcquisitionDevice.class);
		
		// TODO FIXME Add extension points so that the mandlebrot from the examples
		// can be read by the service.
		
		scanners = Collections.unmodifiableMap(tmp);
	}
	
	

	@Override
	public final IPositioner createPositioner() throws ScanningException {
		return createPositioner(null);
	}

	@Override
	public final IPositioner createPositioner(IDeviceConnectorService hservice) throws ScanningException {
		if (hservice==null) hservice = ScannerServiceImpl.deviceService;
		return new ScannablePositioner(hservice);
	}

	
	@Override
	public final <T> IRunnableDevice<T> createRunnableDevice(T model) throws ScanningException {
        return createRunnableDevice(model, null, null);
	}

	@Override
	public final <T> IRunnableDevice<T> createRunnableDevice(T model, IPublisher<ScanBean> publisher, IDeviceConnectorService hservice) throws ScanningException {
		
		if (hservice==null) hservice = ScannerServiceImpl.deviceService;
		
		try {
			final IRunnableDevice<T> scanner = (IRunnableDevice<T>)scanners.get(model.getClass()).newInstance();
			if (scanner instanceof AbstractRunnableDevice) {
				AbstractRunnableDevice<T> ascanner = (AbstractRunnableDevice<T>)scanner;
				ascanner.setScanningService(this);
                ascanner.setDeviceService(hservice);
                ascanner.setPublisher(publisher); // May be null
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
