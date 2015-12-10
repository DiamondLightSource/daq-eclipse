package uk.ac.diamond.malcolm.jacksonzeromq.connector;

import java.io.IOException;

import org.eclipse.scanning.api.event.scan.DeviceState;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class StateDeserializer extends JsonDeserializer<DeviceState> {

	@Override
	public DeviceState deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
		String text = parser.getText();
		return DeviceState.valueOf(text.toUpperCase());
	}

}
