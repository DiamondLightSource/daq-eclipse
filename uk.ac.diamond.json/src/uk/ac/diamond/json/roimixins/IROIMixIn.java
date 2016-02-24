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

package uk.ac.diamond.json.roimixins;

import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class IROIMixIn {

	@JsonProperty abstract void setPoint(double[] point);

	// TODO do these need to be explicitly ignored?
//	public void setPoint(int[] point);
//	public void setPoint(int x, int y);
//	public void setPoint(double x, double y);

	@JsonIgnore abstract double[] getPointRef();

	@JsonProperty abstract double[] getPoint();

	@JsonIgnore abstract double getPointX();

	@JsonIgnore abstract double getPointY();

	@JsonIgnore abstract int[] getIntPoint();

	@JsonProperty abstract void setPlot(boolean require);

	@JsonProperty abstract boolean isPlot();

	@JsonIgnore abstract IRectangularROI getBounds();

	@JsonIgnore abstract boolean isNearOutline(double x, double y, double distance);
}
