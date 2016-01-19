package org.eclipse.scanning.test.scan.real;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IGenerator;
import org.eclipse.scanning.api.points.IGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.StepModel;
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
import org.eclipse.scanning.test.scan.mock.MockScannableConnector;

/**
 * This class is an object which can be started by sprig on the GDA server.
 * 
 * I receives commands and runs a simple test scan.
 * 
 * @author fri44821
 *
 */
public class ScanExecutionTest {
	
	private static IEventService     eventService;
	private static IGeneratorService generatorService;
	private static IScanningService  scanService;
	private static IDeviceConnectorService connector;
	

	public static IDeviceConnectorService getConnector() {
		return connector;
	}

	public static void setConnector(IDeviceConnectorService connector) {
		ScanExecutionTest.connector = connector;
	}

	public ScanExecutionTest() {
		
	}
	
	/**
	 * 
	 * @param uri - for activemq, for instance tcp://sci-serv5.diamond.ac.uk:61616
	 * @throws URISyntaxException 
	 * @throws EventException 
	 */
	public ScanExecutionTest(String uri) throws URISyntaxException, EventException {
		this();
		ISubscriber<IBeanListener<TestScanBean>> sub = eventService.createSubscriber(new URI(uri), "org.eclipse.scanning.test.scan.real.test");
		sub.addListener(new IBeanListener<TestScanBean>() {
			@Override
			public void beanChangePerformed(BeanEvent<TestScanBean> evt) {
				try {
					executeTestScan(evt.getBean());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	protected void executeTestScan(TestScanBean bean) throws Exception {
				
		IWritableDetector<?> detector = connector.getDetector("swmr");
		assertNotNull(detector);
		detector.configure(null);
		
		detector.addRunListener(new IRunListener.Stub() {
			@Override
			public void runPerformed(RunEvent evt) throws ScanningException{
                System.out.println("Ran detector @ "+evt.getPosition());
			}
		});

		IRunnableDevice<ScanModel> scanner = createGridScan(detector, 8, 5); // Outer scan of another scannable, for instance temp.
		scanner.run(null);
		System.out.println("done");
	}
	
	private IRunnableDevice<ScanModel> createGridScan(final IRunnableDevice<?> detector, int... size) throws Exception {
		
		// Create scan points for a grid and make a generator
		GridModel gmodel = new GridModel();
		gmodel.setxName("smx");
		gmodel.setColumns(size[size.length-2]);
		gmodel.setyName("smy");
		gmodel.setRows(size[size.length-1]);
		gmodel.setBoundingBox(new BoundingBox(0,0,2,2));
		
		IGenerator<?,IPosition> gen = generatorService.createGenerator(gmodel);
		
		// We add the outer scans, if any
		if (size.length > 2) { 
			for (int dim = size.length-3; dim>-1; dim--) {
				final StepModel model;
				if (size[dim]-1>0) {
				    model = new StepModel("neXusScannable"+(dim+1), 10,20,11d/(size[dim]-1));
				} else {
					model = new StepModel("neXusScannable"+(dim+1), 10,20,30); // Will generate one value at 10
				}
				final IGenerator<?,IPosition> step = generatorService.createGenerator(model);
				gen = generatorService.createCompoundGenerator(step, gen);
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
		IRunnableDevice<ScanModel> scanner = scanService.createRunnableDevice(smodel, null, connector);
		
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

	public static IEventService getEventService() {
		return eventService;
	}

	public static void setEventService(IEventService eventService) {
		ScanExecutionTest.eventService = eventService;
	}

	public static IGeneratorService getGeneratorService() {
		return generatorService;
	}

	public static void setGeneratorService(IGeneratorService generatorService) {
		ScanExecutionTest.generatorService = generatorService;
	}

	public static IScanningService getScanService() {
		return scanService;
	}

	public static void setScanService(IScanningService scanService) {
		ScanExecutionTest.scanService = scanService;
	}
}
