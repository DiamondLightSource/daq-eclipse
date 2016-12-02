package org.eclipse.scanning.sequencer.watchdog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.expressions.IExpressionEngine;
import org.eclipse.dawnsci.analysis.api.expressions.IExpressionService;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.annotation.scan.PointEnd;
import org.eclipse.scanning.api.annotation.scan.ScanFinally;
import org.eclipse.scanning.api.annotation.scan.ScanStart;
import org.eclipse.scanning.api.device.models.DeviceWatchdogModel;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositionListenable;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Monitors an expression of scannables and if one of the values changes, reevaluates the
 * expression.
 * 
 * @author Matthew Gerring
 *
 */
public class ExpressionWatchdog extends AbstractWatchdog implements IPositionListener {
	
	private static Logger logger = LoggerFactory.getLogger(ExpressionWatchdog.class);
	

	private IExpressionEngine engine;
	private IPosition         lastCompletedPoint;


	private List<IScannable<?>> scannables;
	
	private static IExpressionService expressionService;

	public ExpressionWatchdog() {
		super();
	}
	public ExpressionWatchdog(DeviceWatchdogModel model) {
		super(model);
	}

	/**
	 * Called on a thread when the position changes.
	 */
	public void positionChanged(PositionEvent evt) {
		checkPosition(evt);
	}
	public void positionPerformed(PositionEvent evt) {
		checkPosition(evt);
	}

	private void checkPosition(PositionEvent evt) {
		try {
			if (engine==null) return;
			
			IPosition pos = evt.getPosition();
			if (pos.getNames().size()!=1) return;
			String name = pos.getNames().get(0);
			engine.addLoadedVariable(name, pos.get(name));
			checkExpression();
					
		} catch (Exception ne) {
			logger.error("Cannot process position "+evt, ne);
		}	
	}
	private void checkExpression() throws Exception {
		Boolean ok = engine.evaluate();
		if (!ok) {
			logger.debug("Expression Watchdog pausing on "+device.getName());
			device.pause();
		} else {
			logger.debug("Expression Watchdog resuming on "+device.getName());
			if (lastCompletedPoint!=null) {
				logger.debug("Expression Watchdog seeking on "+device.getName());
				device.seek(lastCompletedPoint.getStepIndex());
			}
			device.resume();
			logger.debug("Expression Watchdog resumed on "+device.getName());
		}
	}
	
	@ScanStart
	public void start() {
		logger.debug("Expression Watchdog starting on "+device.getName());
		try {
		    this.engine = getExpressionService().getExpressionEngine();
		    
		    engine.createExpression(model.getExpression()); // Parses expression, may send exception on syntax
		    Collection<String> names = engine.getVariableNamesFromExpression();
		    this.scannables = new ArrayList<>(names.size());
		    for (String name : names) {
				IScannable<?> scannable = getScannable(name);
				scannables.add(scannable);
				
			    if (!(scannable instanceof IPositionListenable)) throw new ScanningException(name+" is not a position listenable!");
			    ((IPositionListenable)scannable).addPositionListener(this);

				engine.addLoadedVariable(scannable.getName(), scannable.getPosition());
		    }
		    
		} catch (Exception ne) {
			logger.error("Cannot start watchdog!", ne);
		}
		logger.debug("Expression Watchdog started on "+device.getName());
	} 
	
	@PointEnd
	public void pointEnd(IPosition done) {
		this.lastCompletedPoint = done;
	}
	
	@ScanFinally
	public void stop() {
		logger.debug("Expression Watchdog stopping on "+device.getName());
		try {
			if (scannables!=null) for (IScannable<?> scannable : scannables) {
		    	((IPositionListenable)scannable).removePositionListener(this);
			}
			scannables.clear();
			engine = null;
		    
		} catch (Exception ne) {
			logger.error("Cannot stop watchdog!", ne);
		}
		logger.debug("Expression Watchdog stopped on "+device.getName());
	}
	
	public static IExpressionService getExpressionService() {
		return expressionService;
	}
	public static void setExpressionService(IExpressionService eservice) {
		ExpressionWatchdog.expressionService = eservice;
	}

}
