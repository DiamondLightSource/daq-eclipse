package org.eclipse.scanning.api.malcolm.models;

import org.eclipse.scanning.api.device.models.IDetectorModel;


/**
 * Simple abstract base class for Malcolm detector models, with a field for the Malcolm connection info.
 *
 * @author Colin Palmer
 *
 */
public abstract class MalcolmDetectorModel implements IDetectorModel {

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
		MalcolmDetectorModel other = (MalcolmDetectorModel) obj;
		if (connectionInfo == null) {
			if (other.connectionInfo != null)
				return false;
		} else if (!connectionInfo.equals(other.connectionInfo))
			return false;
		return true;
	}
}
