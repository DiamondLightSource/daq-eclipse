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
import static org.junit.Assert.assertTrue;

import java.io.File;
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
import org.eclipse.dawnsci.analysis.dataset.impl.PositionIterator;
import org.eclipse.dawnsci.analysis.dataset.impl.StringDataset;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.scanning.api.IAttributeContainer;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.api.scan.AbstractRunnableDevice;
import org.eclipse.scanning.api.scan.IDeviceConnectorService;
import org.eclipse.scanning.api.scan.IDeviceService;
import org.eclipse.scanning.api.scan.IRunnableDevice;
import org.eclipse.scanning.api.scan.IRunnableEventDevice;
import org.eclipse.scanning.api.scan.IWritableDetector;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.event.RunEvent;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.points.PointGeneratorFactory;
import org.eclipse.scanning.sequencer.DeviceServiceImpl;
import org.eclipse.scanning.test.scan.mock.MockScannableConnector;
import org.junit.Before;
import org.junit.Test;

public class AttributePluginTest {

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
	public void name() throws Exception {
		
		// All scannables should have their name set ok
		IRunnableDevice<ScanModel> scanner = createGridScan(detector, 2, 2);
		scanner.run(null);
		
		checkNexusFile(scanner, 2, 2);
		checkAttribute(scanner, "xNex", "name");
	}

	@Test 
	public void description() throws Exception {
		
		IScannable<?> x = connector.getScannable("xNex");
		if (!(x instanceof IAttributeContainer)) throw new Exception("xNex is not "+IAttributeContainer.class.getSimpleName());
		IAttributeContainer xc = (IAttributeContainer)x;
		xc.setAttribute("description", "Reality is a shapeless unity.\nThe mind which distinguishes between aspects of this unity, sees only disunity.\nRemain unconcerned.");
	
		IRunnableDevice<ScanModel> scanner = createGridScan(detector, 2, 2);
		scanner.run(null);
		
		checkNexusFile(scanner, 2, 2);
		checkAttribute(scanner, "xNex", "description");
	}

	@Test 
	public void lots() throws Exception {
		
		IScannable<?> x = connector.getScannable("xNex");
		if (!(x instanceof IAttributeContainer)) throw new Exception("xNex is not "+IAttributeContainer.class.getSimpleName());
		IAttributeContainer xc = (IAttributeContainer)x;
		
		// @see http://download.nexusformat.org/doc/html/classes/base_classes/NXpositioner.html
		//description: NX_CHAR
		xc.setAttribute("description", "Reality is a shapeless unity.\nThe mind which distinguishes between aspects of this unity, sees only disunity.\nRemain unconcerned.");

		// value[n]: NX_NUMBER {units=NX_ANY}
		// raw_value[n]: NX_NUMBER {units=NX_ANY}
		// target_value[n]: NX_NUMBER {units=NX_ANY}
        // tolerance[n]: NX_NUMBER {units=NX_ANY}

		//soft_limit_min: NX_NUMBER {units=NX_ANY}
		xc.setAttribute("soft_limit_min", 1);

		// soft_limit_max: NX_NUMBER {units=NX_ANY}
		xc.setAttribute("soft_limit_max", 10);

		// velocity: NX_NUMBER {units=NX_ANY}
		xc.setAttribute("velocity", 1.2);
		
		// acceleration_time: NX_NUMBER {units=NX_ANY}
		xc.setAttribute("acceleration_time", 0.1);

		// controller_record: NX_CHAR
		xc.setAttribute("controller_record", "Homer Simpson");
	
		IRunnableDevice<ScanModel> scanner = createGridScan(detector, 2, 2);
		scanner.run(null);
		
		checkNexusFile(scanner, 2, 2);
		
		for(String aName : xc.getAttributeNames()) {
		    checkAttribute(scanner, "xNex", aName);
		}
	}

