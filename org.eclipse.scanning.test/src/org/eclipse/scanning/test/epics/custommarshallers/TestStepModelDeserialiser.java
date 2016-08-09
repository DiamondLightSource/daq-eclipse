package org.eclipse.scanning.test.epics.custommarshallers;

import org.eclipse.scanning.api.points.models.StepModel;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvmarshaller.marshaller.api.IPVStructureDeserialiser;
import org.epics.pvmarshaller.marshaller.deserialisers.Deserialiser;

/**
 * Custom deserialiser for Step model.
 * TODO - make this non 'test' and finalise custom serialisation strategy for models 
 * @author Matt Taylor
 *
 */
public class TestStepModelDeserialiser implements IPVStructureDeserialiser {

	@Override
	public Object fromPVStructure(Deserialiser deserialiser, PVStructure pvStructure) throws Exception {
		StepModel stepModel = new StepModel();
		stepModel.setName(pvStructure.getStringField("name").get());
		stepModel.setStart(pvStructure.getDoubleField("start").get());
		stepModel.setStop(pvStructure.getDoubleField("stop").get());
		stepModel.setStep(pvStructure.getDoubleField("step").get());
		return stepModel;
	}
}
