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
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.command.ParserServiceImpl;
import org.eclipse.scanning.command.PyExpressionNotImplementedException;

class ScanRequestExpresser extends PyModelExpresser<ScanRequest<?>> {

	String pyExpress(ScanRequest<?> request, boolean verbose) throws Exception {
		
		
		// TODO Fragment should be a StringBuilder, it is more efficient.
		String fragment = "mscan(";
		boolean scanRequestPartiallyWritten = false;

		PyExpressionFactory factory = new PyExpressionFactory();
		if (request.getCompoundModel().getModels() != null
				&& request.getCompoundModel().getModels().size() > 0) {

			if (verbose) { fragment += "path="; }

			if (verbose || request.getCompoundModel().getModels().size() > 1) fragment += "[";
			boolean listPartiallyWritten = false;

			for (Object model : request.getCompoundModel().getModels()) {  // Order is important.
				if (listPartiallyWritten) fragment += ", ";
				Collection<IROI> rois = (Collection<IROI>) ParserServiceImpl.getPointGeneratorService().findRegions(model, request.getCompoundModel().getRegions());
				
				String smodel = factory.pyExpress(model, rois, verbose);
				fragment += smodel;
				listPartiallyWritten |= true;
			}

			if (verbose || request.getCompoundModel().getModels().size() > 1) fragment += "]";
			scanRequestPartiallyWritten |= true;
		}

		if (request.getMonitorNames() != null
				&& request.getMonitorNames().size() > 0) {

			if (scanRequestPartiallyWritten) fragment += ", ";
			if (verbose || !scanRequestPartiallyWritten) { fragment += "mon="; }

			if (verbose || request.getMonitorNames().size() > 1) fragment += "[";
			boolean listPartiallyWritten = false;

			for (String monitorName : request.getMonitorNames()) {
				if (listPartiallyWritten) fragment += ", ";
				fragment += "'"+monitorName+"'";
				listPartiallyWritten |= true;
			}

			if (verbose || request.getMonitorNames().size() > 1) fragment += "]";
			scanRequestPartiallyWritten |= true;
		}

		if (request.getDetectors() != null
				&& request.getDetectors().size() > 0) {

			if (scanRequestPartiallyWritten) fragment += ", ";
			if (verbose || !scanRequestPartiallyWritten) { fragment += "det="; }

			if (verbose || request.getDetectors().size() > 1) fragment += "[";

			boolean listPartiallyWritten = false;
			for (String detectorName : request.getDetectors().keySet()) {
				if (listPartiallyWritten) fragment += ", ";
				Object model = request.getDetectors().get(detectorName);
				if (model instanceof IDetectorModel) { // Probably is but does not have to be
					IDetectorModel dmodel = (IDetectorModel)model;
					fragment += "detector('"+detectorName+"', "+dmodel.getExposureTime()+")";
					// TODO We could compute the diffs between this model and the current
					// server version of it and add these as command line arguments.
				}
				listPartiallyWritten |= true;
			}
			
			if (verbose || request.getDetectors().size() > 1) fragment += "]";
			scanRequestPartiallyWritten |= true;
		}
		
		fragment += ")";
		return fragment;

	}
}

