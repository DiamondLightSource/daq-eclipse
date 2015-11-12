package org.eclipse.malcolm.test;

import java.util.Map;

import org.eclipse.malcolm.api.State;
import org.eclipse.malcolm.api.connector.IMalcolmConnectorService;
import org.eclipse.malcolm.api.message.JsonMessage;
import org.eclipse.malcolm.api.message.MalcolmUtil;
import org.eclipse.malcolm.api.message.Type;
import org.junit.Test;

import uk.ac.diamond.malcom.jacksonzeromq.connector.ZeromqConnectorService;

public class SerializationTest {

	
	@Test
	public void testSerialize() throws Exception {
		
		final JsonMessage message = new JsonMessage();
		message.setType(Type.GET);
		message.setId(0);
		message.setParam("zebra.state");
		
		IMalcolmConnectorService<JsonMessage> mapper = new ZeromqConnectorService();
		String json= mapper.marshal(message);
		
		if (json.indexOf("\"type\":\"Get\"")<0) throw new Exception("The string Get was not found!");
		// Should be: {"type":"Get","id":0,"param":"zebra.state","args":null,"val":null}
		
		JsonMessage messageBack = mapper.unmarshal(json, JsonMessage.class);
	
		if (!messageBack.equals(message)) {
			throw new Exception("Failed to serilize and deserilize!");
		}
	}

	@Test
	public void testMapSerialize() throws Exception {
		
		// Value can sometimes be a List or sometimes a Map - TOMAS COBB!!! :)
		final String json = "{\"type\": \"Return\", \"id\": 0, \"value\": [\"DirectoryService\", \"det\"]}";
		IMalcolmConnectorService<JsonMessage> mapper = new ZeromqConnectorService();
		
		// Fails because value is Map
		JsonMessage message = mapper.unmarshal(json, JsonMessage.class);
		
	}
	
	@Test
	public void testMalcolmString() throws Exception {
		
		IMalcolmConnectorService<JsonMessage> mapper = new ZeromqConnectorService();

		String json = 	"{\"type\": \"return\", \"id\": 0, \"value\": {\"timeStamp\": null, \"index\": 1,"+
				"\"choices\": [\"Fault\", \"Idle\", \"Configuring\", \"Ready\", \"Running\", "+
				"\"Pausing\", \"Paused\", \"Aborting\", \"Aborted\", \"Resetting\"]}, \"message\": "+
				"\"\"}}";


		Map<String,Object> message = mapper.unmarshal(json, Map.class);
		if (MalcolmUtil.getState(message)!=State.IDLE) throw new Exception("It's not IDLE!");


		json = 	"{\"type\": \"return\", \"id\": 0, \"value\": {\"timeStamp\": null, \"index\": 2,"+
				"\"choices\": [\"Fault\", \"Idle\", \"Configuring\", \"Ready\", \"Running\", "+
				"\"Pausing\", \"Paused\", \"Aborting\", \"Aborted\", \"Resetting\"]}, \"message\": "+
				"\"\"}}";

		message = mapper.unmarshal(json, Map.class);
		if (MalcolmUtil.getState(message)!=State.CONFIGURING) throw new Exception("It's not CONFIGURING!");

		json = 	"{\"type\": \"return\", \"id\": 0, \"value\": {\"timeStamp\": null, \"index\": 4,"+
				"\"choices\": [\"Fault\", \"Idle\", \"Configuring\", \"Ready\", \"Running\", "+
				"\"Pausing\", \"Paused\", \"Aborting\", \"Aborted\", \"Resetting\"]}, \"message\": "+
				"\"\"}}";

		message = mapper.unmarshal(json, Map.class);
		if (MalcolmUtil.getState(message)!=State.RUNNING) throw new Exception("It's not RUNNING!");

	}
}
