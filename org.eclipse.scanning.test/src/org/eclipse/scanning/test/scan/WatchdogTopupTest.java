package org.eclipse.scanning.test.scan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IDeviceWatchdog;
import org.eclipse.scanning.api.device.IDeviceWatchdogService;
import org.eclipse.scanning.api.device.IPausableDevice;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.IRunnableEventDevice;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.device.IWritableDetector;
import org.eclipse.scanning.api.device.models.DeviceWatchdogModel;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositionListenable;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.event.RunEvent;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.example.classregistry.ScanningExampleClassRegistry;
import org.eclipse.scanning.example.scannable.MockScannable;
import org.eclipse.scanning.example.scannable.MockScannableConnector;
import org.eclipse.scanning.example.scannable.MockTopupScannable;
import org.eclipse.scanning.points.PointGeneratorService;
import org.eclipse.scanning.points.classregistry.ScanningAPIClassRegistry;
import org.eclipse.scanning.points.serialization.PointsModelMarshaller;
import org.eclipse.scanning.sequencer.RunnableDeviceServiceImpl;
import org.eclipse.scanning.sequencer.ServiceHolder;
import org.eclipse.scanning.sequencer.watchdog.DeviceWatchdogService;
import org.eclipse.scanning.sequencer.watchdog.TopupWatchdog;
import org.eclipse.scanning.server.servlet.Services;
import org.eclipse.scanning.test.ScanningTestClassRegistry;
import org.eclipse.scanning.test.scan.mock.MockDetectorModel;
import org.eclipse.scanning.test.scan.mock.MockWritableDetector;
import org.eclipse.scanning.test.scan.mock.MockWritingMandelbrotDetector;
import org.eclipse.scanning.test.scan.mock.MockWritingMandlebrotModel;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

public class WatchdogTopupTest {

	protected IRunnableDeviceService        sservice;
	protected IScannableDeviceService       connector;
	protected IPointGeneratorService        gservice;
	protected IEventService                 eservice;
	private   IWritableDetector<MockDetectorModel>       detector;
	
	private List<IPosition>                 positions;
	private IDeviceWatchdog dog;

	@Before
	public void setup() throws ScanningException {

		// We wire things together without OSGi here 
		// DO NOT COPY THIS IN NON-TEST CODE!
		ActivemqConnectorService.setJsonMarshaller(new MarshallerService(
				Arrays.asList(new ScanningAPIClassRegistry(),
						new ScanningExampleClassRegistry(),
						new ScanningTestClassRegistry()),
				Arrays.asList(new PointsModelMarshaller())
				));
		eservice  = new EventServiceImpl(new ActivemqConnectorService());

		connector = new MockScannableConnector(null);
		sservice  = new RunnableDeviceServiceImpl(connector);
		RunnableDeviceServiceImpl impl = (RunnableDeviceServiceImpl)sservice;
		impl._register(MockDetectorModel.class, MockWritableDetector.class);
		impl._register(MockWritingMandlebrotModel.class, MockWritingMandelbrotDetector.class);
		ServiceHolder.setRunnableDeviceService(sservice);

		gservice  = new PointGeneratorService();
		
		MockDetectorModel dmodel = new MockDetectorModel();
		dmodel.setExposureTime(0.05);
		dmodel.setName("detector");
		detector = (IWritableDetector<MockDetectorModel>) sservice.createRunnableDevice(dmodel);
		
		positions = new ArrayList<>(20);
		detector.addRunListener(new IRunListener() {
			@Override
			public void runPerformed(RunEvent evt) throws ScanningException{
                System.out.println("Ran mock detector @ "+evt.getPosition());
                positions.add(evt.getPosition());
			}
		});
		
		final IScannable<Number>   topups  = connector.getScannable("topup");
		final MockTopupScannable   topup   = (MockTopupScannable)topups;
		assertNotNull(topup);
		topup.start();

		IDeviceWatchdogService wservice = new DeviceWatchdogService();
		ServiceHolder.setWatchdogService(wservice);
		Services.setWatchdogService(wservice);

		// We create a device watchdog (done in spring for real server)
		DeviceWatchdogModel model = new DeviceWatchdogModel();
		model.setCountdownName("topup");
		model.setCooloff(500); // Pause 500ms before
		model.setWarmup(200);  // Unpause 200ms after
		
		this.dog = new TopupWatchdog(model);
		dog.activate();
	}
	
