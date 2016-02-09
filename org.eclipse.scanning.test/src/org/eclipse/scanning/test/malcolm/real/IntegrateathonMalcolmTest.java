package org.eclipse.scanning.test.malcolm.real;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.malcolm.IMalcolmConnection;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.IMalcolmService;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnectorService;
import org.eclipse.scanning.api.malcolm.message.JsonMessage;
import org.eclipse.scanning.api.malcolm.models.TwoDetectorTestMappingModel;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.EmptyModel;
import org.eclipse.scanning.api.scan.IDeviceService;
import org.eclipse.scanning.api.scan.IRunnableDevice;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.malcolm.core.MalcolmService;
import org.eclipse.scanning.points.PointGeneratorFactory;
import org.eclipse.scanning.sequencer.DeviceServiceImpl;
import org.eclipse.scanning.test.scan.mock.MockScannableConnector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.malcolm.jacksonzeromq.connector.ZeromqConnectorService;

//@Ignore(" We use this test to specifically talk to an I05-1 device called 'arpes'")
public class IntegrateathonMalcolmTest {

	protected IMalcolmService        service;
	protected IMalcolmConnection     connection;
	protected IMalcolmDevice<TwoDetectorTestMappingModel>  device;
	protected IMalcolmConnectorService<JsonMessage> connectorService;
	protected IDeviceService         dservice;
	protected IPointGeneratorService pservice;

	@Before
    public void create() throws Exception  {
		
		final URI uri = new URI("tcp://pc0013.cs.diamond.ac.uk:5600");
		
		this.service    = new MalcolmService(); 
		
		this.pservice   = new PointGeneratorFactory();
			
		// Get the objects
		this.connectorService = new ZeromqConnectorService();
		this.connection = service.createConnection(uri, connectorService);
		this.device     = connection.getDevice("lab");

		dservice  = new DeviceServiceImpl(new MockScannableConnector());
     
    }

	@After
    public void dispose() throws Exception  {
		try {
			device.abort();
		} catch (Exception ne) {
			throw ne;
		}
		try {
			device.reset();
		} catch (Exception ne) {
			throw ne;
		}
		connection.dispose();
    }

	@Test
	public void testGetStatus() throws ScanningException {
		
		DeviceState status = device.getState();
		System.out.println(status);
	}
	
	// TODO Tests which call validate and check that the size
	// of the point list is reasonable.
	
	@Test
	public void testConfigure() throws Exception {

		
		device.configure(createModel());
		
		System.out.println(device.getState());
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
		model.setHdf5File1(file1.getAbsolutePath());
		File file2 = File.createTempFile("det2_", ".h5");
		file2.deleteOnExit();
		model.setHdf5File2(file2.getAbsolutePath());
		
		model.setDet1Exposure(0.1);
		model.setDet2Exposure(0.2);
		return model;
	}

	@Test
	public void testRun() throws Exception {

		TwoDetectorTestMappingModel model = createModel();
		
		device.configure(model);
		System.out.println(device.getState());

		device.run(null);
		
		System.out.println(device.getState());

	}
	
	@Test
	public void testRunInScanning() throws Exception {
		
		// We run this scan with a malcolm device.
		device.configure(createModel());
		
		final ScanModel  smodel = new ScanModel();
		smodel.setPositionIterable(pservice.createGenerator(new EmptyModel())); // We run the scan entirely with Malcolm
		smodel.setDetectors(device);
		smodel.setBean(new ScanBean()); // Provides a unique id
		
		final File nxs = File.createTempFile("Test_scan_", ".nxs");
		nxs.deleteOnExit();
		smodel.setFilePath(nxs.getAbsolutePath());
		
		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = dservice.createRunnableDevice(smodel);
		scanner.run(null);
	}


}
