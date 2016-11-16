package org.eclipse.scanning.api.device.models;

import java.util.Arrays;
import java.util.List;

import org.eclipse.scanning.api.annotation.ui.FieldDescriptor;
import org.eclipse.scanning.api.annotation.ui.FileType;
import org.eclipse.scanning.api.points.IPointGenerator;

/**
 * The model for a malcolm device that writes h5 files.
 */
public class MalcolmModel extends AbstractDetectorModel implements IMalcolmModel, IDetectorModel {

	/**
	 * List of fields to exclude when serialising using the Epics V4 marshaller to send to the device
	 */
	public transient final static List<String> FIELDS_TO_EXCLUDE = Arrays.asList("name", "timeout", "exposureTime");

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
