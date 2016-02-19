package uk.ac.diamond.json.internal;

import java.io.IOException;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.json.ROIBean;
import org.eclipse.dawnsci.analysis.dataset.roi.json.ROIBeanFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

public class RegionSerializer extends JsonSerializer<IROI> {

	@Override
	public void serialize(IROI roi, JsonGenerator gen, SerializerProvider prov)
			throws IOException, JsonProcessingException {

		try {
			ROIBean roiBean = (ROIBean) ROIBeanFactory.encapsulate(roi);
			gen.writeObject(roiBean);
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
