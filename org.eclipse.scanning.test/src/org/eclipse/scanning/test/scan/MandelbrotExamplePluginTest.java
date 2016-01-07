package org.eclipse.scanning.test.scan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.dataset.impl.PositionIterator;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.ScannableModel;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.IGenerator;
import org.eclipse.scanning.api.points.IGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.scan.AbstractRunnableDevice;
import org.eclipse.scanning.api.scan.IDeviceConnectorService;
import org.eclipse.scanning.api.scan.IRunnableDevice;
import org.eclipse.scanning.api.scan.IScanningService;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.points.GeneratorServiceImpl;
import org.eclipse.scanning.sequencer.ScanningServiceImpl;
import org.eclipse.scanning.test.scan.mock.MockScannableConnector;
import org.junit.Before;
import org.junit.Test;

public class MandelbrotExamplePluginTest {

	
	private static INexusFileFactory   fileFactory;
	
	private IScanningService        service;
	private IGeneratorService       gservice;
	private IDeviceConnectorService connector;

	@Before
	public void before() throws Exception {
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
			
		MandelbrotModel model = new MandelbrotModel();
		model.setName("mandelbrot");
		model.setxName("xNex");
		model.setyName("yNex");
		
		IRunnableDevice<MandelbrotModel> det = service.createRunnableDevice(model);
		assertNotNull(det);
				
		IRunnableDevice<ScanModel> scanner = createTestScanner(det, 5, 8);
		scanner.run(null);
	
		// Check we reached ready (it will normally throw an exception on error)
        checkFile(model, scanner);
	}

	private void checkFile(MandelbrotModel model, IRunnableDevice<ScanModel> scanner) throws NexusException, ScanningException {
		
		assertEquals(DeviceState.READY, scanner.getState());

		String filePath = ((AbstractRunnableDevice<ScanModel>)scanner).getModel().getFilePath();

		NexusFile nf = fileFactory.newNexusFile(filePath);
		nf.openToRead();

		DataNode d = nf.getData("/entry/instrument/"+model.getName()+"/data");
		IDataset ds = d.getDataset().getSlice().squeeze();
		int[] shape = ds.getShape();

		assertEquals(8, shape[0]);
		assertEquals(5, shape[1]);
		
		// Make sure none of the numbers are NaNs. The detector
		// is expected to fill this scan with non-nulls.
        final PositionIterator it = new PositionIterator(shape);
        while(it.hasNext()) {
        	int[] next = it.getPos();
        	assertFalse(Double.isNaN(ds.getDouble(next)));
        }

		// Check axes
		d     = nf.getData("/entry/instrument/"+model.getxName()+"/value");
		ds    = d.getDataset().getSlice().squeeze();
		shape = ds.getShape();
		assertEquals(8, shape[0]);

		
		d     = nf.getData("/entry/instrument/"+model.getyName()+"/value");
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
		
		// Create a file to scan into.
		File output = File.createTempFile("test_mandel_nexus", ".nxs");
		output.deleteOnExit();
		smodel.setFilePath(output.getAbsolutePath());

		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = service.createRunnableDevice(smodel, null, connector);
		return scanner;
	}

	public static INexusFileFactory getFileFactory() {
		return fileFactory;
	}

	public static void setFileFactory(INexusFileFactory fileFactory) {
		MandelbrotExamplePluginTest.fileFactory = fileFactory;
	}

}
