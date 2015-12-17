package org.eclipse.scanning.test.malcolm.real;

import java.net.URI;

import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.malcolm.IMalcolmConnection;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.IMalcolmService;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnectorService;
import org.eclipse.scanning.api.malcolm.message.JsonMessage;
import org.eclipse.scanning.api.malcolm.models.MappingModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.malcolm.core.MalcolmService;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import uk.ac.diamond.malcolm.jacksonzeromq.connector.ZeromqConnectorService;

//@Ignore(" We use this test to specifically talk to an I05-1 device called 'arpes'")
public class I05_1MalcolmTest {

	protected IMalcolmService    service;
	protected IMalcolmConnection connection;
	protected IMalcolmDevice<MappingModel>  device;
	protected IMalcolmConnectorService<JsonMessage> connectorService;

	@Before
    public void create() throws Exception  {
		
		final URI uri = new URI("tcp://i05-1-ws001.diamond.ac.uk:5600");
		
		// The real service, get it from OSGi outside this test!
		// Not required in OSGi mode (do not add this to your real code GET THE SERVICE FROM OSGi!)
		this.service    = new MalcolmService(); 
			
		// Get the objects
		this.connectorService = new ZeromqConnectorService();
		this.connection = service.createConnection(uri, connectorService);
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
	public void testGetStatus() throws ScanningException {
		
		DeviceState status = device.getState();
		System.out.println(status);
	}
	
	// TODO Tests which call validate and check that the size
	// of the point list is reasonable.
	
	@Test
	public void testConfigure() throws ScanningException {

		MappingModel model = new MappingModel();
		model.setxStart(0);
		model.setxStop(0.5);
		model.setxStep(0.05);
		model.setyStart(0);
		model.setyStop(0.1);
		model.setyStep(0.02);
		model.setExposure(0.075);
		model.setHdf5File("/tmp/foobar.h5");
		
		device.configure(model);
		
		System.out.println(device.getState());
	}

	@Test
	public void testRun() throws ScanningException, InterruptedException {

		MappingModel model = new MappingModel();
		model.setxStart(0);
		model.setxStop(0.5);
		model.setxStep(0.05);
		model.setyStart(0);
		model.setyStop(0.1);
		model.setyStep(0.02);
		model.setExposure(0.075);
		model.setHdf5File("/tmp/foobar.h5");
		
		device.configure(model);
		System.out.println(device.getState());

		device.run();
		
		System.out.println(device.getState());

	}

}
