/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.scanning.scanning.ui.points;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.richbeans.widgets.table.ISeriesItemDescriptor;
import org.eclipse.richbeans.widgets.table.ISeriesItemFilter;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class GeneratorFilter implements ISeriesItemFilter {
	
	private static final Logger logger = LoggerFactory.getLogger(GeneratorFilter.class);
	
	private IPointGeneratorService pservice;

	public GeneratorFilter(IPointGeneratorService pservice) {
		this.pservice     = pservice;
	}
	
	@Override
	public Collection<ISeriesItemDescriptor> getDescriptors(String contents, int position, ISeriesItemDescriptor previous) {
		
		try {
			// Reassign previous, if required
			final Collection<String> ids = pservice.getRegisteredGenerators();			

			// Get sorted generator list.
			final Collection<ISeriesItemDescriptor> ret = new ArrayList<ISeriesItemDescriptor>(7);
			
			for (String id : ids) {
									
				final GeneratorDescriptor des = new GeneratorDescriptor(id, pservice);
				if (!des.isVisible()) continue;
				if (contents!=null && !des.matches(contents)) continue;
				ret.add(des);
			}

			return ret;
			
		} catch (Exception e) {
			logger.error("Cannot get operations!", e);
			return null;
		}
	}

	public List<GeneratorDescriptor> createDescriptors(List<String> ids) throws GeneratorException {
		List<GeneratorDescriptor> descriptions = new ArrayList<GeneratorDescriptor>();
		for (String id : ids) {
			final GeneratorDescriptor des = new GeneratorDescriptor(id, pservice);
			if (!des.isVisible()) continue;
			descriptions.add(des);
		}
		return descriptions;
	}
	
}
