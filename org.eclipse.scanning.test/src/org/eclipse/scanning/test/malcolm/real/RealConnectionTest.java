package org.eclipse.scanning.test.malcolm.real;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scanning.api.malcolm.IMalcolmConnection;
import org.eclipse.scanning.api.malcolm.IMalcolmService;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.malcolm.State;
import org.eclipse.scanning.api.malcolm.event.IMalcolmListener;
import org.eclipse.scanning.api.malcolm.event.MalcolmEvent;
import org.eclipse.scanning.api.malcolm.event.MalcolmEventBean;
import org.eclipse.scanning.malcolm.core.MalcolmService;
import org.eclipse.scanning.test.malcolm.AbstractMalcolmTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.malcom.jacksonzeromq.connector.ZeromqConnectorService;


/**
 * This test is a work in progress trying to figure out the real
 * JSON connection.
 * 
 * @author Matthew Gerring
 * 
 * Test currently hard coded to a real Malcolm

  To start/top Tom Cobb's Malcolm we have:
  
  # Directory:
  cd /dls_sw/prod/common/python/RHEL6-x86_64/malcolm/0-5

  #Start server:
  ./malcolm/iMalcolm/iMalcolmServer.py

  # It echos the URI something like: zmq://tcp://172.23.5.254:5600

  # Then we know how to start client:
  imalcolm 172.23.5.254:5600

 *
 */
public class RealConnectionTest extends AbstractMalcolmTest {

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

	@Test
	public void testGetNames() throws Exception {

		final Collection<String> names = connection.getDeviceNames();
		if (names.size()<1) throw new Exception("Not devices found for test!");
	}
		
	@Test
	public void testRealConnection() throws Exception {
		
		if (device == null) throw new Exception("Cannot get device!");
		
		try {
			try {
			    device.abort();
			} catch (Exception allowed) {
				
			}
			try {
				device.reset();
			} catch (Exception allowed) {
				
			}
			if (device.getState()!=State.IDLE) throw new Exception("Unexpected non-IDLE state!");
		} finally {
			device.dispose();
		}
	}
	
	@Test
	public void testRealConfigure() throws Exception {
		
		if (device == null) throw new Exception("Cannot get device!");
		
		try {
			if (device.getState()!=State.IDLE) throw new Exception("Unexpected non-IDLE state!");
			
			final Map<String, Object> params = new HashMap<String, Object>(2);
			params.put("exposure", 0.1);
			params.put("nframes",  10);
			device.configure(params);
			
			if (device.getState()!=State.READY) throw new Exception("Unexpected non-READY state!");

			
		} finally {
			device.dispose();
		}
	}
	
	@Test
	public void testRealConfigureReset() throws Exception {
		
		if (device == null) throw new Exception("Cannot get device!");
		
		try {
			if (device.getState()!=State.IDLE) throw new Exception("Unexpected non-IDLE state!");
			
			final Map<String, Object> params = new HashMap<String, Object>(2);
			params.put("exposure", 0.1);
			params.put("nframes",  10);
			device.configure(params);
			device.abort();
			device.reset();
			if (device.getState()!=State.IDLE) throw new Exception("Unexpected non-READY state!");
			
		} finally {
			device.dispose();
		}
	}
	
	
	@Test
	public void testRealConfigureResetRun() throws Exception {
		
		if (device == null) throw new Exception("Cannot get device!");
		
		try {
			if (device.getState()!=State.IDLE) throw new Exception("Unexpected non-IDLE state!");
			
			final Map<String, Object> params = new HashMap<String, Object>(2);
			params.put("exposure", 0.1);
			params.put("nframes",  10);
			device.configure(params);
			device.abort();
			device.reset();
			if (device.getState()!=State.IDLE) throw new Exception("Unexpected non-READY state!");
			
			try {
				device.run(); // Should cause exception
			} catch (MalcolmDeviceException expected) {
				return;
			}
			throw new Exception("Was able to run even after abort and reset!");
			
		} finally {
			device.dispose();
		}
	}


	
	@Test
	public void testRealRun() throws Exception {
		
		if (device == null) throw new Exception("Cannot get device!");
		
		try {
			if (device.getState()!=State.IDLE) throw new Exception("Unexpected non-IDLE state!");
			
			final Map<String, Object> params = new HashMap<String, Object>(2);
			params.put("exposure", 0.1);
			params.put("nframes",  10);
			device.configure(params);
			
			final List<MalcolmEventBean> beans = new ArrayList<MalcolmEventBean>(7);
			device.addMalcolmListener(new IMalcolmListener<MalcolmEventBean>() {	
				@Override
				public void eventPerformed(MalcolmEvent<MalcolmEventBean> e) {
					System.out.println(e.getBean().getMessage());
					beans.add(e.getBean());
				}
			});
			
			device.run(); // Blocking
			
			if (beans.size()<10) throw new Exception("Unexpected number of events reported from running dummy detector! "+beans.size());
			
			if (device.getState()!=State.IDLE) {
				throw new Exception("Device did not go back to idle after the run!");
			}
			
		} finally {
			device.dispose();
		}
	}

	
	@Test
	public void testBadConnection() throws Exception {
		
		try {
			final URI uri = new URI("tcp://rubbish.diamond.ac.uk:8080");
			IMalcolmService    service    = new MalcolmService(); // The real service
			IMalcolmConnection connection = service.createConnection(uri);
			connection.getDevice("dev"); 
			
		} catch (Exception expected) {
			return;
		}

		throw new Exception("The URI tcp://rubbish.diamond.ac.uk:8080 did not throw an exception!");
	}
}
