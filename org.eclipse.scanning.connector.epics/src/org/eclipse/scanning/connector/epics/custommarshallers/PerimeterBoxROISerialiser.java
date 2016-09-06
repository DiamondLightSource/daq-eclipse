package org.eclipse.scanning.connector.epics.custommarshallers;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.dawnsci.analysis.dataset.roi.PerimeterBoxROI;
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
public class PerimeterBoxROISerialiser implements IPVStructureSerialiser<PerimeterBoxROI> {

	@Override
	public Structure buildStructure(Serialiser serialiser, PerimeterBoxROI roi) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		FieldCreate fieldCreate = FieldFactory.getFieldCreate();
		
		Structure structure = fieldCreate.createFieldBuilder().
			add("angle", ScalarType.pvDouble).
			addArray("lengths", ScalarType.pvDouble).
			addArray("point", ScalarType.pvDouble).
			setId("PerimeterBoxROI").
			createStructure();
		return structure;
	}

	@Override
	public void populatePVStructure(Serialiser serialiser, PerimeterBoxROI roi, PVStructure pvStructure) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		PVDouble angle = pvStructure.getSubField(PVDouble.class, "angle");
		angle.put(roi.getAngle());		
		PVDoubleArray lengths = pvStructure.getSubField(PVDoubleArray.class, "lengths");
		lengths.put(0, roi.getLengths().length, roi.getLengths(), 0);
		PVDoubleArray point = pvStructure.getSubField(PVDoubleArray.class, "point");
		point.put(0, roi.getPoint().length, roi.getPoint(), 0);
	}
	
}
