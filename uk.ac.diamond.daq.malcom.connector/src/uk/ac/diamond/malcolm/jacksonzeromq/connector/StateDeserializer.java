package uk.ac.diamond.malcolm.jacksonzeromq.connector;

import java.io.IOException;

import org.eclipse.scanning.api.malcolm.State;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class StateDeserializer extends JsonDeserializer<State> {

	@Override
	public State deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
		String text = parser.getText();
		return State.valueOf(text.toUpperCase());
	}

}
