package org.eclipse.scanning.test.malcolm.real;

import java.net.URI;
import java.util.Map;

import org.eclipse.scanning.api.malcolm.IMalcolmConnection;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.IMalcolmService;
import org.eclipse.scanning.malcolm.core.MalcolmService;
import org.eclipse.scanning.test.malcolm.AbstractCommunicationMalcolmTest;
import org.junit.After;
import org.junit.Before;

import uk.ac.diamond.malcom.jacksonzeromq.connector.ZeromqConnectorService;

public class RealCommunicationMalcolmTest extends AbstractCommunicationMalcolmTest {

	@Before
    public void create() throws Exception  {
		
		final URI uri = new URI("tcp://ws157.diamond.ac.uk:5600");
		
		// The real service, get it from OSGi outside this test!
		// Not required in OSGi mode (do not add this to your real code GET THE SERVICE FROM OSGi!)
		this.service    = new MalcolmService(); 
			
		// Get the objects
		this.connectorService = new ZeromqConnectorService();
		this.connection = service.createConnection(uri, connectorService);
		this.device     = connection.getDevice("det");

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

	@Override
	protected void createParameters(Map<String, Object> config, long configureSleep, int imageCount) throws Exception {
		
		// Params for driving mock mode
		config.put("nframes", imageCount); // IMAGE_COUNT images to write		
		
		// The exposure is in seconds and we assume ms
		config.put("exposure", 0.5);
		
		double csleep = configureSleep/1000d;
		if (configureSleep>-1) config.put("configureSleep", csleep); // Sleeps during configure

	}

	@Override
	protected IMalcolmDevice createAdditionalConnection() throws Exception {
		final URI uri = new URI("tcp://ws157.diamond.ac.uk:5600");
		IMalcolmConnection aconnection   = service.createConnection(uri, new ZeromqConnectorService());
		return aconnection.getDevice("det");
	}

}
