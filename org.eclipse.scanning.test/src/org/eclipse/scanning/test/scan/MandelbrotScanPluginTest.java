package org.eclipse.scanning.test.scan;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.points.IGenerator;
import org.eclipse.scanning.api.points.IGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.scan.IDeviceConnectorService;
import org.eclipse.scanning.api.scan.IRunnableDevice;
import org.eclipse.scanning.api.scan.IScanningService;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.points.GeneratorServiceImpl;
import org.eclipse.scanning.sequencer.ScanningServiceImpl;
import org.eclipse.scanning.test.scan.mock.MockDetectorModel;
import org.eclipse.scanning.test.scan.mock.MockScannableConnector;
import org.junit.Before;
import org.junit.Test;

import static  org.junit.Assert.assertNotNull;

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
	public void testDetector() throws ScanningException {
		
		final MandelbrotModel model = new MandelbrotModel(); // Defaults ok
		IRunnableDevice<MandelbrotModel> det = service.createRunnableDevice(model);
		assertNotNull(det);
	}
	
	@Test
	public void testMandelbrotScan() throws Exception {

		// Configure a detector with a collection time.
		final MandelbrotModel model = new MandelbrotModel(); // Defaults ok
		IRunnableDevice<MandelbrotModel> det = service.createRunnableDevice(model);
		IRunnableDevice<ScanModel> scanner = createTestScanner(det);
		
		scanner.run(null);
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
