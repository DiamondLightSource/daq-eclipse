package org.eclipse.scanning.connector.epics.custommarshallers;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.dawnsci.analysis.dataset.roi.RingROI;
import org.epics.pvdata.pv.DoubleArrayData;
import org.epics.pvdata.pv.PVBoolean;
import org.epics.pvdata.pv.PVDouble;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvmarshaller.marshaller.api.IPVStructureDeserialiser;
import org.epics.pvmarshaller.marshaller.deserialisers.Deserialiser;

/**
 * Custom deserialiser for Rectangular ROI.
 * TODO - make this non 'test' and finalise custom serialisation strategy for ROIs 
 * @author Matt Taylor
 *
 */
public class RingROIDeserialiser implements IPVStructureDeserialiser {

	@Override
	public Object fromPVStructure(Deserialiser deserialiser, PVStructure pvStructure)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchFieldException, SecurityException {
		
		RingROI roi = new RingROI();
		
		PVDoubleArray pDoubleArray = pvStructure.getSubField(PVDoubleArray.class, "point");
		DoubleArrayData pDoubleArrayData = new DoubleArrayData();
		pDoubleArray.get(0, pDoubleArray.getLength(), pDoubleArrayData);
		roi.setPoint(pDoubleArrayData.data);
		
		PVDoubleArray rDoubleArray = pvStructure.getSubField(PVDoubleArray.class, "radii");
		DoubleArrayData rDoubleArrayData = new DoubleArrayData();
		rDoubleArray.get(0, rDoubleArray.getLength(), rDoubleArrayData);
		roi.setRadii(rDoubleArrayData.data);
		
		roi.setDpp(pvStructure.getSubField(PVDouble.class, "dpp").get());
		
		roi.setAverageArea(pvStructure.getSubField(PVBoolean.class, "averageArea").get());
		
		return roi;
	}
}
