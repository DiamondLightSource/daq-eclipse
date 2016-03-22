package org.eclipse.scanning.sequencer;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IDeviceConnectorService;
import org.eclipse.scanning.api.device.IDeviceService;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.malcolm.IMalcolmConnection;
import org.eclipse.scanning.api.malcolm.IMalcolmService;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.malcolm.models.MalcolmConnectionInfo;
import org.eclipse.scanning.api.malcolm.models.MalcolmDetectorModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositioner;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

@SuppressWarnings("rawtypes")
public final class DeviceServiceImpl implements IDeviceService {
	
	/**
	 * The default Malcolm Hostname can be injected by spring. Otherwise
	 * this machine will be used for the malcolm host for instance 'beamline'-control.
	 */
	public static String defaultMalcolmHostname = null;
	
	/**
	 * This service can not be present for some tests which run in OSGi
	 * but mock the test laster.
	 */
	private static IDeviceConnectorService deviceService;	
	
	/**
	 * This service must be present.
	 */
	private static IMalcolmService         malcolmService;	
		

	/**
	 * Map of device model class to device class.
	 */
	private static final Map<Class<?>, Class<? extends IRunnableDevice>> aquisitionDevices;
	
	/**
	 * Map of device name to created device. Used to avoid
	 * recreating non-virtual devices many times.
	 * 
	 * TODO Should this be populated by spring?
	 */
	private static final Map<String, IRunnableDevice> namedDevices;
	
	// Use a factory pattern to register the types.
	// This pattern can always be extended by extension points
	// to allow point generators to be dynamically registered. 
	static {
		System.out.println("Starting device service");
		Map<Class<?>, Class<? extends IRunnableDevice>> aqui = new HashMap<>(7);
		aqui.put(ScanModel.class,         AcquisitionDevice.class);
		
		try {
			readExtensions(aqui);
		} catch (CoreException e) {
			e.printStackTrace(); // Static block, intentionally do not use logging.
		}

		
		aquisitionDevices  = aqui;
		namedDevices       = new HashMap<>(3);

	}
	
	
	/**
	 * Map of malcolm connections made.
	 */
	private final Map<URI, IMalcolmConnection> connections;

	/**
	 * Main constructor used in the running server by OSGi (only)
	 */
	public DeviceServiceImpl() {
		connections        = new HashMap<>(3);
	}
	
