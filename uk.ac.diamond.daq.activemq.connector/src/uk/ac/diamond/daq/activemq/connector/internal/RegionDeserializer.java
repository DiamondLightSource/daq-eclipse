package uk.ac.diamond.daq.activemq.connector.internal;

import java.io.IOException;

import org.eclipse.dawnsci.analysis.api.persistence.IJSonMarshaller;
import org.eclipse.dawnsci.analysis.api.roi.IROI;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

public class RegionDeserializer extends JsonDeserializer<IROI> {

	private IJSonMarshaller marshaller;

	public RegionDeserializer(IJSonMarshaller marshaller) {
		this.marshaller = marshaller;
	}

	@Override
	public IROI deserialize(JsonParser parser, DeserializationContext arg1) throws IOException, JsonProcessingException {
		String json = parser.getText();
		try {
			return (IROI)marshaller.unmarshal(json);
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	@Override
	public Object deserializeWithType(JsonParser parser, DeserializationContext context, TypeDeserializer typeDeserializer)
			throws IOException, JsonProcessingException {
		return deserialize(parser, context);
	}
}
