package org.eclipse.scanning.test.scan.mock;

import org.eclipse.scanning.api.IScannable;

public class MockScannable implements IScannable<Number> {

	private int     level;
	private String  name;
	private Number  position = 0d;
	private boolean requireSleep=true;

	
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
	public Number getPosition() {
		return position;
	}
	public void setPosition(Number position) throws InterruptedException {
		moveTo(position);
	}
	public void moveTo(Number position) throws InterruptedException {
		if (requireSleep) {
			Thread.sleep(Math.abs(Math.round((position.doubleValue()-this.position.doubleValue())/1)*100));
		}
		this.position = position;
	}
	@Override
	public String toString() {
		return "MockScannable [level=" + level + ", name=" + name
				+ ", position=" + position + "]";
	}
	public boolean isRequireSleep() {
		return requireSleep;
	}
	public void setRequireSleep(boolean requireSleep) {
		this.requireSleep = requireSleep;
	}


}
