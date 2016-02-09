package org.eclipse.scanning.api.malcolm.models;

/**
 * 
 * We have not generated the correct models for malcolm as yet
 * 
 * @author Matthew Gerring
 *
 */
public class TwoDetectorTestMappingModel {

	
	private double xStart,xStop,xStep;
	private double yStart,yStop,yStep;
	private double det1Exposure;
	private String hdf5File1;
	private double det2Exposure;
	private String hdf5File2;
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
	public double getDet1Exposure() {
		return det1Exposure;
	}
	public void setDet1Exposure(double exposure) {
		this.det1Exposure = exposure;
	}
	public String getHdf5File1() {
		return hdf5File1;
	}
	public void setHdf5File1(String hdf5File) {
		this.hdf5File1 = hdf5File;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(det1Exposure);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(det2Exposure);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result
				+ ((hdf5File1 == null) ? 0 : hdf5File1.hashCode());
		result = prime * result
				+ ((hdf5File2 == null) ? 0 : hdf5File2.hashCode());
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
		TwoDetectorTestMappingModel other = (TwoDetectorTestMappingModel) obj;
		if (Double.doubleToLongBits(det1Exposure) != Double
				.doubleToLongBits(other.det1Exposure))
			return false;
		if (Double.doubleToLongBits(det2Exposure) != Double
				.doubleToLongBits(other.det2Exposure))
			return false;
		if (hdf5File1 == null) {
			if (other.hdf5File1 != null)
				return false;
		} else if (!hdf5File1.equals(other.hdf5File1))
			return false;
		if (hdf5File2 == null) {
			if (other.hdf5File2 != null)
				return false;
		} else if (!hdf5File2.equals(other.hdf5File2))
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
	public double getDet2Exposure() {
		return det2Exposure;
	}
	public void setDet2Exposure(double det2Exposure) {
		this.det2Exposure = det2Exposure;
	}
	public String getHdf5File2() {
		return hdf5File2;
	}
	public void setHdf5File2(String hdf5File2) {
		this.hdf5File2 = hdf5File2;
	}
	
	
}
