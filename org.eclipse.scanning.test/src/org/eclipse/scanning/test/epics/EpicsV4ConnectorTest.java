package org.eclipse.scanning.test.epics;

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
 * Class for testing the Epics V4 Connection
 * @author Matt Taylor
 *
 */
public class EpicsV4ConnectorTest {

	private EpicsV4ConnectorService connectorService;

	private IMalcolmService service;

	private ExampleMalcolmDevice dummyMalcolmDevice;

	/**
	 * Starts an instance of the ExampleMalcolmDevice and then attempts to get the device state from 
	 * it to check that the Epics V4 connection mechanism is working.
	 * @throws Exception
	 */
	@Test
	public void ConnectToValidDevice() throws Exception {

		try {
			// Setup the objects
			this.connectorService = new EpicsV4ConnectorService();

			// The real service, get it from OSGi outside this test!
			// Not required in OSGi mode (do not add this to your real code GET THE SERVICE FROM OSGi!)
			this.service = new MalcolmService(connectorService, null);

			// Start the dummy test device
			new Thread(new DeviceRunner()).start();

			// Get the device
			IMalcolmDevice<ExampleMalcolmModel> modelledDevice = service.getDevice(getTestDeviceName());

			// Get the device state.
			DeviceState deviceState = modelledDevice.getDeviceState();
			
			assertEquals(DeviceState.IDLE, deviceState);

		} catch (Exception ex) {
			ex.printStackTrace();
			fail(ex.getMessage());
		} finally {
			// Stop the device
			dummyMalcolmDevice.stop();
			Thread.sleep(1000);
		}
	}

	/**
	 * Attempts to get the state of a device that doesn't exist. This should throw an exception with a message
	 * detailing that the channel is unavailable.
	 * @throws Exception
	 */
	@Test
	public void ConnectToInvalidDevice() throws Exception {

		try {
			// Setup the objects
			this.connectorService = new EpicsV4ConnectorService();

			// The real service, get it from OSGi outside this test!
			// Not required in OSGi mode (do not add this to your real code GET THE SERVICE FROM OSGi!)
			this.service = new MalcolmService(connectorService, null);

			// Start the dummy test device
			//new Thread(new DeviceRunner()).start();

			// Get the device
			IMalcolmDevice<ExampleMalcolmModel> modelledDevice = service.getDevice("INVALID_DEVICE");

			// Get the device state. This should fail as the device does not exist
			modelledDevice.getDeviceState();
			
			fail("No exception thrown but one was expected");

		} catch (Exception ex) {
			assertEquals(MalcolmDeviceException.class, ex.getClass());
			assertTrue(ex.getMessage().contains("Failed to connect to device 'INVALID_DEVICE'"));
			assertTrue(ex.getMessage().contains("channel not connected"));
		}
	}

	/**
	 * Starts an instance of the ExampleMalcolmDevice and then attempts to get an attribute that doesn't exist.
	 * This should throw an exception with a message detailing that the attribute is not accessible.
	 * @throws Exception
	 */
	@Test
	public void GetNonExistantAttribute() throws Exception {

		try {
			// Setup the objects
			this.connectorService = new EpicsV4ConnectorService();

			// The real service, get it from OSGi outside this test!
			// Not required in OSGi mode (do not add this to your real code GET THE SERVICE FROM OSGi!)
			this.service = new MalcolmService(connectorService, null);

			// Start the dummy test device
			new Thread(new DeviceRunner()).start();

			// Get the device
			IMalcolmDevice<ExampleMalcolmModel> modelledDevice = service.getDevice(getTestDeviceName());

			// Get the device state. This should fail as the device does no exist
			modelledDevice.getAttribute("NON_EXISTANT");

			fail("No exception thrown but one was expected");

		} catch (Exception ex) {
			assertEquals(MalcolmDeviceException.class, ex.getClass());
			assertTrue("Message was: " + ex.getMessage(), ex.getMessage().contains("CreateGet failed for 'NON_EXISTANT'"));
			assertTrue(ex.getMessage().contains("illegal pvRequest"));
		} finally {
			// Stop the device
			dummyMalcolmDevice.stop();
		}
	}

	public class DeviceRunner implements Runnable {

		public void run() {
			String deviceName = getTestDeviceName();
			dummyMalcolmDevice = new ExampleMalcolmDevice(deviceName);
			dummyMalcolmDevice.start();
		}

	}

	private String getTestDeviceName() {
		String deviceName = "DummyMalcolmDevice";

		Map<String, String> env = System.getenv();
		if (env.containsKey("COMPUTERNAME"))
			deviceName = env.get("COMPUTERNAME");
		else if (env.containsKey("HOSTNAME"))
			deviceName = env.get("HOSTNAME");

		return deviceName.replace('.', ':') + ":malcolmTest";
	}

}
