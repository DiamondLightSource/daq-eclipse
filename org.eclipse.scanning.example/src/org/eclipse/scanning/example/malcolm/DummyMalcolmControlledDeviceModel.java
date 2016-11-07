package org.eclipse.scanning.example.malcolm;

import java.util.Collections;
import java.util.List;

import org.eclipse.dawnsci.nexus.NexusScanInfo.ScanRole;

/**
 * A model describing a device that would be controlled by malcolm in a real scan.
 * One or more of these models as children of a {@link DummyMalcolmModel} tells the
 * {@link DummyMalcolmDevice} what nexus files and datasets to write, so that it can be
 * set up to produce a nexus file similar to what a real malcolm device would.
 * 
 * @author Matthew Dickie
 */
public class DummyMalcolmControlledDeviceModel {
	
	private String name;
	
	private String fileName;
	
	private ScanRole role;
	
	private List<DummyMalcolmDatasetModel> datasets = Collections.emptyList();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFileName() {
		return fileName;
	}

	/**
	 * The filename for the device. This is an optional attribute. If not specified
	 * the detector name will be used.
	 * @param fileName
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public ScanRole getRole() {
		return role;
	}

	/**
	 * The role of the device within the scan, one of {@link ScanRole#DETECTOR},
	 * {@link ScanRole#MONITOR} or {@link ScanRole#SCANNABLE}.
	 * @param role
	 */
	public void setRole(ScanRole role) {
		this.role = role;
	}

	public List<DummyMalcolmDatasetModel> getDatasets() {
		return datasets;
	}

	/**
	 * A collection of {@link DummyMalcolmDatasetModel}s each describing a dataset that
	 * should be written for this device.
	 * @param datasets
	 */
	public void setDatasets(List<DummyMalcolmDatasetModel> datasets) {
		this.datasets = datasets;
	}

}
