package org.eclipse.scanning.test.malcolm.mock;

import org.eclipse.scanning.connector.epics.EpicsV4ConnectorService;
import org.eclipse.scanning.test.malcolm.AbstractAbortingMalcolmTest;
import org.eclipse.scanning.test.malcolm.device.MockedMalcolmService;
import org.junit.After;
import org.junit.Before;

public class MockAbortingMalcolmTest extends AbstractAbortingMalcolmTest {

	@Override
	@Before
	public void create() throws Exception {
		this.connectorService = new EpicsV4ConnectorService(); // Just for ActiveMQ connection!
		this.service      = new MockedMalcolmService(true);
		this.device       =  service.getDevice("zebra");
	}

	@Override
	@After
	public void dispose() throws Exception {
		if (device!=null)     device.dispose();
		((MockedMalcolmService)service).dispose();
	}

}
