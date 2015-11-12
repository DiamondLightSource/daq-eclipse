package org.eclipse.malcolm.test.real;

import java.net.URI;
import java.util.Map;

import org.eclipse.malcolm.api.IMalcolmConnection;
import org.eclipse.malcolm.api.IMalcolmDevice;
import org.eclipse.malcolm.api.IMalcolmService;
import org.eclipse.malcolm.core.MalcolmService;
import org.eclipse.malcolm.test.AbstractPausingMalcolmTest;
import org.junit.After;
import org.junit.Before;

import uk.ac.diamond.malcom.jacksonzeromq.connector.ZeromqConnectorService;

public class RealPausingMalcolmTest extends AbstractPausingMalcolmTest {

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

	@Override
	protected IMalcolmDevice createAdditionalConnection() throws Exception {
		final URI uri = new URI("tcp://ws157.diamond.ac.uk:5600");
		IMalcolmConnection aconnection   = service.createConnection(uri, new ZeromqConnectorService());
		return aconnection.getDevice("det");
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
		
		if (configureSleep>-1) {
			double csleep = configureSleep/1000d;
			config.put("configureSleep", csleep); // Sleeps during configure
		}

	}
	
}
