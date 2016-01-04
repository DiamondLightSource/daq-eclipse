package org.eclipse.scanning.test.scan.mock;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.ScannableModel;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;

public class MockScannable implements IScannable<Number> {

	private int     level;
	private String  name;
	private Number  position = 0d;
	private boolean requireSleep=true;
	private ScannableModel model;

	
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
	
	@Override
	public void configure(ScannableModel model) throws ScanningException {
		this.model = model;
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
		setPosition(position, null);
	}
	public void setPosition(Number position, IPosition unused) throws InterruptedException {
		if (requireSleep) {
			long time = Math.abs(Math.round((position.doubleValue()-this.position.doubleValue())/1)*100);
			time = Math.max(time, 1);
			Thread.sleep(time);
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
