package org.eclipse.scanning.test.scan.mock;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Random;
import org.eclipse.scanning.api.scan.IReadableDetector;
import org.eclipse.scanning.api.scan.ScanningException;

public class MockReadableDetector implements IReadableDetector<MockDetectorModel> {
	
	private MockDetectorModel model;
	private int               level;
	private String            name;

	public MockReadableDetector() {
		
	}
	
	public MockReadableDetector(String name) {
		super();
		this.name = name;
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
	
	@Override
	public void run() throws ScanningException {
		try {
			Thread.sleep((long)(model.getCollectionTime()*1000));
			model.setRan(model.getRan()+1);
		} catch (Exception ne) {
			throw new ScanningException("Cannot to do readout", ne);
		}
	}
	
	@Override
	public boolean read() throws ScanningException {
		
		IDataset next = Random.rand(new int[]{1024, 1024});
		model.setRead(model.getRead()+1);
		// TODO write next somewhere?
		
		return true;
	}

	@Override
	public String toString() {
		return "MockDetector [level=" + level + ", name=" + name +  "]";
	}

	@Override
	public void configure(MockDetectorModel model) throws ScanningException {
		this.model = model;
	}

	@Override
	public void abort() throws ScanningException {
		throw new ScanningException("Not implemented!");
	}

	@Override
	public void pause() throws ScanningException {
		throw new ScanningException("Not implemented!");
	}

	@Override
	public void resume() throws ScanningException {
		throw new ScanningException("Not implemented!");
	}
}