	private void checkAttribute(IRunnableDevice<ScanModel> scanner, String sName, String attrName) throws Exception {
		
		IScannable<?> s = connector.getScannable(sName);
		if (!(s instanceof IAttributeContainer)) throw new Exception(sName+" is not "+IAttributeContainer.class.getSimpleName());
		
		IAttributeContainer sc = (IAttributeContainer)s;		
		Object attrValue = sc.getAttribute(attrName);
		
		String filePath = ((AbstractRunnableDevice<ScanModel>) scanner).getModel().getFilePath();
		NexusFile nf = fileFactory.newNexusFile(filePath);
		nf.openToRead();
		
		DataNode node = nf.getData("/entry/instrument/" + sName + "/"+attrName);
		IDataset sData = (IDataset)node.getDataset().getSlice();
		
		if ("name".equals(attrName)) {
			assertEquals(sData.getString(0), sName);
		} else if (attrValue instanceof Number) {
			assertTrue(sData.getDouble(0)==((Number)attrValue).doubleValue());
		} else {
			assertEquals(sData.getString(0), (String)attrValue);
		}
		
	}

	private void checkNexusFile(IRunnableDevice<ScanModel> scanner,
			                    int... sizes) throws NexusException, ScanningException {

		final ScanModel scanModel = ((AbstractRunnableDevice<ScanModel>) scanner).getModel();
		assertEquals(DeviceState.READY, scanner.getDeviceState());

		String filePath = ((AbstractRunnableDevice<ScanModel>) scanner).getModel().getFilePath();
		NexusFile nf = fileFactory.newNexusFile(filePath);
		nf.openToRead();
		
		TreeFile nexusTree = NexusUtils.loadNexusTree(nf);
		NXroot rootNode = (NXroot) nexusTree.getGroupNode();
		NXentry entry = rootNode.getEntry();
		NXinstrument instrument = entry.getInstrument();

		LinkedHashMap<String, Integer> detectorDataFields = new LinkedHashMap<>();
		detectorDataFields.put(NXdetector.NX_DATA, 2); // num additional dimensions
		detectorDataFields.put("spectrum", 1);
		detectorDataFields.put("value", 0);

		String detectorName = scanModel.getDetectors().get(0).getName();
		NXdetector detector = instrument.getDetector(detectorName);
		// map of detector data field to name of nxData group where that field is the @signal field
		Map<String, String> expectedDataGroupNames =
				detectorDataFields.keySet().stream().collect(Collectors.toMap(Function.identity(),
						x -> detectorName + (x.equals(NXdetector.NX_DATA) ? "" : "_" + x)));

		// validate the main NXdata generated by the NexusDataBuilder
		Map<String, NXdata> nxDataGroups = entry.getChildren(NXdata.class);
		assertEquals(detectorDataFields.size(), nxDataGroups.size());
		assertThat(nxDataGroups.keySet(), containsInAnyOrder(
				expectedDataGroupNames.values().toArray()));
		for (String nxDataGroupName : nxDataGroups.keySet()) {
			NXdata nxData = entry.getData(nxDataGroupName);

			String sourceFieldName = nxDataGroupName.equals(detectorName) ? NXdetector.NX_DATA :
				nxDataGroupName.substring(nxDataGroupName.indexOf('_') + 1);
			assertSignal(nxData, sourceFieldName);
			// check the nxData's signal field is a link to the appropriate source data node of the detector
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
			List<String> expectedAxesNames = scannableNames.stream().map(
					x -> x + "_value_demand").collect(Collectors.toList());
			// add placeholder value "." for each additional dimension of dataset
			int valueRank = detectorDataFields.get(sourceFieldName);
			expectedAxesNames.addAll(Collections.nCopies(valueRank, "."));
			assertAxes(nxData, expectedAxesNames.toArray(new String[expectedAxesNames.size()]));

			int[] defaultDimensionMappings = IntStream.range(0, sizes.length)
					.toArray();
			for (int i = 0; i < scannableNames.size(); i++) {
				// Demand values should be 1D
				String scannableName = scannableNames.get(i);
				NXpositioner positioner = instrument
						.getPositioner(scannableName);
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
		
		nf.close();
	}

	private IRunnableDevice<ScanModel> createGridScan(final IRunnableDevice<?> detector, int... size) throws Exception {
		
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


}
