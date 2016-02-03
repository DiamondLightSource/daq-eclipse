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
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

@SuppressWarnings("rawtypes")
public final class ScanningServiceImpl implements IScanningService {
	
	private static IDeviceConnectorService deviceService;
	private BundleContext context;
	
	
	/**
	 * Main constuctor used in the running server by OSGi (only)
	 */
	public ScanningServiceImpl() {
		
	}
	
	// Test
	public ScanningServiceImpl(IDeviceConnectorService dservice) {
		deviceService = dservice;	
	}

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
			final IConfigurationElement[] eles = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.scanning.api.device");
			for (IConfigurationElement e : eles) {
				final IRunnableDevice gen = (IRunnableDevice)e.createExecutableExtension("class");
				final Object     mod = e.createExecutableExtension("model");
				devs.put(mod.getClass(), gen.getClass());
			}
		}
	}
	

	@Override
	public final IPositioner createPositioner() throws ScanningException {
		// Try to set a deviceService if it is null
		if (deviceService==null) deviceService = getDeviceConnector();
		return new ScannablePositioner(deviceService);
	}

	
	@Override
	public final <T> IRunnableDevice<T> createRunnableDevice(T model) throws ScanningException {
        return createRunnableDevice(model, null);
	}

	@Override
	public final <T> IRunnableDevice<T> createRunnableDevice(T model, IPublisher<ScanBean> publisher) throws ScanningException {
				
		try {
			if (deviceService==null) deviceService = getDeviceConnector();
			final Class<IRunnableDevice<T>> clazz = (Class<IRunnableDevice<T>>)scanners.get(model.getClass());
			final IRunnableDevice<T> scanner = clazz.newInstance();
			if (scanner instanceof AbstractRunnableDevice) {
				AbstractRunnableDevice<T> ascanner = (AbstractRunnableDevice<T>)scanner;
				ascanner.setScanningService(this);
                ascanner.setDeviceService(deviceService);
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

	public void start(BundleContext context) {
		this.context = context;
	}
	
	public void stop() {
		this.context = null;
	}
	
    /**
     * Try to get the connector service or throw an exception
     * @return
     */
	private IDeviceConnectorService getDeviceConnector() throws ScanningException {
		ServiceReference<IDeviceConnectorService> ref = context.getServiceReference(IDeviceConnectorService.class);
		return context.getService(ref);
	}

}
