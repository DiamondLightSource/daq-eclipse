package org.eclipse.scanning.test.scan.nexus;

import static org.eclipse.scanning.malcolm.core.MalcolmDatasetType.POSITION_SET;
import static org.eclipse.scanning.malcolm.core.MalcolmDatasetType.POSITION_VALUE;
import static org.eclipse.scanning.malcolm.core.MalcolmDatasetType.PRIMARY;
import static org.eclipse.scanning.malcolm.core.MalcolmDatasetType.SECONDARY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.SymbolicNode;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.nexus.IMultipleNexusDevice;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.NexusScanInfo.ScanRole;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.malcolm.MalcolmTable;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.api.scan.ScanInformation;
import org.eclipse.scanning.example.malcolm.DummyMalcolmControlledDeviceModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmDatasetModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmDevice;
import org.eclipse.scanning.example.malcolm.DummyMalcolmModel;
import org.eclipse.scanning.malcolm.core.AbstractMalcolmDevice;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * This test uses the RunnableDeviceService to create a {@link DummyMalcolmDevice}
 * and run it. Creates nexus files according to the {@link DummyMalcolmModel}.
 * 
 * @author Matt Taylor
 * @author Matthew Dickie
 *
 */
public class DummyMalcolmDeviceTest extends NexusTest {
	
	private File malcolmOutputDir;

	@Before
	public void setUp() throws Exception {
		// create a temp directory for the dummy malcolm device to write hdf files into
		malcolmOutputDir = Files.createTempDirectory(DummyMalcolmDeviceTest.class.getSimpleName()).toFile();
	}

	@After
	public void teardown() throws Exception {
		// delete the temp directory and all its files
		for (File file : malcolmOutputDir.listFiles()) {
			file.delete();
		}
		malcolmOutputDir.delete();
	}

	private IPointGenerator<?> getGenerator(int... size) throws GeneratorException {
		GridModel gmodel = new GridModel();
		gmodel.setFastAxisName("xNex");
		gmodel.setFastAxisPoints(size[size.length - 1]);
		gmodel.setSlowAxisName("yNex");
		gmodel.setSlowAxisPoints(size[size.length - 2]);
		gmodel.setBoundingBox(new BoundingBox(0, 0, 3, 3));

		IPointGenerator<?> gen = gservice.createGenerator(gmodel);

		IPointGenerator<?>[] gens = new IPointGenerator<?>[size.length - 1];
		// We add the outer scans, if any
		if (size.length > 2) {
			for (int dim = size.length - 3; dim > -1; dim--) {
				final StepModel model;
				if (size[dim] - 1 > 0) {
					model = new StepModel("neXusScannable" + (dim + 1), 10, 20,
							9.99d / (size[dim] - 1));
				} else {
					// Will generate one value at 10
					model = new StepModel("neXusScannable" + (dim + 1), 10, 20, 30); 
				}
				final IPointGenerator<?> step = gservice.createGenerator(model);
				gens[dim] = step;
			}
		}
		gens[size.length - 2] = gen;

		gen = gservice.createCompoundGenerator(gens);

		return gen;
	}

	private DummyMalcolmModel createModel() {
		DummyMalcolmModel model = new DummyMalcolmModel();
		model.setName("testMalcolmDevice");
		model.setFilePath(malcolmOutputDir.getAbsolutePath());

		DummyMalcolmControlledDeviceModel det1Model = new DummyMalcolmControlledDeviceModel();
		det1Model.setName("detector");
		det1Model.setRole(ScanRole.DETECTOR);
		det1Model.setFileName("detector.h5");

		DummyMalcolmDatasetModel detector1dataset1 = new DummyMalcolmDatasetModel();
		detector1dataset1.setName("detector");
		detector1dataset1.setRank(2);
		detector1dataset1.setDtype(Double.class);
		detector1dataset1.setMalcolmType(PRIMARY);
		detector1dataset1.setPath("/entry/detector/detector");
		
		DummyMalcolmDatasetModel detector1dataset2 = new DummyMalcolmDatasetModel();
		detector1dataset2.setName("sum");
		detector1dataset2.setRank(1);
		detector1dataset2.setDtype(Double.class);
		detector1dataset2.setMalcolmType(SECONDARY);
		detector1dataset2.setPath("/entry/detector_sum/sum");
		det1Model.setDatasets(Arrays.asList(detector1dataset1, detector1dataset2));

		DummyMalcolmControlledDeviceModel det2Model = new DummyMalcolmControlledDeviceModel();
		det2Model.setName("detector2");
		det2Model.setRole(ScanRole.DETECTOR);
		det2Model.setFileName("detector2.h5");

		DummyMalcolmDatasetModel detector2dataset = new DummyMalcolmDatasetModel();
		detector2dataset.setName("detector2");
		detector2dataset.setRank(2);
		detector2dataset.setMalcolmType(PRIMARY);
		detector2dataset.setDtype(Double.class);
		detector2dataset.setPath("/entry/detector2/detector2");
		det2Model.setDatasets(Arrays.asList(detector2dataset));

		DummyMalcolmControlledDeviceModel stageXModel = createPositionerModel("stage_x");
		DummyMalcolmControlledDeviceModel stageYModel = createPositionerModel("stage_y");

		model.setDummyDeviceModels(Arrays.asList(det1Model, det2Model, stageXModel, stageYModel));

		return model;
	}
	
