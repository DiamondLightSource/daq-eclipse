package org.eclipse.scanning.example.malcolm;

import java.util.Collections;
import java.util.List;

import org.eclipse.scanning.api.annotation.ui.FieldDescriptor;
import org.eclipse.scanning.api.annotation.ui.FileType;
import org.eclipse.scanning.api.device.models.AbstractDetectorModel;
import org.eclipse.scanning.api.points.IPointGenerator;

/**
 * A Malcolm Model for a {@link DummyMalcolmDevice}. This model describes which nexus files
 * and datasets the dummy malcolm device should create. A {@link DummyMalcolmControlledDeviceModel}
 * should be added for each device (i.e. detector, scannable) that is being simulated by the
 * real malcolm device.
 * 
 * @author Matt Taylor
 * @author Matthew Dickie
 */
public class DummyMalcolmModel extends AbstractDetectorModel {

	@FieldDescriptor(label="Directory", hint="The directory where the h5 files will be created", file=FileType.NEW_FOLDER)
	private String filePath;
	
	@FieldDescriptor(visible=false)
	private IPointGenerator<?> generator;
	
	@FieldDescriptor(visible=false)
	private List<DummyMalcolmControlledDeviceModel> dummyDeviceModels = Collections.emptyList();

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public IPointGenerator<?> getGenerator() {
		return generator;
	}

	public void setGenerator(IPointGenerator<?> generator) {
		this.generator = generator;
	}

	public List<DummyMalcolmControlledDeviceModel> getDummyDeviceModels() {
		return dummyDeviceModels;
	}

	public void setDummyDeviceModels(List<DummyMalcolmControlledDeviceModel> dummyDeviceModels) {
		this.dummyDeviceModels = dummyDeviceModels;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((dummyDeviceModels == null) ? 0 : dummyDeviceModels.hashCode());
		result = prime * result + ((filePath == null) ? 0 : filePath.hashCode());
		result = prime * result + ((generator == null) ? 0 : generator.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		DummyMalcolmModel other = (DummyMalcolmModel) obj;
		if (dummyDeviceModels == null) {
			if (other.dummyDeviceModels != null)
				return false;
		} else if (!dummyDeviceModels.equals(other.dummyDeviceModels))
			return false;
		if (filePath == null) {
			if (other.filePath != null)
				return false;
		} else if (!filePath.equals(other.filePath))
			return false;
		if (generator == null) {
			if (other.generator != null)
				return false;
		} else if (!generator.equals(other.generator))
			return false;
		return true;
	}

}
