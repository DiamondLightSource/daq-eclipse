/*-
 * Copyright © 2016 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package org.eclipse.scanning.example.classregistry;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.persistence.IClassRegistry;
import org.eclipse.scanning.api.malcolm.attributes.BooleanArrayAttribute;
import org.eclipse.scanning.api.malcolm.attributes.BooleanAttribute;
import org.eclipse.scanning.api.malcolm.attributes.ChoiceAttribute;
import org.eclipse.scanning.api.malcolm.attributes.MalcolmAttribute;
import org.eclipse.scanning.api.malcolm.attributes.NumberArrayAttribute;
import org.eclipse.scanning.api.malcolm.attributes.NumberAttribute;
import org.eclipse.scanning.api.malcolm.attributes.PointGeneratorAttribute;
import org.eclipse.scanning.api.malcolm.attributes.StringArrayAttribute;
import org.eclipse.scanning.api.malcolm.attributes.StringAttribute;
import org.eclipse.scanning.api.malcolm.attributes.TableAttribute;
import org.eclipse.scanning.example.detector.ConstantVelocityModel;
import org.eclipse.scanning.example.detector.DarkImageModel;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.example.malcolm.TestMalcolmModel;
import org.eclipse.scanning.example.scannable.MockBeanOnMonitor;
import org.eclipse.scanning.example.scannable.MockNeXusScannable;
import org.eclipse.scanning.example.scannable.MockScannable;
import org.eclipse.scanning.example.scannable.MockScannableConnector;
import org.eclipse.scanning.example.scannable.MockScannableModel;
import org.eclipse.scanning.example.scannable.MockTopupMonitor;

public class ScanningExampleClassRegistry implements IClassRegistry {

	private static final Map<String, Class<?>> idToClassMap;
	static {
		Map<String, Class<?>> tmp = new HashMap<String, Class<?>>();
		
		// detector
		registerClass(tmp, ConstantVelocityModel.class);
		registerClass(tmp, DarkImageModel.class);
		registerClass(tmp, MandelbrotModel.class);
		registerClass(tmp, TestMalcolmModel.class);
		registerClass(tmp, ChoiceAttribute.class);
		registerClass(tmp, BooleanArrayAttribute.class);
		registerClass(tmp, BooleanAttribute.class);
		registerClass(tmp, MalcolmAttribute.class);
		registerClass(tmp, NumberArrayAttribute.class);
		registerClass(tmp, NumberAttribute.class);
		registerClass(tmp, PointGeneratorAttribute.class);
		registerClass(tmp, StringArrayAttribute.class);
		registerClass(tmp, StringAttribute.class);
		registerClass(tmp, TableAttribute.class);

		// scannable
		registerClass(tmp, MockBeanOnMonitor.class);
		registerClass(tmp, MockNeXusScannable.class);
		registerClass(tmp, MockScannable.class);
		registerClass(tmp, MockScannableConnector.class);
		registerClass(tmp, MockScannableModel.class);
		registerClass(tmp, MockTopupMonitor.class);

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
