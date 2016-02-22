package uk.ac.diamond.json.internal;

import java.io.IOException;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.json.ROIBean;
import org.eclipse.dawnsci.analysis.dataset.roi.json.ROIBeanFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

public class ROIDeserializer extends JsonDeserializer<IROI> {

	@Override
	public IROI deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
		try {
			ROIBean roiBean = parser.readValueAs(ROIBean.class);
			IROI roi = ROIBeanFactory.decapsulate(roiBean);
			return roi;
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
