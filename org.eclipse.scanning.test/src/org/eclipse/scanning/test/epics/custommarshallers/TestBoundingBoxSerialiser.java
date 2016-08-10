package org.eclipse.scanning.test.epics.custommarshallers;

import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.SpiralModel;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVDouble;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;
import org.epics.pvmarshaller.marshaller.api.IPVStructureSerialiser;
import org.epics.pvmarshaller.marshaller.serialisers.Serialiser;

/**
 * Custom serialiser for Bounding Box.
 * TODO - make this non 'test' and finalise custom serialisation strategy for models 
 * @author Matt Taylor
 *
 */
public class TestBoundingBoxSerialiser implements IPVStructureSerialiser<BoundingBox> {

	@Override
	public Structure buildStructure(Serialiser serialiser, BoundingBox model) throws Exception {
		FieldCreate fieldCreate = FieldFactory.getFieldCreate();
				
		Structure structure = fieldCreate.createFieldBuilder().
			add("fastAxisStart", ScalarType.pvDouble).
			add("slowAxisStart", ScalarType.pvDouble).
			add("fastAxisLength", ScalarType.pvDouble).
			add("slowAxisLength", ScalarType.pvDouble).
			setId("BoundingBox").
			createStructure();
		return structure;
	}

	@Override
	public void populatePVStructure(Serialiser serialiser, BoundingBox model, PVStructure pvStructure) throws Exception {
		PVDouble fastAxisStart = pvStructure.getDoubleField("fastAxisStart");
		fastAxisStart.put(model.getFastAxisStart());
		PVDouble slowAxisStart = pvStructure.getDoubleField("slowAxisStart");
		slowAxisStart.put(model.getSlowAxisStart());
		PVDouble fastAxisLength = pvStructure.getDoubleField("fastAxisLength");
		fastAxisLength.put(model.getFastAxisLength());
		PVDouble slowAxisLength = pvStructure.getDoubleField("slowAxisLength");
		slowAxisLength.put(model.getSlowAxisLength());
	}
	
}
