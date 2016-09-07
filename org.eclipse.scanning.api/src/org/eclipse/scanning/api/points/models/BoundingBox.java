package org.eclipse.scanning.api.points.models;

import java.text.DecimalFormat;

import org.eclipse.scanning.api.annotation.UiHidden;
import org.eclipse.scanning.api.annotation.ui.FieldDescriptor;
import org.eclipse.scanning.api.points.IPointContainer;

/**
 * A model defining a box in two dimensional space, which can be used to confine and give scale to a {@link
 * IBoundingBoxModel}.
 * <p>
 * The two axes of the box are abstracted as "fast" and "slow". Often these will be the X and Y stage motors, but other
 * axes could be used depending on the beamline configuration or the required experiment. The axis names to be used are
 * defined in AbstractBoundingBoxModel.
 *
 * Important difference between BoundingBox and IRectangularROI - rois are in data coordinates and bounding boxes are
 * in axis coordinates i.e. locations of the motors rather than the selection of the data.
 *
 * @author Colin Palmer
 * @author Matthew Gerring
 *
 */
public class BoundingBox implements IPointContainer {

	
	public enum MARKER {
		BOX;
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
	
	@FieldDescriptor(editable=false, hint="Provides information about the visible region we are linked to.")
	private String regionName;


	public BoundingBox() {
		
	}
	
	public BoundingBox(double fastAxisStart, double slowAxisStart, double fastAxisLength, double slowAxisLength) {
		super();
		this.fastAxisStart = fastAxisStart;
		this.slowAxisStart = slowAxisStart;
		this.fastAxisLength = fastAxisLength;
		this.slowAxisLength = slowAxisLength;
	}

	/**
	 * 
	 * @param spt [fastStart, slowStart]
	 * @param ept [fastEnd, slowEnd]
	 */
	public BoundingBox(double[] spt, double[] ept) {
		
		double[] len = new double[2];
		double lx = ept[0] - spt[0];
		double ly = ept[1] - spt[1];
		@SuppressWarnings("unused")
		double ang = 0d; // TODO should be used?
		if (lx > 0) {
			if (ly > 0) {
				len[0] = lx;
				len[1] = ly;
				ang = 0;
			} else {
				len[0] = lx;
				len[1] = -ly;
				ang = Math.PI * 1.5;
			}
		} else {
			if (ly > 0) {
				len[0] = -lx;
				len[1] = ly;
				ang = Math.PI * 0.5;
			} else {
				len[0] = -lx;
				len[1] = -ly;
				ang = Math.PI;
			}
		}

		fastAxisStart  = spt[0];
		fastAxisLength = len[0];
		slowAxisStart  = spt[1];
		slowAxisLength = len[1];
	}

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

	public String getRegionName() {
		return regionName;
	}

	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}

	public double getSlowAxisEnd() {
		return getSlowAxisStart()+getSlowAxisLength();
	}
	
	public double getFastAxisEnd() {
		return getFastAxisStart()+getFastAxisLength();
	}

	@Override
	public String toString() {
		return "Start="+toString(getStart())+" length="+toString(getLength());
	}

	private double[] getStart() {
		return new double[]{fastAxisStart, slowAxisStart};
	}
	private double[] getLength() {
		return new double[]{fastAxisLength, slowAxisLength};
	}
	
    private String toString(double[] a) {
        if (a == null)
            return "null";
        int iMax = a.length - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(format.format(a[i]));
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
    }
    
    private DecimalFormat format = new DecimalFormat("##########0.0###");
    public void setNumberFormat(String sformat) {
    	format = new DecimalFormat(sformat);
    }


	@Override
	public boolean containsPoint(double x, double y) {
		
		double[] spt = new double[]{getFastAxisStart(), getSlowAxisStart()};
		double[] len = new double[]{getFastAxisLength(), getSlowAxisLength()};
		double ang = 0;// TODO angle!
		
		x -= spt[0];
		y -= spt[1];
		if (ang == 0) {
			if (x < 0 || x > len[0])
				return false;
			return y >= 0 && y <= len[1];
		}
		double[] pr = transformToRotated(x, y); // Not really required until angle supported.
		if (pr[0] < 0 || pr[0] > len[0])
			return false;
		return pr[1] >= 0 && pr[1] <= len[1];
	}
	
	/**
	 * @param ox 
	 * @param oy 
	 * @return array with rotated Cartesian coordinates
	 */
	protected double[] transformToRotated(double ox, double oy) {
		double ang  = 0d; // TODO support angle...
		double cang = Math.cos(ang);
		double sang = Math.sin(ang);
		double[] car = { ox * cang + oy * sang, -ox * sang + oy * cang };
		return car;
	}

}
