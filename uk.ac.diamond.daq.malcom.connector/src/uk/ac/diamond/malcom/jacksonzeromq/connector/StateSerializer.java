package uk.ac.diamond.malcom.jacksonzeromq.connector;

import java.io.IOException;

import org.eclipse.scanning.api.malcolm.State;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class StateSerializer extends JsonSerializer<State> {

	@Override
	public void serialize(State state, JsonGenerator gen, SerializerProvider prov) throws IOException, JsonProcessingException {
		final String str = state.toString().toLowerCase();
		final String val = str.substring(0, 1).toUpperCase()+str.substring(1);
		gen.writeString(val);
	}
}
