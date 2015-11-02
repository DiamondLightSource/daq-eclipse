package org.eclipse.scanning.api.event.alive;

import java.util.UUID;

public class TerminateBean {

	private UUID consumerId;

	public UUID getConsumerId() {
		return consumerId;
	}

	public void setConsumerId(UUID consumerId) {
		this.consumerId = consumerId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((consumerId == null) ? 0 : consumerId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TerminateBean other = (TerminateBean) obj;
		if (consumerId == null) {
			if (other.consumerId != null)
				return false;
		} else if (!consumerId.equals(other.consumerId))
			return false;
		return true;
	}
}
