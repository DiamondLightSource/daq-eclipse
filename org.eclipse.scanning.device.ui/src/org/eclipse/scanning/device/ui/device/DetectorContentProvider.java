package org.eclipse.scanning.device.ui.device;

import java.net.URI;
import java.util.Collection;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IDisconnectable;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.device.ui.ServiceHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Connects to server and gets the list of detectors with their current
 * models.
 * 
 * @author Matthew Gerring
 *
 */
class DetectorContentProvider implements IStructuredContentProvider {
	
	private static final Logger logger = LoggerFactory.getLogger(DetectorContentProvider.class);
	
	/**
	 * Remote device service.
	 */
	private IRunnableDeviceService dservice;
	
	public DetectorContentProvider(URI uri) throws EventException {
		dservice = ServiceHolder.getEventService().createRemoteService(uri, IRunnableDeviceService.class);
	}

	@Override
	public void dispose() {
		try {
			if (dservice instanceof IDisconnectable) ((IDisconnectable)dservice).disconnect();
		} catch (EventException e) {
			logger.error("Cannot disconnect from device request object.", e);
		}
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object[] getElements(Object inputElement) {
		
		try {
		    Collection<DeviceInformation<?>> devices = dservice.getDeviceInformation();
		    return devices.toArray(new DeviceInformation[devices.size()]);
		    
		} catch (Exception ne) {
			ne.printStackTrace();
			logger.error("Cannot get devices!", ne);
			return new DeviceInformation<?>[]{new DeviceInformation<Object>("No devices found")};
		}
	}

}
