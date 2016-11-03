package org.eclipse.scanning.test.scan.nexus;

import static org.eclipse.scanning.example.malcolm.MalcolmDatasetType.POSITION;
import static org.eclipse.scanning.example.malcolm.MalcolmDatasetType.PRIMARY;
import static org.eclipse.scanning.example.malcolm.MalcolmDatasetType.SECONDARY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusScanInfo.ScanRole;
import org.eclipse.dawnsci.nexus.NexusUtils;
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
import org.junit.After;
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

		DummyMalcolmControlledDeviceModel stageXModel = new DummyMalcolmControlledDeviceModel();
		stageXModel.setName("stage_x");
		stageXModel.setRole(ScanRole.SCANNABLE);
		stageXModel.setFileName("stage_x.h5");

		DummyMalcolmDatasetModel stageXDataset = new DummyMalcolmDatasetModel();
		stageXDataset.setName("value");
		stageXDataset.setRank(0);
		stageXDataset.setDtype(Double.class);
		stageXDataset.setMalcolmType(POSITION);
		stageXDataset.setPath("/entry/instrument/stage_x/value");
		stageXModel.setDatasets(Arrays.asList(stageXDataset));

		DummyMalcolmControlledDeviceModel stageYModel = new DummyMalcolmControlledDeviceModel();
		stageYModel.setName("stage_y");
		stageYModel.setRole(ScanRole.SCANNABLE);
		stageYModel.setFileName("stage_y.h5");

		DummyMalcolmDatasetModel stageYDataset = new DummyMalcolmDatasetModel();
		stageYDataset.setName("value");
		stageYDataset.setRank(0);
		stageYDataset.setMalcolmType(POSITION);
		stageYDataset.setDtype(Double.class);
		stageYDataset.setPath("/entry/instrument/stage_y/value");
		stageYModel.setDatasets(Arrays.asList(stageYDataset));

		model.setDummyDeviceModels(Arrays.asList(det1Model, det2Model, stageXModel, stageYModel));

		return model;
	}

	@Test
	public void testMalcolmExampleRun() throws Exception {

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
	
	private void setScanInformation(IRunnableDevice<DummyMalcolmModel> malcolmDevice, int scanRank) {
		ScanInformation scanInfo = new ScanInformation();
		scanInfo.setRank(3);
		((DummyMalcolmDevice) malcolmDevice).setScanInformation(scanInfo);
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
			
			String filename = (String) datasetRow.get(DummyMalcolmDevice.TABLE_COLUMN_FILENAME);
			
			// load the nexus entry for the file (may be cached from a previous dataset)
			NXentry entry = nexusEntries.get(filename);
			if (entry == null) {
				entry = getNexusEntry(filename);
				nexusEntries.put(filename, entry);
			}
			assertNotNull(entry);
			
			String path = (String) datasetRow.get(DummyMalcolmDevice.TABLE_COLUMN_PATH);

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
			
			int datasetRank = ((Integer) datasetRow.get(DummyMalcolmDevice.TABLE_COLUMN_RANK)).intValue();
			assertEquals(dataNode.getRank(), scanRank + datasetRank); 
		}
	}

	private NXentry getNexusEntry(String filename) throws Exception {
		String filePath = new File(malcolmOutputDir, filename).getAbsolutePath();
		INexusFileFactory fileFactory = org.eclipse.dawnsci.nexus.ServiceHolder
				.getNexusFileFactory();
		NexusFile nf = fileFactory.newNexusFile(filePath);
		nf.openToRead();

		TreeFile nexusTree = NexusUtils.loadNexusTree(nf);
		NXroot root = (NXroot) nexusTree.getGroupNode();
		return root.getEntry();
	}

}
