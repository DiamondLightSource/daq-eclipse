package org.eclipse.scanning.test.scan.nexus;

import static org.eclipse.scanning.malcolm.core.MalcolmDatasetType.POSITION;
import static org.eclipse.scanning.malcolm.core.MalcolmDatasetType.PRIMARY;
import static org.eclipse.scanning.malcolm.core.MalcolmDatasetType.SECONDARY;
import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertAxes;
import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertIndices;
import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertScanNotFinished;
import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertScanPointsGroup;
import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertSignal;
import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertTarget;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusScanInfo.ScanRole;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.dawnsci.nexus.ServiceHolder;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.PositionIterator;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableEventDevice;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.event.RunEvent;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmControlledDeviceModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmDatasetModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmModel;
import org.eclipse.scanning.malcolm.core.AbstractMalcolmDevice;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MalcolmScanTest extends NexusTest {
	
	private File malcolmOutputDir;

	private IRunnableDevice<?> malcolmDevice;
	
	@Before
	public void before() throws Exception {
		// create a temp directory for the dummy malcolm device to write hdf files into
		malcolmOutputDir = Files.createTempDirectory(DummyMalcolmDeviceTest.class.getSimpleName()).toFile();
		
		DummyMalcolmModel model = new DummyMalcolmModel();
		model.setFilePath(malcolmOutputDir.getAbsolutePath());
		model.setGenerator(null); // TODO set generator?
		
		malcolmDevice = dservice.createRunnableDevice(model);
		assertNotNull(malcolmDevice);
		((AbstractMalcolmDevice<DummyMalcolmModel>) malcolmDevice).addRunListener(new IRunListener() {
			@Override
			public void runPerformed(RunEvent evt) throws ScanningException {
				System.out.println("Ran test malcolm device @ " + evt.getPosition());
			}
		});
		
	}
	
	@After
	public void teardown() throws Exception {
		// delete the temp directory and all its files
		for (File file : malcolmOutputDir.listFiles()) {
			file.delete();
		}
		malcolmOutputDir.delete();
	}

	private DummyMalcolmModel createModel() {
		DummyMalcolmModel model = new DummyMalcolmModel();
		model.setName("testMalcolmDevice");
		model.setFilePath(malcolmOutputDir.getAbsolutePath());

		DummyMalcolmControlledDeviceModel det1Model = new DummyMalcolmControlledDeviceModel();
		det1Model.setName("detector");
		det1Model.setRole(ScanRole.DETECTOR);
		det1Model.setFileName("detector.nxs");

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
		det2Model.setFileName("detector2.nxs");

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
		stageXModel.setFileName("stage_x.nxs");

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
		stageYModel.setFileName("stage_y.nxs");

		DummyMalcolmDatasetModel stageYDataset = new DummyMalcolmDatasetModel();
		stageYDataset.setName("value");
		stageYDataset.setRank(0);
		stageYDataset.setMalcolmType(POSITION);
		stageYDataset.setDtype(Double.class);
		stageYDataset.setPath("/entry/instrument/stage_y/value");
		stageYModel.setDatasets(Arrays.asList(stageYDataset));

		model.setDummyDeviceModels(Arrays.asList(det1Model, det2Model, stageXModel, stageYModel));

		model.setDummyDeviceModels(Arrays.asList(det1Model, det2Model, stageXModel, stageYModel));

		return model;
	}

	@Test
	public void test2DMalcolmScan() throws Exception {
		testMalcolmScan(8, 5);
	}
	
	@Test
	public void test3DMalcolmScan() throws Exception {
		testMalcolmScan(3, 2, 5);
	}
	
	@Test
	public void test4DMalcolmScan() throws Exception {
		testMalcolmScan(3,3,2,2);
	}
	
	@Test
	public void test5DMalcolmScan() throws Exception {
		testMalcolmScan(1,1,1,2,2);
	}
	
	@Test
	public void test8DMalcolmScan() throws Exception {
		testMalcolmScan(1,1,1,1,1,1,2,2);
	}
	
	private void testMalcolmScan(int... shape) throws Exception {
		IRunnableDevice<ScanModel> scanner = createGridScan(malcolmDevice, output, shape); // Outer scan of another scannable, for instance temp.
		assertScanNotFinished(getNexusRoot(scanner).getEntry());
		scanner.run(null);
		
		// Check we reached ready (it will normally throw an exception on error)
		assertEquals(DeviceState.READY, scanner.getDeviceState());
		checkNexusFile(scanner, shape); // Step model is +1 on the size
	}
	
	private NXroot getNexusRoot(IRunnableDevice<ScanModel> scanner) throws Exception {
		String filePath = ((AbstractRunnableDevice<ScanModel>) scanner).getModel().getFilePath();
		
		INexusFileFactory fileFactory = ServiceHolder.getNexusFileFactory();
		NexusFile nf = fileFactory.newNexusFile(filePath);
		nf.openToRead();
		
		TreeFile nexusTree = NexusUtils.loadNexusTree(nf);
		return (NXroot) nexusTree.getGroupNode();
	}
	
	private void checkNexusFile(IRunnableDevice<ScanModel> scanner, int... sizes) throws Exception {
		final ScanModel scanModel = ((AbstractRunnableDevice<ScanModel>) scanner).getModel();
		
		NXroot rootNode = getNexusRoot(scanner);
		NXentry entry = rootNode.getEntry();
		NXinstrument instrument = entry.getInstrument();
		
		// check that the scan points have been written correctly
		assertScanPointsGroup(entry, sizes);
		
		// TODO how to get primary data fields (from config or hardcoded in test?)
		Map<String, List<String>> primaryDataFieldNamesPerDetector = null; // map from detector name -> primary data fields
		// ****************************************************
		// TODO, tuesday: create map of primary field names
		// ****************************************************
		
		for (String detectorName : primaryDataFieldNamesPerDetector.keySet()) {
			NXdetector detector = instrument.getDetector(detectorName);
			
			List<String> primaryDataFieldNames = primaryDataFieldNamesPerDetector.get(detectorName);
			Map<String, String> expectedDataGroupNames = primaryDataFieldNames.stream().collect(Collectors.toMap(
					Function.identity(),
					fieldName -> detectorName + (fieldName.equals(NXdetector.NX_DATA) ? "" : "_" + fieldName)));
			
			Map<String, NXdata> nxDataGroups = entry.getChildren(NXdata.class);
			assertEquals(primaryDataFieldNames.size(), nxDataGroups.size());
			assertTrue(nxDataGroups.keySet().containsAll(expectedDataGroupNames.values()));
			
			for (String fieldName : primaryDataFieldNames) {
				String nxDataGroupName = expectedDataGroupNames.get(fieldName);
				NXdata nxData = entry.getData(nxDataGroupName);
				
				String sourceFieldName = nxDataGroupName.equals(detectorName) ? NXdetector.NX_DATA :
					nxDataGroupName.substring(nxDataGroupName.indexOf('_') + 1);
				
				assertSignal(nxData, sourceFieldName);
				// check the nxData's signal field is a link to the appropriate source data node of the detector
				DataNode dataNode = detector.getDataNode(sourceFieldName);
				IDataset dataset = dataNode.getDataset().getSlice();
				assertSame(dataNode, nxData.getDataNode(sourceFieldName));
				assertTarget(nxData, sourceFieldName, rootNode, "/entry/instrument/" + detectorName
						+ "/" + sourceFieldName);
				
				// check that other primary data fields of the detector haven't been adeed to this NXdata
				for (String primaryDataFieldName : primaryDataFieldNames) {
					if (!primaryDataFieldName.equals(sourceFieldName)) {
						assertNull(nxData.getDataNode(primaryDataFieldName));
					}
				}
				
				int[] shape = dataset.getShape(); // TODO: do we need to test this?
				for (int i = 0; i < sizes.length; i++)
					assertEquals(sizes[i], shape[i]);
				
				// Make sure none of the numbers are NaNs. The detector is expected
				// to fill this scan with non-nulls
				final PositionIterator it = new PositionIterator(shape);
				while (it.hasNext()) {
					int[] next = it.getPos();
					assertFalse(Double.isNaN(dataset.getDouble(next)));
				}
				
				// Check axes
				final IPosition pos = scanModel.getPositionIterable().iterator().next();
				final Collection<String> axisNames = pos.getNames(); // NOTE: xNex and yNex controlled by malcolm
				
				// Append _value_set to each name in list, then add detector axes fields to result
				List<String> expectedAxisNames = Stream.concat(
						axisNames.stream().map(axisName -> axisName + "_value_set"),
						Collections.nCopies(2, ".").stream()).collect(Collectors.toList()); // TODO 2 should be what number?
				assertAxes(nxData, expectedAxisNames.toArray(new String[expectedAxisNames.size()]));
				
				int[] defaultDimensionMappings = IntStream.range(0, sizes.length).toArray();
				int i = -1;
				for (String axisName : axisNames) {
					i++;
					NXpositioner positioner = instrument.getPositioner(axisName);
					assertNotNull(positioner);
					
					dataNode = positioner.getDataNode("value_set");
					dataset = dataNode.getDataset().getSlice();
					shape = dataset.getShape();
					assertEquals(1, shape.length);
					assertEquals(sizes[i], shape[0]);
					
					String nxDataFieldName = axisName + "_value_set";
					assertSame(dataNode, nxData.getDataNode(nxDataFieldName));
					assertIndices(nxData, nxDataFieldName, i);
					assertTarget(nxData, nxDataFieldName, rootNode,
							"/entry/instrument/" + axisName + "/value_set");
					
					// Actual values should be scanD
					dataNode = positioner.getDataNode(NXpositioner.NX_VALUE);
					dataset = dataNode.getDataset().getSlice();
					shape = dataset.getShape();
					assertArrayEquals(sizes, shape);
					
					nxDataFieldName = axisName + "_" + NXpositioner.NX_VALUE;
					assertSame(dataNode, nxData.getDataNode(nxDataFieldName));
					assertIndices(nxData, nxDataFieldName, defaultDimensionMappings);
					assertTarget(nxData, nxDataFieldName, rootNode,
							"/entry/instrument/" + axisName + "/" + NXpositioner.NX_VALUE);
				}
			}
		}
	}
	
	private IRunnableDevice<ScanModel> createGridScan(final IRunnableDevice<?> malcolmDevice, File file, int... size) throws Exception {
		
		// Create scan points for a grid and make a generator
		GridModel gmodel = new GridModel(); // Note xNex and yNex scannables controlled by malcolm
		gmodel.setFastAxisName("xNex");
		gmodel.setFastAxisPoints(size[size.length-1]);
		gmodel.setSlowAxisName("yNex");
		gmodel.setSlowAxisPoints(size[size.length-2]);
		gmodel.setBoundingBox(new BoundingBox(0,0,3,3));
		
		IPointGenerator<?> gen = gservice.createGenerator(gmodel);
		
		IPointGenerator<?>[] gens = new IPointGenerator<?>[size.length - 1];
		if (size.length > 2) {
			for (int dim = size.length - 3; dim > -1; dim--) {
				final StepModel model;
				if (size[dim]-1>0) {
					model = new StepModel("neXusScannable"+(dim+1), 10,20,9.99d/(size[dim]-1));
				} else {
					model = new StepModel("neXusScannable"+(dim+1), 10,20,30);
				}
				final IPointGenerator<?> step = gservice.createGenerator(model);
				gens[dim] = step;
			}
		}
		gens[size.length - 2] = gen;
		
		gen = gservice.createCompoundGenerator(gens);
		
		// Create the model for a scan.
		final ScanModel smodel = new ScanModel();
		smodel.setPositionIterable(gen);
		smodel.setDetectors(malcolmDevice);
		
		// Create a file to scan into.
		smodel.setFilePath(file.getAbsolutePath());
		System.out.println("File writing to " + smodel.getFilePath());
		
		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = dservice.createRunnableDevice(smodel, null);
		
		final IPointGenerator<?> fgen = gen;
		((IRunnableEventDevice<ScanModel>)scanner).addRunListener(new IRunListener() {
			@Override
			public void runWillPerform(RunEvent evt) throws ScanningException {
				try {
					System.out.println("Running acquisition size of scan "+fgen.size());
				} catch (GeneratorException e) {
					throw new ScanningException(e);
				}
			}
		});
		
		return scanner;
	}

}
