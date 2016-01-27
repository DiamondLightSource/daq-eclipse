package org.eclipse.scanning.api.scan.models;

public class ScanDeviceDimensionModel {
	
	private int primaryAxisForDimension = -1;
	
	private int[] dimensionMappings;

	public ScanDeviceDimensionModel(int... dimensionMappings) {
		this.dimensionMappings = dimensionMappings;
	}

	public int getPrimaryAxisForDimension() {
		return primaryAxisForDimension;
	}
	public void setPrimaryAxisForDimension(int primaryAxisForDimension) {
		this.primaryAxisForDimension = primaryAxisForDimension;
	}

	public void setDimensionMappings(int... dimensionMappings) {
		this.dimensionMappings = dimensionMappings;
	}
	
	public int[] getDimensionMappings() {
		return dimensionMappings;
	}

}
