package org.eclipse.scanning.test.epicsv4.custommarshallers;

import org.eclipse.scanning.api.points.models.GridModel;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvmarshaller.marshaller.api.IPVStructureDeserialiser;
import org.epics.pvmarshaller.marshaller.deserialisers.Deserialiser;

/**
 * Custom deserialiser for Grid model.
 * TODO - make this non 'test' and finalise custom serialisation strategy for models 
 * @author Matt Taylor
 *
 */
public class TestGridModelDeserialiser implements IPVStructureDeserialiser {

	@Override
	public Object fromPVStructure(Deserialiser deserialiser, PVStructure pvStructure) throws Exception {
		GridModel gridModel = new GridModel();
		gridModel.setName(pvStructure.getStringField("name").get());
		gridModel.setFastAxisName(pvStructure.getStringField("fastAxisName").get());
		gridModel.setSlowAxisName(pvStructure.getStringField("slowAxisName").get());
		gridModel.setFastAxisPoints(pvStructure.getIntField("fastAxisPoints").get());
		gridModel.setSlowAxisPoints(pvStructure.getIntField("slowAxisPoints").get());
		gridModel.setSnake(pvStructure.getBooleanField("snake").get());
		return gridModel;
	}
}
