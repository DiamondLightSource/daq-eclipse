package org.eclipse.scanning.connector.epics.custommarshallers;

import org.eclipse.scanning.api.points.models.StepModel;
import org.epics.pvdata.pv.PVDouble;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvmarshaller.marshaller.api.IPVStructureDeserialiser;
import org.epics.pvmarshaller.marshaller.deserialisers.Deserialiser;

/**
 * Custom deserialiser for Step model.
 * TODO - make this non 'test' and finalise custom serialisation strategy for models 
 * @author Matt Taylor
 *
 */
public class StepModelDeserialiser implements IPVStructureDeserialiser {

	@Override
	public Object fromPVStructure(Deserialiser deserialiser, PVStructure pvStructure) throws Exception {
		StepModel stepModel = new StepModel();
		stepModel.setName(pvStructure.getSubField(PVString.class, "name").get());
		stepModel.setStart(pvStructure.getSubField(PVDouble.class, "start").get());
		stepModel.setStop(pvStructure.getSubField(PVDouble.class, "stop").get());
		stepModel.setStep(pvStructure.getSubField(PVDouble.class, "step").get());
		return stepModel;
	}
}