	private DummyMalcolmControlledDeviceModel createPositionerModel(String name) {
		DummyMalcolmControlledDeviceModel posModel = new DummyMalcolmControlledDeviceModel();
		posModel.setName(name);
		posModel.setRole(ScanRole.SCANNABLE);
		posModel.setFileName(name + ".h5");

		DummyMalcolmDatasetModel rbvDataset = new DummyMalcolmDatasetModel();
		rbvDataset.setName("value");
		rbvDataset.setRank(0);
		rbvDataset.setDtype(Double.class);
		rbvDataset.setMalcolmType(POSITION_VALUE);
		rbvDataset.setPath("/entry/instrument/" + name +"/value");
		posModel.setDatasets(Arrays.asList(rbvDataset));

		DummyMalcolmDatasetModel setDataset = new DummyMalcolmDatasetModel();
		setDataset.setName("value");
		setDataset.setRank(0);
		setDataset.setDtype(Double.class);
		setDataset.setMalcolmType(POSITION_SET);
		setDataset.setPath("/entry/instrument/" + name +"/value");
		posModel.setDatasets(Arrays.asList(setDataset));

		return posModel;
	}
	

	@Test
	public void testDummyMalcolmNexusFiles() throws Exception {

		DummyMalcolmModel model = createModel();
		model.setGenerator(getGenerator(2, 2)); // Generator isn't actually used by the test malcolm device
		IRunnableDevice<DummyMalcolmModel> malcolmDevice = dservice.createRunnableDevice(model);
		int scanRank = 3;
		setScanInformation(malcolmDevice, scanRank); // normally called when a scan is run using @ScanStart
		assertNotNull(malcolmDevice);

		malcolmDevice.run(null);

		// Check file has been written with some data
		checkMalcolmNexusFiles(model, (IMalcolmDevice<DummyMalcolmModel>) malcolmDevice, scanRank);
	}
	
	@Test
	public void testMalcolmNexusObjects() throws Exception {
		DummyMalcolmModel model = createModel();
		IRunnableDevice<DummyMalcolmModel> malcolmDevice = dservice.createRunnableDevice(model);
		int scanRank = 3;
		setScanInformation(malcolmDevice, scanRank); // normally called when a scan is run using @ScanStart
		malcolmDevice.configure(model);

		NexusScanInfo nexusScanInfo = new NexusScanInfo();
		nexusScanInfo.setRank(scanRank);
		List<NexusObjectProvider<?>> nexusProviders = ((IMultipleNexusDevice) malcolmDevice).getNexusProviders(nexusScanInfo);

		checkNexusObjectProviders(nexusProviders, model, scanRank);
	}
	
	private void setScanInformation(IRunnableDevice<DummyMalcolmModel> malcolmDevice, int scanRank) {
		ScanInformation scanInfo = new ScanInformation();
		scanInfo.setRank(3);
		((DummyMalcolmDevice) malcolmDevice).setScanInformation(scanInfo);
	}
	

	private NXentry getNexusEntry(String filePath) throws Exception {
		INexusFileFactory fileFactory = org.eclipse.dawnsci.nexus.ServiceHolder
				.getNexusFileFactory();
		NexusFile nf = fileFactory.newNexusFile(filePath);
		nf.openToRead();
	
		TreeFile nexusTree = NexusUtils.loadNexusTree(nf);
		NXroot root = (NXroot) nexusTree.getGroupNode();
		return root.getEntry();
	}

