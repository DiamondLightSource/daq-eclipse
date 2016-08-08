package org.eclipse.scanning.api;

/**
 * 
 * This is supposed to fit any object which takes a model and
 * can notify the user if the a given model is valid or not.
 * For instance IRunnableDevice, IPointGenerator.
 * 
 * @author Matthew Gerring
 *
 */
public interface IValidator<T> {

	/**
	 * If the given model is considered "invalid", this method throws a 
	 * PointsValidationException explaining why it is considered invalid.
	 * Otherwise, just returns. A model should be considered invalid if its
	 * parameters would cause the generator implementation to hang or crash.
	 * 
	 * @throw exception if model invalid
	 * @return 
	 */
	default void validate(T model) throws Exception {
		return; // They should implement a validation which throws an exception
	}

}
