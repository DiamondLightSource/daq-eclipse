package org.eclipse.scanning.test.scan;

import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.models.IDetectorModel;

public class BenchmarkBean {
	
	private int size; 
	private long reqTime; 
	private int tries; 
	private boolean silent; 
	private IRunnableDevice<? extends IDetectorModel> detector;
	private String scannableName;
	
	public BenchmarkBean() {
		
	}
	
	public BenchmarkBean(int size, long reqTime, int tries, boolean silent,
			IRunnableDevice<? extends IDetectorModel> detector, String scannableName) {
		super();
		this.size = size;
		this.reqTime = reqTime;
		this.tries = tries;
		this.silent = silent;
		this.detector = detector;
		this.scannableName = scannableName;
	}
	
	public BenchmarkBean(int size, long reqTime, int tries, boolean silent,
			              IRunnableDevice<? extends IDetectorModel> detector) {
		super();
		this.size = size;
		this.reqTime = reqTime;
		this.tries = tries;
		this.silent = silent;
		this.detector = detector;
		this.scannableName = "benchmark1";
	}
	
	public BenchmarkBean(int size, long reqTime, int tries, IRunnableDevice<? extends IDetectorModel> detector) {
		super();
		this.size = size;
		this.reqTime = reqTime;
		this.tries = tries;
		this.silent = false;
		this.detector = detector;
		this.scannableName = "benchmark1";
	}

	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public long getReqTime() {
		return reqTime;
	}
	public void setReqTime(long reqTime) {
		this.reqTime = reqTime;
	}
	public int getTries() {
		return tries;
	}
	public void setTries(int tries) {
		this.tries = tries;
	}
	public boolean isSilent() {
		return silent;
	}
	public void setSilent(boolean silent) {
		this.silent = silent;
	}
	public IRunnableDevice<? extends IDetectorModel> getDetector() {
		return detector;
	}
	public void setDetector(IRunnableDevice<? extends IDetectorModel> detector) {
		this.detector = detector;
	}
	public String getScannableName() {
		return scannableName;
	}
	public void setScannableName(String scannableName) {
		this.scannableName = scannableName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((detector == null) ? 0 : detector.hashCode());
		result = prime * result + (int) (reqTime ^ (reqTime >>> 32));
		result = prime * result + ((scannableName == null) ? 0 : scannableName.hashCode());
		result = prime * result + (silent ? 1231 : 1237);
		result = prime * result + size;
		result = prime * result + tries;
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
		BenchmarkBean other = (BenchmarkBean) obj;
		if (detector == null) {
			if (other.detector != null)
				return false;
		} else if (!detector.equals(other.detector))
			return false;
		if (reqTime != other.reqTime)
			return false;
		if (scannableName == null) {
			if (other.scannableName != null)
				return false;
		} else if (!scannableName.equals(other.scannableName))
			return false;
		if (silent != other.silent)
			return false;
		if (size != other.size)
			return false;
		if (tries != other.tries)
			return false;
		return true;
	}
}
