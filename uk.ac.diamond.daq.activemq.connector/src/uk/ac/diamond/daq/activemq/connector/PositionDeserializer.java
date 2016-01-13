package uk.ac.diamond.daq.activemq.connector;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class PositionDeserializer extends JsonDeserializer<IPosition> {

	@Override
	public IPosition deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>)parser.readValueAs(LinkedHashMap.class);
		return new MapPosition(map);
	}

}
