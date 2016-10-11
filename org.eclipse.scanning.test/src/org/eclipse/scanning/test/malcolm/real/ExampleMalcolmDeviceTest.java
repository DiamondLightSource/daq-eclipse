package org.eclipse.scanning.test.malcolm.real;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.IMalcolmService;
import org.eclipse.scanning.api.malcolm.attributes.MalcolmAttribute;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.SpiralModel;
import org.eclipse.scanning.connector.epics.EpicsV4ConnectorService;
import org.eclipse.scanning.example.malcolm.ExampleMalcolmDevice;
import org.eclipse.scanning.example.malcolm.ExampleMalcolmModel;
import org.eclipse.scanning.malcolm.core.MalcolmService;
import org.eclipse.scanning.points.PointGeneratorFactory;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVUnion;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.Union;
import org.junit.Test;

public class ExampleMalcolmDeviceTest {

	EpicsV4ConnectorService connectorService;

	IMalcolmService service;

	IPointGenerator<?> generator = null;

	ExampleMalcolmDevice dummyMalcolmDevice;

	/**
	 * Starts an instance of the ExampleMalcolmDevice and then probes it with the configure() and call() methods
	 * as well as getting a list of all attributes and several specific attributes
	 * @throws Exception
	 */
	@Test
	public void ConfigureAndRunDummyMalcolm() throws Exception {

		try {
			// Setup the objects
			this.connectorService = new EpicsV4ConnectorService();

			// The real service, get it from OSGi outside this test!
			// Not required in OSGi mode (do not add this to your real code GET THE SERVICE FROM OSGi!)
			this.service = new MalcolmService(connectorService);

			// Start the dummy test device
			new Thread(new DeviceRunner()).start();

			// Get the device
			IMalcolmDevice<ExampleMalcolmModel> modelledDevice = service.getDevice(getTestDeviceName());

			// Setup the model and other configuration items
			List<IROI> regions = new LinkedList<>();
			regions.add(new CircularROI(2, 6, 7));
			regions.add(new CircularROI(3, 8, 9));

			IPointGeneratorService pgService = new PointGeneratorFactory();
			IPointGenerator<SpiralModel> temp = pgService
					.createGenerator(new SpiralModel("x", "y", 2, new BoundingBox(0, -5, 10, 5)), regions);
			IPointGenerator<?> scan = pgService.createCompoundGenerator(temp);

			ExampleMalcolmModel pmac1 = new ExampleMalcolmModel();
			pmac1.setExposureTime(45f);
			pmac1.setGenerator(scan);

			// Call configure
			modelledDevice.configure(pmac1);

			// Call run
			modelledDevice.run(null);

			// Get a list of all attributes on the device
			List<MalcolmAttribute> attribs = modelledDevice.getAllAttributes();
			assertEquals(10, attribs.size());

			boolean stateFound = false;
			boolean statusFound = false;
			boolean busyFound = false;
			boolean totalStepsFound = false;
			boolean aFound = false;
			boolean bFound = false;
			boolean axesFound = false;
			boolean datasetsFound = false;
			boolean generatorFound = false;
			boolean currentStepFound = false;
			boolean aIsNotWriteable = false;
			boolean bIsWriteable = false;

			for (MalcolmAttribute ma : attribs) {
				if (ma.getName().equals("state")) {
					stateFound = true;
				} else if (ma.getName().equals("status")) {
					statusFound = true;
				} else if (ma.getName().equals("busy")) {
					busyFound = true;
				} else if (ma.getName().equals("totalSteps")) {
					totalStepsFound = true;
				} else if (ma.getName().equals("A")) {
					aFound = true;
					aIsNotWriteable = !ma.isWriteable();
				} else if (ma.getName().equals("B")) {
					bFound = true;
					bIsWriteable = ma.isWriteable();
				} else if (ma.getName().equals("axes")) {
					axesFound = true;
				} else if (ma.getName().equals("datasets")) {
					datasetsFound = true;
				} else if (ma.getName().equals("generator")) {
					generatorFound = true;
				} else if (ma.getName().equals("currentStep")) {
					currentStepFound = true;
				}
			}

			assertTrue(stateFound);
			assertTrue(statusFound);
			assertTrue(busyFound);
			assertTrue(totalStepsFound);
			assertTrue(aFound);
			assertTrue(aIsNotWriteable);
			assertTrue(bFound);
			assertTrue(bIsWriteable);
			assertTrue(axesFound);
			assertTrue(datasetsFound);
			assertTrue(generatorFound);
			assertTrue(currentStepFound);

			// Get a specific choice (string) attribute
			Object stateValue = modelledDevice.getAttributeValue("state");
			if (stateValue instanceof String) {
				String stateValueStr = (String) stateValue;
				assertTrue(stateValueStr.equals("IDLE"));
			} else {
				fail("state value was was expected to be a string but wasn't");
			}

			// Get a specific string attribute
			Object statusValue = modelledDevice.getAttributeValue("status");
			if (statusValue instanceof String) {
				String statusValueStr = (String) statusValue;
				assertTrue(statusValueStr.equals("Test Status"));
			} else {
				fail("status value was was expected to be a string but wasn't");
			}

			// Get a specific boolean attribute
			Object busyValue = modelledDevice.getAttributeValue("busy");
			if (busyValue instanceof Boolean) {
				Boolean busyValueBool = (Boolean) busyValue;
				assertTrue(busyValueBool.equals(false));
			} else {
				fail("busy value was was expected to be a boolean but wasn't");
			}

			// Get a specific number attribute
			Object totalStepsValue = modelledDevice.getAttributeValue("totalSteps");
			if (totalStepsValue instanceof Integer) {
				Integer totalStepsValueInt = (Integer) totalStepsValue;
				assertTrue(totalStepsValueInt.equals(123));
			} else {
				fail("totalSteps value was was expected to be an int but wasn't");
			}

			// Check the RPC calls were received correctly by the device
			Map<String, PVStructure> rpcCalls = dummyMalcolmDevice.getReceivedRPCCalls();

			assertEquals(2, rpcCalls.size());

			// configure
			PVStructure configureCall = rpcCalls.get("configure");

			Union union = FieldFactory.getFieldCreate().createVariantUnion();

			Structure generatorStructure = FieldFactory.getFieldCreate().createFieldBuilder()
					.addArray("mutators", union).addArray("generators", union).addArray("excluders", union)
					.setId("scanpointgenerator:generator/CompoundGenerator:1.0").createStructure();

			Structure spiralGeneratorStructure = FieldFactory.getFieldCreate().createFieldBuilder()
					.addArray("centre", ScalarType.pvDouble).add("scale", ScalarType.pvDouble)
					.add("units", ScalarType.pvString).addArray("names", ScalarType.pvString)
					.add("alternate_direction", ScalarType.pvBoolean).add("radius", ScalarType.pvDouble)
					.setId("scanpointgenerator:generator/SpiralGenerator:1.0").createStructure();

			Structure configureStructure = FieldFactory.getFieldCreate().createFieldBuilder()
					.add("exposure", ScalarType.pvFloat).add("generator", generatorStructure).createStructure();

			PVStructure spiralGeneratorPVStructure = PVDataFactory.getPVDataCreate()
					.createPVStructure(spiralGeneratorStructure);
			double[] centre = new double[] { 7.5, 8.5 };
			spiralGeneratorPVStructure.getSubField(PVDoubleArray.class, "centre").put(0, centre.length, centre, 0);
			spiralGeneratorPVStructure.getDoubleField("scale").put(2.0);
			spiralGeneratorPVStructure.getStringField("units").put("mm");
			String[] names = new String[] { "x", "y" };
			spiralGeneratorPVStructure.getSubField(PVStringArray.class, "names").put(0, names.length, names, 0);
			spiralGeneratorPVStructure.getBooleanField("alternate_direction").put(false);
			spiralGeneratorPVStructure.getDoubleField("radius").put(4.949747468305833);

			PVStructure configurePVStructure = PVDataFactory.getPVDataCreate().createPVStructure(configureStructure);
			PVUnion pvu1 = PVDataFactory.getPVDataCreate().createPVVariantUnion();
			pvu1.set(spiralGeneratorPVStructure);
			PVUnion[] unionArray = new PVUnion[1];
			unionArray[0] = pvu1;
			configurePVStructure.getUnionArrayField("generator.generators").put(0, unionArray.length, unionArray, 0);
			configurePVStructure.getFloatField("exposure").put(45.0f);

			assertEquals(configureStructure, configureCall.getStructure());
			assertEquals(configurePVStructure, configureCall);

			// run
			PVStructure runCall = rpcCalls.get("run");

			Structure runStructure = FieldFactory.getFieldCreate().createFieldBuilder().createStructure();
			PVStructure runPVStructure = PVDataFactory.getPVDataCreate().createPVStructure(runStructure);

			assertEquals(runStructure, runCall.getStructure());
			assertEquals(runPVStructure, runCall);

			modelledDevice.dispose();

		} catch (Exception ex) {
			ex.printStackTrace();
			fail(ex.getMessage());
		} finally {
			// Stop the device
			dummyMalcolmDevice.stop();
		}
	}

	public class DeviceRunner implements Runnable {

		public void run() {
			String deviceName = getTestDeviceName();
			dummyMalcolmDevice = new ExampleMalcolmDevice(deviceName);
			dummyMalcolmDevice.run();
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
