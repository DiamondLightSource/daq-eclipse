package org.eclipse.scanning.api.scan.event;

import java.util.EventListener;

public interface ILocationListener extends EventListener {

	void locationPerformed(LocationEvent evt);
}
