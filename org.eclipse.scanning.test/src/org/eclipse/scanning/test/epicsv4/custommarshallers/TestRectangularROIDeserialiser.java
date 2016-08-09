package org.eclipse.scanning.test.epicsv4.custommarshallers;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.epics.pvdata.pv.DoubleArrayData;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVScalarArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvmarshaller.marshaller.api.IPVStructureDeserialiser;
import org.epics.pvmarshaller.marshaller.deserialisers.Deserialiser;

/**
 * Custom deserialiser for Rectangular ROI.
 * TODO - make this non 'test' and finalise custom serialisation strategy for ROIs 
 * @author Matt Taylor
 *
 */
public class TestRectangularROIDeserialiser implements IPVStructureDeserialiser {

	@Override
	public Object fromPVStructure(Deserialiser deserialiser, PVStructure pvStructure)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchFieldException, SecurityException {
		
		RectangularROI rectangularRoi = new RectangularROI();
		PVScalarArray lpvScallarArray = pvStructure.getScalarArrayField("lengths", ScalarType.pvDouble);
		PVDoubleArray ldoubleArray = (PVDoubleArray)lpvScallarArray;
		DoubleArrayData lDdoubleArrayData = new DoubleArrayData();
		ldoubleArray.get(0, ldoubleArray.getLength(), lDdoubleArrayData);
		rectangularRoi.setLengths(lDdoubleArrayData.data);
		rectangularRoi.setAngle(pvStructure.getDoubleField("angle").get());
		PVScalarArray pvScallarArray = pvStructure.getScalarArrayField("point", ScalarType.pvDouble);
		PVDoubleArray doubleArray = (PVDoubleArray)pvScallarArray;
		DoubleArrayData doubleArrayData = new DoubleArrayData();
		doubleArray.get(0, doubleArray.getLength(), doubleArrayData);
		rectangularRoi.setPoint(doubleArrayData.data);
		return rectangularRoi;
	}
}
