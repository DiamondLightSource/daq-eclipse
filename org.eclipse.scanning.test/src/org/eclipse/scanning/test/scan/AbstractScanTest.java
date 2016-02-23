package org.eclipse.scanning.test.scan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.scanning.api.ILevel;
import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.event.scan.IScanListener;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanEvent;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.AbstractPointsModel;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.api.scan.AbstractRunnableDevice;
import org.eclipse.scanning.api.scan.IDeviceConnectorService;
import org.eclipse.scanning.api.scan.IRunnableDevice;
import org.eclipse.scanning.api.scan.IDeviceService;
import org.eclipse.scanning.api.scan.IWritableDetector;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.eclipse.scanning.api.scan.event.IPositioner;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.test.scan.mock.MockDetectorModel;
import org.eclipse.scanning.test.scan.mock.MockScannable;
import org.junit.Test;

public class AbstractScanTest {

	protected IDeviceService              sservice;
	protected IDeviceConnectorService       connector;
	protected IPointGeneratorService        gservice;
	protected IEventService                 eservice;

	@Test
	public void testSetSimplePosition() throws Exception {

		IPositioner     pos    = sservice.createPositioner();
		pos.setPosition(new MapPosition("x:0:1, y:0:2"));
		
		assertTrue(connector.getScannable("x").getPosition().equals(1d));
		assertTrue(connector.getScannable("y").getPosition().equals(2d));
	}
	
	@Test
	public void testNames() throws ScanningException {
		List<String> names = connector.getScannableNames();
		assertTrue(names.contains("x"));
		assertTrue(names.contains("xNex"));
		assertTrue(names.contains("yNex"));
	}
	
	@Test
	public void testLevels() throws Exception {

		IPositioner     pos    = sservice.createPositioner();
		
		final List<String> scannablesMoved = new ArrayList<>(6);
		pos.addPositionListener(new IPositionListener.Stub() {
			@Override
			public void levelPerformed(PositionEvent evt) {
				System.out.println("Level complete "+evt.getLevel());
				for (INameable s : evt.getLevelObjects()) scannablesMoved.add(s.getName());
			}
		});
		
		pos.setPosition(new MapPosition("a:0:10, b:0:10, p:0:10, q:0:10, x:0:10, y:0:10"));
		
		assertTrue(scannablesMoved.get(0).equals("a") || scannablesMoved.get(0).equals("b"));
		assertTrue(scannablesMoved.get(1).equals("a") || scannablesMoved.get(1).equals("b"));
		assertTrue(scannablesMoved.get(2).equals("p") || scannablesMoved.get(2).equals("q"));
		assertTrue(scannablesMoved.get(3).equals("p") || scannablesMoved.get(3).equals("q"));
		assertTrue(scannablesMoved.get(4).equals("x") || scannablesMoved.get(4).equals("y"));
		assertTrue(scannablesMoved.get(5).equals("x") || scannablesMoved.get(5).equals("y"));
		
		for (String name : pos.getPosition().getNames()) {
			assertTrue(connector.getScannable(name).getPosition().equals(10d));
		}
	}
	
