package org.eclipse.scanning.connector.epics.custommarshallers;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.dawnsci.analysis.dataset.roi.SectorROI;
import org.epics.pvdata.pv.DoubleArrayData;
import org.epics.pvdata.pv.PVBoolean;
import org.epics.pvdata.pv.PVDouble;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvmarshaller.marshaller.api.IPVStructureDeserialiser;
import org.epics.pvmarshaller.marshaller.deserialisers.Deserialiser;

/**
 * Custom deserialiser for Rectangular ROI.
 * TODO - make this non 'test' and finalise custom serialisation strategy for ROIs 
 * @author Matt Taylor
 *
 */
public class SectorROIDeserialiser implements IPVStructureDeserialiser {

	@Override
	public Object fromPVStructure(Deserialiser deserialiser, PVStructure pvStructure)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchFieldException, SecurityException {
		
		SectorROI roi = new SectorROI();
		
		PVDoubleArray pDoubleArray = pvStructure.getSubField(PVDoubleArray.class, "point");
		DoubleArrayData pDoubleArrayData = new DoubleArrayData();
		pDoubleArray.get(0, pDoubleArray.getLength(), pDoubleArrayData);
		roi.setPoint(pDoubleArrayData.data);
		
		PVDoubleArray rDoubleArray = pvStructure.getSubField(PVDoubleArray.class, "radii");
		DoubleArrayData rDoubleArrayData = new DoubleArrayData();
		rDoubleArray.get(0, rDoubleArray.getLength(), rDoubleArrayData);
		roi.setRadii(rDoubleArrayData.data);
		
		PVDoubleArray aDoubleArray = pvStructure.getSubField(PVDoubleArray.class, "angles");
		DoubleArrayData aDoubleArrayData = new DoubleArrayData();
		aDoubleArray.get(0, aDoubleArray.getLength(), aDoubleArrayData);
		roi.setAngles(aDoubleArrayData.data);
		
		PVDoubleArray adDoubleArray = pvStructure.getSubField(PVDoubleArray.class, "anglesDegrees");
		DoubleArrayData adDoubleArrayData = new DoubleArrayData();
		adDoubleArray.get(0, adDoubleArray.getLength(), adDoubleArrayData);
		roi.setAnglesDegrees(adDoubleArrayData.data);
		
		roi.setDpp(pvStructure.getSubField(PVDouble.class, "dpp").get());
		
		roi.setAverageArea(pvStructure.getSubField(PVBoolean.class, "averageArea").get());
		
		roi.setSymmetry(pvStructure.getSubField(PVInt.class, "symmetry").get());
		
		return roi;
	}
}