	@After
	public void disconnect() throws ScanningException {
		final IScannable<Number>   topups  = connector.getScannable("topup");
		final MockTopupScannable   topup   = (MockTopupScannable)topups;
		assertNotNull(topup);
		topup.disconnect();
	}
	
	@AfterClass
	public static void cleanup() throws Exception {
		ServiceHolder.setRunnableDeviceService(null);
		ServiceHolder.setWatchdogService(null);
	}
	
	@Test(expected=Exception.class)
	public void testBeamOn() throws Exception {
		
		final IScannable<Number>   beamon   = connector.getScannable("beamon");
		beamon.setLevel(1);
		
		// x and y are level 3
		IRunnableDevice<ScanModel> scanner = createTestScanner(beamon);
		scanner.run(null);
		
		assertEquals(10, positions.size());
	}

	
	private IRunnableDevice<ScanModel> createTestScanner(IScannable<?> monitor) throws Exception {
		
		// Create scan points for a grid and make a generator
		GridModel gmodel = new GridModel("x", "y");
		gmodel.setSlowAxisPoints(5);
		gmodel.setFastAxisPoints(5);
		gmodel.setBoundingBox(new BoundingBox(0,0,3,3));	
		IPointGenerator<?> gen = gservice.createGenerator(gmodel);

		// Create the model for a scan.
		final ScanModel  smodel = new ScanModel();
		smodel.setPositionIterable(gen);
		smodel.setDetectors(detector);
		if (monitor!=null) smodel.setMonitors(monitor);
		
		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = sservice.createRunnableDevice(smodel, null, false);
		List<IDeviceWatchdog> dogs = ServiceHolder.getWatchdogService().create((IPausableDevice<?>)scanner);
		smodel.setAnnotationParticipants(dogs);

		scanner.configure(smodel);

		return scanner;
	}

	@Test
	public void topupPeriod() throws Exception {
		
		final IScannable<Number>   topups  = connector.getScannable("topup");
		final MockTopupScannable   topup   = (MockTopupScannable)topups;
		assertNotNull(topup);
		
		long fastPeriod = 500;
		long orig = topup.getPeriod();
		topup.setPeriod(fastPeriod);
		
		try {
			int max = Integer.MIN_VALUE;
			int min = Integer.MAX_VALUE;
			// We start a check the topup value for 15s
			long start = System.currentTimeMillis();
			while((System.currentTimeMillis()-start)<(fastPeriod*2.1)) {
				int pos = topup.getPosition().intValue();
				max = Math.max(max, pos);
				min = Math.min(min, pos);
				Thread.sleep(fastPeriod/10);
			}
			assertTrue(max<600&&max>300);
			assertTrue(min<200&&min>-1);
			
		} finally {
			topup.setPeriod(orig);

		}
	}
	
	@Test
	public void topupInScan() throws Exception {

		// x and y are level 3
		IRunnableEventDevice<ScanModel> scanner = (IRunnableEventDevice<ScanModel>)createTestScanner(null);
		
		Set<DeviceState> states = new HashSet<>();
		// This run should get paused for beam and restarted.
		scanner.addRunListener(new IRunListener() {
			public void stateChanged(RunEvent evt) throws ScanningException {
				states.add(evt.getDeviceState());
			}
		});
		
		scanner.run(null);
		
		assertTrue(states.contains(DeviceState.PAUSED));
		assertTrue(states.contains(DeviceState.RUNNING));
		assertTrue(states.contains(DeviceState.SEEKING));
	}
	
