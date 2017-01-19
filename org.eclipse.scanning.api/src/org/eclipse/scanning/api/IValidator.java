package org.eclipse.scanning.api;

/**
 * 
 * This is supposed to fit any object which takes a model and
 * can notify the user if the a given model is valid or not.
 * For instance IRunnableDevice, IPointGenerator.
 * 
 * IMPORTANT: A model should not be an IValidator. Models should be 
 * maintained as vanilla as possible.
 * 
 * @author Matthew Gerring
 *
 */
public interface IValidator<T> {

	/**
	 * If the given model is considered "invalid", this method throws a 
	 * ModelValidationException explaining why it is considered invalid.
	 * Otherwise, just returns. A model should be considered invalid if its
	 * parameters would cause the generator implementation to hang or crash.
	 * 
	 * @throw exception if model invalid
	 * @return 
	 */
	default void validate(T model) throws ValidationException, InstantiationException, IllegalAccessException {
		return; // They should implement a validation which throws an exception
	}

	/**
	 * The validation server will set itself on any validator incase that validator
	 * want to validate sub-parts of a complex model.
	 * 
	 * @param vservice
	 */
	default void setService(IValidatorService vservice) {
		
	}
}
