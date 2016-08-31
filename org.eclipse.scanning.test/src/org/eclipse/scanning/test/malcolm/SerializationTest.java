package org.eclipse.scanning.test.malcolm;

import java.util.Map;

import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnectorService;
import org.eclipse.scanning.api.malcolm.message.MalcolmMessage;
import org.eclipse.scanning.api.malcolm.message.MalcolmUtil;
import org.eclipse.scanning.api.malcolm.message.Type;
import org.junit.Test;

import uk.ac.diamond.malcolm.jacksonzeromq.connector.ZeromqConnectorService;

public class SerializationTest {

	
	@Test
	public void testSerialize() throws Exception {
		
		final MalcolmMessage message = new MalcolmMessage();
		message.setType(Type.GET);
		message.setId(0);
		message.setParam("zebra.state");
		
		IMalcolmConnectorService<MalcolmMessage> mapper = new ZeromqConnectorService();
		String json= mapper.marshal(message);
		
		if (json.indexOf("\"type\":\"Get\"")<0) throw new Exception("The string Get was not found!");
		// Should be: {"type":"Get","id":0,"param":"zebra.state","args":null,"val":null}
		
		MalcolmMessage messageBack = mapper.unmarshal(json, MalcolmMessage.class);
	
		if (!messageBack.equals(message)) {
			throw new Exception("Failed to serilize and deserilize!");
		}
	}

	@Test
	public void testMapSerialize() throws Exception {
		
		// Value can sometimes be a List or sometimes a Map - TOMAS COBB!!! :)
		final String json = "{\"type\": \"Return\", \"id\": 0, \"value\": [\"DirectoryService\", \"det\"]}";
		IMalcolmConnectorService<MalcolmMessage> mapper = new ZeromqConnectorService();
		
		// Fails because value is Map
		MalcolmMessage message = mapper.unmarshal(json, MalcolmMessage.class);
		
	}
	
	@Test
	public void testMalcolmString() throws Exception {
		
		IMalcolmConnectorService<MalcolmMessage> mapper = new ZeromqConnectorService();

		String json = 	"{\"type\": \"return\", \"id\": 0, \"value\": {\"timeStamp\": null, \"index\": 1,"+
				"\"choices\": [\"Fault\", \"Idle\", \"Configuring\", \"Ready\", \"Running\", "+
				"\"Pausing\", \"Paused\", \"Aborting\", \"Aborted\", \"Resetting\"]}, \"message\": "+
				"\"\"}}";


		Map<String,Object> message = mapper.unmarshal(json, Map.class);
		if (MalcolmUtil.getState(message)!=DeviceState.IDLE) throw new Exception("It's not IDLE!");


		json = 	"{\"type\": \"return\", \"id\": 0, \"value\": {\"timeStamp\": null, \"index\": 2,"+
				"\"choices\": [\"Fault\", \"Idle\", \"Configuring\", \"Ready\", \"Running\", "+
				"\"Pausing\", \"Paused\", \"Aborting\", \"Aborted\", \"Resetting\"]}, \"message\": "+
				"\"\"}}";

		message = mapper.unmarshal(json, Map.class);
		if (MalcolmUtil.getState(message)!=DeviceState.CONFIGURING) throw new Exception("It's not CONFIGURING!");

		json = 	"{\"type\": \"return\", \"id\": 0, \"value\": {\"timeStamp\": null, \"index\": 4,"+
				"\"choices\": [\"Fault\", \"Idle\", \"Configuring\", \"Ready\", \"Running\", "+
				"\"Pausing\", \"Paused\", \"Aborting\", \"Aborted\", \"Resetting\"]}, \"message\": "+
				"\"\"}}";

		message = mapper.unmarshal(json, Map.class);
		if (MalcolmUtil.getState(message)!=DeviceState.RUNNING) throw new Exception("It's not RUNNING!");

	}
}
