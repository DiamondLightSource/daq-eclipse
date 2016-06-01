/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.scanning.test.scan.mock;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.model.ValueModel;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Maths;

/**
 * Subtracts either one dataset from another or a scalar value from all values of a dataset.
 * @author Matthew Gerring
 *
 */
class MockSubtractOperation extends MockAbstractMathsOperation<ValueModel, OperationData> {
	
	protected IDataset operation(IDataset a, Object value) {
		return a instanceof Dataset ? ((Dataset) a).isubtract(value) : Maths.subtract(a, value);
	}

	@Override
	public String getId() {
		return "uk.ac.diamond.scisoft.analysis.processing.subtractOperation";
	}
	@Override
    public String getName() {
		return "Subtract datasets";
	}
	
	
}