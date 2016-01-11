package org.eclipse.scanning.test.scan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.dataset.impl.PositionIterator;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.IGenerator;
import org.eclipse.scanning.api.points.IGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.StepModel;
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
	public void test2DNexusScan() throws Exception {	
			
		MandelbrotModel model = new MandelbrotModel();
		model.setName("mandelbrot");
		model.setxName("xNex");
		model.setyName("yNex");
		
		IRunnableDevice<MandelbrotModel> det = service.createRunnableDevice(model);
		assertNotNull(det);
				
		IRunnableDevice<ScanModel> scanner = createGridScan(det, 8, 5);
		scanner.run(null);
	
		// Check we reached ready (it will normally throw an exception on error)
        check2DFile(scanner, 8, 5);
	}

	private void check2DFile(IRunnableDevice<ScanModel> scanner, int... sizes) throws NexusException, ScanningException {
		
		final ScanModel mod = ((AbstractRunnableDevice<ScanModel>)scanner).getModel();
		
		assertEquals(DeviceState.READY, scanner.getState());

		String filePath = ((AbstractRunnableDevice<ScanModel>)scanner).getModel().getFilePath();

		NexusFile nf = fileFactory.newNexusFile(filePath);
		nf.openToRead();

		DataNode d = nf.getData("/entry/instrument/"+mod.getDetectors().get(0).getName()+"/data");
		IDataset ds = d.getDataset().getSlice().squeeze();
		int[] shape = ds.getShape();

		for (int i = 0; i < sizes.length; i++) assertEquals(sizes[i], shape[i]);
		
		// Make sure none of the numbers are NaNs. The detector
		// is expected to fill this scan with non-nulls.
        final PositionIterator it = new PositionIterator(shape);
        while(it.hasNext()) {
        	int[] next = it.getPos();
        	assertFalse(Double.isNaN(ds.getDouble(next)));
        }

		// Check axes
        final IPosition      pos = mod.getPositionIterator().iterator().next();
        final List<String> names = pos.getNames();
        
        for (int i = 0; i < names.size(); i++) {
    		d     = nf.getData("/entry/instrument/"+names.get(i)+"/value");
    		ds    = d.getDataset().getSlice().squeeze();
    		shape = ds.getShape();
    		assertEquals(sizes[i], shape[0]);
		}
	}

	private IRunnableDevice<ScanModel> createGridScan(final IRunnableDevice<?> detector, int... size) throws Exception {
		
		// Create scan points for a grid and make a generator
		GridModel gmodel = new GridModel();
		gmodel.setxName("xNex");
		gmodel.setColumns(size[size.length-2]);
		gmodel.setyName("yNex");
		gmodel.setRows(size[size.length-1]);
		gmodel.setBoundingBox(new BoundingBox(0,0,3,3));
		
		IGenerator<?,IPosition> gen = gservice.createGenerator(gmodel);
		
		// We add the outer scans, if any
		if (size.length > 2) { 
			for (int dim = size.length-3; dim>-1; dim--) {
				final StepModel model = new StepModel("neXusScannable"+dim, 10,20,11/size[dim]);
				final IGenerator<?,IPosition> step = gservice.createGenerator(gmodel);
				gen = gservice.createCompoundGenerator(step, gen);
			}
		}
	
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
