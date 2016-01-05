package org.eclipse.scanning.test.scan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.LazyWriteableDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.PositionIterator;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NexusFile;
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
import org.eclipse.scanning.points.GeneratorServiceImpl;
import org.eclipse.scanning.sequencer.ScanningServiceImpl;
import org.eclipse.scanning.test.scan.mock.MockScannableConnector;
import org.eclipse.scanning.test.scan.mock.MockWritingDetector;
import org.eclipse.scanning.test.scan.mock.MockWritingModel;
import org.junit.Before;
import org.junit.Test;

/**
 * Uses extension points so please run as plugin test.
 * 
 * @author fcp94556
 *
 */
public class LowLevelDetectorPluginTest {
	

	private static INexusFileFactory factory;

	public static INexusFileFactory getFactory() {
		return factory;
	}

	public static void setFactory(INexusFileFactory factory) {
		LowLevelDetectorPluginTest.factory = factory;
	}

	private IScanningService        service;
	private IGeneratorService       gservice;
	private IDeviceConnectorService connector;
	
	@Before
	public void before() {
		service   = new ScanningServiceImpl(); // Not testing OSGi so using hard coded service.
		gservice  = new GeneratorServiceImpl();
		connector = new MockScannableConnector();
	}
	
	@Test
	public void testDetector() throws Exception {
		
		final MockWritingModel model = createMockWriterModel();
		IRunnableDevice<MockWritingModel> det = service.createRunnableDevice(model);
		model.getFile().close();
		assertNotNull(det);
	}
	
	@Test
	public void testMandelbrotScan() throws Exception {

		// Configure a detector with a collection time.
		final MockWritingModel model = createMockWriterModel();
		model.setPoints(25); // This could also come from the generator made in createTestScanner(...) but it is hard coded

		IRunnableDevice<MockWritingModel> det = service.createRunnableDevice(model);
		model.getFile().close();
		
		IRunnableDevice<ScanModel> scanner = createTestScanner(det);
		scanner.run(null);
		
		// Check we reached ready (it will normally throw an exception on error)
		assertEquals(DeviceState.READY, scanner.getState());
			
		// Check what was written.
		NexusFile nf = factory.newNexusFile(model.getFile().getFilePath());
		nf.openToRead();
		
		DataNode d = nf.getData("/entry1/instrument/detector/"+model.getName());
		IDataset ds = d.getDataset().getSlice().squeeze();
		int[] shape = ds.getShape();

		assertEquals(25, shape[0]);
		
		// Make sure none of the numbers are NaNs. The detector
		// is expected to fill this scan with non-nulls.
        final PositionIterator it = new PositionIterator(shape);
        while(it.hasNext()) {
        	int[] next = it.getPos();
        	assertFalse(Double.isNaN(ds.getDouble(next)));
        }
	}
	
	private MockWritingModel createMockWriterModel() throws Exception {
		
		MockWritingModel model = new MockWritingModel(); // Defaults ok
		File output = File.createTempFile("test_nexus", ".nxs");
		output.deleteOnExit();
		NexusFile file = factory.newNexusFile(output.getAbsolutePath(), true);  // DO NOT COPY!
		file.openToWrite(true); // DO NOT COPY!
		model.setFile(file);
				
		return model;
	}

	private IRunnableDevice<ScanModel> createTestScanner(final IRunnableDevice<?> detector) throws Exception {
		
		// Create scan points for a grid and make a generator
		GridModel gmodel = new GridModel();
		gmodel.setRows(5);
		gmodel.setColumns(5);
		gmodel.setBoundingBox(new BoundingBox(0,0,3,3));	
		IGenerator<?,IPosition> gen = gservice.createGenerator(gmodel);

		// Create the model for a scan.
		final ScanModel  smodel = new ScanModel();
		smodel.setPositionIterator(gen);
		smodel.setDetectors(detector);
		
		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = service.createRunnableDevice(smodel, null, connector);
		return scanner;
	}

}
