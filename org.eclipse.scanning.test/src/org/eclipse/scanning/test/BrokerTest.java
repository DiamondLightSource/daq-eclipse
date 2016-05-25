package org.eclipse.scanning.test;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.URI;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.usage.SystemUsage;
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

	protected URI uri;     
	
	private BrokerService service;

	@Before
	public final void startBroker() throws Exception {
		uri = createUri(); // Each test uses a new port if the port is running on another test.
        service = new BrokerService();
        service.addConnector(uri);
        service.setPersistent(false); 
        SystemUsage systemUsage = service.getSystemUsage();
        systemUsage.getStoreUsage().setLimit(1024 * 1024 * 8);
        systemUsage.getTempUsage().setLimit(1024 * 1024 * 8);
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
			return new URI("tcp://localhost:"+getFreePort());
		} catch (Exception ne) {
			ne.printStackTrace();
		    return null;
		}
	}

	private static int getFreePort() {
		final int start = 8619+Math.round((float)Math.random()*100);
		return getFreePort(start);
	}

	
	private static int getFreePort(final int startPort) {

	    int port = startPort;
	    while(!isPortFree(port)) port++;

	    return port;
	}
	/**
	 * Checks if a port is free.
	 * @param port
	 * @return
	 */
	public static boolean isPortFree(int port) {

	    ServerSocket ss = null;
	    DatagramSocket ds = null;
	    try {
	        ss = new ServerSocket(port);
	        ss.setReuseAddress(true);
	        ds = new DatagramSocket(port);
	        ds.setReuseAddress(true);
	        return true;
	    } catch (IOException e) {
	    } finally {
	        if (ds != null) {
	            ds.close();
	        }

	        if (ss != null) {
	            try {
	                ss.close();
	            } catch (IOException e) {
	                /* should not be thrown */
	            }
	        }
	    }

	    return false;
	}


}
