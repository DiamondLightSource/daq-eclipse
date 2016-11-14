package org.eclipse.scanning.example.malcolm;

/**
 * A model describing a dataset that should be written by a {@link DummyMalcolmDevice}.
 * 
 * @author Matthew Dickie
 */
public class DummyMalcolmDatasetModel {
	
	private String name;
	
	private Class<?> dtype; // type of element in the dataset, e.g. String or Double
	
	private int rank;
	
	public DummyMalcolmDatasetModel() {
		// no args constructor for spring instantiation
	}
	
	public DummyMalcolmDatasetModel(String name, int rank, Class<?> dtype) {
		this.name = name;
		this.rank = rank;
		this.dtype = dtype;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * The rank of the data for this dataset at each point in the scan, e.g. 2 for images.
	 * In the Nexus file this dataset will have a rank of scan rank plus this value. 
	 * @return rank of data at each scan point
	 */
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

}
