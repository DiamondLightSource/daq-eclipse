package uk.ac.diamond.json.internal;

import java.io.IOException;

import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.persistence.IJSonMarshaller;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

// FIXME this currently writes a String object to the output instead of a FunctionBean
public class FunctionSerializer extends JsonSerializer<IFunction> {

	private IJSonMarshaller marshaller;

	public FunctionSerializer(IJSonMarshaller marshaller) {
		this.marshaller = marshaller;
	}

	@Override
	public void serialize(IFunction func, JsonGenerator gen, SerializerProvider prov)
			throws IOException, JsonProcessingException {

		try {
			gen.writeObject(marshaller.marshal(func));
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	@Override
	public void serializeWithType(IFunction func, JsonGenerator gen, SerializerProvider prov, TypeSerializer typeSer)
			throws IOException, JsonProcessingException {
		serialize(func, gen, prov);
	}
}
