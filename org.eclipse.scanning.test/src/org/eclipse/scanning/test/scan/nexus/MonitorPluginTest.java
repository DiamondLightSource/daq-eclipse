package org.eclipse.scanning.test.scan.nexus;

import static org.eclipse.dawnsci.nexus.test.util.NexusAssert.assertAxes;
import static org.eclipse.dawnsci.nexus.test.util.NexusAssert.assertIndices;
import static org.eclipse.dawnsci.nexus.test.util.NexusAssert.assertTarget;
import static org.eclipse.dawnsci.nexus.test.util.NexusAssert.assertSignal;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.analysis.dataset.impl.PositionIterator;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.api.scan.AbstractRunnableDevice;
import org.eclipse.scanning.api.scan.IDeviceConnectorService;
import org.eclipse.scanning.api.scan.IDeviceService;
import org.eclipse.scanning.api.scan.IRunnableDevice;
import org.eclipse.scanning.api.scan.IRunnableEventDevice;
import org.eclipse.scanning.api.scan.IWritableDetector;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.event.RunEvent;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.detector.ConstantVelocityModel;
import org.eclipse.scanning.points.PointGeneratorFactory;
import org.eclipse.scanning.sequencer.DeviceServiceImpl;
import org.eclipse.scanning.test.scan.mock.MockScannableConnector;
import org.junit.BeforeClass;
import org.junit.Test;

public class MonitorPluginTest {

	
	private static INexusFileFactory   fileFactory;
	
	private static IDeviceService        service;
	private static IPointGeneratorService       gservice;
	private static IDeviceConnectorService connector;
	
	private static IWritableDetector<ConstantVelocityModel> detector;

	@BeforeClass
	public static void before() throws Exception {
		
		connector = new MockScannableConnector();
		service   = new DeviceServiceImpl(connector); // Not testing OSGi so using hard coded service.
		gservice  = new PointGeneratorFactory();
		
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
		
		final List<String>        monitors = Arrays.asList("monitor1", "monitor2");
		IRunnableDevice<ScanModel> scanner = createNestedStepScanWithMonitors(detector, monitors, shape); // Outer scan of another scannable, for instance temp.
		scanner.run(null);
	
		// Check we reached ready (it will normally throw an exception on error)
        checkNexusFile(scanner, monitors, shape); // Step model is +1 on the size
	}


