package org.eclipse.scanning.example.malcolm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.scanning.api.ITimeoutable;
import org.eclipse.scanning.api.device.models.MalcolmModel;

import gda.factory.Configurable;
import gda.factory.FactoryException;

/**
 * A Malcolm Model for a {@link DummyMalcolmDevice}. This model describes which nexus files
 * and datasets the dummy malcolm device should create. A {@link DummyMalcolmControlledDetectorModel}
 * should be added for each device (i.e. detector, scannable) that is being simulated by the
 * real malcolm device.
 * 
 * @author Matthew Dickie
 */
public class DummyMalcolmModel extends MalcolmModel implements ITimeoutable, Configurable {

	// timeout is added to the dummy model so that it can be increased for debugging purposes
	private long timeout = -1;
	
	private List<DummyMalcolmControlledDetectorModel> dummyDetectorModels;

	private List<String> monitorNames;

	@Override
	public void configure() throws FactoryException {
		setName("malcolm");
		
		// If no detectors have been explicitly configured,
		// add a single detector with a single dataset
		if (dummyDetectorModels == null) {
			final List<DummyMalcolmDatasetModel> datasets = new ArrayList<>();
			datasets.add(new DummyMalcolmDatasetModel("detector", 2, Double.class));

			final DummyMalcolmControlledDetectorModel detModel = new DummyMalcolmControlledDetectorModel("detector");
			detModel.setDatasets(datasets);

			setDummyDetectorModels(Arrays.asList(detModel));
		}
		
		// Set axes to move if not explicitly set
		if (getAxesToMove() == null) {
			setAxesToMove(Arrays.asList("stage_x", "stage_y"));
		}
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

	public List<String> getMonitorNames() {
		if (monitorNames == null) return Collections.emptyList();
		return monitorNames;
	}

	public void setMonitorNames(List<String> monitorNames) {
		this.monitorNames = monitorNames;
	}

	@Override
	public String toString() {
		return "DummyMalcolmModel [name = " + getName() + ", timeout=" + timeout + ", dummyDetectorModels=" + dummyDetectorModels
				+ ", monitorNames=" + monitorNames + ", fileDir=" + getFileDir() + ", axesToMove=" + getAxesToMove()
				+ ", getExposureTime()=" + getExposureTime() + "]";
	}

}
