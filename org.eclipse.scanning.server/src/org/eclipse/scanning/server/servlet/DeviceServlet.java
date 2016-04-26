package org.eclipse.scanning.server.servlet;

import org.eclipse.scanning.api.device.DeviceResponse;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.IResponseProcess;
import org.eclipse.scanning.api.event.scan.DeviceRequest;

/**
 * A servlet to get the available devices from the IDeviceService.
 * 
     Spring config started, for instance:
    <pre>
    
    {@literal <bean id="deviceServlet" class="org.eclipse.scanning.server.servlet.DeviceServlet" init-method="connect">}
    {@literal    <property name="broker"          value="tcp://p45-control:61616" />}
    {@literal    <property name="requestTopic"    value="uk.ac.diamond.p45.requestTopic" />}
    {@literal    <property name="responseTopic"   value="uk.ac.diamond.p45.responseTopic"   />}
    {@literal </bean>}
     
    </pre>
    
    FIXME Add security via activemq layer. Anyone can run this now.

 * 
 * @author Matthew Gerring
 *
 */
public class DeviceServlet extends AbstractResponderServlet<DeviceRequest> {

	@Override
	public IResponseProcess<DeviceRequest> createResponder(DeviceRequest bean, IPublisher<DeviceRequest> response) throws EventException {
		return new DeviceResponse(Services.getRunnableDeviceService(), bean, response);
	}

}
