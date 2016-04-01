package org.eclipse.scanning.test.scan.nexus;

import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertAxes;
import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertIndices;
import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertSignal;
import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertTarget;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.analysis.dataset.impl.AbstractDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.PositionIterator;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NXsample;
import org.eclipse.dawnsci.nexus.NXuser;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IDeviceConnectorService;
import org.eclipse.scanning.api.device.IDeviceService;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableEventDevice;
import org.eclipse.scanning.api.device.IWritableDetector;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.event.RunEvent;
import org.eclipse.scanning.api.scan.models.ScanMetadata;
import org.eclipse.scanning.api.scan.models.ScanMetadata.MetadataType;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.points.PointGeneratorFactory;
import org.eclipse.scanning.sequencer.DeviceServiceImpl;
import org.eclipse.scanning.test.scan.mock.MockScannableConnector;
import org.junit.Before;
import org.junit.Test;

public class ScanMetadataPluginTest {
	
	protected IDeviceService              dservice;
	protected IDeviceConnectorService     connector;
	protected IPointGeneratorService      gservice;
	protected IEventService               eservice;
	private IWritableDetector<MandelbrotModel> detector;
	private INexusFileFactory             fileFactory;
	
	@Before
	public void setup() throws ScanningException {
		
		fileFactory = new NexusFileFactoryHDF5();		
		
		connector = new MockScannableConnector();
		dservice  = new DeviceServiceImpl(connector); // Not testing OSGi so using hard coded service.
		gservice  = new PointGeneratorFactory();
		
		MandelbrotModel model = new MandelbrotModel();
		model.setName("mandelbrot");
		model.setxName("xNex");
		model.setyName("yNex");
		
		detector = (IWritableDetector<MandelbrotModel>)dservice.createRunnableDevice(model);
		assertNotNull(detector);
		detector.addRunListener(new IRunListener.Stub() {
			@Override
			public void runPerformed(RunEvent evt) throws ScanningException{
                System.out.println("Ran mandelbrot detector @ "+evt.getPosition());
			}
		});

	}

	@Test
	public void testScanMetadata() throws Exception {
		List<ScanMetadata> scanMetadata = new ArrayList<>();
		ScanMetadata entryMetadata = new ScanMetadata(MetadataType.ENTRY);
		entryMetadata.addMetadataField(NXentry.NX_TITLE, "Scan Metadata Test Entry");
		entryMetadata.addMetadataField(NXentry.NX_EXPERIMENT_IDENTIFIER, "i05-1");
		entryMetadata.addMetadataField(NXentry.NX_START_TIME, "2016-03-21T16:41:27Z");
		scanMetadata.add(entryMetadata);
		
		ScanMetadata instrumentMetadata = new ScanMetadata(MetadataType.INSTRUMENT);
		instrumentMetadata.addMetadataField(NXinstrument.NX_NAME, "i05-1");
		scanMetadata.add(instrumentMetadata);
		
		ScanMetadata sampleMetadata = new ScanMetadata(MetadataType.SAMPLE);
		sampleMetadata.addMetadataField(NXsample.NX_CHEMICAL_FORMULA, "H2O");
		sampleMetadata.addMetadataField(NXsample.NX_TEMPERATURE, 22.0);
		sampleMetadata.addMetadataField(NXsample.NX_DESCRIPTION, "Test sample");
		scanMetadata.add(sampleMetadata);
		
		ScanMetadata userMetadata = new ScanMetadata(MetadataType.USER);
		userMetadata.addMetadataField(NXuser.NX_NAME, "testuser");
		userMetadata.addMetadataField(NXuser.NX_ADDRESS, "Diamond Light Source, Diamond House, Harwell Science & Innovation Campus, Didcot, Oxfordshire, OX11 0DE");
		userMetadata.addMetadataField(NXuser.NX_EMAIL, "user@diamond.ac.uk");
		userMetadata.addMetadataField(NXuser.NX_TELEPHONE_NUMBER, "01");
		scanMetadata.add(userMetadata);
		
		IRunnableDevice<ScanModel> scanner = createGridScan(detector, scanMetadata, 2, 2);
		scanner.run(null);
		
		checkNexusFile(scanner, scanMetadata, 2, 2);
	}
	
	private IRunnableDevice<ScanModel> createGridScan(final IRunnableDevice<?> detector,
			List<ScanMetadata> scanMetadata, int... size) throws Exception {
		
		// Create scan points for a grid and make a generator
		GridModel gmodel = new GridModel();
		gmodel.setxName("xNex");
		gmodel.setColumns(size[size.length-1]);
		gmodel.setyName("yNex");
		gmodel.setRows(size[size.length-2]);
		gmodel.setBoundingBox(new BoundingBox(0,0,3,3));
		
		IPointGenerator<?,IPosition> gen = gservice.createGenerator(gmodel);
		
		// We add the outer scans, if any
		if (size.length > 2) { 
			for (int dim = size.length-3; dim>-1; dim--) {
				final StepModel model;
				if (size[dim]-1>0) {
				    model = new StepModel("neXusScannable"+(dim+1), 10,20,9.99d/(size[dim]-1));
				} else {
					model = new StepModel("neXusScannable"+(dim+1), 10,20,30); // Will generate one value at 10
				}
				final IPointGenerator<?,IPosition> step = gservice.createGenerator(model);
				gen = gservice.createCompoundGenerator(step, gen);
			}
		}
	
		// Create the model for a scan.
		final ScanModel  smodel = new ScanModel();
		smodel.setPositionIterable(gen);
		smodel.setDetectors(detector);
		smodel.setScanMetadata(scanMetadata);
		
		// Create a file to scan into.
		File output = File.createTempFile("test_mandel_nexus", ".nxs");
		output.deleteOnExit();
		smodel.setFilePath(output.getAbsolutePath());
		System.out.println("File writing to "+smodel.getFilePath());

		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = dservice.createRunnableDevice(smodel, null);
		
		final IPointGenerator<?,IPosition> fgen = gen;
		((IRunnableEventDevice<ScanModel>)scanner).addRunListener(new IRunListener.Stub() {
			@Override
			public void runWillPerform(RunEvent evt) throws ScanningException {
				try {
					System.out.println("Running acquisition scan of size "+fgen.size());
				} catch (GeneratorException e) {
					throw new ScanningException(e);
				}
			}
		});

		return scanner;
	}
	
