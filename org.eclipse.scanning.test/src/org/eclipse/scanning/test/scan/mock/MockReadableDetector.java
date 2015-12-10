package org.eclipse.scanning.test.scan.mock;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Random;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.scan.AbstractRunnableDevice;
import org.eclipse.scanning.api.scan.IReadableDetector;
import org.eclipse.scanning.api.scan.ScanningException;

public class MockReadableDetector extends AbstractRunnableDevice<MockDetectorModel> implements IReadableDetector<MockDetectorModel> {
	
	private MockDetectorModel model;

	public MockReadableDetector() {
		super();
	}
	
	public MockReadableDetector(String name) {
		super();
		setName(name);
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
		return "MockDetector [level=" + getLevel() + ", name=" + getName() +  "]";
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
        try {
	        setState(DeviceState.PAUSING);
	        Thread.sleep(100);
	        setState(DeviceState.PAUSED);
		} catch (Exception e) {
			throw new ScanningException(e);
		}
	}

	@Override
	public void resume() throws ScanningException {
        try {
			setState(model!=null ? DeviceState.READY : DeviceState.IDLE);
		} catch (Exception e) {
			throw new ScanningException(this, e);
		}
  	}
}
