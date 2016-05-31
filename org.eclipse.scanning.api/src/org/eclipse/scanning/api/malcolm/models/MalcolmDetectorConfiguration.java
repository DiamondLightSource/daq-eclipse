package org.eclipse.scanning.api.malcolm.models;

/**
 * A class to provide a malcolm model and the information required to connect to the malcolm device.
 *
 * @author Matthew Gerring
 *
 */
public class MalcolmDetectorConfiguration<T> {
	
	/**
	 * The model that will  be sent to the MalcolmDevice for instance
	 * OneDetectorTestMappingModel which can be sent to the device safely.
	 */
	private T model;
	
	/**
	 * This should not be sent to the malcolm device, it is logic to do with the connection.
	 */
	private MalcolmConnectionInfo connectionInfo;

	public MalcolmConnectionInfo getConnectionInfo() {
		return connectionInfo;
	}

	public void setConnectionInfo(MalcolmConnectionInfo connectionInfo) {
		this.connectionInfo = connectionInfo;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((connectionInfo == null) ? 0 : connectionInfo.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MalcolmDetectorConfiguration other = (MalcolmDetectorConfiguration) obj;
		if (connectionInfo == null) {
			if (other.connectionInfo != null)
				return false;
		} else if (!connectionInfo.equals(other.connectionInfo))
			return false;
		return true;
	}

	public T getModel() {
		return model;
	}

	public void setModel(T payload) {
		this.model = payload;
	}

}
