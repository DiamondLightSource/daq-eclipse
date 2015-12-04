package org.eclipse.scanning.test.scan.mock;

import java.util.concurrent.TimeUnit;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Random;
import org.eclipse.scanning.api.IDetector;

public class MockDetector implements IDetector<IDataset> {
	
	public MockDetector() {
		
	}
	
	public MockDetector(String name) {
		super();
		this.name = name;
	}

	private int    level;
	private String name;
	private double collectionTime;
	
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
	
	private IDataset next;
	@Override
	public void collectData() throws Exception {
		Thread.sleep(100);
		next = Random.rand(new int[]{1024, 1024});
	}
	
	@Override
	public IDataset readout() throws Exception {
		Thread.sleep(50);
		return next;
	}

	@Override
	public String toString() {
		return "MockDetector [level=" + level + ", name=" + name + ", next="
				+ next + "]";
	}

	public double getCollectionTime() {
		return collectionTime;
	}

	public void setCollectionTime(double collectionTime) {
		this.collectionTime = collectionTime;
	}
	public void setCollectionTime(long time, TimeUnit unit) {
		this.collectionTime = unit.toSeconds(time);
	}

}
