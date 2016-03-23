package org.eclipse.scanning.api.event.scan;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.script.ScriptRequest;
import org.eclipse.scanning.api.script.ScriptResponse;

/**
 * A class to encapsulate minimal information required to run a scan.
 * 
 * For instance the JSON string of this class could be used on B23
 * 
 * The class automatically assigns a unique id for the run.
 * 
 * @author Matthew Gerring
 * @param T must be type of region that the regions correspond to. For instance IROI for any region type or IRectangularROI is all known to be rectangular.
 *
 */
public class ScanRequest<T> {

	/**
	 * The models for generating the points for a scan 
	 * The models must be in the same nested order that the
	 * compound scan will be generated as.
	 * 
	 * e.g. a StepModel
	 */
	private IScanPathModel[] models;
	
	/**
	 * A map of the unique id of a model to the set of regions (if any) required by that model.
	 */
	private Map<String, T[]> regions;

	/** 
	 * The names of the detectors to use in the scan, may be null.
	 */
	private Map<String, IDetectorModel> detectors;
	
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
	 * The script run before the data collection but after the start position has been set.
	 */
	private ScriptRequest     before;
	private ScriptResponse<?> beforeResponse;

	/**
	 * The end position or null if there is no start position to move to.
	 */
	private IPosition end;
	
	
	/**
	 * The script run after the data collection but before the end position has been set.
	 */
	private ScriptRequest after;
	private ScriptResponse<?> afterResponse;

	/**
	 * Set to ignore processing of this request if the request has been 
	 * prepared for a specific server. For instance in the case where the client
	 * has build a legal scan request for a given beamline, it will not want this
	 * request preprocessed.
	 * 
	 * Default is false.
	 */
	private boolean ignorePreprocess;

	public ScanRequest() {

	}
	
	public ScanRequest(IScanPathModel model, String filePath, String... monitorNames) {
		super();
		models = new IScanPathModel[]{model};
		this.monitorNames = monitorNames;
		this.filePath = filePath;
	}
	
	public ScanRequest(IScanPathModel model, T region, String filePath, String... monitorNames) {
		super();
		models = new IScanPathModel[]{model};
		putRegion(model.getUniqueKey(), region);
		this.monitorNames = monitorNames;
		this.filePath = filePath;
	}

	public IScanPathModel[] getModels() {
		return models;
	}
	
	public void setModels(IScanPathModel... models) {
		this.models = models;
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
		result = prime * result + ((after == null) ? 0 : after.hashCode());
		result = prime * result + ((afterResponse == null) ? 0 : afterResponse.hashCode());
		result = prime * result + ((before == null) ? 0 : before.hashCode());
		result = prime * result + ((beforeResponse == null) ? 0 : beforeResponse.hashCode());
		result = prime * result + ((detectors == null) ? 0 : detectors.hashCode());
		result = prime * result + ((end == null) ? 0 : end.hashCode());
		result = prime * result + ((filePath == null) ? 0 : filePath.hashCode());
		result = prime * result + (ignorePreprocess ? 1231 : 1237);
		result = prime * result + Arrays.hashCode(models);
		result = prime * result + Arrays.hashCode(monitorNames);
		result = prime * result + ((regions == null) ? 0 : regions.hashCode());
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
		ScanRequest<?> other = (ScanRequest<?>) obj;
		if (after == null) {
			if (other.after != null)
				return false;
		} else if (!after.equals(other.after))
			return false;
		if (afterResponse == null) {
			if (other.afterResponse != null)
				return false;
		} else if (!afterResponse.equals(other.afterResponse))
			return false;
		if (before == null) {
			if (other.before != null)
				return false;
		} else if (!before.equals(other.before))
			return false;
		if (beforeResponse == null) {
			if (other.beforeResponse != null)
				return false;
		} else if (!beforeResponse.equals(other.beforeResponse))
			return false;
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
		if (ignorePreprocess != other.ignorePreprocess)
			return false;
		if (!Arrays.equals(models, other.models))
			return false;
		if (!Arrays.equals(monitorNames, other.monitorNames))
			return false;
		if (regions == null) {
			if (other.regions != null)
				return false;
		} else if (!regions.equals(other.regions))
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
		return "ScanRequest [models=" + Arrays.toString(models) + ", detectors=" + detectors + ", monitorNames="
				+ Arrays.toString(monitorNames) + ", filePath=" + filePath + ", start=" + start + ", end=" + end + "]";
	}

	public Map<String, IDetectorModel> getDetectors() {
		return detectors;
	}

	public void setDetectors(Map<String, IDetectorModel> detectors) {
		this.detectors = detectors;
	}

	public void putDetector(String name, IDetectorModel dmodel) {
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

	public Map<String, T[]> getRegions() {
		return regions;
	}
	public T[] getRegions(String uniqueKey) {
		if (regions==null) return null;
		return regions.get(uniqueKey);
	}

	public void setRegions(Map<String, T[]> regions) {
		this.regions = regions;
	}
	
	@SafeVarargs
	public final void putRegion(String unqiueId, T... areas) {
		if (this.regions==null) this.regions = new HashMap<>(3);
		this.regions.put(unqiueId, areas);
	}

	public boolean isIgnorePreprocess() {
		return ignorePreprocess;
	}

	public void setIgnorePreprocess(boolean ignorePreprocess) {
		this.ignorePreprocess = ignorePreprocess;
	}

	public ScriptRequest getBefore() {
		return before;
	}

	public void setBefore(ScriptRequest before) {
		this.before = before;
	}

	public ScriptRequest getAfter() {
		return after;
	}

	public void setAfter(ScriptRequest after) {
		this.after = after;
	}

	public ScriptResponse<?> getBeforeResponse() {
		return beforeResponse;
	}

	public void setBeforeResponse(ScriptResponse<?> beforeResponse) {
		this.beforeResponse = beforeResponse;
	}

	public ScriptResponse<?> getAfterResponse() {
		return afterResponse;
	}

	public void setAfterResponse(ScriptResponse<?> afterResponse) {
		this.afterResponse = afterResponse;
	}


}