	@Test
	public void topupWithExternalPause() throws Exception {

		// Stop topup, we want to controll it programmatically.
		final IScannable<Number>   topups  = connector.getScannable("topup");
		final MockTopupScannable   topup   = (MockTopupScannable)topups;
		assertNotNull(topup);
		topup.disconnect();
		Thread.sleep(120); // Make sure it stops, it sets value every 100ms but it should get interrupted
		assertTrue(topup.isDisconnected());
		topup.setPosition(5000);

		// x and y are level 3
		IRunnableEventDevice<ScanModel> scanner = (IRunnableEventDevice<ScanModel>)createTestScanner(null);
		
		Set<DeviceState> states = new HashSet<>();
		// This run should get paused for beam and restarted.
		scanner.addRunListener(new IRunListener() {
			public void stateChanged(RunEvent evt) throws ScanningException {
				states.add(evt.getDeviceState());
			}
		});
		
		scanner.start(null);
		Thread.sleep(25);  // Do a bit
		scanner.pause();   // Pausing externally should override any watchdog resume.
		
		topup.setPosition(0);    // Should do nothing, device is already paused
		topup.setPosition(5000); // Should not resume, device was already paused
		
		Thread.sleep(100);       // Ensure watchdog event has fired and it did something.		
		assertEquals(DeviceState.PAUSED, scanner.getDeviceState());
		
		scanner.resume();
		
		Thread.sleep(10);       	
		assertNotEquals(DeviceState.PAUSED, scanner.getDeviceState());

System.out.println("SEtting topup");
		topup.setPosition(0);

		Thread.sleep(100);       
		assertEquals(DeviceState.PAUSED, scanner.getDeviceState());
		
		scanner.resume(); // It should not, topup is 0!

		Thread.sleep(25);       // Ensure watchdog event has fired and it did something.		
		assertEquals(DeviceState.PAUSED, scanner.getDeviceState());

		scanner.abort();
	}

	
	@Test
	public void topupOutScan() throws Exception {

		try {
			dog.deactivate(); // Are a testing a pausing monitor here

			// x and y are level 3
			IRunnableEventDevice<ScanModel> scanner = (IRunnableEventDevice<ScanModel>)createTestScanner(null);
			
			List<DeviceState> states = new ArrayList<>();
			// This run should get paused for beam and restarted.
			scanner.addRunListener(new IRunListener() {
				public void stateChanged(RunEvent evt) throws ScanningException {
					states.add(evt.getDeviceState());
				}
			});
			
			scanner.run(null);
			
			assertFalse(states.contains(DeviceState.PAUSED));
			assertTrue(states.contains(DeviceState.RUNNING));
			assertFalse(states.contains(DeviceState.SEEKING));
			
		} finally {
			dog.activate();
		}
	}
	
	@Test
	public void testPause() throws Exception {
		
		try {
			dog.deactivate(); // Are a testing a pausing monitor here
			detector.getModel().setExposureTime(0.0001); // Save some scan time.

			final List<String> moved   = new ArrayList<>();
			final IScannable<Number>   pauser   = connector.getScannable("pauser");
			if (pauser instanceof MockScannable) {
				((MockScannable)pauser).setRealisticMove(false);
			}
			((IPositionListenable)pauser).addPositionListener(new IPositionListener() {
				@Override
				public void positionPerformed(PositionEvent evt) {
					moved.add(pauser.getName());
				}
			});
			final IScannable<Number>   x       = connector.getScannable("x");
			if (x instanceof MockScannable) {
				((MockScannable)x).setRealisticMove(false);
			}
			((IPositionListenable)x).addPositionListener(new IPositionListener() {
				@Override
				public void positionPerformed(PositionEvent evt) {
					moved.add(x.getName());
				}
			});
			pauser.setLevel(1);
			
			// x and y are level 3
			IRunnableDevice<ScanModel> scanner = createTestScanner(pauser);
			scanner.run(null);
			
			assertEquals(25, positions.size());
			assertEquals(50, moved.size());
			assertTrue(moved.get(0).equals("pauser"));
			assertTrue(moved.get(1).equals("x"));
			
			moved.clear();
			positions.clear();
			pauser.setLevel(5); // Above x
			scanner.run(null);
			
			assertEquals(25, positions.size());
			assertEquals(50, moved.size());
			assertTrue(moved.get(0).equals("x"));
			assertTrue(moved.get(1).equals("pauser"));
		
		} finally {
			dog.activate();
			detector.getModel().setExposureTime(0.25);
		}
	}

}
