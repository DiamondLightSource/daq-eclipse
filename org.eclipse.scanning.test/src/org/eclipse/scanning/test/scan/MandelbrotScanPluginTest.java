package org.eclipse.scanning.test.scan;

import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NexusFile;
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
public class MandelbrotScanPluginTest {
	
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
		
		final MockWritingModel model = new MockWritingModel(); // Defaults ok
		File output = File.createTempFile("test_nexus", ".nxs");
		output.deleteOnExit();
		model.setFilePath(output.getAbsolutePath());
		
		IRunnableDevice<MockWritingModel> det = service.createRunnableDevice(model);
		assertNotNull(det);
	}
	
	@Test
	public void testMandelbrotScan() throws Exception {

		// Configure a detector with a collection time.
		final MockWritingModel model = new MockWritingModel(); // Defaults ok
		File output = File.createTempFile("test_nexus", ".nxs");
		output.deleteOnExit();
		model.setFilePath(output.getAbsolutePath());

		IRunnableDevice<MockWritingModel> det = service.createRunnableDevice(model);
		IRunnableDevice<ScanModel> scanner = createTestScanner(det);
		scanner.run(null);
			
		// Check what was written.
		INexusFileFactory factory = MockWritingDetector.getFactory();
		NexusFile nf = factory.newNexusFile(output.getAbsolutePath());
		nf.openToRead();
		
		DataNode d = nf.getData("/entry1/instrument/detector/"+model.getName());
		IDataset ds = d.getDataset().getSlice().squeeze();
		int[] shape = ds.getShape();

		System.out.println(shape);
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
