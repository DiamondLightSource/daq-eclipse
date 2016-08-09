package org.eclipse.scanning.test.epicsv4.custommarshallers;

import org.eclipse.scanning.api.points.models.GridModel;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVBoolean;
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;
import org.epics.pvmarshaller.marshaller.api.IPVStructureSerialiser;
import org.epics.pvmarshaller.marshaller.serialisers.Serialiser;

/**
 * Custom serialiser for Grid model.
 * TODO - make this non 'test' and finalise custom serialisation strategy for models 
 * @author Matt Taylor
 *
 */
public class TestGridModelSerialiser implements IPVStructureSerialiser<GridModel> {

	@Override
	public Structure buildStructure(Serialiser serialiser, GridModel model) throws Exception {
		FieldCreate fieldCreate = FieldFactory.getFieldCreate();
				
		Structure structure = fieldCreate.createFieldBuilder().
			add("name", ScalarType.pvString).
			add("fastAxisName", ScalarType.pvString).
			add("slowAxisName", ScalarType.pvString).
			add("fastAxisPoints", ScalarType.pvInt).
			add("slowAxisPoints", ScalarType.pvInt).
			add("snake", ScalarType.pvBoolean).
			setId("GridModel").
			createStructure();
		return structure;
	}

	@Override
	public void populatePVStructure(Serialiser serialiser, GridModel model, PVStructure pvStructure) throws Exception {
		PVString name = pvStructure.getStringField("name");
		name.put(model.getName());	
		PVString fastAxisName = pvStructure.getStringField("fastAxisName");
		fastAxisName.put(model.getFastAxisName());		
		PVString slowAxisName = pvStructure.getStringField("slowAxisName");
		slowAxisName.put(model.getSlowAxisName());		
		PVInt fastAxisPoints = pvStructure.getIntField("fastAxisPoints");
		fastAxisPoints.put(model.getFastAxisPoints());	
		PVInt slowAxisPoints = pvStructure.getIntField("slowAxisPoints");
		slowAxisPoints.put(model.getSlowAxisPoints());
		PVBoolean snake = pvStructure.getBooleanField("snake");
		snake.put(model.isSnake());
	}
	
}
