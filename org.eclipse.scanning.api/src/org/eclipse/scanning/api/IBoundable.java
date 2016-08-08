package org.eclipse.scanning.api;

/**
 * 
 * Interface which, if implemented, will provide information
 * about the upper and lower values which the value may take.
 * 
 * In GDA8 there are several concepts of bounds, the underlying
 * hardware, the acquistion layer etc. In Solstice there is one
 * limit and it is in the same unit as the position.
 * 
 * @author Matthew Gerring
 *
 * @param <T>
 */
public interface IBoundable<T> {
	
	/**
	 * The position is the value which should be:
	 * 1. Greater than or equal to the lower
	 * 2. Less than or equal to the upper.
	 * 
	 * @return
	 * @throws Exception
	 */
	T getPosition() throws Exception;
 
	
	/**
	 * The upper limit in the same unit as the position.
	 *
	 * @return null if there is no upper limit. In this case it 
	 * will be the upper limit of the type T.
	 */
	default T getMaximum() {
		return null;
	}
	/**
	 * The lower limit in the same unit as the position.
	 * 
	 * @return null if there is no upper limit. In this case it 
	 * will be the upper limit of the type T.
	 */
	default T getMinimum() {
		return null;
	}

}
