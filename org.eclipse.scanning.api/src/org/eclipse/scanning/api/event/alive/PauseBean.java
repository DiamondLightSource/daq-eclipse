package org.eclipse.scanning.api.event.alive;

/**
 * Used to pause the consumer. This does not pause the running process, if there is one.
 * It stops the consumer consuming more jobs and running them. 
 * 
 * A pause is required internally before attempting to reorder queues to avoid collisions.
 * An API will be put over reordering so that consumers are paused and blocked before the
 * submit queue is edited. 
 * 
 * @author Matthew Gerring
 *
 */
public class PauseBean extends ConsumerCommandBean {

	private boolean pause = true;

	public boolean isPause() {
		return pause;
	}

	public void setPause(boolean pause) {
		this.pause = pause;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (pause ? 1231 : 1237);
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
		PauseBean other = (PauseBean) obj;
		if (pause != other.pause)
			return false;
		return true;
	}
}
