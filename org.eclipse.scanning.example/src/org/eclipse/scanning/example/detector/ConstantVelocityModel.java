package org.eclipse.scanning.example.detector;

import org.eclipse.scanning.api.ITimeoutable;

public class ConstantVelocityModel implements ITimeoutable {

	private double start,stop,step;
	private String name      = "constantVelocityExample";
	private int lineSize     = 1;
	private int channelCount = 64;
	private int spectraSize  = 46;
	private long timeout  = 100;

	public ConstantVelocityModel() {

	}
	
	public ConstantVelocityModel(String name, double start, double stop, double step) {
		
		this.name  = name;
		this.start = start;
		this.stop  = stop;
		this.step  = step;
		
		double div = ((stop-start)/step);
		this.lineSize = (int)Math.floor(div+1);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + channelCount;
		result = prime * result + lineSize;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + spectraSize;
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
		ConstantVelocityModel other = (ConstantVelocityModel) obj;
		if (channelCount != other.channelCount)
			return false;
		if (lineSize != other.lineSize)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (spectraSize != other.spectraSize)
			return false;
		return true;
	}
	public int getLineSize() {
		return lineSize;
	}
	public void setLineSize(int lineSize) {
		this.lineSize = lineSize;
	}
	public int getChannelCount() {
		return channelCount;
	}
	public void setChannelCount(int channelCount) {
		this.channelCount = channelCount;
	}
	public int getSpectraSize() {
		return spectraSize;
	}
	public void setSpectraSize(int spectraSize) {
		this.spectraSize = spectraSize;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public double getStart() {
		return start;
	}

	public void setStart(double start) {
		this.start = start;
	}

	public double getStop() {
		return stop;
	}

	public void setStop(double stop) {
		this.stop = stop;
	}

	public double getStep() {
		return step;
	}

	public void setStep(double step) {
		this.step = step;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}



}