package org.eclipse.scanning.example.malcolm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.scanning.api.ITimeoutable;
import org.eclipse.scanning.api.device.models.MalcolmModel;

/**
 * A Malcolm Model for a {@link DummyMalcolmDevice}. This model describes which nexus files
 * and datasets the dummy malcolm device should create. A {@link DummyMalcolmControlledDetectorModel}
 * should be added for each device (i.e. detector, scannable) that is being simulated by the
 * real malcolm device.
 * 
 * @author Matthew Dickie
 */
public class DummyMalcolmModel extends MalcolmModel implements ITimeoutable {

	// timeout is added to the dummy model so that it can be increased for debugging purposes
	private long timeout = -1;
	
	private List<DummyMalcolmControlledDetectorModel> dummyDetectorModels = Collections.emptyList();

	private List<String> positionerNames;
	
	private List<String> monitorNames;
	
	public DummyMalcolmModel() {
		// the default model has a single detector with a single dataset
		// this can be overridden by calling setDummyDetectorModels()
		DummyMalcolmControlledDetectorModel detModel = new DummyMalcolmControlledDetectorModel("detector");
		List<DummyMalcolmDatasetModel> datasets = new ArrayList<>();
		datasets.add(new DummyMalcolmDatasetModel("detector", 2, Double.class));
		detModel.setDatasets(datasets);
		List<String> axes = Arrays.asList("stage_x", "stage_y");
		setAxesToMove(axes); // determines the _set (i.e. demand) values to be written
		setPositionerNames(axes); // determines the value (a.k.a rbv) values to be written
		setDummyDetectorModels(Arrays.asList(detModel));
	}
	
	public List<DummyMalcolmControlledDetectorModel> getDummyDetectorModels() {
		if (dummyDetectorModels == null) return Collections.emptyList();
		return dummyDetectorModels;
	}

	public void setDummyDetectorModels(List<DummyMalcolmControlledDetectorModel> dummyDetectorModels) {
		this.dummyDetectorModels = dummyDetectorModels;
	}
	
	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public List<String> getPositionerNames() {
		if (positionerNames == null) return Collections.emptyList();
		return positionerNames;
	}

	public void setPositionerNames(List<String> positionerNames) {
		this.positionerNames = positionerNames;
	}

	public List<String> getMonitorNames() {
		if (monitorNames == null) return Collections.emptyList();
		return monitorNames;
	}

	public void setMonitorNames(List<String> monitorNames) {
		this.monitorNames = monitorNames;
	}

}
