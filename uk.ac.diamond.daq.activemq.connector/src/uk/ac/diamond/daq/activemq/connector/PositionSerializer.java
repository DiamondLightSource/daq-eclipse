package uk.ac.diamond.daq.activemq.connector;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scanning.api.points.IPosition;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class PositionSerializer extends JsonSerializer<IPosition> {

	@Override
	public void serialize(IPosition pos, JsonGenerator gen, SerializerProvider prov) throws IOException, JsonProcessingException {

		final Map<String,Object> values = new HashMap<String, Object>(pos.size());
		for (String name : pos.getNames()) values.put(name, pos.get(name));
		gen.writeObject(values);
	}
}
