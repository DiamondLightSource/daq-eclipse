/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.scanning.device.ui.points;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.richbeans.widgets.table.ISeriesItemDescriptor;
import org.eclipse.richbeans.widgets.table.ISeriesItemFilter;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class GeneratorFilter implements ISeriesItemFilter {
	
	private static final Logger logger = LoggerFactory.getLogger(GeneratorFilter.class);
	
	private IPointGeneratorService pservice;
	private IEventConnectorService cservice;

	public GeneratorFilter(IPointGeneratorService pservice, IEventConnectorService cservice) {
		this.pservice     = pservice;
		this.cservice     = cservice;
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

	public List<GeneratorDescriptor<?>> createDescriptors(String key) throws Exception {
		
	    List<GeneratorDescriptor<?>> descriptions = new ArrayList<>();
		List<? extends IScanPathModel>  models = cservice.unmarshal(key, ArrayList.class);
		if (models!=null && models.size()>0) {
			for (IScanPathModel model : models) {
				final GeneratorDescriptor<?> des = new GeneratorDescriptor<>(model, pservice);
				if (!des.isVisible()) continue;
				descriptions.add(des);
			}
		}
		return descriptions;
	}
	
	public String createKey(List<ISeriesItemDescriptor> seriesItems) throws Exception {
		List<Object> models = new ArrayList<>();
		for (ISeriesItemDescriptor des : seriesItems) {
			if (des instanceof GeneratorDescriptor) {
				GeneratorDescriptor<?> gdes = (GeneratorDescriptor<?>)des;
				models.add(gdes.getModel());
			}
		}
		if (models.size()>0) {
		    return cservice.marshal(models);
		} else {
			return null;
		}
	}
	
}
