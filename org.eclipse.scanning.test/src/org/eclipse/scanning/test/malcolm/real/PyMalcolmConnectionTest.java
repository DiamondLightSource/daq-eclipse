package org.eclipse.scanning.test.malcolm.real;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Map;

import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.IMalcolmService;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.connector.epics.EpicsV4ConnectorService;
import org.eclipse.scanning.example.malcolm.ExampleMalcolmDevice;
import org.eclipse.scanning.example.malcolm.ExampleMalcolmModel;
import org.eclipse.scanning.malcolm.core.MalcolmService;
import org.junit.Test;

/**
 * Class for testing the Epics V4 Connection to a real PyMalcolm Device
 * @author Matt Taylor
 *
 */
public class PyMalcolmConnectionTest {

	private EpicsV4ConnectorService connectorService;

	private IMalcolmService service;

	private ExampleMalcolmDevice dummyMalcolmDevice;

	/**
	 * Creates a connection to a test Malcolm device running PyMalcolm.
	 * N.B. The PyMalcolm example 'DEMO-HELLO-PVA' needs to be running or this test will fail.
	 * See http://pymalcolm.readthedocs.io/en/latest/user_docs/hello.html
	 * @throws Exception
	 */
	@Test
	public void ConnectToPyMalcolmDevice() throws Exception {

		try {
			// Setup the objects
			this.connectorService = new EpicsV4ConnectorService();

			// The real service, get it from OSGi outside this test!
			// Not required in OSGi mode (do not add this to your real code GET THE SERVICE FROM OSGi!)
			this.service = new MalcolmService(connectorService, null);

			// Get the device
			IMalcolmDevice<ExampleMalcolmModel> modelledDevice = service.getDevice("hello3");

			// Get the device state. This should fail as the device does no exist
			DeviceState deviceState = modelledDevice.getDeviceState();
			
			assertEquals(DeviceState.READY, deviceState);

		} catch (Exception ex) {
			ex.printStackTrace();
			fail(ex.getMessage());
		} finally {
			Thread.sleep(1000);
		}
	}

}
