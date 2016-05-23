package org.eclipse.scanning.test;

import java.net.URI;

import org.apache.activemq.broker.BrokerService;
import org.junit.After;
import org.junit.Before;

/**
 * Doing this works better than using vm:// uris.
 * 
 * Please do not use vm:// as it does not work when many tests are started and stopped
 * in a big unit testing system because each test uses the same in VM broker.
 *
 *
 *  TODO Should have static start of broker or per test start for problematic tests
 * 
 * @author Matthew Gerring.
 *
 */
public class BrokerTest {

	protected static final URI     uri = createUri();
	
	private BrokerService service;

	@Before
	public final void startBroker() throws Exception {
		
        service = new BrokerService();
        service.addConnector(uri);
        service.start();
		service.waitUntilStarted();
	}

	@After
	public final void stopBroker() throws Exception {
		
		service.stop();
		service.waitUntilStopped();
	}

	private static URI createUri() {
		try {
			return new URI("tcp://localhost:8619");
		} catch (Exception ne) {
			ne.printStackTrace();
		    return null;
		}
	}

}
