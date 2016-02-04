package org.eclipse.scanning.test.scan.nexus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.dataset.impl.PositionIterator;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
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
import org.eclipse.scanning.api.scan.IRunnableDevice;
import org.eclipse.scanning.api.scan.IRunnableEventDevice;
import org.eclipse.scanning.api.scan.IDeviceService;
import org.eclipse.scanning.api.scan.IWritableDetector;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.event.RunEvent;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.detector.DarkImageModel;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.points.PointGeneratorFactory;
import org.eclipse.scanning.sequencer.DeviceServiceImpl;
import org.eclipse.scanning.test.scan.mock.MockScannableConnector;
import org.junit.BeforeClass;
import org.junit.Test;

public class DarkPluginTest {

	
	private static INexusFileFactory   fileFactory;
	
	private static IDeviceService        service;
	private static IPointGeneratorService       gservice;
	private static IDeviceConnectorService connector;
	
	private static IWritableDetector<MandelbrotModel> detector;
	private static IWritableDetector<DarkImageModel>  dark;

	@BeforeClass
	public static void before() throws Exception {
		
		connector = new MockScannableConnector();
		service   = new DeviceServiceImpl(connector); // Not testing OSGi so using hard coded service.
		gservice  = new PointGeneratorFactory();
		
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

		DarkImageModel dmodel = new DarkImageModel();
		dark =  (IWritableDetector<DarkImageModel>)service.createRunnableDevice(dmodel);
		assertNotNull(dark);
		dark.addRunListener(new IRunListener.Stub() {
			@Override
			public void writePerformed(RunEvent evt) throws ScanningException{
                System.out.println("Wrote dark image @ "+evt.getPosition());
			}
		});
	}
	
	/**
	 * This test fails if the chunking is not done by the detector.
	 *  
	 * @throws Exception
	 */
	@Test
	public void testDarkImage() throws Exception {	

		// Tell configure detector to write 1 image into a 2D scan
		IRunnableDevice<ScanModel> scanner = createGridScan(8, 5);
		scanner.run(null);

		checkDark(scanner, 8, 5);
		checkImages(scanner, 8, 5);
	}
		
	private void checkDark(IRunnableDevice<ScanModel> scanner, int... sizes) throws NexusException, ScanningException {
		
		final ScanModel mod = ((AbstractRunnableDevice<ScanModel>)scanner).getModel();                      
        
		assertEquals(DeviceState.READY, scanner.getState());                                                

		String filePath = ((AbstractRunnableDevice<ScanModel>)scanner).getModel().getFilePath();            
        
		NexusFile nf = fileFactory.newNexusFile(filePath);                                                  
		nf.openToRead();                                                                                    
                                                                                                            
		DataNode d = nf.getData("/entry/instrument/"+mod.getDetectors().get(1).getName()+"/data");          
		IDataset ds = d.getDataset().getSlice();                                                            
		int[] shape = ds.getShape();                                                                        

		double size = 1; 
		for (double i : sizes)size*=i;
		size = size/(((AbstractRunnableDevice<DarkImageModel>)dark).getModel().getFrequency());
		
		assertEquals(shape[0], (int)size);
	}
	
	private void checkImages(IRunnableDevice<ScanModel> scanner, int... sizes) throws NexusException, ScanningException {
		
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
        
        // Demand values should be 1D
        for (int i = 0; i < names.size(); i++) {
    		d     = nf.getData("/entry/instrument/"+names.get(i)+"/value_demand");
    		ds    = d.getDataset().getSlice().squeeze();
    		shape = ds.getShape();
    		if (sizes[i]>1) {
    		    assertEquals(sizes[i], shape[0]);
    		} else {
    			assertEquals(0, shape.length);
    		}
		}
        
        // Actual values should be scanD
        for (int i = 0; i < names.size(); i++) {
    		d     = nf.getData("/entry/instrument/"+names.get(i)+"/value");
    		ds    = d.getDataset().getSlice().squeeze();
    		shape = ds.getShape();
    		assertTrue(Arrays.equals(sizes, shape));
		}
	}

	private IRunnableDevice<ScanModel> createGridScan(int... size) throws Exception {
		
		// Create scan points for a grid and make a generator
		GridModel gmodel = new GridModel();
		gmodel.setxName("xNex");
		gmodel.setColumns(size[size.length-2]);
		gmodel.setyName("yNex");
		gmodel.setRows(size[size.length-1]);
		gmodel.setBoundingBox(new BoundingBox(0,0,3,3));
		
		IPointGenerator<?,IPosition> gen = gservice.createGenerator(gmodel);
		
		// We add the outer scans, if any
		if (size.length > 2) { 
			for (int dim = size.length-3; dim>-1; dim--) {
				final StepModel model;
				if (size[dim]-1>0) {
				    model = new StepModel("neXusScannable"+(dim+1), 10,20,9.9d/(size[dim]-1));
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
		smodel.setDetectors(detector, dark);
		
		// Create a file to scan into.
		File output = File.createTempFile("test_dark_nexus", ".nxs");
		output.deleteOnExit();
		smodel.setFilePath(output.getAbsolutePath());
		System.out.println("File writing to "+smodel.getFilePath());

		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = service.createRunnableDevice(smodel, null);
		
		final IPointGenerator<?,IPosition> fgen = gen;
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
		DarkPluginTest.fileFactory = fileFactory;
	}

}
