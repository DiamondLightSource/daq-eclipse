package org.eclipse.scanning.test.scan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.builder.NexusBuilderFactory;
import org.eclipse.dawnsci.nexus.builder.NexusEntryBuilder;
import org.eclipse.dawnsci.nexus.builder.NexusFileBuilder;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.ScannableModel;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.IGenerator;
import org.eclipse.scanning.api.points.IGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.scan.IDeviceConnectorService;
import org.eclipse.scanning.api.scan.IRunnableDevice;
import org.eclipse.scanning.api.scan.IScanningService;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.points.GeneratorServiceImpl;
import org.eclipse.scanning.sequencer.ScanningServiceImpl;
import org.eclipse.scanning.test.scan.mock.MockScannableConnector;
import org.junit.Before;
import org.junit.Test;

public class MandelbrotExamplePluginTest {

	
	private static NexusBuilderFactory factory;
	private static INexusFileFactory   fileFactory;
	
	private IScanningService        service;
	private IGeneratorService       gservice;
	private IDeviceConnectorService connector;

	
	@Before
	public void before() throws Exception {
		assertNotNull(factory);
		service   = new ScanningServiceImpl(); // Not testing OSGi so using hard coded service.
		gservice  = new GeneratorServiceImpl();
		connector = new MockScannableConnector();
	}
	
	@Test
	public void testExampleDetectorThere() throws Exception {	
		MandelbrotModel model = new MandelbrotModel();
		IRunnableDevice<MandelbrotModel> det = service.createRunnableDevice(model);
		assertNotNull(det);
	}
	
	@Test
	public void testNexusScan() throws Exception {	
		
		// Create a builder
		File output = File.createTempFile("test_mandel_nexus", ".nxs");
//		output.deleteOnExit();
		
		final NexusFileBuilder  fbuilder = factory.newNexusFileBuilder(output.getAbsolutePath());
		final NexusEntryBuilder builder  = fbuilder.newEntry();
		builder.addDefaultGroups();
			
		MandelbrotModel model = new MandelbrotModel();
		model.setxName("xNex");
		model.setyName("yNex");
		
		IRunnableDevice<MandelbrotModel> det = service.createRunnableDevice(model);
		assertNotNull(det);
		//builder.add((INexusDevice)det).getNexusProvider());
		
		IScannable<Number> x = connector.getScannable("xNex");
		x.configure(new ScannableModel(1));
		builder.add(((INexusDevice)x).getNexusProvider());
		
		IScannable<Number> y = connector.getScannable("yNex");
		y.configure(new ScannableModel(1));
		builder.add(((INexusDevice)y).getNexusProvider());

		fbuilder.saveFile();
		
		IRunnableDevice<ScanModel> scanner = createTestScanner(det, 5, 8);
		scanner.run(null);
	
		// Check we reached ready (it will normally throw an exception on error)
		assertEquals(DeviceState.READY, scanner.getState());

		NexusFile nf = fileFactory.newNexusFile(output.getAbsolutePath());
		nf.openToRead();

		// Check axes
		DataNode d     = nf.getData("/entry/instrument/"+x.getName()+"/value");
		IDataset ds    = d.getDataset().getSlice().squeeze();
		int[] shape = ds.getShape();
		assertEquals(8, shape[0]);

		
		d     = nf.getData("/entry/instrument/"+y.getName()+"/value");
		ds    = d.getDataset().getSlice().squeeze();
		shape = ds.getShape();
		assertEquals(5, shape[0]);

	}

	private IRunnableDevice<ScanModel> createTestScanner(final IRunnableDevice<?> detector, int... size) throws Exception {
		
		// Create scan points for a grid and make a generator
		GridModel gmodel = new GridModel();
		gmodel.setRows(size[0]);
		gmodel.setColumns(size[1]);
		gmodel.setBoundingBox(new BoundingBox(0,0,3,3));
		gmodel.setxName("xNex");
		gmodel.setyName("yNex");
		IGenerator<?,IPosition> gen = gservice.createGenerator(gmodel);

		// Create the model for a scan.
		final ScanModel  smodel = new ScanModel();
		smodel.setPositionIterator(gen);
		smodel.setDetectors(detector);
		
		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = service.createRunnableDevice(smodel, null, connector);
		return scanner;
	}

	
	/**
	 * 
	 * 	private void createNexusFile(List<NexusObjectProvider<NXpositioner>> scannables) throws Exception {
		final DefaultNexusBuilderFactory builderFactory = new DefaultNexusBuilderFactory();
		final NexusFileBuilder nexusFileBuilder = builderFactory.newNexusFileBuilder(nexusFilePath);
		final NexusEntryBuilder nexusEntryBuilder = nexusFileBuilder.newEntry();
		nexusEntryBuilder.addDefaultGroups();
		for (final NexusObjectProvider<NXpositioner> scannable : scannables) {
			nexusEntryBuilder.add(scannable);
		}

		// also build the NXdata? we need the detector for that
		//final NexusDataBuilder nexusDataBuilder = nexusEntryBuilder.createDefaultData();
//		nexusDataBuilder.setDataDevice(nexusObjectProvider);
//		int positionerDimensionIndex = 0;
//		for (final NexusObjectProvider<NXpositioner> scannable : scannables) {
//			final int[] dimensionMappings = new int[] { positionerDimensionIndex++ };
//			nexusDataBuilder.addAxisDevice(detector, dimensionMappings);
//		}

		nexusFileBuilder.saveFile();
	}

*/

	public static NexusBuilderFactory getFactory() {
		return factory;
	}

	public static void setFactory(NexusBuilderFactory factory) {
		MandelbrotExamplePluginTest.factory = factory;
	}

	public static INexusFileFactory getFileFactory() {
		return fileFactory;
	}

	public static void setFileFactory(INexusFileFactory fileFactory) {
		MandelbrotExamplePluginTest.fileFactory = fileFactory;
	}

}
