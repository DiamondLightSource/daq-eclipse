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
package org.eclipse.scanning.test.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.filter.Filter;
import org.eclipse.scanning.api.filter.IFilter;
import org.eclipse.scanning.api.filter.IFilterService;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.example.scannable.MockScannableConnector;
import org.eclipse.scanning.server.application.PseudoSpringParser;
import org.junit.BeforeClass;
import org.junit.Test;

public class FilterTest {
	
	private static IScannableDeviceService sservice;
	private static IFilterService          fservice;

	@BeforeClass
	public static void parseSpring() throws Exception {
		
		fservice = IFilterService.DEFAULT;
		sservice = new MockScannableConnector(null);
	}
	
	@Test
	public void notNull() {
		assertNotNull(IFilterService.DEFAULT);
		assertNotNull(IFilterService.DEFAULT.getFilter("org.eclipse.scanning.scannableFilter"));
		assertEquals(7, IFilterService.DEFAULT.getFilter("org.eclipse.scanning.scannableFilter").getExcludes().size());
		assertEquals(5, IFilterService.DEFAULT.getFilter("org.eclipse.scanning.scannableFilter").getIncludes().size());
	}
	
	@Test
	public void noFilter() throws ScanningException {
		assertEquals(sservice.getScannableNames(), fservice.filter("not.there", sservice.getScannableNames()));
	}
	
	@Test
	public void testFilterSpring() throws Exception {
		
		fservice.clear();
		PseudoSpringParser parser = new PseudoSpringParser();
		parser.parse(FilterTest.class.getResourceAsStream("test_filters.xml"));
		
		check();
	}

	@Test
	public void testFilterManual() throws Exception {
		
		fservice.clear();
		
		// Spring does this for us in "test_filters.xml"
		IFilter<String> filter = new Filter();
		filter.setName("org.eclipse.scanning.scannableFilter");
		filter.setExcludes(Arrays.asList("qvach", "monitor1", "a", "b", "c", "beam.*", "neXusScannable.*"));
		filter.setIncludes(Arrays.asList("monitor.*", "beamcurrent", "neXusScannable2", "neXusScannable", "rubbish"));
		fservice.register(filter);
		
		check();
	}

	private void check() throws ScanningException {
		
		sservice.getScannable("aa"); // Create an aa scannable
		List<String> names    = sservice.getScannableNames();
		List<String> filtered = fservice.filter("org.eclipse.scanning.scannableFilter", names);
		
		// Stuff not matched should be there
		assertTrue(filtered.contains("stage_x"));
		assertTrue(filtered.contains("stage_y"));
		assertTrue(filtered.contains("x"));
		assertTrue(filtered.contains("y"));
		assertTrue(filtered.contains("z"));

		// Stuff that got included
		assertTrue(filtered.contains("monitor1"));
		assertTrue(filtered.contains("neXusScannable2"));
		assertTrue(filtered.contains("beamcurrent"));
		assertTrue(filtered.contains("aa")); // The one we created
		
		// Things which should surely not be around.
		assertFalse(filtered.contains("qvach"));
		assertFalse(filtered.contains("a"));
		assertFalse(filtered.contains("b"));
		assertFalse(filtered.contains("c"));
		assertFalse(filtered.contains("rubbish"));
		assertFalse(filtered.contains("neXusScannable"));
		assertFalse(filtered.contains("neXusScannable1"));
		assertFalse(filtered.contains("neXusScannable3"));
		assertFalse(filtered.contains("neXusScannable4"));
	}

	
}
