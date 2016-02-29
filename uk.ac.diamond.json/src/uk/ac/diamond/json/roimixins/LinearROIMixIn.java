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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;


public abstract class LinearROIMixIn {

	@JsonIgnore abstract void setPointKeepEndPoint(int[] pt);

	@JsonIgnore abstract void setPointKeepEndPoint(double[] pt);

	@JsonIgnore abstract double[] getEndPoint();

	@JsonIgnore abstract int[] getIntEndPoint();

	@JsonIgnore abstract void setEndPoint(double eptx, double epty);

	@JsonIgnore abstract void setEndPoint(double[] ept);

	@JsonIgnore abstract void setEndPoint(int[] ept);

	@JsonIgnore abstract double[] getMidPoint();

	@JsonIgnore abstract void setMidPoint(double[] mpt);

	@JsonProperty abstract void setLength(double len);

	@JsonProperty abstract double getLength();

	@JsonIgnore abstract void setCrossHair(boolean crossHair);

	@JsonIgnore abstract boolean isCrossHair();
}
