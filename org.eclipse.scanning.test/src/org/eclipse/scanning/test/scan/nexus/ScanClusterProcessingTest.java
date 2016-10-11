package org.eclipse.scanning.test.scan.nexus;

import static org.eclipse.scanning.sequencer.analysis.ClusterProcessingRunnableDevice.DEFAULT_ENTRY_PATH;
import static org.eclipse.scanning.sequencer.analysis.ClusterProcessingRunnableDevice.NEXUS_FILE_EXTENSION;
import static org.eclipse.scanning.sequencer.analysis.ClusterProcessingRunnableDevice.PROCESSING_QUEUE_NAME;
import static org.eclipse.scanning.sequencer.nexus.ScanPointsWriter.GROUP_NAME_SOLSTICE_SCAN;
import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertAxes;
import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertIndices;
import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertScanNotFinished;
import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertScanPointsGroup;
import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertSignal;
import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertTarget;
import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.dawnsci.analysis.api.processing.IOperationBean;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.PositionIterator;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableEventDevice;
import org.eclipse.scanning.api.device.IWritableDetector;
import org.eclipse.scanning.api.device.models.ClusterProcessingModel;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.dry.FastRunCreator;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.event.status.StatusBean;
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
import org.eclipse.scanning.api.ui.CommandConstants;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.sequencer.ServiceHolder;
import org.eclipse.scanning.sequencer.analysis.ClusterProcessingRunnableDevice;
import org.eclipse.scanning.sequencer.nexus.ScanPointsWriter;
import org.eclipse.scanning.test.BrokerTest;
import org.eclipse.scanning.test.scan.mock.DummyOperationBean;
import org.hamcrest.CoreMatchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

public class ScanClusterProcessingTest extends NexusTest {
	
	private static IConsumer<StatusBean> consumer;

	@BeforeClass
	public static void beforeClass() throws Exception {
		// called after NexusTest.beforeClass()
		BrokerTest.startBroker();
		
		IEventService eventService = new EventServiceImpl(new ActivemqConnectorService());
		ServiceHolder.setEventService(eventService);
		
		URI uri = URI.create(CommandConstants.getScanningBrokerUri());
		consumer = eventService.createConsumer(uri, PROCESSING_QUEUE_NAME,
				"scisoft.operation.STATUS_SET", "scisoft.operation.STATUS_TOPIC");
		// we need a runner, but it doesn't have to do anything
		consumer.setRunner(new FastRunCreator(0, 1, 1, 10, false));
		consumer.start();
	}
	
	@AfterClass
	public static void afterClass() throws Exception {
		BrokerTest.stopBroker();
	}
	
	@Test
	public void testNexusScanWithClusterProcessing() throws Exception {
		testScan(2, 2);
	}
	
	private void testScan(int... shape) throws Exception {
		IRunnableDevice<ScanModel> scanner = createGridScan(shape);
		assertScanNotFinished(getNexusRoot(scanner).getEntry());
		scanner.run(null);
		
		Thread.sleep(100);
		// Check the main nexus file
		checkNexusFile(scanner, shape);
		
		// Check the processing bean was submitted successfully
		checkSubmittedBean();
	}
	
