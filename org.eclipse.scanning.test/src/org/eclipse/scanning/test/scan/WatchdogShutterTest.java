package org.eclipse.scanning.test.scan;

import static org.junit.Assert.assertEquals;
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
import org.eclipse.scanning.api.device.IDeviceController;
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
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.event.RunEvent;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.example.classregistry.ScanningExampleClassRegistry;
import org.eclipse.scanning.example.scannable.MockScannableConnector;
import org.eclipse.scanning.points.PointGeneratorService;
import org.eclipse.scanning.points.classregistry.ScanningAPIClassRegistry;
import org.eclipse.scanning.points.serialization.PointsModelMarshaller;
import org.eclipse.scanning.sequencer.RunnableDeviceServiceImpl;
import org.eclipse.scanning.sequencer.ServiceHolder;
import org.eclipse.scanning.sequencer.expression.ServerExpressionService;
import org.eclipse.scanning.sequencer.watchdog.DeviceWatchdogService;
import org.eclipse.scanning.sequencer.watchdog.ExpressionWatchdog;
import org.eclipse.scanning.test.ScanningTestClassRegistry;
import org.eclipse.scanning.test.scan.mock.MockDetectorModel;
import org.eclipse.scanning.test.scan.mock.MockWritableDetector;
import org.eclipse.scanning.test.scan.mock.MockWritingMandelbrotDetector;
import org.eclipse.scanning.test.scan.mock.MockWritingMandlebrotModel;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

public class WatchdogShutterTest {

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
		
		assertNotNull(connector.getScannable("beamcurrent"));
		assertNotNull(connector.getScannable("portshutter"));

		ExpressionWatchdog.setTestExpressionService(new ServerExpressionService());

		IDeviceWatchdogService wservice = new DeviceWatchdogService();
		ServiceHolder.setWatchdogService(wservice);

		// We create a device watchdog (done in spring for real server)
		DeviceWatchdogModel model = new DeviceWatchdogModel();
		model.setExpression("beamcurrent >= 1.0 && !portshutter.equalsIgnoreCase(\"Closed\")");
		
		this.dog = new ExpressionWatchdog(model);
		dog.activate();
	}
	
	
	@AfterClass
	public static void cleanup() throws Exception {
		ServiceHolder.setRunnableDeviceService(null);
		ServiceHolder.setWatchdogService(null);
	}
	
	
	@Test
	public void beamLostInScan() throws Exception {

		// x and y are level 3
		IDeviceController controller = createTestScanner(null);
		IRunnableEventDevice<?> scanner = (IRunnableEventDevice<?>)controller.getDevice();
		
		List<DeviceState> states = new ArrayList<>();
		// This run should get paused for beam and restarted.
		scanner.addRunListener(new IRunListener() {
			public void stateChanged(RunEvent evt) throws ScanningException {
				states.add(evt.getDeviceState());
			}
		});
		
		scanner.start(null);
		
		final IScannable<Number>   mon  = connector.getScannable("beamcurrent");
		mon.setPosition(0.1);
		Thread.sleep(100);
		
		assertTrue(states.get(states.size()-1)==DeviceState.PAUSED);
		
		mon.setPosition(2.1);
		Thread.sleep(100);
		assertTrue(states.get(states.size()-1)==DeviceState.RUNNING);

	}
	
	@Test
	public void shutterClosedInScan() throws Exception {

		// x and y are level 3
		IDeviceController controller = createTestScanner(null);
		IRunnableEventDevice<?> scanner = (IRunnableEventDevice<?>)controller.getDevice();
		
		List<DeviceState> states = new ArrayList<>();
		// This run should get paused for beam and restarted.
		scanner.addRunListener(new IRunListener() {
			public void stateChanged(RunEvent evt) throws ScanningException {
				states.add(evt.getDeviceState());
			}
		});
		
		scanner.start(null);
		
		final IScannable<String>   mon  = connector.getScannable("portshutter");
		mon.setPosition("Closed");
		Thread.sleep(100);
		
		assertTrue(states.get(states.size()-1)==DeviceState.PAUSED);
		
		mon.setPosition("Open");
		Thread.sleep(100);
		assertTrue(states.get(states.size()-1)==DeviceState.RUNNING);

	}

	
	private IDeviceController createTestScanner(IScannable<?> monitor) throws Exception {
		
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
		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = sservice.createRunnableDevice(smodel, null, false);
		IDeviceController controller = ServiceHolder.getWatchdogService().create((IPausableDevice<?>)scanner);
		smodel.setAnnotationParticipants(controller.getObjects());
		scanner.configure(smodel);
		
		return controller;
	}

	
	@Test
	public void monitorWithExternalPauseSimple() throws Exception {

		// x and y are level 3
		IDeviceController controller = createTestScanner(null);
		IRunnableEventDevice<?> scanner = (IRunnableEventDevice<?>)controller.getDevice();
		
		Set<DeviceState> states = new HashSet<>();
		// This run should get paused for beam and restarted.
		scanner.addRunListener(new IRunListener() {
			public void stateChanged(RunEvent evt) throws ScanningException {
				states.add(evt.getDeviceState());
			}
		});
		
		scanner.start(null);
		controller.pause("test", null);  // Pausing externally should override any watchdog resume.
		
		final IScannable<String>   mon  = connector.getScannable("portshutter");
		mon.setPosition("Closed");
		mon.setPosition("Open");
		Thread.sleep(100); // Watchdog should not start it again, it was paused first..
		assertEquals(scanner.getDeviceState(), DeviceState.PAUSED);
		
		controller.abort("test");
		
		mon.setPosition("Closed");
		assertNotEquals(scanner.getDeviceState(), DeviceState.PAUSED);
	}
	
	@Test
	public void monitorWithExternalPauseComplex() throws Exception {

		// x and y are level 3
		IDeviceController controller = createTestScanner(null);
		IRunnableEventDevice<?> scanner = (IRunnableEventDevice<?>)controller.getDevice();
		
		Set<DeviceState> states = new HashSet<>();
		// This run should get paused for beam and restarted.
		scanner.addRunListener(new IRunListener() {
			public void stateChanged(RunEvent evt) throws ScanningException {
				states.add(evt.getDeviceState());
			}
		});
		
		scanner.start(null);
		controller.pause("test", null);  // Pausing externally should override any watchdog resume.
		
		final IScannable<String>   mon  = connector.getScannable("portshutter");
		mon.setPosition("Closed");
		mon.setPosition("Open");
		Thread.sleep(100); // Watchdog should not start it again, it was paused first..
		assertEquals(DeviceState.PAUSED, scanner.getDeviceState());
		
		controller.resume("test");
		
		Thread.sleep(100); 
		assertNotEquals(DeviceState.PAUSED, scanner.getDeviceState());

		mon.setPosition("Closed");
		
		Thread.sleep(100); 
		assertEquals(DeviceState.PAUSED, scanner.getDeviceState());
		
		controller.resume("test"); // The external resume should still not resume it

		Thread.sleep(100);
		assertEquals(DeviceState.PAUSED, scanner.getDeviceState());

		controller.abort("test");
	}

}
