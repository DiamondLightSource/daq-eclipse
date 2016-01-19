package org.eclipse.scanning.api.event.alive;

import java.util.EventListener;

public interface IHeartbeatListener extends EventListener {

	public class Stub implements IHeartbeatListener {

		@Override
		public void heartbeatPerformed(HeartbeatEvent evt) {
			// TODO Auto-generated method stub

		}

	}

	void heartbeatPerformed(HeartbeatEvent evt);
}
