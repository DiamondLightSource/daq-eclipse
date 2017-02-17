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
package org.eclipse.scanning.command.factory;

import java.util.Collection;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.scanning.api.points.models.RepeatedPointModel;

class RepeatedPointExpresser extends PyModelExpresser<RepeatedPointModel> {

	@Override
	public String pyExpress(RepeatedPointModel model, Collection<IROI> rois, boolean verbose) {
		
		if (rois != null && rois.size() > 0) throw new IllegalStateException("RepeatedPointModel cannot be associated with ROIs.");
		
		// TODO Use StringBuilder
		return "repeat("
			+(verbose?"axis=":"")
			+"'"+model.getName()+"'"+", "
			+(verbose?"count=":"")
			+model.getCount()+", "
			+(verbose?"value=":"")
			+model.getValue()+", "
			+(verbose?"sleep=":"")
			+model.getSleep()
		+")";
	}

}
