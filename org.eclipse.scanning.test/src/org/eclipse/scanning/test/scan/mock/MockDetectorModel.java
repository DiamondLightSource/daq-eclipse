package org.eclipse.scanning.test.scan.mock;

import org.eclipse.scanning.api.device.models.IDetectorModel;


public class MockDetectorModel implements IDetectorModel {

	public MockDetectorModel() {
	
	}
	
	public MockDetectorModel(double exposureTime) {
		super();
		this.exposureTime = exposureTime;
	}

	private double exposureTime;
	
	private int ran=0;
	private int written=0;
	private int abortCount=-1;
	private String name;

	
	public double getExposureTime() {
		return exposureTime;
	}

	public void setExposureTime(double exposureTime) {
		this.exposureTime = exposureTime;
	}

	public int getRan() {
		return ran;
	}

	public void setRan(int ran) {
		this.ran = ran;
	}

	public int getWritten() {
		return written;
	}

	public void setWritten(int read) {
		this.written = read;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + abortCount;
		long temp;
		temp = Double.doubleToLongBits(exposureTime);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ran;
		result = prime * result + written;
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
		MockDetectorModel other = (MockDetectorModel) obj;
		if (abortCount != other.abortCount)
			return false;
		if (Double.doubleToLongBits(exposureTime) != Double
				.doubleToLongBits(other.exposureTime))
			return false;
		if (ran != other.ran)
			return false;
		if (written != other.written)
			return false;
		return true;
	}

	public int getAbortCount() {
		return abortCount;
	}

	public void setAbortCount(int abortCount) {
		this.abortCount = abortCount;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
