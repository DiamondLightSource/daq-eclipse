package org.eclipse.scanning.test.event;

import java.net.InetAddress;
import java.util.UUID;

import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.points.MapPosition;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

public class SerializationTest {

	private ActivemqConnectorService connectorService;

	@Before
	public void create() throws Exception {
		// Non-OSGi for test - do not copy!
		this.connectorService = new ActivemqConnectorService(); // Just for ActiveMQ connection!
	}
	
	@Test
	public void testSerializeScanBean() throws Exception {
		
		final ScanBean sent = new ScanBean();
		sent.setDeviceName("fred");
		sent.setDeviceState(DeviceState.RUNNING);
		sent.setPreviousDeviceState(DeviceState.READY);
		sent.setPosition(new MapPosition("x", 0, 1.0));
		sent.setUniqueId(UUID.randomUUID().toString());
		sent.setHostName(InetAddress.getLocalHost().getHostName());
		
        String json = connectorService.marshal(sent);
        
        ScanBean ret = connectorService.unmarshal(json, ScanBean.class);
        
        if (!ret.equals(sent)) throw new Exception("Cannot deserialize "+ScanBean.class.getName());
        if (!ret.getPosition().equals(sent.getPosition())) throw new Exception("Cannot deserialize "+ScanBean.class.getName());
	}
}
