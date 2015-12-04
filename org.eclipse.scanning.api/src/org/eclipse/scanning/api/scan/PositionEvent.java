package org.eclipse.scanning.api.scan;

import java.util.EventObject;
import java.util.List;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.points.IPosition;

/**
 * 
 * 
 * @author Matthew Gerring
 *
 */
public class PositionEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6101070929612847926L;
	
	private int                 level;
	private List<IScannable<?>> scannables;
	
	public PositionEvent(IPosition position) {
		super(position);
	}
	
	/**
	 * The current position during a move or the final
	 * position at the end of a move.
	 * 
	 * If during a move the position will be read from the
	 * Scannables.
	 * 
	 * @return
	 */
	public IPosition getPosition() {
		return (IPosition)getSource();
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public List<IScannable<?>> getScannables() {
		return scannables;
	}

	public void setScannables(List<IScannable<?>> scannables) {
		this.scannables = scannables;
	}

}
