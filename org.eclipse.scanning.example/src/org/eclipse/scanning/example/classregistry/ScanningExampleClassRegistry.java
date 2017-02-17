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

package org.eclipse.scanning.example.classregistry;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.persistence.IClassRegistry;
import org.eclipse.scanning.example.detector.ConstantVelocityModel;
import org.eclipse.scanning.example.detector.DarkImageModel;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmTriggeredModel;
import org.eclipse.scanning.example.scannable.MockScannableModel;

public class ScanningExampleClassRegistry implements IClassRegistry {

	private static final Map<String, Class<?>> idToClassMap;
	static {
		Map<String, Class<?>> tmp = new HashMap<String, Class<?>>();
		
		// detector
		registerClass(tmp, ConstantVelocityModel.class);
		registerClass(tmp, DarkImageModel.class);
		registerClass(tmp, MandelbrotModel.class);
		registerClass(tmp, DummyMalcolmModel.class);
		registerClass(tmp, DummyMalcolmTriggeredModel.class);

		// scannable
		registerClass(tmp, MockScannableModel.class);

		idToClassMap = Collections.unmodifiableMap(tmp);
	}
	
	private static void registerClass(Map<String, Class<?>> map, Class<?> clazz) {
		map.put(clazz.getSimpleName(), clazz);
	}

	@Override
	public Map<String, Class<?>> getIdToClassMap() {
		return idToClassMap;
	}
}