	private void checkSubmittedBean() throws Exception {
		List<StatusBean> statusSet = consumer.getStatusSet();
		assertThat(statusSet.size(), is(1));
		assertThat(statusSet.get(0), is(instanceOf(DummyOperationBean.class)));
		DummyOperationBean operationBean = (DummyOperationBean) statusSet.get(0);
		
		assertThat(operationBean.getDataKey(), is(equalTo(DEFAULT_ENTRY_PATH + GROUP_NAME_SOLSTICE_SCAN)));
		assertThat(operationBean.getFilePath(),
				both(startsWith("/tmp/test_mandel_nexus")).and(endsWith(NEXUS_FILE_EXTENSION)));
		assertThat(operationBean.getOutputFilePath(), 
				both(startsWith("/tmp/processed/test_mandel_nexus")).and(endsWith(NEXUS_FILE_EXTENSION)));
		assertThat(operationBean.getDatasetPath(), is(equalTo(DEFAULT_ENTRY_PATH + "mandelbrot")));
		assertThat(operationBean.getSlicing(), is(nullValue()));
		assertThat(operationBean.getProcessingPath(), is(equalTo("/tmp/sum.nxs")));
		assertThat(operationBean.isDeleteProcessingFile(), is(false));
		assertThat(operationBean.getAxesNames(), is(nullValue()));
		assertThat(operationBean.getXmx(), is(equalTo("1024m")));
		assertThat(operationBean.getDataDimensions(), is(nullValue()));
		assertThat(operationBean.getScanRank(), is(2));
		assertThat(operationBean.isReadable(), is(true));
		assertThat(operationBean.getName(), is("sum"));
		assertThat(operationBean.getRunDirectory(), is("/tmp"));
		assertThat(operationBean.getNumberOfCores(), is(1));
	}
	
	private NXroot getNexusRoot(IRunnableDevice<ScanModel> scanner) throws Exception {
		String filePath = ((AbstractRunnableDevice<ScanModel>) scanner).getModel().getFilePath();
		
		NexusFile nf = fileFactory.newNexusFile(filePath);
		nf.openToRead();
		
		TreeFile nexusTree = NexusUtils.loadNexusTree(nf);
		return (NXroot) nexusTree.getGroupNode();
	}
	
