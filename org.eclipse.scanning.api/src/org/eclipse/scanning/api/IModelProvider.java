package org.eclipse.scanning.api;

/**
 * 
 * A model provider is any object that holds a model.
 * 
 * @author Matthew Gerring
 *
 * @param <T>
 */
public interface IModelProvider<T> {

	public T getModel();
}
