package org.eclipse.malcolm.test.mock;

import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.malcolm.api.IMalcolmConnection;
import org.eclipse.malcolm.api.IMalcolmDevice;
import org.eclipse.malcolm.test.AbstractAbortingMalcolmTest;
import org.eclipse.malcolm.test.device.MockedMalcolmService;
import org.junit.After;
import org.junit.Before;

import uk.ac.diamond.malcom.jacksonzeromq.connector.ZeromqConnectorService;

public class MockAbortingMalcolmTest extends AbstractAbortingMalcolmTest {

	@Override
	@Before
	public void create() throws Exception {
		this.connectorService = new ZeromqConnectorService(); // Just for ActiveMQ connection!
		this.service      = new MockedMalcolmService();
		this.connection   = service.createConnection(PAUSABLE);
		this.device       =  connection.getDevice("zebra");
	}

	@Override
	@After
	public void dispose() throws Exception {
		if (device!=null)     device.dispose();
		if (connection!=null) connection.dispose();
		((MockedMalcolmService)service).dispose();
	}

}
