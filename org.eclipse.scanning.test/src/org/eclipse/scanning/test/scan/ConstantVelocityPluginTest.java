package org.eclipse.scanning.test.scan;

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
import org.eclipse.scanning.api.points.IGenerator;
import org.eclipse.scanning.api.points.IGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
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
import org.eclipse.scanning.example.detector.ConstantVelocityModel;
import org.eclipse.scanning.points.GeneratorServiceImpl;
import org.eclipse.scanning.sequencer.ScanningServiceImpl;
import org.eclipse.scanning.test.scan.mock.MockScannableConnector;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConstantVelocityPluginTest {

	
	private static INexusFileFactory   fileFactory;
	
	private static IScanningService        service;
	private static IGeneratorService       gservice;
	private static IDeviceConnectorService connector;
	
	private static IWritableDetector<ConstantVelocityModel> detector;

	@BeforeClass
	public static void before() throws Exception {
		
		service   = new ScanningServiceImpl(); // Not testing OSGi so using hard coded service.
		gservice  = new GeneratorServiceImpl();
		connector = new MockScannableConnector();
		
		ConstantVelocityModel model = new ConstantVelocityModel("cv scan", 100, 200, 25);
		model.setName("cv device");
			
		detector = (IWritableDetector<ConstantVelocityModel>)service.createRunnableDevice(model);
		assertNotNull(detector);
		
		detector.addRunListener(new IRunListener.Stub() {
			@Override
			public void runPerformed(RunEvent evt) throws ScanningException{
                System.out.println("Ran cv device detector @ "+evt.getPosition());
			}
		});

	}
	

	@Test
	public void test1DOuter() throws Exception {		
		testScan(8);
	}
	
	@Test
	public void test2DOuter() throws Exception {		
		testScan(5, 8);
	}
	
	@Test
	public void test3DOuter() throws Exception {		
		testScan(2, 2, 2);
	}
	
	@Test
	public void test8DOuter() throws Exception {		
		testScan(2, 1, 1, 1, 1, 1, 1, 1);
	}

	
	private void testScan(int... shape) throws Exception {
		
		IRunnableDevice<ScanModel> scanner = createNestedStepScan(detector, shape); // Outer scan of another scannable, for instance temp.
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
    		ds    = d.getDataset().getSlice();
    		shape = ds.getShape();
    		for (int j = 0; j < sizes.length; j++)  assertEquals(sizes[j], shape[j]);
		}

	}

	private IRunnableDevice<ScanModel> createNestedStepScan(final IRunnableDevice<?> detector, int... size) throws Exception {
		
		// Create scan points for a grid and make a generator
		StepModel smodel;
		int ySize = size[size.length-1];
		if (ySize-1>0) {
			smodel = new StepModel("yNex", 10,20,11d/ySize);
		} else {
			smodel = new StepModel("yNex", 10,20,30); // Will generate one value at 10
		}
		
		IGenerator<?,IPosition> gen = gservice.createGenerator(smodel);
		assertEquals(ySize, gen.size());
		
		// We add the outer scans, if any
		if (size.length > 1) { 
			for (int dim = size.length-2; dim>-1; dim--) {
				final StepModel model;
				if (size[dim]-1>0) {
				    model = new StepModel("neXusScannable"+(dim+1), 10,20,11d/(size[dim]));
				} else {
					model = new StepModel("neXusScannable"+(dim+1), 10,20,30); // Will generate one value at 10
				}
				final IGenerator<?,IPosition> step = gservice.createGenerator(model);
				gen = gservice.createCompoundGenerator(step, gen);
			}
		}
	
		// Create the model for a scan.
		final ScanModel  scanModel = new ScanModel();
		scanModel.setPositionIterable(gen);
		scanModel.setDetectors(detector);
		
		// Create a file to scan into.
		File output = File.createTempFile("test_mandel_nexus", ".nxs");
		output.deleteOnExit();
		scanModel.setFilePath(output.getAbsolutePath());
		System.out.println("File writing to "+scanModel.getFilePath());

		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = service.createRunnableDevice(scanModel, null, connector);
		
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
		ConstantVelocityPluginTest.fileFactory = fileFactory;
	}

}
