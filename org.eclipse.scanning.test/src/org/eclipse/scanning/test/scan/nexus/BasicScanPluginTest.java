package org.eclipse.scanning.test.scan.nexus;

import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertAxes;
import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertIndices;
import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertScanPointsGroup;
import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertTarget;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.io.File;
import java.util.List;
import java.util.stream.IntStream;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IDeviceConnectorService;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableEventDevice;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.event.RunEvent;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.points.PointGeneratorFactory;
import org.eclipse.scanning.sequencer.RunnableDeviceServiceImpl;
import org.eclipse.scanning.test.scan.mock.MockScannableConnector;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class BasicScanPluginTest {

	
	private static INexusFileFactory   fileFactory;
	
	private static IRunnableDeviceService        service;
	private static IPointGeneratorService  gservice;
	private static IDeviceConnectorService connector;
	
	@BeforeClass
	public static void before() throws Exception {
		connector = new MockScannableConnector();	
		service   = new RunnableDeviceServiceImpl(connector); // Not testing OSGi so using hard coded service.
		gservice  = new PointGeneratorFactory();
	}
	
    private IScannable<?>                  monitor;
    private IScannable<?>                  metadataScannable;

    @Before
	public void beforeTest() throws Exception {
		monitor = connector.getScannable("monitor1");
		metadataScannable = connector.getScannable("metadataScannable1");
	}
	
	@Test
	public void testBasicScan1D() throws Exception {	
		test(null, null, 5);
	}
	
	@Test
	public void testBasicScan2D() throws Exception {	
		test(null, null, 8, 5);
	}
	
	@Test
	public void testBasicScan3D() throws Exception {	
		test(null, null, 5, 8, 5);
	}
	
	@Test
	public void testBasicScan1DWithMonitor() throws Exception {	
		test(monitor, null, 5);
	}
	
	@Test
	public void testBasicScan2DWithMonitor() throws Exception {	
		test(monitor, null, 8, 5);
	}
	
	@Test
	public void testBasicScan3DWithMonitor() throws Exception {	
		test(monitor, null, 5, 8, 5);
	}

	@Test
	public void testBasicScanWithMetadataScannable() throws Exception {
		test(monitor, metadataScannable, 8, 5);
	}

	private void test(IScannable<?> monitor, IScannable<?> metadataScannable, int... shape) throws Exception {

		// Tell configure detector to write 1 image into a 2D scan
		IRunnableDevice<ScanModel> scanner = createStepScan(monitor, metadataScannable, shape);
		scanner.run(null);

		checkNexusFile(scanner, shape);
	}

	private void checkNexusFile(IRunnableDevice<ScanModel> scanner, int... sizes) throws NexusException, ScanningException {
		final ScanModel scanModel = ((AbstractRunnableDevice<ScanModel>) scanner).getModel();
		assertEquals(DeviceState.READY, scanner.getDeviceState());

		String filePath = ((AbstractRunnableDevice<ScanModel>) scanner).getModel().getFilePath();

		NexusFile nf = fileFactory.newNexusFile(filePath);
		nf.openToRead();
		
		TreeFile nexusTree = NexusUtils.loadNexusTree(nf);
		NXroot rootNode = (NXroot) nexusTree.getGroupNode();
		NXentry entry = rootNode.getEntry();
		NXinstrument instrument = entry.getInstrument();
		
		// check the scan points have been written correctly
		assertScanPointsGroup(entry, sizes);
		
		DataNode dataNode = null;
		IDataset dataset = null;
		int[] shape = null;
		
		// check metadata scannables
		if (scanModel.getMetadataScannables() != null) {
			checkMetadataScannables(scanModel, instrument);
		}
		
		final IPosition pos = scanModel.getPositionIterable().iterator().next();
		final List<String> scannableNames = pos.getNames();
		final boolean hasMonitor = scanModel.getMonitors() != null && !scanModel.getMonitors().isEmpty();
		
		String dataGroupName = hasMonitor ? scanModel.getMonitors().get(0).getName() :
			"solstice_scan_data"; // name of NXdata group created from ScanPointsWriter's data when no detectors or monitors 
		NXdata nxData = entry.getData(dataGroupName);
		assertNotNull(nxData);

		// Check axes
		String[] expectedAxesNames = scannableNames.stream().map(x -> x + "_value_demand").toArray(String[]::new);
		assertAxes(nxData, expectedAxesNames);

		int[] defaultDimensionMappings = IntStream.range(0, sizes.length).toArray();
		for (int i = 0; i < scannableNames.size(); i++) {
			// Demand values should be 1D
			String scannableName = scannableNames.get(i);
			
			NXpositioner positioner = instrument.getPositioner(scannableName);
			assertNotNull(positioner);
			
			dataNode = positioner.getDataNode("value_demand");
			dataset = dataNode.getDataset().getSlice();
			shape = dataset.getShape();
			assertEquals(1, shape.length);
			assertEquals(sizes[i], shape[0]);
			
			String nxDataFieldName = scannableName + "_value_demand";
			assertSame(dataNode, nxData.getDataNode(nxDataFieldName));
			assertIndices(nxData, nxDataFieldName, i);
			assertTarget(nxData, nxDataFieldName, rootNode,
					"/entry/instrument/" + scannableName + "/value_demand");

			// Actual values should be scanD
			dataNode = positioner.getDataNode(NXpositioner.NX_VALUE);
			dataset = dataNode.getDataset().getSlice();
			shape = dataset.getShape();
			assertArrayEquals(sizes, shape);
			
			nxDataFieldName = scannableName + "_" + NXpositioner.NX_VALUE;
			assertSame(dataNode, nxData.getDataNode(nxDataFieldName));
			assertIndices(nxData, nxDataFieldName, defaultDimensionMappings);
			assertTarget(nxData, nxDataFieldName, rootNode,
					"/entry/instrument/" + scannableName + "/" + NXpositioner.NX_VALUE);
		}
	}

	private void checkMetadataScannables(final ScanModel scanModel, NXinstrument instrument) {
		DataNode dataNode;
		IDataset dataset;
		int[] shape;
		for (IScannable<?> metadataScannable : scanModel.getMetadataScannables()) {
			NXpositioner positioner = instrument.getPositioner(metadataScannable.getName());
			assertNotNull(positioner);
			assertEquals(metadataScannable.getName(), positioner.getNameScalar());
			
			dataNode = positioner.getDataNode("value_demand"); // TODO should not be here for metadata scannable
			assertNotNull(dataNode);
			dataset = dataNode.getDataset().getSlice();
			shape = dataset.getShape();
			assertArrayEquals(new int[] { 1 }, shape);
			assertEquals(Dataset.FLOAT64, ((Dataset) dataset).getDtype());
			assertEquals(10.0, dataset.getDouble(0), 1e-15);
			
			dataNode = positioner.getDataNode(NXpositioner.NX_VALUE);
			assertNotNull(dataNode);
			dataset = dataNode.getDataset().getSlice();
			shape = dataset.getShape();
			assertArrayEquals(new int[] { 1 }, shape);
			assertEquals(Dataset.FLOAT64, ((Dataset) dataset).getDtype());
			assertEquals(10.0, dataset.getDouble(0), 1e-15);
		}
	}

	private IRunnableDevice<ScanModel> createStepScan(IScannable<?> monitor,
			IScannable<?> metadataScannable, int... size) throws Exception {
		
		IPointGenerator<?,IPosition> gen = null;
		
		// We add the outer scans, if any
		for (int dim = size.length-1; dim>-1; dim--) {
			final StepModel model;
			if (size[dim]-1>0) {
				model = new StepModel("neXusScannable"+(dim+1), 10,20,9.9d/(size[dim]-1));
			} else {
				model = new StepModel("neXusScannable"+(dim+1), 10,20,30); // Will generate one value at 10
			}
			final IPointGenerator<?,IPosition> step = gservice.createGenerator(model);
			if (gen!=null) {
				gen = gservice.createCompoundGenerator(step, gen);
			} else {
				gen = step;
			}
		}
	
		// Create the model for a scan.
		final ScanModel  smodel = new ScanModel();
		smodel.setPositionIterable(gen);
		if (monitor!=null) smodel.setMonitors(monitor);
		if (metadataScannable != null) smodel.setMetadataScannables(metadataScannable);
		
		// Create a file to scan into.
		File output = File.createTempFile("test_simple_nexus", ".nxs");
		output.deleteOnExit();
		smodel.setFilePath(output.getAbsolutePath());
		System.out.println("File writing to " + smodel.getFilePath());

		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = service.createRunnableDevice(smodel, null);
		
		final IPointGenerator<?,IPosition> fgen = gen;
		((IRunnableEventDevice<ScanModel>)scanner).addRunListener(new IRunListener.Stub() {
			@Override
			public void runWillPerform(RunEvent evt) throws ScanningException{
                try {
					System.out.println("Running acquisition scan of size "+fgen.size());
				} catch (GeneratorException e) {
					throw new ScanningException(e);
				}
			}
		});

		return scanner;
	}

	public static INexusFileFactory getFileFactory() {
		return fileFactory;
	}

	public static void setFileFactory(INexusFileFactory fileFactory) {
		BasicScanPluginTest.fileFactory = fileFactory;
	}

}
