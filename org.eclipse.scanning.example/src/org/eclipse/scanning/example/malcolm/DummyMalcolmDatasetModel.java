package org.eclipse.scanning.example.malcolm;

/**
 * A model describing a dataset that should be written by a {@link DummyMalcolmDevice}.
 * 
 * @author Matthew Dickie
 */
public class DummyMalcolmDatasetModel {
	
	private String name;
	
	private Class<?> dtype; // type of element in the dataset, e.g. String or Double
	
	/**
	 * Type of dataset e.g. primary, position, actually identifies the type. This mainly
	 * identifies the type of device that this dataset is for, e.g. a detector or scannable.
	 * Malcolm wraps all this information up in the datasets attribute 
	 */
	private MalcolmDatasetType malcolmType; 
	
	private String path;
	
	private int rank;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public Class<?> getDtype() {
		return dtype;
	}

	public void setDtype(Class<?> dtype) {
		this.dtype = dtype;
	}

	public MalcolmDatasetType getMalcolmType() {
		return malcolmType;
	}

	public void setMalcolmType(MalcolmDatasetType malcolmType) {
		this.malcolmType = malcolmType;
	}
	
	

}
