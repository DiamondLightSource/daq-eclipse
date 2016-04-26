package org.eclipse.scanning.test.command;

import org.eclipse.scanning.api.event.IEventService;


public class SubmissionPluginTest extends AbstractSubmissionTest {

	// OSGI will call this.
	public static void setService(IEventService eservice) {
		SubmissionPluginTest.eservice = eservice;
	}

	@Override
	protected void setUpEventService() {
		// OSGI has already done it!
	}
}
