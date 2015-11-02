package org.eclipse.scanning.api.event.alive;

import org.eclipse.scanning.api.event.IEventListener;

public interface IHeartbeatListener extends IEventListener<HeartbeatBean> {

	
	public class Stub implements IHeartbeatListener {

		@Override
		public Class<HeartbeatBean> getBeanClass() {
			return HeartbeatBean.class;
		}

		@Override
		public void heartbeatPerformed(HeartbeatEvent evt) {
			// TODO Auto-generated method stub

		}

	}

	void heartbeatPerformed(HeartbeatEvent evt);
}
