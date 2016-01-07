package org.eclipse.scanning.api.scan.models;

import java.util.Arrays;
import java.util.List;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.IRunnableDevice;

public class ScanModel {
	
	/**
	 * If you want the scan to attempt to write to a given
	 * path, set this field. If it is set the scan will 
	 * attempt to use the NexusBuilderFactory and register all the
	 * devices with it.
	 */
	private String filePath;
	
	/**
	 * Normally this is a generator for the scan points
	 * of the scan. IGenerator implements Iterable
	 */
	private Iterable<IPosition> positionIterator;
	
	/**
	 * This is the set of detectors which should be collected
	 * and (if they are IReadableDetector) read out during the 
	 * scan.
	 */
	private List<IRunnableDevice<?>> detectors;
	
	/**
	 * The bean which was submitted. May be null but if it is not,
	 * all points are published using this bean.
	 */
	private ScanBean bean;
	
	/**
	 * A set of scannables may optionally be 'readout' during
	 * the scan without being told a value for their location.
	 * They have setPosition(null, IPosition) called and should 
	 * ensure that if their value is null, they do not move but
	 * still readout position
	 */
	private List<IScannable<?>> monitors;
	
	
	public ScanModel() {
	    this(null);
	}	
	public ScanModel(Iterable<IPosition> positionIterator, IRunnableDevice<?>... detectors) {
		this.positionIterator = positionIterator;
		if (detectors!=null && detectors.length>0) this.detectors = Arrays.asList(detectors);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bean == null) ? 0 : bean.hashCode());
		result = prime * result
				+ ((detectors == null) ? 0 : detectors.hashCode());
		result = prime * result
				+ ((filePath == null) ? 0 : filePath.hashCode());
		result = prime * result
				+ ((monitors == null) ? 0 : monitors.hashCode());
		result = prime
				* result
				+ ((positionIterator == null) ? 0 : positionIterator.hashCode());
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
		ScanModel other = (ScanModel) obj;
		if (bean == null) {
			if (other.bean != null)
				return false;
		} else if (!bean.equals(other.bean))
			return false;
		if (detectors == null) {
			if (other.detectors != null)
				return false;
		} else if (!detectors.equals(other.detectors))
			return false;
		if (filePath == null) {
			if (other.filePath != null)
				return false;
		} else if (!filePath.equals(other.filePath))
			return false;
		if (monitors == null) {
			if (other.monitors != null)
				return false;
		} else if (!monitors.equals(other.monitors))
			return false;
		if (positionIterator == null) {
			if (other.positionIterator != null)
				return false;
		} else if (!positionIterator.equals(other.positionIterator))
			return false;
		return true;
	}
	public ScanBean getBean() {
		return bean;
	}
	public void setBean(ScanBean bean) {
		this.bean = bean;
	}


	public Iterable<IPosition> getPositionIterator() {
		return positionIterator;
	}

	public void setPositionIterator(Iterable<IPosition> positionIterator) {
		this.positionIterator = positionIterator;
	}

	public List<IRunnableDevice<?>> getDetectors() {
		return detectors;
	}

	public void setDetectors(List<IRunnableDevice<?>> ds) {
		this.detectors = ds;
	}

	public void setDetectors(IRunnableDevice<?>... detectors) {
		this.detectors = Arrays.asList(detectors);
	}
	
	public List<IScannable<?>> getMonitors() {
		return monitors;
	}
	
	public void setMonitors(List<IScannable<?>> monitors) {
		this.monitors = monitors;
	}
	
	public void setMonitors(IScannable<?>... monitors) {
		this.monitors = Arrays.asList(monitors);
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
}
