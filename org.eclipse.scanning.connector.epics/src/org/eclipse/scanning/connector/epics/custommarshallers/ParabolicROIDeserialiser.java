package org.eclipse.scanning.connector.epics.custommarshallers;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.dawnsci.analysis.dataset.roi.ParabolicROI;
import org.epics.pvdata.pv.DoubleArrayData;
import org.epics.pvdata.pv.PVDouble;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvmarshaller.marshaller.api.IPVStructureDeserialiser;
import org.epics.pvmarshaller.marshaller.deserialisers.Deserialiser;

/**
 * Custom deserialiser for Circular ROI.
 * TODO - make this non 'test' and finalise custom serialisation strategy for ROIs 
 * @author Matt Taylor
 *
 */
public class ParabolicROIDeserialiser implements IPVStructureDeserialiser {

	@Override
	public Object fromPVStructure(Deserialiser deserialiser, PVStructure pvStructure)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchFieldException, SecurityException {
		
		PVDoubleArray doubleArray = pvStructure.getSubField(PVDoubleArray.class, "point");
		DoubleArrayData doubleArrayData = new DoubleArrayData();
		doubleArray.get(0, doubleArray.getLength(), doubleArrayData);
		
		double focalParameter = pvStructure.getSubField(PVDouble.class, "focalParameter").get();
		double angle = pvStructure.getSubField(PVDouble.class, "angle").get();
		
		ParabolicROI roi = new ParabolicROI();
		roi.setPoint(doubleArrayData.data);
		roi.setFocalParameter(focalParameter);
		roi.setAngle(angle);
		
		return roi;
	}
}