	private void checkNexusFile(IRunnableDevice<ScanModel> scanner, int... sizes) throws Exception {
		final ScanModel scanModel = ((AbstractRunnableDevice<ScanModel>) scanner).getModel();
		assertEquals(DeviceState.READY, scanner.getDeviceState());

		NXroot rootNode = getNexusRoot(scanner);
		NXentry entry = rootNode.getEntry();
		NXinstrument instrument = entry.getInstrument();
		
		// check that the scan points have been written correctly
		assertScanPointsGroup(entry, sizes);
		
		LinkedHashMap<String, List<String>> signalFieldAxes = new LinkedHashMap<>();
		// axis for additional dimensions of a datafield, e.g. image
		signalFieldAxes.put(NXdetector.NX_DATA, Arrays.asList("real", "imaginary"));
		signalFieldAxes.put("spectrum", Arrays.asList("spectrum_axis"));
		signalFieldAxes.put("value", Collections.emptyList());
		
		String detectorName = scanModel.getDetectors().get(0).getName();
		NXdetector detector = instrument.getDetector(detectorName);
		// map of detector data field to name of nxData group where that field is the @signal field
		Map<String, String> expectedDataGroupNames =
				signalFieldAxes.keySet().stream().collect(Collectors.toMap(Function.identity(),
				x -> detectorName + (x.equals(NXdetector.NX_DATA) ? "" : "_" + x)));

		// validate the main NXdata generated by the NexusDataBuilder
		Map<String, NXdata> nxDataGroups = entry.getChildren(NXdata.class);
		assertEquals(signalFieldAxes.size(), nxDataGroups.size());
		assertTrue(nxDataGroups.keySet().containsAll(
				expectedDataGroupNames.values()));
		for (String nxDataGroupName : nxDataGroups.keySet()) {
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

			// check that the other primary data fields of the detector haven't been added to this NXdata
			for (String primaryDataFieldName : signalFieldAxes.keySet()) {
				if (!primaryDataFieldName.equals(sourceFieldName)) {
					assertNull(nxData.getDataNode(primaryDataFieldName));
				}
			}

			int[] shape = dataset.getShape();
			for (int i = 0; i < sizes.length; i++)
				assertEquals(sizes[i], shape[i]);

			// Make sure none of the numbers are NaNs. The detector
			// is expected to fill this scan with non-nulls.
			final PositionIterator it = new PositionIterator(shape);
			while (it.hasNext()) {
				int[] next = it.getPos();
				assertFalse(Double.isNaN(dataset.getDouble(next)));
			}

			// Check axes
			final IPosition pos = scanModel.getPositionIterable().iterator().next();
			final Collection<String> scannableNames = pos.getNames();

			// Append _value_demand to each name in list, then add detector axis fields to result
			List<String> expectedAxesNames = Stream.concat(
					scannableNames.stream().map(x -> x + "_value_demand"),
					signalFieldAxes.get(sourceFieldName).stream()).collect(Collectors.toList());
			assertAxes(nxData, expectedAxesNames.toArray(new String[expectedAxesNames.size()]));

			int[] defaultDimensionMappings = IntStream.range(0, sizes.length).toArray();
			int i = -1;
			for (String  scannableName : scannableNames) {
				
			    i++;
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
						"/entry/instrument/" + scannableName + "/"
								+ NXpositioner.NX_VALUE);
			}
		}
	}

	private IRunnableDevice<ScanModel> createGridScan(int... size) throws Exception {
		// Create scan points for a grid and make a generator
		GridModel gmodel = new GridModel();
		gmodel.setFastAxisName("xNex");
		gmodel.setFastAxisPoints(size[size.length-1]);
		gmodel.setSlowAxisName("yNex");
		gmodel.setSlowAxisPoints(size[size.length-2]);
		gmodel.setBoundingBox(new BoundingBox(0,0,3,3));
		
		IPointGenerator<?> gen = gservice.createGenerator(gmodel);
		IPointGenerator<?>[] gens = new IPointGenerator<?>[size.length - 1];
		// We add the outer scans, if any
		if (size.length > 2) {
			for (int dim = size.length-3; dim>-1; dim--) {
				final StepModel model;
				if (size[dim]-1>0) {
					model = new StepModel("neXusScannable"+(dim+1), 10,20,9.99d/(size[dim]-1));
				} else {
					model = new StepModel("neXusScannable"+(dim+1), 10,20,30); // Will generate one value at 10
				}
				final IPointGenerator<?> step = gservice.createGenerator(model);
				gens[dim] = step;
			}
		}
		gens[size.length - 2 ] = gen;
		
		gen = gservice.createCompoundGenerator(gens);
		
		// Create the model for a scan
		final ScanModel smodel = new ScanModel();
		smodel.setPositionIterable(gen);
		
		// Create a file to scan into
		smodel.setFilePath(output.getAbsolutePath());
		System.out.println("File writing to " + smodel.getFilePath());
		
		// Set up the Mandelbrot
		MandelbrotModel model = createMandelbrotModel();
		IWritableDetector<MandelbrotModel> detector =
				(IWritableDetector<MandelbrotModel>) dservice.createRunnableDevice(model);
		assertNotNull(detector);
		detector.addRunListener(new IRunListener() {
			@Override
			public void runPerformed(RunEvent evt) throws ScanningException{
				System.out.println("Ran mandelbrot detector @ "+evt.getPosition());
			}
		});
		
		// TODO set up the processing
		ClusterProcessingModel pmodel = new ClusterProcessingModel();
		pmodel.setDetectorName("mandelbrot");
		pmodel.setProcessingFilePath("/tmp/sum.nxs");
		pmodel.setName("sum");
		
		final IRunnableDevice<ClusterProcessingModel> processor = dservice.createRunnableDevice(pmodel);
		smodel.setDetectors(detector, processor);
		
		final IRunnableDevice<ScanModel> scanner = dservice.createRunnableDevice(smodel, null);
		
		final IPointGenerator<?> fgen = gen;
		((IRunnableEventDevice<ScanModel>)scanner).addRunListener(new IRunListener() {
			@Override
			public void runWillPerform(RunEvent evt) throws ScanningException {
				try {
					System.out.println("Running acquisition scan of size " + fgen.size());
				} catch (GeneratorException e) {
					throw new ScanningException(e);
				}
			}
		});
		
		return scanner;
	}

}
