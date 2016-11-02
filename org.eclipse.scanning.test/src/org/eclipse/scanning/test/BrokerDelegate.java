package org.eclipse.scanning.test;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.URI;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.usage.SystemUsage;

public class BrokerDelegate {

	private URI           uri;     
	private BrokerService service;

	public void start() throws Exception {
		uri = createUri(); // Each test uses a new port if the port is running on another test.
		System.setProperty("org.eclipse.scanning.broker.uri", uri.toString());
        service = new BrokerService();
        service.addConnector(uri);
        service.setPersistent(false); 
        SystemUsage systemUsage = service.getSystemUsage();
        systemUsage.getStoreUsage().setLimit(1024 * 1024 * 8);
        systemUsage.getTempUsage().setLimit(1024 * 1024 * 8);
        service.start();
		service.waitUntilStarted();
	}

	public void stop() throws Exception {
		
		service.stop();
		service.waitUntilStopped();
		service = null;

	}
	
	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
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
