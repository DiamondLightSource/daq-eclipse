package org.eclipse.scanning.test.scan.mock;


public class MockDetectorModel {

	public MockDetectorModel() {
	
	}
	
	public MockDetectorModel(double collectionTime) {
		super();
		this.collectionTime = collectionTime;
	}

	private double collectionTime;
	
	private int ran=0;
	private int written=0;
	private int abortCount=-1;
	private String name;

	
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
		temp = Double.doubleToLongBits(collectionTime);
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
		if (Double.doubleToLongBits(collectionTime) != Double
				.doubleToLongBits(other.collectionTime))
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
