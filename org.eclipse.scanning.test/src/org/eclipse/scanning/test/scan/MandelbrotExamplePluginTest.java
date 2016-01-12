package org.eclipse.scanning.test.scan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IGenerator;
import org.eclipse.scanning.api.points.IGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.api.scan.AbstractRunnableDevice;
import org.eclipse.scanning.api.scan.IDeviceConnectorService;
import org.eclipse.scanning.api.scan.IRunnableDevice;
import org.eclipse.scanning.api.scan.IRunnableEventDevice;
import org.eclipse.scanning.api.scan.IScanningService;
import org.eclipse.scanning.api.scan.IWritableDetector;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.event.RunEvent;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.points.GeneratorServiceImpl;
import org.eclipse.scanning.sequencer.ScanningServiceImpl;
import org.eclipse.scanning.test.scan.mock.MockScannableConnector;
import org.junit.BeforeClass;
import org.junit.Test;

public class MandelbrotExamplePluginTest {

	
	private static INexusFileFactory   fileFactory;
	
	private static IScanningService        service;
	private static IGeneratorService       gservice;
	private static IDeviceConnectorService connector;
	
	private static IWritableDetector<MandelbrotModel> detector;

	@BeforeClass
	public static void before() throws Exception {
		
		service   = new ScanningServiceImpl(); // Not testing OSGi so using hard coded service.
		gservice  = new GeneratorServiceImpl();
		connector = new MockScannableConnector();
		
		MandelbrotModel model = new MandelbrotModel();
		model.setName("mandelbrot");
		model.setxName("xNex");
		model.setyName("yNex");
		
		detector = (IWritableDetector<MandelbrotModel>)service.createRunnableDevice(model);
		assertNotNull(detector);
		detector.addRunListener(new IRunListener.Stub() {
			@Override
			public void runPerformed(RunEvent evt) throws ScanningException{
                System.out.println("Ran mandelbrot detector @ "+evt.getPosition());
			}
		});

	}
	
	@Test
	public void test2ConsecutiveSmallScans() throws Exception {	
		
		IRunnableDevice<ScanModel> scanner = createGridScan(detector, 2, 2);
		scanner.run(null);

		scanner = createGridScan(detector, 2, 2);
		scanner.run(null);
	}
	
	/**
	 * This test fails if the chunking is not done by the detector.
	 *  
	 * @throws Exception
	 */
	@Test
	public void testWriteTime2Dvs3D() throws Exception {	

		// Tell configure detector to write 1 image into a 2D scan
		IRunnableDevice<ScanModel> scanner = createGridScan(detector, 8, 5);
		ScanModel mod = ((AbstractRunnableDevice<ScanModel>)scanner).getModel();      
		IPosition first = mod.getPositionIterable().iterator().next();
		detector.run(first);
		
		long before = System.currentTimeMillis();
		detector.write(first);
		long after = System.currentTimeMillis();
		long diff2 = (after-before);
		System.out.println("Writing 1 image in 3D stack took: "+diff2+" ms");
		
		scanner = createGridScan(detector, 10, 8, 5);
		mod = ((AbstractRunnableDevice<ScanModel>)scanner).getModel();      
		first = mod.getPositionIterable().iterator().next();
		detector.run(first);
		
		before = System.currentTimeMillis();
		detector.write(first);
		after = System.currentTimeMillis();
		long diff3 = (after-before);
		System.out.println("Writing 1 image in 4D stack took: "+diff3+" ms");

		assertTrue(diff3<Math.max(20, diff2*1.5));
	}
		
	@Test
	public void test2DNexusScan() throws Exception {		
		testScan(8,5);
	}
	
	@Test
	public void test3DNexusScan() throws Exception {	
		testScan(3,2,5);
	}
	
	// TODO Why does this not pass?
	//@Test
	public void test3DNexusScanLarge() throws Exception {	
		long before = System.currentTimeMillis();
		testScan(300,2,5);
		long after = System.currentTimeMillis();
		long diff  = after-before;
		assertTrue(diff<20000);
	}

	@Test
	public void test4DNexusScan() throws Exception {			
		testScan(3,3,2,2);
	}
	
	@Test
	public void test5DNexusScan() throws Exception {	
		testScan(1,1,1,2,2);
	}
	
	@Test
	public void test8DNexusScan() throws Exception {	
		testScan(1,1,1,1,1,1,2,2);
	}
	
	private void testScan(int... shape) throws Exception {
		
		IRunnableDevice<ScanModel> scanner = createGridScan(detector, shape); // Outer scan of another scannable, for instance temp.
		scanner.run(null);
	
		// Check we reached ready (it will normally throw an exception on error)
        checkFile(scanner, shape); // Step model is +1 on the size
	}


	private void checkFile(IRunnableDevice<ScanModel> scanner, int... sizes) throws NexusException, ScanningException {
		
		final ScanModel mod = ((AbstractRunnableDevice<ScanModel>)scanner).getModel();                      
		                                                                                                    
		assertEquals(DeviceState.READY, scanner.getState());                                                
                                                                                                            
		String filePath = ((AbstractRunnableDevice<ScanModel>)scanner).getModel().getFilePath();            
                                                                                                            
		NexusFile nf = fileFactory.newNexusFile(filePath);                                                  
		nf.openToRead();                                                                                    
                                                                                                            
		DataNode d = nf.getData("/entry/instrument/"+mod.getDetectors().get(0).getName()+"/data");          
		IDataset ds = d.getDataset().getSlice();                                                            
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
        final IPosition      pos = mod.getPositionIterable().iterator().next();
        final List<String> names = pos.getNames();
        
        for (int i = 0; i < names.size(); i++) {
    		d     = nf.getData("/entry/instrument/"+names.get(i)+"/value");
    		ds    = d.getDataset().getSlice().squeeze();
    		shape = ds.getShape();
    		if (sizes[i]>1) {
    		    assertEquals(sizes[i], shape[0]);
    		} else {
    			assertEquals(0, shape.length);
    		}
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
				final StepModel model;
				if (size[dim]-1>0) {
				    model = new StepModel("neXusScannable"+(dim+1), 10,20,11/(size[dim]-1));
				} else {
					model = new StepModel("neXusScannable"+(dim+1), 10,20,30); // Will generate one value at 10
				}
				final IGenerator<?,IPosition> step = gservice.createGenerator(model);
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
		IRunnableDevice<ScanModel> scanner = service.createRunnableDevice(smodel, null, connector);
		
		final IGenerator<?,IPosition> fgen = gen;
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
		MandelbrotExamplePluginTest.fileFactory = fileFactory;
	}

}
