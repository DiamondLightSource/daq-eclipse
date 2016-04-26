package org.eclipse.scanning.api.event.scan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.scan.models.ScanMetadata;
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
	private Collection<IScanPathModel> models;
	
	/**
	 * A map of the unique id of a model to the set of regions (if any) required by that model.
	 */
	private Map<String, Collection<T>> regions;

	/** 
	 * The names of the detectors to use in the scan, may be null.
	 */
	private Map<String, IDetectorModel> detectors;
	
	/**
	 * The names of monitors in the scan, may be null.
	 */
	private Collection<String> monitorNames;
	
	/**
	 * The names of metadata scannables. These are scannables whose field value(s)
	 * are written once during the scan, may be null.
	 */
	private Collection<String> metadataScannableNames;

	/**
	 * Scan metadata that is not produced by a particular device, e.g.
	 * scan command, chemical formula etc., grouped by type.
	 */
	private List<ScanMetadata> scanMetadata; // TODO use EnumMap instead of list?
	
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
		models = Arrays.asList(model);
		this.monitorNames = Arrays.asList(monitorNames);
		this.filePath = filePath;
	}
	
	public ScanRequest(IScanPathModel model, T region, String filePath, String... monitorNames) {
		this(model, filePath, monitorNames);
		putRegion(model.getUniqueKey(), region);
	}

	public Collection<IScanPathModel> getModels() {
		return models;
	}
	
	public void setModels(Collection<IScanPathModel> models) {
		this.models = models;
	}

	// This varargs implementation has been added for convenience of users of ScanRequest objects
	// However it requires special handling for serialization (since there are two setters) so be careful changing it!
	public void setModels(IScanPathModel... models) {
		setModels(Arrays.asList(models));
	}

	public Collection<String> getMonitorNames() {
		return monitorNames;
	}

	public void setMonitorNames(Collection<String> monitorNames) {
		this.monitorNames = monitorNames;
	}
	
	// This varargs implementation has been added for convenience of users of ScanRequest objects
	// However it requires special handling for serialization (since there are two setters) so be careful changing it!
	public void setMonitorNames(String... monitorNames) {
		setMonitorNames(Arrays.asList(monitorNames));
	}

	public Collection<String> getMetadataScannableNames() {
		return metadataScannableNames;
	}
	
	public void setMetadataScannableNames(Collection<String> metadataScannableNames) {
		this.metadataScannableNames = metadataScannableNames;
	}
	
	// This varargs implementation has been added for convenience of users of ScanRequest objects
	// However it requires special handling for serialization (since there are two setters) so be careful changing it!
	public void setMetadataScannableNames(String... metadataScannableNames) {
		setMetadataScannableNames(Arrays.asList(metadataScannableNames));
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
		result = prime * result + ((models == null) ? 0 : models.hashCode());
		result = prime * result + ((monitorNames == null) ? 0 : monitorNames.hashCode());
		result = prime * result + ((regions == null) ? 0 : regions.hashCode());
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		result = prime * result + ((scanMetadata == null) ? 0 : scanMetadata.hashCode());
		result = prime * result + ((metadataScannableNames == null) ? 0 : metadataScannableNames.hashCode());
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
		if (models == null) {
			if (other.models != null)
				return false;
		} else if (!models.equals(other.models))
			return false;
		if (monitorNames == null) {
			if (other.monitorNames != null)
				return false;
		} else if (!monitorNames.equals(other.monitorNames))
			return false;
		if (metadataScannableNames == null) {
			if (other.metadataScannableNames != null)
				return false;
		}
		if (regions == null) {
			if (other.regions != null)
				return false;
		} else if (!regions.equals(other.regions))
			return false;
		if (scanMetadata == null) {
			if (other.scanMetadata != null)
				return false;
		} else if (!scanMetadata.equals(other.scanMetadata))
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
		return "ScanRequest [models=" + models + ", detectors=" + detectors +
				", monitorNames=" + monitorNames +
				", metadataScannableNames=" + metadataScannableNames +
				", filePath=" + filePath + ", start=" + start + ", end=" + end + "]";
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

	public Map<String, Collection<T>> getRegions() {
		return regions;
	}
	public Collection<T> getRegions(String uniqueKey) {
		if (regions==null) return null;
		return regions.get(uniqueKey);
	}

	public void setRegions(Map<String, Collection<T>> regions) {
		this.regions = regions;
	}
	
	@SafeVarargs
	public final void putRegion(String uniqueId, T... areas) {
		if (this.regions==null) this.regions = new HashMap<>(3);
		this.regions.put(uniqueId, Arrays.asList(areas));
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
	
	public List<ScanMetadata> getScanMetadata() {
		return scanMetadata;
	}
	
	public void setScanMetadata(List<ScanMetadata> scanMetadata) {
		this.scanMetadata = scanMetadata;
	}
	
	public void addScanMetadata(ScanMetadata scanMetadata) {
		if (this.scanMetadata == null) {
			this.scanMetadata = new ArrayList<>();
		}
		this.scanMetadata.add(scanMetadata);
	}

}
