package org.eclipse.scanning.connector.epics.custommarshallers;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.dawnsci.analysis.dataset.roi.HyperbolicROI;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVDouble;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;
import org.epics.pvmarshaller.marshaller.api.IPVStructureSerialiser;
import org.epics.pvmarshaller.marshaller.serialisers.Serialiser;

/**
 * Custom serialiser for Circular ROI.
 * TODO - make this non 'test' and finalise custom serialisation strategy for ROIs 
 * @author Matt Taylor
 *
 */
public class HyperbolicROISerialiser implements IPVStructureSerialiser<HyperbolicROI> {

	@Override
	public Structure buildStructure(Serialiser serialiser, HyperbolicROI roi) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		FieldCreate fieldCreate = FieldFactory.getFieldCreate();
		
		Structure structure = fieldCreate.createFieldBuilder().
			add("semilatusRectum", ScalarType.pvDouble).
			add("eccentricity", ScalarType.pvDouble).
			add("asymptoteAngle", ScalarType.pvDouble).
			add("angle", ScalarType.pvDouble).
			addArray("point", ScalarType.pvDouble).
			setId("HyperbolicROI").
			createStructure();
		return structure;
	}

	@Override
	public void populatePVStructure(Serialiser serialiser, HyperbolicROI roi, PVStructure pvStructure) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		PVDouble semilatusRectum = pvStructure.getSubField(PVDouble.class, "semilatusRectum");
		semilatusRectum.put(roi.getSemilatusRectum());	
		PVDouble eccentricity = pvStructure.getSubField(PVDouble.class, "eccentricity");
		eccentricity.put(roi.getEccentricity());	
		PVDouble asymptoteAngle = pvStructure.getSubField(PVDouble.class, "asymptoteAngle");
		asymptoteAngle.put(roi.getAsymptoteAngle());	
		PVDouble angle = pvStructure.getSubField(PVDouble.class, "angle");
		angle.put(roi.getAngle());		
		PVDoubleArray point = pvStructure.getSubField(PVDoubleArray.class, "point");
		point.put(0, roi.getPoint().length, roi.getPoint(), 0);
	}
	
}
