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
import org.eclipse.scanning.api.scan.IScanningService;
import org.eclipse.scanning.api.scan.IWritableDetector;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.event.RunEvent;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.detector.DarkImageModel;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.points.PointGeneratorFactory;
import org.eclipse.scanning.sequencer.ScanningServiceImpl;
import org.eclipse.scanning.test.scan.mock.MockScannableConnector;
import org.junit.BeforeClass;
import org.junit.Test;

public class BasicScanPluginTest {

	
	private static INexusFileFactory   fileFactory;
	
	private static IScanningService        service;
	private static IPointGeneratorService  gservice;
	private static IDeviceConnectorService connector;
	
	@BeforeClass
	public static void before() throws Exception {
		
		service   = new ScanningServiceImpl(); // Not testing OSGi so using hard coded service.
		gservice  = new PointGeneratorFactory();
		connector = new MockScannableConnector();
			
	}
	
	@Test
	public void testBasicScan1D() throws Exception {	
		test(5);
	}
	
	@Test
	public void testBasicScan2D() throws Exception {	
		test(8, 5);
	}
	
	@Test
	public void testBasicScan3D() throws Exception {	
		test(5, 8, 5);
	}

	private void test(int... shape) throws Exception {

		// Tell configure detector to write 1 image into a 2D scan
		IRunnableDevice<ScanModel> scanner = createStepScan(shape);
		scanner.run(null);

		checkBasicScan(scanner, shape);
	}

	private void checkBasicScan(IRunnableDevice<ScanModel> scanner, int... sizes) throws NexusException, ScanningException {
		
		final ScanModel mod = ((AbstractRunnableDevice<ScanModel>)scanner).getModel();                      
		                                                                                                    
		assertEquals(DeviceState.READY, scanner.getState());                                                
                                                                                                            
		String filePath = ((AbstractRunnableDevice<ScanModel>)scanner).getModel().getFilePath();            
                                                                                                            
		NexusFile nf = fileFactory.newNexusFile(filePath);                                                  
		nf.openToRead();                                                                                    
                                                                                                            
		DataNode d  = null;        
		IDataset ds = null;                                                            
		int[] shape = null;                                                                        
 
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

	private IRunnableDevice<ScanModel> createStepScan(int... size) throws Exception {
		
		IPointGenerator<?,IPosition> gen = null;
		
		// We add the outer scans, if any
		for (int dim = size.length-1; dim>-1; dim--) {
			final StepModel model;
			if (size[dim]-1>0) {
				model = new StepModel("neXusScannable"+(dim+1), 10,20,9.9d/(size[dim]-1));
			} else {
				model = new StepModel("neXusScannable"+(dim+1), 10,20,30); // Will generate one value at 10
			}
			final IPointGenerator<?,IPosition> step = gservice.createGenerator(model);
			if (gen!=null) {
				gen = gservice.createCompoundGenerator(step, gen);
			} else {
				gen = step;
			}
		}
	
		// Create the model for a scan.
		final ScanModel  smodel = new ScanModel();
		smodel.setPositionIterable(gen);
		
		// Create a file to scan into.
		File output = File.createTempFile("test_simple_nexus", ".nxs");
		output.deleteOnExit();
		smodel.setFilePath(output.getAbsolutePath());
		System.out.println("File writing to "+smodel.getFilePath());

		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = service.createRunnableDevice(smodel, null, connector);
		
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
		BasicScanPluginTest.fileFactory = fileFactory;
	}

}
