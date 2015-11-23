package org.eclipse.scanning.api.event.scan;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;


/**
 * This bean is used to disseminate messages about what has happened
 * to the scan while it is being written.
 * 
 * Do not extend this class to allow arbitrary information to be sent.
 * The event encapsulated by this bean should be sending just the information
 * defined here, metadata that cannot circumvent the nexus file. 
 * 
 * For instance adding a dynamic set of information, a map perhaps, would
 * allow information which should be saved in the Nexus file to circumvent
 * the file and be set in the event. It was decided in various meetings
 * that doing this could mean that some data is not recorded as it should be
 * in nexus. Therefore these events are simply designed to contain events
 * not data. They are not the same as the old ScanDataPoint system in GDA
 * 
 * @author Matthew Gerring
 *
 */
public final class ScanBean extends StatusBean { 
		
	// General Information
	private String  deviceName;
	private String  beamline;
	
	// Where are we in the scan
	private int     point;
	private int     size;
	private Map<String, Object> value;
	
	// State information
	private DeviceState   deviceState;
	private DeviceState   previousDeviceState;
	
	// Dataset information
	private String  filePath;
	private String  datasetPath;
    private int[]   oldShape;
    private int[]   newShape;
		
	public ScanBean() {
        super();
	}
	
	public ScanBean(DeviceState state, String message) {
		this(state);
		this.message = message;
	}

	public ScanBean(DeviceState state) {
		this.deviceState = state;
	}
	
	
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public String getDatasetPath() {
		return datasetPath;
	}
	public void setDatasetPath(String datasetPath) {
		this.datasetPath = datasetPath;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((beamline == null) ? 0 : beamline.hashCode());
		result = prime * result
				+ ((datasetPath == null) ? 0 : datasetPath.hashCode());
		result = prime * result
				+ ((deviceName == null) ? 0 : deviceName.hashCode());
		result = prime * result
				+ ((deviceState == null) ? 0 : deviceState.hashCode());
		result = prime * result
				+ ((filePath == null) ? 0 : filePath.hashCode());
		result = prime * result + Arrays.hashCode(newShape);
		result = prime * result + Arrays.hashCode(oldShape);
		result = prime * result + point;
		result = prime
				* result
				+ ((previousDeviceState == null) ? 0 : previousDeviceState
						.hashCode());
		result = prime * result + size;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScanBean other = (ScanBean) obj;
		if (beamline == null) {
			if (other.beamline != null)
				return false;
		} else if (!beamline.equals(other.beamline))
			return false;
		if (datasetPath == null) {
			if (other.datasetPath != null)
				return false;
		} else if (!datasetPath.equals(other.datasetPath))
			return false;
		if (deviceName == null) {
			if (other.deviceName != null)
				return false;
		} else if (!deviceName.equals(other.deviceName))
			return false;
		if (deviceState != other.deviceState)
			return false;
		if (filePath == null) {
			if (other.filePath != null)
				return false;
		} else if (!filePath.equals(other.filePath))
			return false;
		if (!Arrays.equals(newShape, other.newShape))
			return false;
		if (!Arrays.equals(oldShape, other.oldShape))
			return false;
		if (point != other.point)
			return false;
		if (previousDeviceState != other.previousDeviceState)
			return false;
		if (size != other.size)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getBeamline() {
		return beamline;
	}

	public void setBeamline(String beanline) {
		this.beamline = beanline;
	}

	public DeviceState getDeviceState() {
		return deviceState;
	}

	public void setDeviceState(DeviceState state) {
		this.previousDeviceState = this.deviceState;
		this.deviceState = state;
	}

	public int[] getOldShape() {
		return oldShape;
	}

	public void setOldShape(int[] oldShape) {
		this.oldShape = oldShape;
	}

	public int[] getNewShape() {
		return newShape;
	}

	public void setNewShape(int[] newShape) {
		this.newShape = newShape;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "ScanBean [deviceName=" + deviceName + ", beamline=" + beamline
				+ ", point=" + point + ", size=" + size + ", value=" + value
				+ ", deviceState=" + deviceState + ", previousDeviceState="
				+ previousDeviceState + ", filePath=" + filePath
				+ ", datasetPath=" + datasetPath + ", oldShape="
				+ Arrays.toString(oldShape) + ", newShape="
				+ Arrays.toString(newShape) + super.toString()+"]";
	}

	public DeviceState getPreviousDeviceState() {
		return previousDeviceState;
	}

	public void setPreviousDeviceState(DeviceState previousState) {
		this.previousDeviceState = previousState;
	}

	public int getPoint() {
		return point;
	}

	public void setPoint(int frame) {
		this.point = frame;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public Map<String, Object> getValue() {
		return value;
	}

	public void setValue(Map<String, Object> value) {
		this.value = value;
	}
	
	public void putValue(String name, Object val) {
		if (value == null) value = new HashMap<>(3);
		value.put(name, val);
	}
	
	public boolean scanStart() {
		return Status.QUEUED ==previousStatus && Status.RUNNING==status;
	}

	public boolean scanEnd() {
		return Status.RUNNING ==previousStatus && Status.RUNNING!=status;
	}

}
