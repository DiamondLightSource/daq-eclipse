package org.eclipse.scanning.test.scan;

import java.net.URI;

import org.eclipse.scanning.api.malcolm.IMalcolmConnection;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.IMalcolmService;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnectorService;
import org.eclipse.scanning.api.malcolm.message.JsonMessage;
import org.eclipse.scanning.api.malcolm.models.MappingModel;
import org.eclipse.scanning.api.points.IGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.api.scan.IRunnableDevice;
import org.eclipse.scanning.api.scan.IScanningService;
import org.eclipse.scanning.api.scan.ScanModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.event.RunEvent;
import org.eclipse.scanning.malcolm.core.MalcolmService;
import org.eclipse.scanning.points.GeneratorServiceImpl;
import org.eclipse.scanning.sequencer.ScanningServiceImpl;
import org.eclipse.scanning.test.scan.mock.MockScannableConnector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.malcolm.jacksonzeromq.connector.ZeromqConnectorService;

public class ARPESScanTest {

	protected IMalcolmService                       mservice;
	protected IMalcolmConnection                    connection;
	protected IMalcolmDevice<MappingModel>          device;
	protected IMalcolmConnectorService<JsonMessage> connectorService;
	protected IGeneratorService                     gservice;
	protected IScanningService                      sservice;

	
	/**
	 * THESE SERVICES ARE HARD CODED - DO NOT COPY, GET THEM FROM OSGi
	 * IN REAL CODE!
	 * 
	 * @throws Exception
	 */
	@Before
    public void create() throws Exception  {
		
		final URI uri = new URI("tcp://i05-1-ws001.diamond.ac.uk:5600");
		
		// The real service, get it from OSGi outside this test!
		// Not required in OSGi mode (do not add this to your real code GET THE SERVICE FROM OSGi!)
		this.mservice    = new MalcolmService(); 
		this.gservice    = new GeneratorServiceImpl();
		this.sservice    = new ScanningServiceImpl();
			
		// Get the objects
		this.connectorService = new ZeromqConnectorService();
		this.connection = mservice.createConnection(uri, connectorService);
		this.device     = connection.getDevice("arpes");

    }

	@After
    public void dispose() throws Exception  {
		// Try to leave it good to avoid wrong failures.
		try {
		    device.abort();
		} catch (Exception allowed) {
			
		}
		try {
			device.reset();
		} catch (Exception allowed) {
			
		}
		connection.dispose();
    }

	@Test
	public void testSimpleScan() throws Exception {
	

		final MappingModel model = new MappingModel();
		model.setxStart(0);
		model.setxStop(0.5);
		model.setxStep(0.05);
		model.setyStart(0);
		model.setyStop(0.1);
		model.setyStep(0.02);
		model.setExposure(0.01);

		device.addRunListener(new IRunListener.Stub() {
			@Override
			public void runWillPerform(RunEvent evt) throws ScanningException {
				final IPosition pos = evt.getPosition();
				model.setHdf5File("/tmp/foobar_temp"+pos.get("temperature")+".h5");
				device.configure(model);
			}			
		});
		
		Iterable<IPosition>      points = gservice.createGenerator(new StepModel("temperature", 290, 300, 1));
		IRunnableDevice<ScanModel> scan = sservice.createRunnableDevice(new ScanModel(points, device), null,  new MockScannableConnector());
		scan.run();
	}

	/**
	 * 
	 * 	
	// TODO write python generators.
	@Test
	public void testScanWithPythonGenerator() throws Exception {
	

		final MappingModel model = new MappingModel();
		model.setxStart(0);
		model.setxStop(0.5);
		model.setxStep(0.05);
		model.setyStart(0);
		model.setyStop(0.1);
		model.setyStep(0.02);
		model.setExposure(0.01);
		model.setGenerator("tempScan1");
		
		// Create generator on the server
		ds = malcolm.getDevice("DirectoryService");
		ds.createStepGenerator("tempScan1");
		ts1 = malcolm.getDevice("tempScan1");
		ts1.configure(290, 300, 1);
		device.configure(model);

//		device.addRunListener(new IRunListener.Stub() {
//			@Override
//			public void runWillPerform(RunEvent evt) throws ScanningException {
//				final IPosition pos = evt.getPosition();
//				model.setHdf5File("/tmp/foobar_temp"+pos.get("temperature")+".h5");
//				device.configure(model);
//			}			
//		});
		
		Iterable<IPosition>      points = gservice.createGenerator(new StepModel("temperature", 290, 300, 1));
		IRunnableDevice<ScanModel> scan = sservice.createRunnableDevice(new ScanModel(points, device), null,  new MockScannableConnector());
		scan.run();
	}
	 */
}