	private void checkNexusFile(IRunnableDevice<ScanModel> scanner, List<String> monitorNames, int... sizes) throws NexusException, ScanningException {
		
		final ScanModel scanModel = ((AbstractRunnableDevice<ScanModel>)scanner).getModel();                      
		                                                                                                    
		assertEquals(DeviceState.READY, scanner.getDeviceState());                                                
                                                                                                            
		String filePath = ((AbstractRunnableDevice<ScanModel>)scanner).getModel().getFilePath();            
                                                                                                            
		NexusFile nf = fileFactory.newNexusFile(filePath);                                                  
		nf.openToRead();                                      
		
		TreeFile nexusTree = NexusUtils.loadNexusTree(nf);
		NXroot rootNode = (NXroot) nexusTree.getGroupNode();
		NXentry entry = rootNode.getEntry();
		NXinstrument instrument = entry.getInstrument();
		
		String detectorName = scanModel.getDetectors().get(0).getName();
		NXdetector detector = instrument.getDetector(detectorName);
		DataNode dataNode = detector.getDataNode(NXdetector.NX_DATA);          
		IDataset dataset = dataNode.getDataset().getSlice();                                                            
		int[] shape = dataset.getShape();
		
		// validate the NXdata generated by the NexusDataBuilder
		NXdata nxData = entry.getData(detectorName);
		assertNotNull(nxData);
		assertSignal(nxData, NXdetector.NX_DATA);
		assertSame(dataNode, nxData.getDataNode(NXdetector.NX_DATA));
                                                                                                            
		for (int i = 0; i < sizes.length; i++) assertEquals(sizes[i], shape[i]);                            
		                                                                                                    
		// Make sure none of the numbers are NaNs. The detector                                             
		// is expected to fill this scan with non-nulls.                                                    
        final PositionIterator it = new PositionIterator(shape);                                            
        while(it.hasNext()) {
        	int[] next = it.getPos();
        	assertFalse(Double.isNaN(dataset.getDouble(next)));
        }

		// Check axes
        final IPosition      pos = scanModel.getPositionIterable().iterator().next();
        final List<String> scannableNames = pos.getNames();
        final List<String> allNames = new ArrayList<>(scannableNames);
        allNames.addAll(monitorNames);

        // Append _value_demand to each name in scannable names list, and appends
        // the item "." 3 times to the resulting list
        String[] expectedAxesNames = Stream.concat(scannableNames.stream().map(x -> x + "_value_demand"),
        		Collections.nCopies(3, ".").stream()).toArray(String[]::new);
        assertAxes(nxData, expectedAxesNames);
        
        int[] defaultDimensionMappings = IntStream.range(0, sizes.length).toArray();
        for (int i = 0; i < allNames.size(); i++) {
        	String deviceName = allNames.get(i);
        	// This test uses NXpositioner for all scannables and monitors
        	NXpositioner positioner = instrument.getPositioner(deviceName);
        	assertNotNull(positioner);
        	
    		dataNode = positioner.getDataNode("value_demand");
    		dataset = dataNode.getDataset().getSlice();
    		shape = dataset.getShape();
			assertEquals(1, shape.length);
			if (i < scannableNames.size()) {
				// TODO what's the expected size for monitors?
				// maybe they just wouldn't have this field in practice
				assertEquals(sizes[i], shape[0]);
			}
			
			String nxDataFieldName = deviceName + "_value_demand";
			assertSame(dataNode, nxData.getDataNode(nxDataFieldName));
			assertIndices(nxData, nxDataFieldName, i);
			assertTarget(nxData, nxDataFieldName, rootNode,
					"/entry/instrument/" + deviceName + "/value_demand");
        
			// Actual values should be scanD
			dataNode = positioner.getDataNode(NXpositioner.NX_VALUE);
    		dataset = dataNode.getDataset().getSlice();
    		shape = dataset.getShape();
    		assertArrayEquals(sizes, shape);
    		
    		nxDataFieldName = deviceName + "_" + NXpositioner.NX_VALUE;
    		assertSame(dataNode, nxData.getDataNode(nxDataFieldName));
    		assertIndices(nxData, nxDataFieldName, defaultDimensionMappings);
    		assertTarget(nxData, nxDataFieldName, rootNode,
    				"/entry/instument/" + deviceName + "/" + NXpositioner.NX_VALUE);
		}
	}

	private IRunnableDevice<ScanModel> createNestedStepScanWithMonitors(final IRunnableDevice<?> detector, List<String> monitorNames, int... size) throws Exception {
		
		// Create scan points for a grid and make a generator
		StepModel smodel;
		int ySize = size[size.length-1];
		if (ySize-1>0) {
			smodel = new StepModel("yNex", 10,20,11d/ySize);
		} else {
			smodel = new StepModel("yNex", 10,20,30); // Will generate one value at 10
		}
		
		IPointGenerator<?,IPosition> gen = gservice.createGenerator(smodel);
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
				final IPointGenerator<?,IPosition> step = gservice.createGenerator(model);
				gen = gservice.createCompoundGenerator(step, gen);
			}
		}
	
		// Create the model for a scan.
		final ScanModel  scanModel = new ScanModel();
		scanModel.setPositionIterable(gen);
		scanModel.setDetectors(detector);
		scanModel.setMonitors(createMonitors(monitorNames));
		
		// Create a file to scan into.
		File output = File.createTempFile("test_mandel_nexus", ".nxs");
		output.deleteOnExit();
		scanModel.setFilePath(output.getAbsolutePath());
		System.out.println("File writing to "+scanModel.getFilePath());

		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = service.createRunnableDevice(scanModel, null);
		
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

	private List<IScannable<?>> createMonitors(List<String> monitorNames) throws ScanningException {
		final List<IScannable<?>> ret = new ArrayList<IScannable<?>>(monitorNames.size());
		for (String name : monitorNames) ret.add(connector.getScannable(name));
		return ret;
	}


	public static INexusFileFactory getFileFactory() {
		return fileFactory;
	}

	public static void setFileFactory(INexusFileFactory fileFactory) {
		MonitorPluginTest.fileFactory = fileFactory;
	}

}
