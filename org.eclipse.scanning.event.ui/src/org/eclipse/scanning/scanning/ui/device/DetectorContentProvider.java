package org.eclipse.scanning.scanning.ui.device;

import java.net.URI;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.DeviceRequest;
import org.eclipse.scanning.event.ui.ServiceHolder;
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
	
	private IRequester<DeviceRequest> requester;
	
	public DetectorContentProvider(URI uri, String request, String response) throws EventException {
		requester = ServiceHolder.getEventService().createRequestor(uri, request, response);
	}

	@Override
	public void dispose() {
		try {
			requester.disconnect();
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
		    DeviceRequest req = requester.post(new DeviceRequest());
		    return req.getDevices().toArray(new DeviceInformation<?>[req.size()]);
		} catch (Exception ne) {
			ne.printStackTrace();
			logger.error("Cannot get devices!", ne);
			return new DeviceInformation<?>[]{new DeviceInformation<Object>("No devices found")};
		}
	}

}
