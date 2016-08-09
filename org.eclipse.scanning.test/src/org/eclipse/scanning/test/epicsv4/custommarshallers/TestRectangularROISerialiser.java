package org.eclipse.scanning.test.epicsv4.custommarshallers;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
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
 * Custom serialiser for rectangular ROIs.
 * TODO - make this non 'test' and finalise custom serialisation strategy for ROIs 
 * @author Matt Taylor
 *
 */
public class TestRectangularROISerialiser implements IPVStructureSerialiser<RectangularROI> {

	@Override
	public Structure buildStructure(Serialiser serialiser, RectangularROI rectangularROI) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		FieldCreate fieldCreate = FieldFactory.getFieldCreate();
		
		Structure structure = fieldCreate.createFieldBuilder().
			addArray("lengths", ScalarType.pvDouble).
			add("angle", ScalarType.pvDouble).
			addArray("point", ScalarType.pvDouble).
			setId("RectangularROI").
			createStructure();
		return structure;
	}

	@Override
	public void populatePVStructure(Serialiser serialiser, RectangularROI rectangularROI, PVStructure pvStructure) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		PVDoubleArray lengths = pvStructure.getSubField(PVDoubleArray.class, "lengths");
		lengths.put(0, rectangularROI.getLengths().length, rectangularROI.getLengths(), 0);
		PVDouble angle = pvStructure.getSubField(PVDouble.class, "angle");
		angle.put(rectangularROI.getAngle());		
		PVDoubleArray point = pvStructure.getSubField(PVDoubleArray.class, "point");
		point.put(0, rectangularROI.getPoint().length, rectangularROI.getPoint(), 0);
	}
	
}
