package org.eclipse.scanning.test.command;

import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.points.serialization.PointsModelMarshaller;
import org.eclipse.scanning.server.servlet.Services;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;


public class SubmissionTest extends AbstractSubmissionTest {

	protected void setUpEventService() {
		ActivemqConnectorService.setJsonMarshaller(new MarshallerService(new PointsModelMarshaller()));
		eservice = new EventServiceImpl(new ActivemqConnectorService());
		Services services = new Services();
		services.setEventService(eservice);
	}

}
