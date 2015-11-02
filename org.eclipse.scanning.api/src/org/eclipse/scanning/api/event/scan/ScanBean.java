package org.eclipse.scanning.api.event.scan;

import java.util.Arrays;
import java.util.UUID;

import org.eclipse.scanning.api.event.IdBean;


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
public final class ScanBean extends IdBean { 
		
	// General Information
	private String  deviceName;
	private String  beamline;
	private String  message;
	
	private String  scanName;
	private double  start;
	private double  stop;
	private double  step;
	private double  percentComplete;
	
	// State information
	private State   state;
	private State   previousState;
	
	// Dataset information
	private String  filePath;
	private String  datasetPath;
    private int[]   oldShape;
    private int[]   newShape;
		
	public ScanBean() {

	}
	
	public ScanBean(State state, String message) {
		this(state);
		this.message = message;
	}

	public ScanBean(State state) {
		this.state = state;
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
				+ ((filePath == null) ? 0 : filePath.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + Arrays.hashCode(newShape);
		result = prime * result + Arrays.hashCode(oldShape);
		long temp;
		temp = Double.doubleToLongBits(percentComplete);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result
				+ ((previousState == null) ? 0 : previousState.hashCode());
		result = prime * result
				+ ((scanName == null) ? 0 : scanName.hashCode());
		temp = Double.doubleToLongBits(start);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		temp = Double.doubleToLongBits(step);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(stop);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		if (filePath == null) {
			if (other.filePath != null)
				return false;
		} else if (!filePath.equals(other.filePath))
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (!Arrays.equals(newShape, other.newShape))
			return false;
		if (!Arrays.equals(oldShape, other.oldShape))
			return false;
		if (Double.doubleToLongBits(percentComplete) != Double
				.doubleToLongBits(other.percentComplete))
			return false;
		if (previousState != other.previousState)
			return false;
		if (scanName == null) {
			if (other.scanName != null)
				return false;
		} else if (!scanName.equals(other.scanName))
			return false;
		if (Double.doubleToLongBits(start) != Double
				.doubleToLongBits(other.start))
			return false;
		if (state != other.state)
			return false;
		if (Double.doubleToLongBits(step) != Double
				.doubleToLongBits(other.step))
			return false;
		if (Double.doubleToLongBits(stop) != Double
				.doubleToLongBits(other.stop))
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

	public double getPercentComplete() {
		return percentComplete;
	}

	public void setPercentComplete(double percentComplete) {
		this.percentComplete = percentComplete;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.previousState = this.state;
		this.state = state;
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
				+ ", message=" + message + ", scanName=" + scanName
				+ ", start=" + start + ", stop=" + stop + ", step=" + step
				+ ", percentComplete=" + percentComplete + ", state=" + state
				+ ", previousState=" + previousState + ", filePath=" + filePath
				+ ", datasetPath=" + datasetPath + ", oldShape="
				+ Arrays.toString(oldShape) + ", newShape="
				+ Arrays.toString(newShape) + "]";
	}

	public State getPreviousState() {
		return previousState;
	}

	public void setPreviousState(State previousState) {
		this.previousState = previousState;
	}

	public String getScanName() {
		return scanName;
	}

	public void setScanName(String scanName) {
		this.scanName = scanName;
	}

	public double getStart() {
		return start;
	}

	public void setStart(double start) {
		this.start = start;
	}

	public double getStop() {
		return stop;
	}

	public void setStop(double stop) {
		this.stop = stop;
	}

	public double getStep() {
		return step;
	}

	public void setStep(double step) {
		this.step = step;
	}
}