	@Test
	public void testMassiveMove() throws Exception {

		MapPosition pos = new MapPosition();
		for (int ilevel = 0; ilevel < 100; ilevel++) {
			for (int iscannable = 0; iscannable < 1000; iscannable++) {
				String name = "pos"+ilevel+"_"+iscannable;
				
				// We set the level in this loop, normally this comes
				// in via spring.
				IScannable<?> motor = connector.getScannable(name);
				motor.setLevel(ilevel);
				if (motor instanceof MockScannable) ((MockScannable)motor).setRequireSleep(false);
				
				// We set the position required
				pos.put(name, ilevel+iscannable);
 			}
		} 
		
		IPositioner positioner   = sservice.createPositioner();

		final List<String> levelsMoved = new ArrayList<>(6);
		positioner.addPositionListener(new IPositionListener.Stub() {
			@Override
			public void levelPerformed(PositionEvent evt) {
				System.out.println("Level complete "+evt.getLevel());
				for (ILevel s : evt.getLevelObjects()) {
					levelsMoved.add(String.valueOf(s.getLevel()));
				}
			}
		});

		long start = System.currentTimeMillis();
		positioner.setPosition(pos);
		long end   = System.currentTimeMillis();
		
		// Check the size
		assertTrue(levelsMoved.size()==100000);
		
		// Check that the level order was right
		final List<String> sorted = new ArrayList<String>(levelsMoved.size());
	    sorted.addAll(levelsMoved);
	    Collections.sort(sorted, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return Integer.parseInt(o1)-Integer.parseInt(o2);
			}
		});
		
	    for (int i = 0; i < levelsMoved.size(); i++) {
		    assertEquals("The wrong level was encountered sorted='"+sorted.get(i)+"' moved='"+levelsMoved.get(i)+"'", levelsMoved.get(i), sorted.get(i));
		}
	    
		System.out.println("Positioning 100,000 test motor with 100 levels took "+(end-start)+" ms");
	}
	
	@Test
	public void testSimpleScan() throws Exception {
				
		IRunnableDevice<ScanModel> scanner = createTestScanner(null, null, null, null, null);
		scanner.run(null);
		checkRun(scanner);
	}
	
	@Test
	public void testStepScan() throws Exception {
		
		StepModel model = new StepModel();
		model.setStart(0);
		model.setStop(10);
		model.setStep(1);
		model.setName("myStepModel");  // ScanningException thrown without this. TODO: Add test.
		
		IRunnableDevice<ScanModel> scanner = createTestScanner(model, null, null, null, null);
		scanner.run(null);
		checkRun(scanner);
	}
	
	@Test
	public void testInvalidStepScan() throws Exception {

		StepModel model = new StepModel();
		model.setStart(0);
		model.setStop(10);
		model.setStep(-1);
		model.setName("myStepModel");
		
		try {
			IRunnableDevice<ScanModel> scanner = createTestScanner(model, null, null, null, null);
			
			// Cast to AbstractRunnableDevice gives us non-blocking .start() method.
			((AbstractRunnableDevice<ScanModel>) scanner).start(null);
			
			Thread.sleep(5000);  // testStepScan (the valid one) takes ~2 seconds total.
			
		} catch (GeneratorException ex) {
			assertEquals("Invalid points model!", ex.getMessage());
			return;
		}
		
		throw new Exception("Generator failed to throw GeneratorException(\"Invalid model!\") on invalid step model input.");
	}

	@Test
	public void testZeroStepStepScan() throws Exception {

		StepModel model = new StepModel();
		model.setStart(0);
		model.setStop(10);
		model.setStep(0);
		model.setName("myStepModel");
		
		try {
			IRunnableDevice<ScanModel> scanner = createTestScanner(model, null, null, null, null);
			
			// Cast to AbstractRunnableDevice gives us non-blocking .start() method.
			((AbstractRunnableDevice<ScanModel>) scanner).start(null);
			
			Thread.sleep(5000);  // testStepScan (the valid one) takes ~2 seconds total.
			
		} catch (GeneratorException ex) {
			assertEquals("Invalid points model!", ex.getMessage());
			return;
		}
		
		throw new Exception("Generator failed to throw GeneratorException(\"Invalid model!\") on invalid step model input.");
	}
	
	
	@Test
	public void testScanError() throws Exception {
		
		// 1. Set the model to make the detector throw an exception
		MockDetectorModel dmodel = new MockDetectorModel();
		dmodel.setCollectionTime(0.1);
		dmodel.setAbortCount(3); // Aborts on the third write call by throwing an exception
		IWritableDetector<MockDetectorModel> detector = (IWritableDetector<MockDetectorModel>)sservice.createRunnableDevice(dmodel);
		
		// 2. Check run fails and check exception is that which the detector provided
		// Not some horrible reflection one.
		IRunnableDevice<ScanModel> scanner = createTestScanner(null, null, null, null, detector);
		boolean ok=false;
		try {
		    scanner.run(null);
		} catch (ScanningException expected) {
			if (!expected.getMessage().equals("The detector had a problem writing! This exception should stop the scan running!")) {
				throw new Exception("Expected the precise message from the mock detector not to be lost but it was! It was '"+expected.getMessage()+"'");
			}
			ok=true;
		}
		if (!ok) throw new Exception("The exception was not thrown by the scan as expected!");
		
		// 3. Check that it died after 3 and it is FAULT
		assertEquals(3, dmodel.getWritten());
		assertTrue(scanner.getDeviceState()==DeviceState.FAULT);
		
		// 4. Check that running it again fails
		ok=false;
		try {
			scanner.run(null);
		} catch (ScanningException expected) {
			ok=true;
		}
		if (!ok) throw new Exception("The exception was not thrown by the scan as expected!");
		
		// 5. Reset everything and see if can run ok
		scanner.reset();
		
		// Put the model back for the test and reconfigure
		dmodel.setRan(0);
		dmodel.setWritten(0);
		dmodel.setAbortCount(-1); // Solves the problem with the detector!
		detector.configure(dmodel);
		
		// Reconfigure - might need better way of doing?
		AbstractRunnableDevice<ScanModel> ascanner = (AbstractRunnableDevice<ScanModel>)scanner;
		scanner.configure(ascanner.getModel());
 		
		// Run again, should be ok.
		scanner.run(null);
		checkRun(scanner);
	}


	@Test
	public void testSimpleScanWithStatus() throws Exception {
			
		
		final ScanBean bean = new ScanBean();
		bean.setName("Fred");
		bean.setUniqueId("fred");
		
		final URI uri = new URI("tcp://sci-serv5.diamond.ac.uk:61616");	
		final IPublisher<ScanBean> publisher = eservice.createPublisher(uri, IEventService.STATUS_TOPIC);
		
		final ISubscriber<IScanListener> subscriber = eservice.createSubscriber(uri, IEventService.STATUS_TOPIC);
		final List<ScanBean>    events = new ArrayList<ScanBean>(11);
		final List<DeviceState> states = new ArrayList<DeviceState>(11);
		subscriber.addListener(new IScanListener.Stub() {		
			@Override
			public void scanStateChanged(ScanEvent evt) {
				states.add(evt.getBean().getDeviceState());
			}
			@Override
			public void scanEventPerformed(ScanEvent evt) {
				events.add(evt.getBean());
				System.out.println("State : "+evt.getBean().getDeviceState());
				System.out.println("Percent complete : "+evt.getBean().getPercentComplete());
				System.out.println(evt.getBean().getPosition());
			}
		});
		
		try {
			
			// Create a scan and run it without publishing events
			IRunnableDevice<ScanModel> scanner = createTestScanner(null, bean, publisher, null, null);
			scanner.run(null);
			
			Thread.sleep(1000); // Wait for all events to make it over from ActiveMQ
			
			checkRun(scanner);
			
			// Bit of a hack to get the generator from the model - should this be easier?
			IPointGenerator<?,IPosition> gen = (IPointGenerator<?,IPosition>)((ScanModel)((AbstractRunnableDevice)scanner).getModel()).getPositionIterable();
			assertEquals(gen.size(), events.size());
			assertEquals(Arrays.asList(DeviceState.READY, DeviceState.RUNNING, DeviceState.READY), states);
			
			for (ScanBean b : events) assertEquals("fred", b.getUniqueId());
		
		} finally {
			publisher.disconnect();
		}
	}

	@Test
	public void testSimpleScanSetPositionCalls() throws Exception {
			
		IScannable<Number> x = connector.getScannable("x");
		IRunnableDevice<ScanModel> scanner = createTestScanner(null, null, null, null, null);
		
		scanner.run(null);
		
		checkRun(scanner);
		
		// NOTE Did with Mockito but caused dependency issues.
		MockScannable ms = (MockScannable)x;
		ms.verify(0.3, new Point(0,0.3,0,0.3));
		ms.verify(1.5, new Point(0,1.5,2,0.3));
		ms.verify(0.3, new Point(2,0.3,0,1.5));
		ms.verify(1.5, new Point(2,1.5,2,1.5));
	}
	
	@Test
	public void testSimpleScanWithMonitor() throws Exception {
			
		IScannable<Number> monitor = connector.getScannable("monitor");
		IRunnableDevice<ScanModel> scanner = createTestScanner(null, null, null, monitor, null);
		
		scanner.run(null);
		
		checkRun(scanner);
		
		// NOTE Did with Mockito but caused dependency issues.
		MockScannable ms = (MockScannable)monitor;
		ms.verify(null, new Point(0,0.3,0,0.3));
		ms.verify(null, new Point(0,1.5,2,0.3));
		ms.verify(null, new Point(2,0.3,0,1.5));
		ms.verify(null, new Point(2,1.5,2,1.5));
	}

	private void checkRun(IRunnableDevice<ScanModel> scanner) throws Exception {
		// Bit of a hack to get the generator from the model - should this be easier?
		// Do not copy this code
		ScanModel smodel = (ScanModel)((AbstractRunnableDevice)scanner).getModel();
		IPointGenerator<?,IPosition> gen = (IPointGenerator<?,IPosition>)smodel.getPositionIterable();
		MockDetectorModel dmodel = (MockDetectorModel)((AbstractRunnableDevice)smodel.getDetectors().get(0)).getModel();
		assertEquals(gen.size(), dmodel.getRan());
		assertEquals(gen.size(), dmodel.getWritten());
	}

	private IRunnableDevice<ScanModel> createTestScanner(
			AbstractPointsModel pmodel,
			final ScanBean bean,
			final IPublisher<ScanBean> publisher,
			IScannable<?> monitor,
			IRunnableDevice<MockDetectorModel> detector
		) throws Exception {
		
		// Configure a detector with a collection time.
		if (detector == null) {
			MockDetectorModel dmodel = new MockDetectorModel();
			dmodel.setCollectionTime(0.1);
			dmodel.setName("detector");
			detector = sservice.createRunnableDevice(dmodel);
		}
		
		// If none passed, create scan points for a grid.
		if (pmodel == null) {
			pmodel = new GridModel();
			((GridModel) pmodel).setRows(5);
			((GridModel) pmodel).setColumns(5);
			((GridModel) pmodel).setBoundingBox(new BoundingBox(0,0,3,3));
		}
		
		IPointGenerator<?,IPosition> gen = gservice.createGenerator(pmodel);

		// Create the model for a scan.
		final ScanModel  smodel = new ScanModel();
		smodel.setPositionIterable(gen);
		smodel.setDetectors(detector);
		smodel.setBean(bean);
		if (monitor!=null) smodel.setMonitors(monitor);
		
		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = sservice.createRunnableDevice(smodel, publisher);
		return scanner;
	}

}
