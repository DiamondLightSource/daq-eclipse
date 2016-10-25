package org.eclipse.scanning.api.event.alive;

/**
 * Used to stop the consumer, optionally the process running the consumer may be exited.
 * 
 * @author Matthew Gerring
 *
 */
public class KillBean extends ConsumerCommandBean {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2504956804006283562L;
	
	private boolean restart=false;
	private boolean exitProcess=true;
	private boolean disconnect=true;
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (disconnect ? 1231 : 1237);
		result = prime * result + (exitProcess ? 1231 : 1237);
		result = prime * result + (restart ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		KillBean other = (KillBean) obj;
		if (disconnect != other.disconnect)
			return false;
		if (exitProcess != other.exitProcess)
			return false;
		if (restart != other.restart)
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

	public boolean isRestart() {
		return restart;
	}

	public void setRestart(boolean restart) {
		this.restart = restart;
	}
}
