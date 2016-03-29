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

package uk.ac.diamond.json.mixin;

import java.util.Collection;

import org.eclipse.scanning.api.points.models.IScanPathModel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class ScanRequestMixIn {

	@JsonProperty abstract Collection<IScanPathModel> getModels();

	@JsonProperty abstract void setModels(Collection<IScanPathModel> models);

	@JsonIgnore abstract void setModels(IScanPathModel... models);

	@JsonProperty abstract Collection<String> getMonitorNames();

	@JsonProperty abstract void setMonitorNames(Collection<String> monitorNames);

	@JsonIgnore abstract void setMonitorNames(String... monitorNames);
}
