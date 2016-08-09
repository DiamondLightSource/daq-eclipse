package org.eclipse.scanning.test.epicsv4.custommarshallers;

import org.eclipse.scanning.api.points.models.StepModel;
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
 * Custom deserialiser for Step model.
 * TODO - make this non 'test' and finalise custom serialisation strategy for models 
 * @author Matt Taylor
 *
 */
public class TestStepModelSerialiser implements IPVStructureSerialiser<StepModel> {

	@Override
	public Structure buildStructure(Serialiser serialiser, StepModel model) throws Exception {
		FieldCreate fieldCreate = FieldFactory.getFieldCreate();
				
		Structure structure = fieldCreate.createFieldBuilder().
			add("name", ScalarType.pvString).
			add("start", ScalarType.pvDouble).
			add("stop", ScalarType.pvDouble).
			add("step", ScalarType.pvDouble).
			setId("StepModel").
			createStructure();
		return structure;
	}

	@Override
	public void populatePVStructure(Serialiser serialiser, StepModel model, PVStructure pvStructure) throws Exception {
		PVString name = pvStructure.getStringField("name");
		name.put(model.getName());		
		PVDouble start = pvStructure.getDoubleField("start");
		start.put(model.getStart());			
		PVDouble stop = pvStructure.getDoubleField("stop");
		stop.put(model.getStop());		
		PVDouble step = pvStructure.getDoubleField("step");
		step.put(model.getStep());
	}
	
}
