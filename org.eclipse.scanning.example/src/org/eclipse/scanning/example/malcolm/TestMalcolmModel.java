package org.eclipse.scanning.example.malcolm;

import org.eclipse.scanning.api.points.IPointGenerator;

/**
 * A Test Malcolm Model.
 * 
 * @author Matt Taylor
 *
 */
public class TestMalcolmModel {

	private String filePath;
	private IPointGenerator<?> generator;

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
}
