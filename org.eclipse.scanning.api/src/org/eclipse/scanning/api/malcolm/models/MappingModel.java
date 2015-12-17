package org.eclipse.scanning.api.malcolm.models;

public class MappingModel {

	
	private double xStart,xStop,xStep;
	private double yStart,yStop,yStep;
	private double exposure;
	private String hdf5File;
	public double getxStart() {
		return xStart;
	}
	public void setxStart(double xStart) {
		this.xStart = xStart;
	}
	public double getxStop() {
		return xStop;
	}
	public void setxStop(double xStop) {
		this.xStop = xStop;
	}
	public double getxStep() {
		return xStep;
	}
	public void setxStep(double xStep) {
		this.xStep = xStep;
	}
	public double getyStart() {
		return yStart;
	}
	public void setyStart(double yStart) {
		this.yStart = yStart;
	}
	public double getyStop() {
		return yStop;
	}
	public void setyStop(double yStop) {
		this.yStop = yStop;
	}
	public double getyStep() {
		return yStep;
	}
	public void setyStep(double yStep) {
		this.yStep = yStep;
	}
	public double getExposure() {
		return exposure;
	}
	public void setExposure(double exposure) {
		this.exposure = exposure;
	}
	public String getHdf5File() {
		return hdf5File;
	}
	public void setHdf5File(String hdf5File) {
		this.hdf5File = hdf5File;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(exposure);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result
				+ ((hdf5File == null) ? 0 : hdf5File.hashCode());
		temp = Double.doubleToLongBits(xStart);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(xStep);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(xStop);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(yStart);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(yStep);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(yStop);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MappingModel other = (MappingModel) obj;
		if (Double.doubleToLongBits(exposure) != Double
				.doubleToLongBits(other.exposure))
			return false;
		if (hdf5File == null) {
			if (other.hdf5File != null)
				return false;
		} else if (!hdf5File.equals(other.hdf5File))
			return false;
		if (Double.doubleToLongBits(xStart) != Double
				.doubleToLongBits(other.xStart))
			return false;
		if (Double.doubleToLongBits(xStep) != Double
				.doubleToLongBits(other.xStep))
			return false;
		if (Double.doubleToLongBits(xStop) != Double
				.doubleToLongBits(other.xStop))
			return false;
		if (Double.doubleToLongBits(yStart) != Double
				.doubleToLongBits(other.yStart))
			return false;
		if (Double.doubleToLongBits(yStep) != Double
				.doubleToLongBits(other.yStep))
			return false;
		if (Double.doubleToLongBits(yStop) != Double
				.doubleToLongBits(other.yStop))
			return false;
		return true;
	}
	
	
}
