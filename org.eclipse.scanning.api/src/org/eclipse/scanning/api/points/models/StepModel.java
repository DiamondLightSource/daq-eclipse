package org.eclipse.scanning.api.points.models;

import org.eclipse.scanning.api.annotation.ui.FieldDescriptor;

/**
 * A model for a scan along one axis with start and stop positions and a step size.
 */
public class StepModel extends AbstractPointsModel {

	@FieldDescriptor(label="Device")
	private String name;
	
	@FieldDescriptor(label="Start", scannable="name") // The scannable lookup gets the units
	private double start;
	
	@FieldDescriptor(label="Stop", scannable="name") // The scannable lookup gets the units
	private double stop;
	
	@FieldDescriptor(label="Step", scannable="name") // The scannable lookup gets the units
	private double step;
	
	public StepModel() {
	}
	public StepModel(String name, double start, double stop, double step) {
		super();
		this.name = name;
		this.start = start;
		this.stop = stop;
		this.step = step;
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
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		long temp;
		temp = Double.doubleToLongBits(start);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(step);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(stop);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		StepModel other = (StepModel) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (Double.doubleToLongBits(start) != Double
				.doubleToLongBits(other.start))
			return false;
		if (Double.doubleToLongBits(step) != Double
				.doubleToLongBits(other.step))
			return false;
		if (Double.doubleToLongBits(stop) != Double
				.doubleToLongBits(other.stop))
			return false;
		return true;
	}
}
