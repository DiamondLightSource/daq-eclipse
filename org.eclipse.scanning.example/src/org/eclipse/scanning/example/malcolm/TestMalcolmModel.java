package org.eclipse.scanning.example.malcolm;

import org.eclipse.scanning.api.annotation.ui.FieldDescriptor;
import org.eclipse.scanning.api.annotation.ui.FileType;
import org.eclipse.scanning.api.device.models.AbstractMalcolmModel;

/**
 * A Test Malcolm Model.
 * 
 * @author Matt Taylor
 *
 */
public class TestMalcolmModel extends AbstractMalcolmModel {

	@FieldDescriptor(label="Directory", hint="The directory where the h5 files will be created", file=FileType.NEW_FOLDER)
	private String filePath;
	

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((filePath == null) ? 0 : filePath.hashCode());
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
		TestMalcolmModel other = (TestMalcolmModel) obj;
		if (filePath == null) {
			if (other.filePath != null)
				return false;
		} else if (!filePath.equals(other.filePath))
			return false;
		return true;
	}


}
