package uk.ac.diamond.malcom.jacksonzeromq.connector;

import java.io.IOException;

import org.eclipse.scanning.api.malcolm.message.Type;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class TypeDeserializer extends JsonDeserializer<Type> {

	@Override
	public Type deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
		String text = parser.getText();
		return Type.valueOf(text.toUpperCase());
	}
}
