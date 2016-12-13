package org.eclipse.scanning.test.scan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IDeviceController;
import org.eclipse.scanning.api.device.IDeviceWatchdogService;
import org.eclipse.scanning.api.device.IPausableDevice;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.device.IWritableDetector;
import org.eclipse.scanning.api.event.IEventService;
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
import org.eclipse.scanning.sequencer.watchdog.DeviceWatchdogService;
import org.eclipse.scanning.server.servlet.Services;
import org.eclipse.scanning.test.ScanningTestClassRegistry;
import org.eclipse.scanning.test.scan.mock.MockDetectorModel;
import org.eclipse.scanning.test.scan.mock.MockWritableDetector;
import org.eclipse.scanning.test.scan.mock.MockWritingMandelbrotDetector;
import org.eclipse.scanning.test.scan.mock.MockWritingMandlebrotModel;
import org.junit.AfterClass;
import org.junit.Before;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

public abstract class AbstractWatchdogTest {

	protected IRunnableDeviceService        sservice;
	protected IScannableDeviceService       connector;
	protected IPointGeneratorService        gservice;
	protected IEventService                 eservice;
	protected IWritableDetector<MockDetectorModel>       detector;
	protected List<IPosition>               positions;


	abstract void createWatchdogs()  throws Exception;

	@Before
	public void setupServices() throws Exception {

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
		
		IDeviceWatchdogService wservice = new DeviceWatchdogService();
		ServiceHolder.setWatchdogService(wservice);
		Services.setWatchdogService(wservice);

		createWatchdogs();
	}

	
	@AfterClass
	public static void cleanup() throws Exception {
		ServiceHolder.setRunnableDeviceService(null);
		ServiceHolder.setWatchdogService(null);
	}

	
	protected IDeviceController createTestScanner(IScannable<?> monitor) throws Exception {
		
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

}
