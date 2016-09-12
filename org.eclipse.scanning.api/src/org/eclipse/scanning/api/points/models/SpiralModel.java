/*-
 * Copyright © 2015 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package org.eclipse.scanning.api.points.models;

import org.eclipse.scanning.api.points.models.AbstractBoundingBoxModel;

public class SpiralModel extends AbstractBoundingBoxModel {

	private double scale = 1;

	public SpiralModel() {
		setName("Fermat Spiral");
	}
	public SpiralModel(String fastName, String slowName, double scale, BoundingBox box) {
		super(fastName, slowName, box);
		setName("Fermat Spiral");
		this.scale = scale;
	}

	public double getScale() {
		return scale;
	}

	public void setScale(double newValue) {
		double oldValue = this.scale;
		this.scale = newValue;
		this.pcs.firePropertyChange("scale", oldValue, newValue);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(scale);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SpiralModel other = (SpiralModel) obj;
		if (Double.doubleToLongBits(scale) != Double.doubleToLongBits(other.scale))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "SpiralModel [scale=" + scale + ", getFastAxisName()=" + getFastAxisName() + ", getSlowAxisName()="
				+ getSlowAxisName() + "]";
	}
}
