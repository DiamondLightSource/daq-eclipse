package org.eclipse.scanning.api.points.models;

import org.eclipse.scanning.api.annotation.UiHidden;
import org.eclipse.scanning.api.annotation.ui.FieldDescriptor;

/**
 * A model defining a box in two dimensional space, which can be used to confine and give scale to a {@link
 * IBoundingBoxModel}.
 * <p>
 * The two axes of the box are abstracted as "fast" and "slow". Often these will be the X and Y stage motors, but other
 * axes could be used depending on the beamline configuration or the required experiment. The axis names to be used are
 * defined in AbstractBoundingBoxModel.
 *
 * @author Colin Palmer
 *
 */
public class BoundingBox {

	public BoundingBox() {
		
	}
	
	public BoundingBox(double fastAxisStart, double slowAxisStart, double fastAxisLength, double slowAxisLength) {
		super();
		this.fastAxisStart = fastAxisStart;
		this.slowAxisStart = slowAxisStart;
		this.fastAxisLength = fastAxisLength;
		this.slowAxisLength = slowAxisLength;
	}
	
	@FieldDescriptor(visible=false)
	private String fastAxisName="x";
	
	@FieldDescriptor(visible=false)
	private String slowAxisName="y";
	
	@FieldDescriptor(scannable="fastAxisName")
	private double fastAxisStart;
	
	@FieldDescriptor(scannable="fastAxisName", validif="fastAxisLength!=0")
	private double fastAxisLength;
	
	@FieldDescriptor(scannable="slowAxisName")
	private double slowAxisStart;
	
	@FieldDescriptor(scannable="slowAxisName", validif="slowAxisLength!=0")
	private double slowAxisLength;

	public double getFastAxisStart() {
		return fastAxisStart;
	}
	public void setFastAxisStart(double fastAxisStart) {
		this.fastAxisStart = fastAxisStart;
	}
	public double getSlowAxisStart() {
		return slowAxisStart;
	}
	public void setSlowAxisStart(double yStart) {
		this.slowAxisStart = yStart;
	}
	public double getFastAxisLength() {
		return fastAxisLength;
	}
	public void setFastAxisLength(double fastAxisLength) {
		this.fastAxisLength = fastAxisLength;
	}
	public double getSlowAxisLength() {
		return slowAxisLength;
	}
	public void setSlowAxisLength(double slowAxisLength) {
		this.slowAxisLength = slowAxisLength;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(slowAxisLength);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(fastAxisLength);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(fastAxisStart);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(slowAxisStart);
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
		BoundingBox other = (BoundingBox) obj;
		if (Double.doubleToLongBits(slowAxisLength) != Double
				.doubleToLongBits(other.slowAxisLength))
			return false;
		if (Double.doubleToLongBits(fastAxisLength) != Double
				.doubleToLongBits(other.fastAxisLength))
			return false;
		if (Double.doubleToLongBits(fastAxisStart) != Double
				.doubleToLongBits(other.fastAxisStart))
			return false;
		if (Double.doubleToLongBits(slowAxisStart) != Double
				.doubleToLongBits(other.slowAxisStart))
			return false;
		return true;
	}
	@UiHidden
	public String getFastAxisName() {
		return fastAxisName;
	}
	@UiHidden
	public void setFastAxisName(String fastAxisName) {
		this.fastAxisName = fastAxisName;
	}
	@UiHidden
	public String getSlowAxisName() {
		return slowAxisName;
	}
	@UiHidden
	public void setSlowAxisName(String slowAxisName) {
		this.slowAxisName = slowAxisName;
	}
}