	private void checkMalcolmNexusFiles(DummyMalcolmModel model,
			IMalcolmDevice<DummyMalcolmModel> malcolmDevice, int scanRank)
			throws MalcolmDeviceException, Exception {
		Object datasetsValue = malcolmDevice.getAttributeValue("datasets");
		MalcolmTable table = (MalcolmTable) datasetsValue;
		Map<String, NXentry> nexusEntries = new HashMap<>();
		for (Map<String, Object> datasetRow : table) {
			for (String heading : datasetRow.keySet()) {
				System.err.print(heading + "=" + datasetRow.get(heading) + " ");
			}
			System.err.println();
			
			String filename = (String) datasetRow.get(AbstractMalcolmDevice.DATASETS_TABLE_COLUMN_FILENAME);
			
			// load the nexus entry for the file (may be cached from a previous dataset)
			NXentry entry = nexusEntries.get(filename);
			if (entry == null) {
				entry = getNexusEntry(filename);
				nexusEntries.put(filename, entry);
			}
			assertNotNull(entry);
			
			String path = (String) datasetRow.get(AbstractMalcolmDevice.DATASETS_TABLE_COLUMN_PATH);

			String[] pathSegments = path.split("/");
			assertEquals("", pathSegments[0]); // first element is empty as path starts with '/'
			assertEquals("entry", pathSegments[1]);
			// find the parent group
			GroupNode groupNode = entry;
			for (int i = 2; i < pathSegments.length - 1; i++) {
				groupNode = groupNode.getGroupNode(pathSegments[i]);
			}
			assertNotNull(groupNode);
			DataNode dataNode = groupNode.getDataNode(pathSegments[pathSegments.length - 1]);
			assertNotNull(dataNode);
			
			int datasetRank = ((Integer) datasetRow.get(AbstractMalcolmDevice.DATASETS_TABLE_COLUMN_RANK)).intValue();
			assertEquals(dataNode.getRank(), scanRank + datasetRank); 
		}
	}

	private void checkNexusObjectProviders(List<NexusObjectProvider<?>> nexusProviders,
			DummyMalcolmModel model, int scanRank) {
		// convert list into a map keyed by name
		Map<String, NexusObjectProvider<?>> nexusObjectMap = nexusProviders.stream().collect(
				Collectors.toMap(n -> n.getName(), Function.identity()));
		for (DummyMalcolmControlledDeviceModel device : model.getDummyDeviceModels()) {
			NexusObjectProvider<?> nexusProvider = nexusObjectMap.get(device.getName());
			NXobject nexusObject = nexusProvider.getNexusObject();
			assertNotNull(nexusProvider);
			assertNotNull(nexusObject);
			assertEquals(getExpectedNexusClassForRole(device.getRole()), nexusProvider.getNexusBaseClass());
			String expectedFileName = model.getFilePath() + "/" + device.getFileName();
			assertEquals(expectedFileName, nexusProvider.getExternalFileName());
			
			for (DummyMalcolmDatasetModel dataset : device.getDatasets()) {
				final String datasetName = dataset.getName();
				SymbolicNode externalLinkNode = nexusObject.getSymbolicNode(datasetName);
				assertNotNull(externalLinkNode);
				assertEquals(scanRank + dataset.getRank(), nexusProvider.getExternalDatasetRank(datasetName));
				assertEquals(externalLinkNode.getPath(), dataset.getPath());
				assertEquals(expectedFileName, externalLinkNode.getSourceURI().toString());
				
				// check the nexus provider which describes how to add the device to the tree
				// (in particular how NXdata groups should be built) is configured correctly 
				switch (dataset.getMalcolmType()) {
					case PRIMARY:
						assertEquals(datasetName, nexusProvider.getPrimaryDataFieldName());
						break;
					case SECONDARY:
						assertTrue(nexusProvider.getAdditionalPrimaryDataFieldNames().contains(datasetName));
						break;
					case MONITOR:
						assertTrue(nexusProvider.getAxisDataFieldNames().contains(datasetName));
						break;
					case POSITION_SET:
						assertEquals(datasetName, nexusProvider.getDefaultAxisDataFieldName());
						break;
					case POSITION_VALUE:
						assertTrue(nexusProvider.getAxisDataFieldNames().contains(datasetName));
						break;
				}
			}
		}
		
	}
	
	private NexusBaseClass getExpectedNexusClassForRole(ScanRole scanRole) {
		switch (scanRole) {
			case DETECTOR: return NexusBaseClass.NX_DETECTOR;
			case MONITOR: return NexusBaseClass.NX_MONITOR;
			case SCANNABLE: return NexusBaseClass.NX_POSITIONER;
			default: Assert.fail("Unexpected scanrole "+ scanRole); return null;
		}
		
	}
	
}
