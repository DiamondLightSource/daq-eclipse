package org.eclipse.scanning.connector.epics.custommarshallers;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.scanning.api.malcolm.attributes.TableAttribute;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.StringArrayData;
import org.epics.pvmarshaller.marshaller.api.IPVStructureDeserialiser;
import org.epics.pvmarshaller.marshaller.deserialisers.Deserialiser;

/**
 * Custom deserialiser for Malcolm Table
 * @author Matt Taylor
 *
 */
public class MalcolmTableDeserialiser implements IPVStructureDeserialiser {
	
	private final String valueField = "value";
	private final String headingsTagField = "headings";
	private final String metaField = "meta";
	private final String descriptionField = "description";
	private final String writeableField = "writeable";
	private final String labelField = "label";
	private final String tagsField = "tags";

	@Override
	public Object fromPVStructure(Deserialiser deserialiser, PVStructure pvStructure) throws Exception {
		
		PVStructure metaStructure = pvStructure.getStructureField(metaField);
		String description = metaStructure.getStringField(descriptionField).get();
		boolean writeable = metaStructure.getBooleanField(writeableField).get();
		String label = metaStructure.getStringField(labelField).get();
		PVStringArray tagsArray = metaStructure.getSubField(PVStringArray.class, tagsField);
		StringArrayData tagsArrayData = new StringArrayData();
		tagsArray.get(0, tagsArray.getLength(), tagsArrayData);
		
		
		TableAttribute attribute = new TableAttribute();
		
		attribute.setDescription(description);
		attribute.setLabel(label);
		attribute.setTags(tagsArrayData.data);
		attribute.setWriteable(writeable);
		attribute.setName(pvStructure.getFullName());
		
		PVStringArray headingsArray = metaStructure.getSubField(PVStringArray.class, headingsTagField);
		StringArrayData headingsArrayData = new StringArrayData();
		headingsArray.get(0, headingsArray.getLength(), headingsArrayData);
		attribute.setHeadings(headingsArrayData.data);
		
		PVStructure valuePVStructure = pvStructure.getStructureField(valueField);
		
		Map<?, ?> valueMap = deserialiser.getMapDeserialiser().createMapFromPVStructure(valuePVStructure, LinkedHashMap.class, Object.class);
		
		attribute.setValue(valueMap);
		
		return attribute;
		
	}
}
