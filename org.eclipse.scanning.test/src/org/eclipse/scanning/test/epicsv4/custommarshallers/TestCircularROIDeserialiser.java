package org.eclipse.scanning.test.epicsv4.custommarshallers;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.epics.pvdata.pv.DoubleArrayData;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVScalarArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvmarshaller.marshaller.api.IPVStructureDeserialiser;
import org.epics.pvmarshaller.marshaller.deserialisers.Deserialiser;

/**
 * Custom deserialiser for Circular ROI.
 * TODO - make this non 'test' and finalise custom serialisation strategy for ROIs 
 * @author Matt Taylor
 *
 */
public class TestCircularROIDeserialiser implements IPVStructureDeserialiser {

	@Override
	public Object fromPVStructure(Deserialiser deserialiser, PVStructure pvStructure)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchFieldException, SecurityException {
		
		PVScalarArray pvScallarArray = pvStructure.getScalarArrayField("point", ScalarType.pvDouble);
		PVDoubleArray doubleArray = (PVDoubleArray)pvScallarArray;
		DoubleArrayData doubleArrayData = new DoubleArrayData();
		doubleArray.get(0, doubleArray.getLength(), doubleArrayData);
		
		double radius = pvStructure.getDoubleField("radius").get();
		double angle = pvStructure.getDoubleField("angle").get();
		
		CircularROI circularROI = new CircularROI(radius, doubleArrayData.data[0], doubleArrayData.data[1]);
		circularROI.setAngle(angle);
		
		return circularROI;
	}
}
