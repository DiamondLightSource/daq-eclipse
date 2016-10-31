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
import org.eclipse.scanning.api.malcolm.MalcolmTable;
import org.eclipse.scanning.api.malcolm.attributes.MalcolmAttribute;
import org.eclipse.scanning.api.malcolm.attributes.StringAttribute;
import org.eclipse.scanning.api.malcolm.attributes.TableAttribute;
import org.eclipse.scanning.api.points.IMutator;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.SpiralModel;
import org.eclipse.scanning.connector.epics.EpicsV4ConnectorService;
import org.eclipse.scanning.example.malcolm.ExampleMalcolmDevice;
import org.eclipse.scanning.example.malcolm.ExampleMalcolmModel;
import org.eclipse.scanning.malcolm.core.MalcolmService;
import org.eclipse.scanning.points.PointGeneratorFactory;
import org.eclipse.scanning.points.mutators.FixedDurationMutator;
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
			regions.add(new CircularROI(2, 6, -1));
			regions.add(new CircularROI(3, 8, 9));
			
			List<IMutator> mutators = new LinkedList<>();
			mutators.add(new FixedDurationMutator(23.1));

			IPointGeneratorService pgService = new PointGeneratorFactory();
			IPointGenerator<SpiralModel> temp = pgService
					.createGenerator(new SpiralModel("stage_x", "stage_y", 2, new BoundingBox(0, -5, 10, 5)), regions);
			IPointGenerator<?> scan = pgService.createCompoundGenerator(temp);
			
			CompoundModel<?> cm = (CompoundModel<?>) scan.getModel();
			cm.setMutators(mutators);

			ExampleMalcolmModel pmac1 = new ExampleMalcolmModel();
			pmac1.setExposure(45f);
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
			boolean completedStepsFound = false;
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
				} else if (ma.getName().equals("completedSteps")) {
					completedStepsFound = true;
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
			assertTrue(completedStepsFound);

			// Get a specific choice (string) attribute
			Object stateValue = modelledDevice.getAttributeValue("state");
			if (stateValue instanceof String) {
				String stateValueStr = (String) stateValue;
				assertEquals("READY", stateValueStr);
			} else {
				fail("state value was expected to be a string but wasn't");
			}

			// Get a specific string attribute
			Object statusValue = modelledDevice.getAttributeValue("status");
			if (statusValue instanceof String) {
				String statusValueStr = (String) statusValue;
				assertEquals("Test Status", statusValueStr);
			} else {
				fail("status value was expected to be a string but wasn't");
			}

			// Get a specific boolean attribute
			Object busyValue = modelledDevice.getAttributeValue("busy");
			if (busyValue instanceof Boolean) {
				Boolean busyValueBool = (Boolean) busyValue;
				assertTrue(busyValueBool.equals(false));
			} else {
				fail("busy value was expected to be a boolean but wasn't");
			}

			// Get a specific number attribute
			Object totalStepsValue = modelledDevice.getAttributeValue("totalSteps");
			if (totalStepsValue instanceof Integer) {
				Integer totalStepsValueInt = (Integer) totalStepsValue;
				assertEquals((Integer)123, totalStepsValueInt);
			} else {
				fail("totalSteps value was expected to be an int but wasn't");
			}

			// Get a specific string attribute (full attribute)
			Object statusAttributeValue = modelledDevice.getAttribute("status");
			if (statusAttributeValue instanceof StringAttribute) {
				StringAttribute statusAttributeValueStr = (StringAttribute) statusAttributeValue;
				assertEquals("status", statusAttributeValueStr.getName());
				assertEquals("Test Status", statusAttributeValueStr.getValue());
			} else {
				fail("status value was expected to be a StringAttribute but wasn't");
			}
			
			// Get a specific table attribute (full attribute)
			Object datasetsAttributeValue = modelledDevice.getAttribute("datasets");
			if (datasetsAttributeValue instanceof TableAttribute) {
				TableAttribute datasetAttributeValueTable = (TableAttribute) datasetsAttributeValue;
				assertEquals("datasets", datasetAttributeValueTable.getName());
				MalcolmTable malcolmTable = datasetAttributeValueTable.getValue();
				
				assertEquals(4, malcolmTable.getHeadings().size());
				assertEquals("detector", malcolmTable.getHeadings().get(0));
				assertEquals("filename", malcolmTable.getHeadings().get(1));
				assertEquals("dataset", malcolmTable.getHeadings().get(2));
				assertEquals("users", malcolmTable.getHeadings().get(3));
				assertEquals(3, malcolmTable.getColumn("dataset").size());
				assertEquals("/entry/detector/I200", malcolmTable.getColumn("dataset").get(0));
				assertEquals("/entry/detector/Iref", malcolmTable.getColumn("dataset").get(1));
				assertEquals("/entry/detector/det1", malcolmTable.getColumn("dataset").get(2));
			} else {
				fail("datasets value was expected to be a TableAttribute but wasn't");
			}

			// Check the RPC calls were received correctly by the device
			Map<String, PVStructure> rpcCalls = dummyMalcolmDevice.getReceivedRPCCalls();

			assertEquals(2, rpcCalls.size());

			// configure
			PVStructure configureCall = rpcCalls.get("configure");

			Union union = FieldFactory.getFieldCreate().createVariantUnion();

			Structure generatorStructure = FieldFactory.getFieldCreate().createFieldBuilder()
					.addArray("mutators", union)
					.addArray("generators", union)
					.addArray("excluders", union)
					.setId("scanpointgenerator:generator/CompoundGenerator:1.0").createStructure();

			Structure spiralGeneratorStructure = FieldFactory.getFieldCreate().createFieldBuilder()
					.addArray("centre", ScalarType.pvDouble)
					.add("scale", ScalarType.pvDouble)
					.add("units", ScalarType.pvString)
					.addArray("names", ScalarType.pvString)
					.add("alternate_direction", ScalarType.pvBoolean)
					.add("radius", ScalarType.pvDouble)
					.setId("scanpointgenerator:generator/SpiralGenerator:1.0").createStructure();

			Structure fixedMutatorStructure = FieldFactory.getFieldCreate().createFieldBuilder()
					.add("duration", ScalarType.pvDouble)
					.setId("scanpointgenerator:mutator/FixedDurationMutator:1.0").createStructure();

			Structure configureStructure = FieldFactory.getFieldCreate().createFieldBuilder()
					.add("exposure", ScalarType.pvFloat)
					.add("generator", generatorStructure)
					.createStructure();

			PVStructure spiralGeneratorPVStructure = PVDataFactory.getPVDataCreate()
					.createPVStructure(spiralGeneratorStructure);
			double[] centre = new double[] { 5, -2.5 };
			spiralGeneratorPVStructure.getSubField(PVDoubleArray.class, "centre").put(0, centre.length, centre, 0);
			spiralGeneratorPVStructure.getDoubleField("scale").put(2.0);
			spiralGeneratorPVStructure.getStringField("units").put("mm");
			String[] names = new String[] { "stage_x", "stage_y" };
			spiralGeneratorPVStructure.getSubField(PVStringArray.class, "names").put(0, names.length, names, 0);
			spiralGeneratorPVStructure.getBooleanField("alternate_direction").put(false);
			spiralGeneratorPVStructure.getDoubleField("radius").put(5.5901699437494745);

			PVStructure fixedMutatorPVStructure = PVDataFactory.getPVDataCreate()
					.createPVStructure(fixedMutatorStructure);
			fixedMutatorPVStructure.getDoubleField("duration").put(23.1);
			
			PVStructure configurePVStructure = PVDataFactory.getPVDataCreate().createPVStructure(configureStructure);
			PVUnion pvu1 = PVDataFactory.getPVDataCreate().createPVVariantUnion();
			pvu1.set(spiralGeneratorPVStructure);
			PVUnion[] unionArray = new PVUnion[1];
			unionArray[0] = pvu1;
			configurePVStructure.getUnionArrayField("generator.generators").put(0, unionArray.length, unionArray, 0);
			PVUnion pvu2 = PVDataFactory.getPVDataCreate().createPVVariantUnion();
			pvu2.set(fixedMutatorPVStructure);
			PVUnion[] unionArray2 = new PVUnion[1];
			unionArray2[0] = pvu2;
			configurePVStructure.getUnionArrayField("generator.mutators").put(0, unionArray2.length, unionArray2, 0);
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
