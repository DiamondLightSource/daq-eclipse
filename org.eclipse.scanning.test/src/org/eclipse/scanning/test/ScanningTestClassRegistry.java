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

package org.eclipse.scanning.test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.persistence.IClassRegistry;
import org.eclipse.scanning.test.event.queues.dummy.DummyAtom;
import org.eclipse.scanning.test.event.queues.dummy.DummyBean;
import org.eclipse.scanning.test.event.queues.dummy.DummyHasQueue;
import org.eclipse.scanning.test.scan.mock.AnnotatedMockDetectorModel;
import org.eclipse.scanning.test.scan.mock.MockDetectorModel;
import org.eclipse.scanning.test.scan.mock.MockWritingMandlebrotModel;
import org.eclipse.scanning.test.scan.real.TestScanBean;

public class ScanningTestClassRegistry implements IClassRegistry {

	private static final Map<String, Class<?>> idToClassMap;
	static {
		Map<String, Class<?>> tmp = new HashMap<String, Class<?>>();
		
		// scan.mock
		registerClass(tmp, AnnotatedMockDetectorModel.class);
		registerClass(tmp, MockDetectorModel.class);
		registerClass(tmp, MockWritingMandlebrotModel.class);	
		
		// event.queues.dummy
		registerClass(tmp, DummyAtom.class);
		registerClass(tmp, DummyBean.class);
		registerClass(tmp, DummyHasQueue.class);

		// scan.real
		registerClass(tmp, TestScanBean.class);

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
