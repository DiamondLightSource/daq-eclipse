package org.eclipse.scanning.example.malcolm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.scanning.api.INameable;

/**
 * A model describing a detector that would be controlled by malcolm in a real scan.
 * 
 * Note that this is <em>NOT</em> the model for a malcolm device - {@link DummyMalcolmModel}
 * fulfils that role. Instead one or more of these models in a child of a 
   {@link DummyMalcolmModel} tells the
 * {@link DummyMalcolmDevice} what nexus files and datasets to write, so that it can be
 * set up to produce a nexus file similar to what a real malcolm device would.
 * 
 * The first {@link DummyMalcolmDatasetModel} in the {@link List} returned by
 * {@link #getDatasets()} will be the primary one, the others will be secondary.
 * 
 * @author Matthew Dickie
 */
public class DummyMalcolmControlledDetectorModel implements INameable {
	
	private String name;
	
	private List<DummyMalcolmDatasetModel> datasets = null;
	
	public DummyMalcolmControlledDetectorModel() {
		// no-arg constructor
	}
	
	public DummyMalcolmControlledDetectorModel(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	public List<DummyMalcolmDatasetModel> getDatasets() {
		if (datasets == null) return Collections.emptyList();
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
	
	public void addDataset(DummyMalcolmDatasetModel dataset) {
		if (this.datasets == null) this.datasets = new ArrayList<>();
		datasets.add(dataset);
	}

	@Override
	public String toString() {
		return "DummyMalcolmControlledDetectorModel [name=" + name + ", datasets=" + datasets + "]";
	}

}
