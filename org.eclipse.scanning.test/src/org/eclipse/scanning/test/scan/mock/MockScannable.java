package org.eclipse.scanning.test.scan.mock;

import org.eclipse.scanning.api.IScannable;

public class MockScannable implements IScannable<Double> {

	private int    level;
	private String name;
	private Double position = 0d;

	
    public MockScannable() {
    	
    }
    public MockScannable(double position) {
    	this.position = position;
    }
	public MockScannable(String name, double position) {
		super();
		this.name = name;
		this.position = position;
	}
	
	public MockScannable(String name, Double position, int level) {
		super();
		this.level = level;
		this.name = name;
		this.position = position;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Double getPosition() {
		return position;
	}
	public void setPosition(Double position) throws InterruptedException {
		moveTo(position);
	}
	public void moveTo(Double position) throws InterruptedException {
		Thread.sleep(Math.abs(Math.round((position-this.position)/1)*100));
		this.position = position;
	}
	@Override
	public String toString() {
		return "MockScannable [level=" + level + ", name=" + name
				+ ", position=" + position + "]";
	}


}
