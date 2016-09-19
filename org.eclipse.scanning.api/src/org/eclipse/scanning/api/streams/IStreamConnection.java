package org.eclipse.scanning.api.streams;


public interface IStreamConnection<T> {

	/**
	 * T is any type which the plotting system may plot as
	 * a dynamic dataset.
	 * 
	 * @return
	 * @throws StreamConnectionException
	 */
	T connect() throws StreamConnectionException;

	/**
	 * Call to disconnect
	 * @throws StreamConnectionException
	 */
	void disconnect() throws StreamConnectionException;

    /**
     * Called to show the configuration options for the stream. 
     * For instance the URI to which it should connect.
     * 
     * @throws StreamConnectionException
     */
	void configure() throws StreamConnectionException;


	/** Id of the connection **/
	void setId(String id);
	String getId();

	/** Label of the connection **/
	void setLabel(String label);
	String getLabel();


}
