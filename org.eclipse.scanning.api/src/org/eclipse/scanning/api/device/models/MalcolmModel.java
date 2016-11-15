package org.eclipse.scanning.api.device.models;

import java.util.List;

import org.eclipse.scanning.api.annotation.ui.FieldDescriptor;
import org.eclipse.scanning.api.annotation.ui.FileType;
import org.eclipse.scanning.api.points.IPointGenerator;

/**
 * The model for a malcolm device that writes h5 files.
 */
public class MalcolmModel implements IMalcolmModel {

	@FieldDescriptor(visible=false)
	private IPointGenerator<?> generator;
	
	@FieldDescriptor(visible=false, file=FileType.NEW_FOLDER)
	private String fileDir;
	
	private List<String> axesToMove;
	
	public IPointGenerator<?> getGenerator() {
		return generator;
	}

	public void setGenerator(IPointGenerator<?> generator) {
		this.generator = generator;
	}

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
