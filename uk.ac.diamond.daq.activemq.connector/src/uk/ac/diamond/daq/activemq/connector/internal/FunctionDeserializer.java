package uk.ac.diamond.daq.activemq.connector.internal;

import java.io.IOException;

import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.persistence.IJSonMarshaller;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class FunctionDeserializer extends JsonDeserializer<IFunction> {

	private IJSonMarshaller marshaller;

	public FunctionDeserializer(IJSonMarshaller marshaller) {
		this.marshaller = marshaller;
	}

	@Override
	public IFunction deserialize(JsonParser parser, DeserializationContext arg1)
			throws IOException, JsonProcessingException {
		String json = parser.getText();
		try {
			return (IFunction)marshaller.unmarshal(json);
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

}
