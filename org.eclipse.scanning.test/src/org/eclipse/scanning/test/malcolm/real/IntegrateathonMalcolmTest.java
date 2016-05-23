package org.eclipse.scanning.test.malcolm.real;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.event.scan.IScanListener;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanEvent;
import org.eclipse.scanning.api.malcolm.IMalcolmConnection;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.IMalcolmService;
import org.eclipse.scanning.api.malcolm.models.TwoDetectorTestMappingModel;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.EmptyModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.malcolm.core.MalcolmService;
import org.eclipse.scanning.points.PointGeneratorFactory;
import org.eclipse.scanning.points.serialization.PointsModelMarshaller;
import org.eclipse.scanning.sequencer.RunnableDeviceServiceImpl;
import org.eclipse.scanning.test.BrokerTest;
import org.eclipse.scanning.test.scan.mock.MockScannableConnector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;
import uk.ac.diamond.malcolm.jacksonzeromq.connector.ZeromqConnectorService;

//@Ignore(" We use this test to specifically talk to an I05-1 device called 'arpes'")
public class IntegrateathonMalcolmTest extends BrokerTest {

	protected IMalcolmConnection         connection;
	protected IMalcolmDevice<TwoDetectorTestMappingModel>  device;
	protected IRunnableDeviceService             dservice;
	protected IPointGeneratorService     pservice;
	protected IPublisher<ScanBean>       publisher;
	protected ISubscriber<IScanListener> subscriber;
	protected int scanPoints;

	@Before
    public void create() throws Exception  {
		
		
		this.pservice   = new PointGeneratorFactory();

		ActivemqConnectorService.setJsonMarshaller(new MarshallerService(new PointsModelMarshaller()));
		IEventService eservice   = new EventServiceImpl(new ActivemqConnectorService());
		publisher  = eservice.createPublisher(uri, "test.malcolm.scanEventTopic");
		subscriber = eservice.createSubscriber(uri, "test.malcolm.scanEventTopic");
		
		scanPoints = 0;
		subscriber.addListener(new IScanListener() {
			@Override
			public void scanEventPerformed(ScanEvent evt) {
				System.out.println(evt.getBean());
				System.out.println("Scan point: "+evt.getBean().getPoint());
				scanPoints++;
			}
		});
		
		// Get the objects
		IMalcolmService service    = new MalcolmService(); 
		this.connection = service.createConnection(new URI("tcp://pc0013.cs.diamond.ac.uk:5600"), new ZeromqConnectorService());
		this.device     = connection.getDevice("lab", publisher);
		
		this.dservice  = new RunnableDeviceServiceImpl(new MockScannableConnector());
		DeviceState state = device.getDeviceState();
		if (state!=DeviceState.IDLE && state!=DeviceState.READY) device.reset();
    }

	@After
    public void dispose() throws Exception  {
		
		publisher.disconnect();
		subscriber.disconnect();
		
		DeviceState state = device.getDeviceState();
		if (state!=DeviceState.IDLE && state!=DeviceState.READY) {
		    device.abort();
		}
		if (state!=DeviceState.IDLE && state!=DeviceState.READY) device.reset();

		connection.dispose();
    }

	@Test
	public void testGetStatus() throws ScanningException {
		
		DeviceState status = device.getDeviceState();
		System.out.println(status);
	}
	
	@Test
	public void testConfigure() throws Exception {

		device.configure(createModel());
		System.out.println(device.getDeviceState());
	}

	private TwoDetectorTestMappingModel createModel() throws IOException {
		
		TwoDetectorTestMappingModel model = new TwoDetectorTestMappingModel();
		model.setxStart(-11);
		model.setxStop(-10);
		model.setxStep(0.5);
		model.setyStart(-4);
		model.setyStop(-3);
		model.setyStep(0.5);
		
		File file1 = File.createTempFile("det1_", ".h5");
		file1.deleteOnExit();
		model.setHdf5File1("/tmp/"+file1.getName());
		File file2 = File.createTempFile("det2_", ".h5");
		file2.deleteOnExit();
		model.setHdf5File2("/tmp/"+file2.getName());
		
		model.setDet1Exposure(0.1);
		model.setDet2Exposure(0.2);
		return model;
	}

	@Test
	public void testRun() throws Exception {

		TwoDetectorTestMappingModel model = createModel();
		
		device.configure(model);
		System.out.println(device.getDeviceState());
		
		device.run(null);
		
		System.out.println(device.getDeviceState());

	}
	
	@Test
	public void testRunInScanning() throws Exception {
		
		// We run this scan with a malcolm device.
		device.configure(createModel());
		
		final ScanModel  smodel = new ScanModel();
		// Sets the outer scan to none.
		smodel.setPositionIterable(pservice.createGenerator(new EmptyModel()));
		smodel.setDetectors(device);
		smodel.setBean(new ScanBean()); // Provides a unique id
		
		final File nxs = File.createTempFile("Test_scan_", ".nxs");
		nxs.deleteOnExit();
		smodel.setFilePath(nxs.getAbsolutePath());
		
		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = dservice.createRunnableDevice(smodel);
		scanner.run(null);
		
		assertEquals(10, scanPoints);
	}


}
