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
	
	private String uniqueId;
	
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

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}
	
	public ScanRole getRole() {
		return role;
	}

	public void setRole(ScanRole role) {
		this.role = role;
	}

	public List<DummyMalcolmDatasetModel> getDatasets() {
		return datasets;
	}

	public void setDatasets(List<DummyMalcolmDatasetModel> datasets) {
		this.datasets = datasets;
	}
	
	

}
