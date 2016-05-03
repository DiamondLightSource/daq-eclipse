package org.eclipse.scanning.api.event.alive;

import java.util.EventListener;

public interface IHeartbeatListener extends EventListener {

	default void heartbeatPerformed(HeartbeatEvent evt) {
		// default implementation does nothing, subclasses should override as necessary
	}
}
