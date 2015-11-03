package org.eclipse.scanning.api.event.alive;

import java.util.UUID;

/**
 * Used to stop the consumer, optionally the process running the consumer may be exited.
 * 
 * @author fcp94556
 *
 */
public class KillBean {

	private UUID    consumerId;
	private boolean exitProcess=true;
	private boolean disconnect=true;

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
		result = prime * result + (disconnect ? 1231 : 1237);
		result = prime * result + (exitProcess ? 1231 : 1237);
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
		KillBean other = (KillBean) obj;
		if (consumerId == null) {
			if (other.consumerId != null)
				return false;
		} else if (!consumerId.equals(other.consumerId))
			return false;
		if (disconnect != other.disconnect)
			return false;
		if (exitProcess != other.exitProcess)
			return false;
		return true;
	}

	public boolean isExitProcess() {
		return exitProcess;
	}

	public void setExitProcess(boolean exitProcess) {
		this.exitProcess = exitProcess;
	}

	public boolean isDisconnect() {
		return disconnect;
	}

	public void setDisconnect(boolean disconnect) {
		this.disconnect = disconnect;
	}
}
