package org.eclipse.scanning.api;

import org.eclipse.scanning.api.scan.ScanningException;

/**
 * 
 * Implementors of this interface may be configured with a model.
 * 
 * @author Matthew Gerring
 *
 */
public interface IConfigurable<M> {

	/**
	 * Call to configure the device. If the model provided is
	 * invalid, a scanning exception will be thrown.
	 * 
	 * @param model
	 * @throws ScanningException
	 */
	void configure(M model) throws ScanningException;
}
