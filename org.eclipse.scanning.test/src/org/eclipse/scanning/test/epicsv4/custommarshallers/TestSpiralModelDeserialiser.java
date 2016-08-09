package org.eclipse.scanning.test.epicsv4.custommarshallers;

import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.SpiralModel;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvmarshaller.marshaller.api.IPVStructureDeserialiser;
import org.epics.pvmarshaller.marshaller.deserialisers.Deserialiser;

/**
 * Custom deserialiser for Spiral model.
 * TODO - make this non 'test' and finalise custom serialisation strategy for models 
 * @author Matt Taylor
 *
 */
public class TestSpiralModelDeserialiser implements IPVStructureDeserialiser {

	@Override
	public Object fromPVStructure(Deserialiser deserialiser, PVStructure pvStructure) throws Exception {
		SpiralModel spiralModel = new SpiralModel();
		spiralModel.setName(pvStructure.getStringField("name").get());
		spiralModel.setBoundingBox(deserialiser.fromPVStructure(pvStructure.getStructureField("boundingBox"), BoundingBox.class));
		spiralModel.setFastAxisName(pvStructure.getStringField("fastAxisName").get());
		spiralModel.setSlowAxisName(pvStructure.getStringField("slowAxisName").get());
		spiralModel.setScale(pvStructure.getDoubleField("scale").get());
		return spiralModel;
	}
}
