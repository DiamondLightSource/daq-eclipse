package org.eclipse.malcolm.test.mock;

import org.eclipse.malcolm.api.IMalcolmConnection;
import org.eclipse.malcolm.api.IMalcolmDevice;
import org.eclipse.malcolm.test.AbstractMultipleClientMalcolmTest;
import org.eclipse.malcolm.test.device.MockedMalcolmService;
import org.junit.After;
import org.junit.Before;

import uk.ac.diamond.malcom.jacksonzeromq.connector.ZeromqConnectorService;

public class MockMultipleClientTest extends AbstractMultipleClientMalcolmTest {
	
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
	
	@Override
	protected IMalcolmDevice createAdditionalConnection() throws Exception {
		IMalcolmConnection aconnection   = service.createConnection(PAUSABLE);
		return aconnection.getDevice("zebra");
	}

}
