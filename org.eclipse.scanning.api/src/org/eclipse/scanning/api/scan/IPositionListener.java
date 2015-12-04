package org.eclipse.scanning.api.scan;

import java.util.EventListener;

/**
 * A positioner moves the motors, taking into account level 
 * and blocks until done.
 * 
 * It is posible to get an event for the move, both when each level
 * is complete and at the end of the move, using this listener. This
 * can be useful for designs that to and action which should notify other
 * objects about moves.
 * 
 * @author Matthew Gerring
 *
 */
public interface IPositionListener extends EventListener {

	/**
	 * Called after a given move level has been reached.
	 * @param evt
	 */
	void levelPerformed(PositionEvent evt);
	
	/**
	 * Called after a given position is reached.
	 * @param evt
	 */
	void positionPerformed(PositionEvent evt);
	
	public class Stub implements IPositionListener {

		@Override
		public void levelPerformed(PositionEvent evt) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void positionPerformed(PositionEvent evt) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
