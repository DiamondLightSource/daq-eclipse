package org.eclipse.scanning.api.event.scan;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scanning.api.points.IPosition;
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
	

	/** 
	 * The names of the detectors to use in the scan, may be null.
	 */
	private Map<String, Object> detectors;
	
	/**
	 * The names of monitors in the scan, may be null.
	 */
	private String[] monitorNames;
	
	/**
	 * Part or all of the file path to be used for this scan.
	 */
	private String filePath;
	
	/**
	 * The start position or null if there is no start position to move to.
	 */
	private IPosition start;
	
	/**
	 * The end position or null if there is no start position to move to.
	 */
	private IPosition end;

	public ScanRequest() {
		
	}
	
	public ScanRequest(IScanPathModel model, String filePath, String... monitorNames) {
		super();
		this.model = model;
		this.monitorNames = monitorNames;
		this.filePath = filePath;
	}

	public IScanPathModel getModel() {
		return model;
	}

	public void setModel(IScanPathModel model) {
		this.model = model;
	}


	public String[] getMonitorNames() {
		return monitorNames;
	}

	public void setMonitorNames(String... monitorNames) {
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
		result = prime * result + ((detectors == null) ? 0 : detectors.hashCode());
		result = prime * result + ((end == null) ? 0 : end.hashCode());
		result = prime * result + ((filePath == null) ? 0 : filePath.hashCode());
		result = prime * result + ((model == null) ? 0 : model.hashCode());
		result = prime * result + Arrays.hashCode(monitorNames);
		result = prime * result + ((start == null) ? 0 : start.hashCode());
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
		if (detectors == null) {
			if (other.detectors != null)
				return false;
		} else if (!detectors.equals(other.detectors))
			return false;
		if (end == null) {
			if (other.end != null)
				return false;
		} else if (!end.equals(other.end))
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
		if (start == null) {
			if (other.start != null)
				return false;
		} else if (!start.equals(other.start))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ScanRequest [model=" + model + ", detectors=" + detectors + ", monitorNames="
				+ Arrays.toString(monitorNames) + ", filePath=" + filePath + ", start=" + start + ", end=" + end + "]";
	}

	public Map<String, Object> getDetectors() {
		return detectors;
	}

	public void setDetectors(Map<String, Object> detectors) {
		this.detectors = detectors;
	}

	public void putDetector(String name, Object dmodel) {
		if (detectors==null) detectors = new HashMap<>(3);
		detectors.put(name, dmodel);
	}

	public IPosition getStart() {
		return start;
	}

	public void setStart(IPosition start) {
		this.start = start;
	}

	public IPosition getEnd() {
		return end;
	}

	public void setEnd(IPosition end) {
		this.end = end;
	}
	
}
