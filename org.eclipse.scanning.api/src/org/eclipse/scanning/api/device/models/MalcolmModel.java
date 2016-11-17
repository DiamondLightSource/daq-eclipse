package org.eclipse.scanning.api.device.models;

import java.util.List;

import org.eclipse.scanning.api.annotation.ui.FieldDescriptor;
import org.eclipse.scanning.api.annotation.ui.FileType;

/**
 * The model for a malcolm device that writes h5 files.
 */
public class MalcolmModel extends AbstractDetectorModel implements IMalcolmModel, IDetectorModel {

	@FieldDescriptor(visible=false, file=FileType.NEW_FOLDER)
	private String fileDir;
	
	@FieldDescriptor(editable=false)
	private List<String> axesToMove;
	
	public String getFileDir() {
		return fileDir;
	}

	public void setFileDir(String fileDir) {
		this.fileDir = fileDir;
	}

	@Override
	public List<String> getAxesToMove() {
		return axesToMove;
	}
	
	public void setAxesToMove(List<String> axesToMove) {
		this.axesToMove = axesToMove;
	}
}
