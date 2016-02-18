package org.eclipse.scanning.api.malcolm.models;

/**
 * 
 * Bean for requesting a malcolm device. May be set in a ScanRequest to provide
 * the information to connect to malcolm.
 * 
 * @author Matthew Gerring
 *
 */
public class MalcolmRequest<T> {
	

	/**
	 * Name of the malcolm device which we would like to run.
	 * Must be set.
	 */
	private String deviceName;

	/**
	 * The model for the detector.
	 * Must be set
	 */
	private T deviceModel;
	
	/**
	 * The hostname to construct the malcolm uri with. 
	 * Optional to set, attempts to default to local machine of the server
	 * or one provided to the implementation of IDeviceService via spring.
	 */
	private String hostName;
	
	/**
	 * The port to use when constructing the malcolm uri. Default is 5600.
	 */
	private int port = 5600;

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public T getDeviceModel() {
		return deviceModel;
	}

	public void setDeviceModel(T deviceModel) {
		this.deviceModel = deviceModel;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((deviceModel == null) ? 0 : deviceModel.hashCode());
		result = prime * result + ((deviceName == null) ? 0 : deviceName.hashCode());
		result = prime * result + ((hostName == null) ? 0 : hostName.hashCode());
		result = prime * result + port;
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
		MalcolmRequest other = (MalcolmRequest) obj;
		if (deviceModel == null) {
			if (other.deviceModel != null)
				return false;
		} else if (!deviceModel.equals(other.deviceModel))
			return false;
		if (deviceName == null) {
			if (other.deviceName != null)
				return false;
		} else if (!deviceName.equals(other.deviceName))
			return false;
		if (hostName == null) {
			if (other.hostName != null)
				return false;
		} else if (!hostName.equals(other.hostName))
			return false;
		if (port != other.port)
			return false;
		return true;
	}

}
