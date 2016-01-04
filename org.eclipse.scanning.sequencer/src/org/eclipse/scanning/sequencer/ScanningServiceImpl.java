package org.eclipse.scanning.sequencer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.scan.AbstractRunnableDevice;
import org.eclipse.scanning.api.scan.IDeviceConnectorService;
import org.eclipse.scanning.api.scan.IRunnableDevice;
import org.eclipse.scanning.api.scan.IScanningService;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositioner;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.osgi.service.component.ComponentContext;

public final class ScanningServiceImpl implements IScanningService {
	
	private static IDeviceConnectorService deviceService;

	private static final Map<Class<?>, Class<? extends IRunnableDevice>> scanners;
	
	// Use a factory pattern to register the types.
	// This pattern can always be extended by extension points
	// to allow point generators to be dynamically registered. 
	static {
		System.out.println("Starting scanning service");
		Map<Class<?>, Class<? extends IRunnableDevice>> tmp = new HashMap<>(7);
		tmp.put(ScanModel.class,         AcquisitionDevice.class);
		
		try {
			readExtensions(tmp);
		} catch (CoreException e) {
			e.printStackTrace(); // Static block, intentionally do not use logging.
		}

		
		scanners = Collections.unmodifiableMap(tmp);
	}
	

	private static void readExtensions(Map<Class<?>, Class<? extends IRunnableDevice>> devs) throws CoreException {
		
		if (Platform.getExtensionRegistry()!=null) {
			final IConfigurationElement[] eles = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.scanning.api.detector");
			for (IConfigurationElement e : eles) {
				final IRunnableDevice gen = (IRunnableDevice)e.createExecutableExtension("class");
				final Object     mod = e.createExecutableExtension("model");
				devs.put(mod.getClass(), gen.getClass());
			}
		}
	}
	

	@Override
	public final IPositioner createPositioner() throws ScanningException {
		return createPositioner(null);
	}

	@Override
	public final IPositioner createPositioner(IDeviceConnectorService hservice) throws ScanningException {
		if (hservice==null) hservice = ScanningServiceImpl.deviceService;
		return new ScannablePositioner(hservice);
	}

	
	@Override
	public final <T> IRunnableDevice<T> createRunnableDevice(T model) throws ScanningException {
        return createRunnableDevice(model, null, null);
	}

	@Override
	public final <T> IRunnableDevice<T> createRunnableDevice(T model, IPublisher<ScanBean> publisher, IDeviceConnectorService hservice) throws ScanningException {
		
		if (hservice==null) hservice = ScanningServiceImpl.deviceService;
		
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
		ScanningServiceImpl.deviceService = connectorService;
	}

	private static ComponentContext context;

	public void start(ComponentContext context) {
		ScanningServiceImpl.context = context;
	}
	
	public void stop() {
		context = null;
	}

}
