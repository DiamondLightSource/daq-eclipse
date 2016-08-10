package org.eclipse.scanning.test.epics.custommarshallers;

import org.eclipse.scanning.api.points.models.BoundingBox;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvmarshaller.marshaller.api.IPVStructureDeserialiser;
import org.epics.pvmarshaller.marshaller.deserialisers.Deserialiser;

/**
 * Custom deserialiser for Bounding Box.
 * TODO - make this non 'test' and finalise custom serialisation strategy for models 
 * @author Matt Taylor
 *
 */
public class TestBoundingBoxDeserialiser implements IPVStructureDeserialiser {

	@Override
	public Object fromPVStructure(Deserialiser deserialiser, PVStructure pvStructure) throws Exception {
		BoundingBox spiralModel = new BoundingBox();
		spiralModel.setFastAxisStart(pvStructure.getDoubleField("fastAxisStart").get());
		spiralModel.setSlowAxisStart(pvStructure.getDoubleField("slowAxisStart").get());
		spiralModel.setFastAxisLength(pvStructure.getDoubleField("fastAxisLength").get());
		spiralModel.setSlowAxisLength(pvStructure.getDoubleField("slowAxisLength").get());
		return spiralModel;
	}
}
