package uk.ac.diamond.daq.activemq.connector;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

public class PositionDeserializer extends JsonDeserializer<IPosition> {

	@Override
	public IPosition deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
		TypeReference<LinkedHashMap<String, Object>> linkedHashMap = new TypeReference<LinkedHashMap<String, Object>>() {};
		Map<String, Object> map = parser.readValueAs(linkedHashMap);
		return new MapPosition(map);
	}

	@Override
	public Object deserializeWithType(JsonParser parser, DeserializationContext context, TypeDeserializer typeDeserializer)
			throws IOException, JsonProcessingException {
		return deserialize(parser, context);
	}
}
