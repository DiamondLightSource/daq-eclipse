package org.eclipse.scanning.test.scan.mock;


public class MockDetectorModel {

	private double collectionTime;
	
	private int ran=0;
	private int read=0;

	
	public double getCollectionTime() {
		return collectionTime;
	}

	public void setCollectionTime(double collectionTime) {
		this.collectionTime = collectionTime;
	}

	public int getRan() {
		return ran;
	}

	public void setRan(int ran) {
		this.ran = ran;
	}

	public int getRead() {
		return read;
	}

	public void setRead(int read) {
		this.read = read;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(collectionTime);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ran;
		result = prime * result + read;
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
		if (Double.doubleToLongBits(collectionTime) != Double
				.doubleToLongBits(other.collectionTime))
			return false;
		if (ran != other.ran)
			return false;
		if (read != other.read)
			return false;
		return true;
	}

}
