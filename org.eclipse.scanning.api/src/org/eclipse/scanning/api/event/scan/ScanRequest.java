package org.eclipse.scanning.api.event.scan;

import java.util.Arrays;

import org.eclipse.scanning.api.points.models.IScanPathModel;

/**
 * A class to encapsulate minimal information required to run a scan.
 * 
 * For instance the JSON string of this class could be used on B23
 * 
 * The class automatically assigns a unique id for the run.
 * 
 * @author Matthew Gerring
 *
 */
public class ScanRequest {

	/**
	 * The model for generating the points for a scan 
	 * e.g. a StepModel
	 */
	private IScanPathModel model;
	
	public ScanRequest() {
		
	}
	
	public ScanRequest(IScanPathModel model, String[] detectorNames, String[] monitorNames, String filePath) {
		super();
		this.model = model;
		this.detectorNames = detectorNames;
		this.monitorNames = monitorNames;
		this.filePath = filePath;
	}

	/** 
	 * The names of the detectors to use in the scan, may be null.
	 */
	private String[] detectorNames;
	
	/**
	 * The names of monitors in the scan, may be null.
	 */
	private String[] monitorNames;
	
	/**
	 * Part or all of the file path to be used for this scan.
	 */
	private String filePath;

	public IScanPathModel getModel() {
		return model;
	}

	public void setModel(IScanPathModel model) {
		this.model = model;
	}

	public String[] getDetectorNames() {
		return detectorNames;
	}

	public void setDetectorNames(String... detectorNames) {
		this.detectorNames = detectorNames;
	}

	public String[] getMonitorNames() {
		return monitorNames;
	}

	public void setMonitorNames(String[] monitorNames) {
		this.monitorNames = monitorNames;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(detectorNames);
		result = prime * result + ((filePath == null) ? 0 : filePath.hashCode());
		result = prime * result + ((model == null) ? 0 : model.hashCode());
		result = prime * result + Arrays.hashCode(monitorNames);
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
		ScanRequest other = (ScanRequest) obj;
		if (!Arrays.equals(detectorNames, other.detectorNames))
			return false;
		if (filePath == null) {
			if (other.filePath != null)
				return false;
		} else if (!filePath.equals(other.filePath))
			return false;
		if (model == null) {
			if (other.model != null)
				return false;
		} else if (!model.equals(other.model))
			return false;
		if (!Arrays.equals(monitorNames, other.monitorNames))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ScanRequest [pointsModelClass=" + model.getClass() + ", detectorNames=" + Arrays.toString(detectorNames) + ", monitorNames="
				+ Arrays.toString(monitorNames) + ", filePath=" + filePath + "]";
	}
	
}
