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
package org.eclipse.scanning.test.scan.mock;

import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;
import org.eclipse.dawnsci.analysis.api.processing.OperationRank;
import org.eclipse.dawnsci.analysis.api.processing.model.ValueModel;
import org.eclipse.dawnsci.analysis.dataset.operations.AbstractOperationBase;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.IDataset;

/**
 * Maths operations are bascially just for testing at the moment.
 * 
 * They do not currently operate in a generic way, see FIXMEs below.
 * 
 * @author Matthew Gerring
 *
 */
abstract class MockAbstractMathsOperation<T extends ValueModel, D extends OperationData> extends AbstractOperationBase<ValueModel, OperationData> {

	/**
	 * TODO This operation is only an example.
	 */
	@Override
	public OperationData execute(IDataset a, IMonitor monitor) throws OperationException {
		
		try {
			IDataset result= operation(a, model.getValue());
			// TODO Need to set up axes and meta correctly.
			return new OperationData(result);
			
		} catch (Exception e) {
			throw new OperationException(this, e);
		}
	}
	
	protected abstract IDataset operation(IDataset a, Object value);

	
	public OperationRank getInputRank() {
		return OperationRank.ANY; // Images
	}
	public OperationRank getOutputRank() {
		return OperationRank.SAME; // Addition for instance
	}

}
