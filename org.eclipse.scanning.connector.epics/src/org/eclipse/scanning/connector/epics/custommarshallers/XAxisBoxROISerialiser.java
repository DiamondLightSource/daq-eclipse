/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.connector.epics.custommarshallers;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.dawnsci.analysis.dataset.roi.XAxisBoxROI;
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
public class XAxisBoxROISerialiser implements IPVStructureSerialiser<XAxisBoxROI> {

	@Override
	public Structure buildStructure(Serialiser serialiser, XAxisBoxROI roi) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		FieldCreate fieldCreate = FieldFactory.getFieldCreate();
		
		Structure structure = fieldCreate.createFieldBuilder().
			addArray("lengths", ScalarType.pvDouble).
			add("angle", ScalarType.pvDouble).
			addArray("point", ScalarType.pvDouble).
			setId("XAxisBoxROI").
			createStructure();
		return structure;
	}

	@Override
	public void populatePVStructure(Serialiser serialiser, XAxisBoxROI roi, PVStructure pvStructure) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		PVDoubleArray lengths = pvStructure.getSubField(PVDoubleArray.class, "lengths");
		lengths.put(0, roi.getLengths().length, roi.getLengths(), 0);
		PVDouble angle = pvStructure.getSubField(PVDouble.class, "angle");
		angle.put(roi.getAngle());		
		PVDoubleArray point = pvStructure.getSubField(PVDoubleArray.class, "point");
		point.put(0, roi.getPoint().length, roi.getPoint(), 0);
	}
	
}
