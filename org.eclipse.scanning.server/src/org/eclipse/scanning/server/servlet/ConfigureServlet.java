package org.eclipse.scanning.server.servlet;

import java.util.Map;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.scan.IDeviceService;
import org.eclipse.scanning.api.scan.IRunnableDevice;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositioner;

/**
 * 
 * Class to be used for 
 * 
<pre>
    {@literal <bean id="configurer" class="org.eclipse.scanning.server.servlet.ConfigureServlet">}
    {@literal    <property name="broker"   value="tcp://p45-control:61616" />}
    {@literal    <property name="topic" value="org.eclipse.scanning.server.servlet.configure" />}
    {@literal </bean>}
</pre>
 * 
 *  FIXME Add security via activemq layer. Anyone can run this now.
    
    TODO No test for this servlet.
 * 
 * @author Matthew Gerring
 *
 */
public class ConfigureServlet extends AbstractSubscriberServlet<Map<String, Object>> {

	/**
	 * 
	 * 
	 * @param position The map of devices to configure, usually just one at a time.
	 * @param response
	 * @throws EventException
	 */
	@Override
	public void doObject(Map<String, Object> devices, IPublisher<Map<String, Object>> response) throws EventException {
		
		try {
			IDeviceService service = Services.getScanService();
			for (String name : devices.keySet()) {
				
				IRunnableDevice<Object> device = service.getRunnableDevice(name);
				if (device==null) throw new EventException("There is no created device called '"+name+"'");
				
				Object model = devices.get(name);
				device.configure(model);
				
			}
			
		    // TODO Figure out how to broadcast success nicely. Should send that new state has been reached by device.
		    //response.broadcast(devices);

		    
		} catch (ScanningException ne) {
			throw new EventException(ne);
		}
	}

}
