package org.eclipse.scanning.api.scan.event;

import java.util.EventListener;

import org.eclipse.scanning.api.scan.PositionEvent;

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
	 * Called just before the position reaches a given value
	 * @param evt
	 * @return to abort the point but not the overall scan.
	 */
	boolean positionWillPerform(PositionEvent evt);

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

		@Override
		public boolean positionWillPerform(PositionEvent evt) {
			return true; // Means carry on
		}
		
	}
}