	private void checkMetadata(NXentry entry, List<ScanMetadata> scanMetadataList) {
		for (ScanMetadata scanMetadata : scanMetadataList) {
			MetadataType type = scanMetadata.getType();
			NXobject object = getNexusObjectForMetadataType(entry, type);
			
			for (String metadataFieldName : scanMetadata.getMetadataFieldNames()) {
				Object expectedValue = scanMetadata.getMetadataFieldValue(metadataFieldName);

				Dataset dataset = (Dataset) object.getDataset(metadataFieldName);
				assertNotNull(dataset);
				assertEquals(1, dataset.getRank());
				assertEquals(1, dataset.getSize());
				assertEquals(AbstractDataset.getDTypeFromObject(expectedValue),
						dataset.getDtype());
				assertEquals(expectedValue, dataset.getObject(0));
			}
		}
	}
	
	private NXobject getNexusObjectForMetadataType(NXentry entry, MetadataType type) {
		if (type == null) {
			return entry;
		}
		
		switch (type) {
			case ENTRY: 
				return entry;
			case INSTRUMENT:
				return entry.getInstrument();
			case SAMPLE:
				return entry.getSample();
			case USER:
				return entry.getUser();
			default:
				throw new IllegalArgumentException("Unknown metadata type " + type);
		}
	}
	
	private void checkNexusFile(IRunnableDevice<ScanModel> scanner,
			List<ScanMetadata> scanMetadata, int... sizes)
			throws NexusException, ScanningException {

		final ScanModel scanModel = ((AbstractRunnableDevice<ScanModel>) scanner).getModel();
		assertEquals(DeviceState.READY, scanner.getDeviceState());

		String filePath = ((AbstractRunnableDevice<ScanModel>) scanner).getModel().getFilePath();
		NexusFile nf = fileFactory.newNexusFile(filePath);
		nf.openToRead();

		TreeFile nexusTree = NexusUtils.loadNexusTree(nf);
		NXroot rootNode = (NXroot) nexusTree.getGroupNode();
		NXentry entry = rootNode.getEntry();
		checkMetadata(entry, scanMetadata);
		
		NXinstrument instrument = entry.getInstrument();

		LinkedHashMap<String, Integer> detectorDataFields = new LinkedHashMap<>();
		detectorDataFields.put(NXdetector.NX_DATA, 2); // num additional
														// dimensions
		detectorDataFields.put("spectrum", 1);
		detectorDataFields.put("value", 0);

		String detectorName = scanModel.getDetectors().get(0).getName();
		NXdetector detector = instrument.getDetector(detectorName);
		// map of detector data field to name of nxData group where that field
		// is the @signal field
		Map<String, String> expectedDataGroupNames = detectorDataFields.keySet().stream()
				.collect(Collectors.toMap(Function.identity(),
						x -> detectorName + (x.equals(NXdetector.NX_DATA) ? "" : "_" + x)));

		// validate the main NXdata generated by the NexusDataBuilder
		Map<String, NXdata> nxDataGroups = entry.getChildren(NXdata.class);
		assertEquals(detectorDataFields.size(), nxDataGroups.size());
		assertThat(nxDataGroups.keySet(),
				containsInAnyOrder(expectedDataGroupNames.values().toArray()));
		for (String nxDataGroupName : nxDataGroups.keySet()) {
			NXdata nxData = entry.getData(nxDataGroupName);

			String sourceFieldName = nxDataGroupName.equals(detectorName) ? NXdetector.NX_DATA
					: nxDataGroupName.substring(nxDataGroupName.indexOf('_') + 1);
			assertSignal(nxData, sourceFieldName);
			// check the nxData's signal field is a link to the appropriate
			// source data node of the detector
			DataNode dataNode = detector.getDataNode(sourceFieldName);
			IDataset dataset = dataNode.getDataset().getSlice();

			assertSame(dataNode, nxData.getDataNode(sourceFieldName));

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
			final List<String> scannableNames = pos.getNames();

			// Append _value_demand to each name in list
			List<String> expectedAxesNames = scannableNames.stream().map(x -> x + "_value_demand")
					.collect(Collectors.toList());
			// add placeholder value "." for each additional dimension of
			// dataset
			int valueRank = detectorDataFields.get(sourceFieldName);
			expectedAxesNames.addAll(Collections.nCopies(valueRank, "."));
			assertAxes(nxData, expectedAxesNames.toArray(new String[expectedAxesNames.size()]));

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

		nf.close();
	}

}
