package org.eclipse.scanning.connector.epics.custommarshallers;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.dawnsci.analysis.dataset.roi.EllipticalROI;
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
public class EllipticalROIDeserialiser implements IPVStructureDeserialiser {

	@Override
	public Object fromPVStructure(Deserialiser deserialiser, PVStructure pvStructure)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchFieldException, SecurityException {
		
		PVDoubleArray pDoubleArray = pvStructure.getSubField(PVDoubleArray.class, "point");
		DoubleArrayData pDoubleArrayData = new DoubleArrayData();
		pDoubleArray.get(0, pDoubleArray.getLength(), pDoubleArrayData);
		
		PVDoubleArray saDoubleArray = pvStructure.getSubField(PVDoubleArray.class, "semiAxes");
		DoubleArrayData saDoubleArrayData = new DoubleArrayData();
		saDoubleArray.get(0, saDoubleArray.getLength(), saDoubleArrayData);
		
		double angle = pvStructure.getSubField(PVDouble.class, "angle").get();
		
		EllipticalROI roi = new EllipticalROI();
		roi.setPoint(pDoubleArrayData.data);
		roi.setSemiaxes(saDoubleArrayData.data);
		roi.setAngle(angle);
		
		return roi;
	}
}
