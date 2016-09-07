package org.eclipse.scanning.api.points.models;

import org.eclipse.scanning.api.points.IPointContainer;

public interface IContainerModel extends IScanPathModel {

	default IPointContainer getContainer() {
		return null;
	}
}
