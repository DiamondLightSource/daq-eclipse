package org.eclipse.scanning.test.command;

import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.server.servlet.Services;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;


public class SubmissionTest extends AbstractSubmissionTest {

	protected void setUpEventService() {
		setUpNonOSGIActivemqMarshaller();
		eservice = new EventServiceImpl(new ActivemqConnectorService());
		Services.setEventService(eservice);
	}

}
