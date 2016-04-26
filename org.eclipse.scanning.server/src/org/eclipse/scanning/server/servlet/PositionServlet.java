package org.eclipse.scanning.server.servlet;

import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositioner;

/**
 * 
 * Class to be used for 
 * 
<pre>
    {@literal <bean id="positioner" class="org.eclipse.scanning.server.servlet.PositionServlet">}
    {@literal    <property name="broker"   value="tcp://p45-control:61616" />}
    {@literal    <property name="topic" value="org.eclipse.scanning.server.servlet.position" />}
    {@literal </bean>}
</pre>

 * @see example.xml
 * 
 *  FIXME Add security via activemq layer. Anyone can run this now.
    TODO No test for this servlet.
 * 
 * @author Matthew Gerring
 *
 */
public class PositionServlet extends AbstractSubscriberServlet<IPosition> {

	@Override
	public void doObject(IPosition position, IPublisher<IPosition> response) throws EventException {
		
		try {
			IRunnableDeviceService service = Services.getRunnableDeviceService();
			IPositioner    poser   = service.createPositioner();
		    poser.setPosition(position);
		    
		    // TODO Figure out how to broadcast success nicely.
		    //response.broadcast(position);
		    
		} catch (ScanningException | InterruptedException ne) {
			throw new EventException(ne);
		}
	}

}
