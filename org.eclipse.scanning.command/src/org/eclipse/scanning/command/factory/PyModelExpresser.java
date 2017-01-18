package org.eclipse.scanning.command.factory;

import java.util.Collection;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.scanning.command.PyExpressionNotImplementedException;

/**
 * You must override at least one of the pyExpress(...) methods.
 * 
 * @author Matthew Gerring
 *
 * @param <T> the model for which we are expressing.
 */
abstract class PyModelExpresser<T> {

	protected PyExpressionFactory factory;
	void setFactory(PyExpressionFactory f) {
		factory = f;
	}
	/**
	 * Call to express the model T as a python command.
	 * @param model
	 * @param rois
	 * @param verbose
	 * @return py string
	 */
	String pyExpress(T model, boolean verbose) throws Exception {
		return pyExpress(model, null, verbose);
	}
	/**
	 * Call to express the model T as a python command.
	 * @param model
	 * @param rois
	 * @param verbose
	 * @return py string
	 */
	String pyExpress(T model, Collection<IROI> rois, boolean verbose) throws Exception {
		throw new PyExpressionNotImplementedException("Cannot express "+model+" with rois "+rois);
	}
}
