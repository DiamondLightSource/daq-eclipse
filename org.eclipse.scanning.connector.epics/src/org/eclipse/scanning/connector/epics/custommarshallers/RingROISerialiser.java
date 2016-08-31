package org.eclipse.scanning.connector.epics.custommarshallers;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.dawnsci.analysis.dataset.roi.RingROI;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVBoolean;
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
public class RingROISerialiser implements IPVStructureSerialiser<RingROI> {

	@Override
	public Structure buildStructure(Serialiser serialiser, RingROI roi) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		FieldCreate fieldCreate = FieldFactory.getFieldCreate();
		
		Structure structure = fieldCreate.createFieldBuilder().
			add("averageArea", ScalarType.pvBoolean).
			add("dpp", ScalarType.pvDouble).
			addArray("radii", ScalarType.pvDouble).
			addArray("point", ScalarType.pvDouble).
			setId("RingROI").
			createStructure();
		return structure;
	}

	@Override
	public void populatePVStructure(Serialiser serialiser, RingROI roi, PVStructure pvStructure) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		PVBoolean averageArea = pvStructure.getSubField(PVBoolean.class, "averageArea");
		averageArea.put(roi.isAverageArea());		
		PVDouble dpp = pvStructure.getSubField(PVDouble.class, "dpp");
		dpp.put(roi.getDpp());		
		PVDoubleArray radii = pvStructure.getSubField(PVDoubleArray.class, "radii");
		radii.put(0, roi.getRadii().length, roi.getRadii(), 0);
		PVDoubleArray point = pvStructure.getSubField(PVDoubleArray.class, "point");
		point.put(0, roi.getPoint().length, roi.getPoint(), 0);
	}
	
}
