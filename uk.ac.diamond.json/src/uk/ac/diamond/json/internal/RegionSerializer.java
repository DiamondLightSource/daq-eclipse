package uk.ac.diamond.json.internal;

import java.io.IOException;

import org.eclipse.dawnsci.analysis.api.persistence.IJSonMarshaller;
import org.eclipse.dawnsci.analysis.api.roi.IROI;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

public class RegionSerializer extends JsonSerializer<IROI> {

	private IJSonMarshaller marshaller;

	public RegionSerializer(IJSonMarshaller marshaller) {
		this.marshaller = marshaller;
	}

	@Override
	public void serialize(IROI arg0, JsonGenerator gen, SerializerProvider arg2)
			throws IOException, JsonProcessingException {
		
		try {
			gen.writeObject(marshaller.marshal(arg0));
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	@Override
	public void serializeWithType(IROI roi, JsonGenerator gen, SerializerProvider prov, TypeSerializer typeSer)
			throws IOException, JsonProcessingException {
		serialize(roi, gen, prov);
	}
}
