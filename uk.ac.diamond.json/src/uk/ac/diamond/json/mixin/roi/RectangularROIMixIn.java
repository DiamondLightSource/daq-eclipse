/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.json.mixin.roi;

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class RectangularROIMixIn {

	@JsonIgnore abstract void setClippingCompensation(boolean clippingCompensation);

	@JsonIgnore abstract boolean isClippingCompensation();

	@JsonProperty abstract void setLengths(double len[]);

	@JsonIgnore abstract void setLengths(double major, double minor);

	@JsonIgnore abstract double[] getEndPoint();

	@JsonIgnore abstract double[] getMidPoint();

	@JsonIgnore abstract void setMidPoint(double[] mpt);

	@JsonProperty abstract double[] getLengths();

	@JsonIgnore abstract void setEndPointKeepLengths(double[] pt);

	@JsonIgnore abstract void setEndPoint(int[] pt);

	@JsonIgnore abstract void setEndPoint(double[] pt);

	@JsonIgnore abstract void setEndPoint(int[] pt, boolean moveX, boolean moveY);

	@JsonIgnore abstract void setEndPoint(double[] pt, boolean moveX, boolean moveY);

	@JsonIgnore abstract void setPointKeepEndPoint(int[] dpt, boolean moveX, boolean moveY);

	@JsonIgnore abstract void setPointKeepEndPoint(double[] dpt, boolean moveX, boolean moveY);

	@JsonIgnore abstract RectangularROI getBounds();
}
