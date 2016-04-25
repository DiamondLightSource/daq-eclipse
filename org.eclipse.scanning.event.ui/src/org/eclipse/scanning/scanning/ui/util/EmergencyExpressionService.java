package org.eclipse.scanning.scanning.ui.util;

import org.eclipse.dawnsci.analysis.api.expressions.IExpressionEngine;
import org.eclipse.dawnsci.analysis.api.expressions.IExpressionService;

public class EmergencyExpressionService implements IExpressionService {

	@Override
	public IExpressionEngine getExpressionEngine() {
		return new EmergencyExpressionEngine();
	}

}