	// Test
	public DeviceServiceImpl(IDeviceConnectorService dservice) {
		this();
		deviceService = dservice;	
	}
	
	
	private static void readExtensions(Map<Class<?>, Class<? extends IRunnableDevice>> devs) throws CoreException {
		
		if (Platform.getExtensionRegistry()!=null) {
			final IConfigurationElement[] eles = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.scanning.api.device");
			for (IConfigurationElement e : eles) {
				
				final Object     mod = e.createExecutableExtension("model");
				
				if (e.getName().equals("device")) {
					final IRunnableDevice device = (IRunnableDevice)e.createExecutableExtension("class");
					devs.put(mod.getClass(), device.getClass());

				} else {
					throw new CoreException(new Status(IStatus.ERROR, "org.eclipse.scanning.sequencer", "Unrecognized device "+e.getName()));
				}
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
        return createRunnableDevice(model, null, true);
	}
	

	@Override
	public <T> IRunnableDevice<T> createRunnableDevice(T model, boolean configure) throws ScanningException {
        return createRunnableDevice(model, null, configure);
	}

	@Override
	public final <T> IRunnableDevice<T> createRunnableDevice(T model, IPublisher<ScanBean> publisher) throws ScanningException {
        return createRunnableDevice(model, publisher, true);
	}
	
	private final <T> IRunnableDevice<T> createRunnableDevice(T model, IPublisher<ScanBean> publisher, boolean configure) throws ScanningException {
				
		try {
			if (deviceService==null) deviceService = getDeviceConnector();
			
			final IRunnableDevice<T> scanner = createDevice(model);
			if (scanner instanceof AbstractRunnableDevice) {
				AbstractRunnableDevice<T> ascanner = (AbstractRunnableDevice<T>)scanner;
				ascanner.setScanningService(this);
                ascanner.setDeviceService(deviceService);
                ascanner.setPublisher(publisher); // May be null
                
                // If the model has a name for the device, we use
                // it automatically.
                try {
                    final Method getName = model.getClass().getMethod("getName");
                    String name = (String)getName.invoke(model);
                    ascanner.setName(name);
                } catch (NoSuchMethodException ignored) {
                	// getName() is not compulsory in the model
                }
			}
			
			if (configure) scanner.configure(model);
			
			if (!scanner.isVirtual()) {
				namedDevices.put(scanner.getName(), scanner);
			}
			
			return scanner;
			
		} catch (ScanningException s) {
			throw s;
		} catch (Exception ne) {
			throw new ScanningException(ne);
		}
	}
	
	@Override
	public <T> IRunnableDevice<T> getRunnableDevice(String name) throws ScanningException {
		return getRunnableDevice(name, null);
	}

	@Override
	public <T> IRunnableDevice<T> getRunnableDevice(String name, IPublisher<ScanBean> publisher) throws ScanningException {
		
		IRunnableDevice<T> device = (IRunnableDevice<T>)namedDevices.get(name);
		if (device!=null && publisher!=null && device instanceof AbstractRunnableDevice) {
			AbstractRunnableDevice<T> adevice = (AbstractRunnableDevice<T>)device;
			adevice.setPublisher(publisher); // Now all its moves will be reported by this publisher.
		}
		return device;
	}
	
	private <T> IRunnableDevice createDevice(T model) throws ScanningException, InstantiationException, IllegalAccessException, URISyntaxException, UnknownHostException {
		
		final IRunnableDevice<T> scanner;
		
		if (model instanceof MalcolmDetectorModel) {
			MalcolmConnectionInfo info = ((MalcolmDetectorModel) model).getConnectionInfo();
			URI            uri = createMalcolmURI(info);
			IMalcolmConnection conn = connections.get(uri);
			if (conn==null || !conn.isConnected()) {
				conn = malcolmService.createConnection(uri);
				connections.put(uri, conn);
			}
			return conn.getDevice(info.getDeviceName());
			
		} else if (aquisitionDevices.containsKey(model.getClass())) {
			final Class<IRunnableDevice<T>> clazz = (Class<IRunnableDevice<T>>)aquisitionDevices.get(model.getClass());
			if (clazz == null) throw new ScanningException("The model '"+model.getClass()+"' does not have a device registered for it!");
			scanner = clazz.newInstance();
			
			// TODO Might have other extension point driven devices
		} else {
			throw new ScanningException("The model '"+model.getClass()+"' does not have a device registered for it!");
		}
		return scanner;
	}

	/**
	 * 
	 * @param req
	 * @return malcolm uri for instance <code>tcp://i05-1.control.ac.uk:5600</code>
	 * @throws UnknownHostException
	 * @throws URISyntaxException 
	 */
	private URI createMalcolmURI(MalcolmConnectionInfo req) throws UnknownHostException, URISyntaxException {
		String hostName = req.getHostName();
		if (hostName == null) hostName = defaultMalcolmHostname;
		if (hostName == null) hostName = InetAddress.getLocalHost().getHostName();
		int    port  = req.getPort();
		StringBuilder buf = new StringBuilder("tcp://");
		buf.append(hostName);
		if (port>0) {
			buf.append(":");
			buf.append(port);
		}
		return new URI(buf.toString());
	}

	public static IDeviceConnectorService getDeviceService() {
		return deviceService;
	}

	public static void setDeviceService(IDeviceConnectorService connectorService) {
		DeviceServiceImpl.deviceService = connectorService;
	}
	
	private BundleContext context;

	public void start(BundleContext context) {
		this.context = context;
	}
	
	public void stop() {
		this.context = null;
		for (URI uri : connections.keySet()) {
			try {
				connections.get(uri).dispose();
			} catch (MalcolmDeviceException e) {
				System.out.println("Problem closing malcolm connection to "+uri);
				e.printStackTrace();
			}
		}
	}
	
    /**
     * Try to get the connector service or throw an exception
     * @return
     */
	private IDeviceConnectorService getDeviceConnector() throws ScanningException {
		ServiceReference<IDeviceConnectorService> ref = context.getServiceReference(IDeviceConnectorService.class);
		return context.getService(ref);
	}

	public static IMalcolmService getMalcolmService() {
		return malcolmService;
	}

	public static void setMalcolmService(IMalcolmService malcolmService) {
		DeviceServiceImpl.malcolmService = malcolmService;
	}

	public static String getDefaultMalcolmHostname() {
		return defaultMalcolmHostname;
	}

	public static void setDefaultMalcolmHostname(String defaultMalcolmHostname) {
		DeviceServiceImpl.defaultMalcolmHostname = defaultMalcolmHostname;
	}

	/**
	 * Used for testing only
	 * @param model
	 * @param device
	 */
	public void _register(Class<?> model, Class<? extends IRunnableDevice> device) {
		aquisitionDevices.put(model, device);
	}
	/**
	 * Used for testing only
	 * @param uri
	 * @param connection
	 */
	public void _registerConnection(URI uri, IMalcolmConnection connection) {
		connections.put(uri, connection);
	}

}
