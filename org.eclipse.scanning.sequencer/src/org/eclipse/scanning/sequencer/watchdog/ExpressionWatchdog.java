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
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositionListenable;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.eclipse.scanning.sequencer.expression.ServerExpressionService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Monitors an expression of scannables and if one of the values changes, reevaluates the
 * expression.
 * 
 
  Example XML configuration
    <pre>
    {@literal <!--  Watchdog Expression Example -->}
	{@literal <bean id="expressionModel" class="org.eclipse.scanning.api.device.models.DeviceWatchdogModel">}
	{@literal 	<property name="expression"   value="beamcurrent >= 1.0 &amp;&amp; !portshutter.equalsIgnoreCase(&quot;Closed&quot;)"/>}
	{@literal 	<property name="message"      value="Beam has been lost"/>}
    {@literal   <property name="bundle"       value="org.eclipse.scanning.api" /> <!-- Delete for real spring? -->}
	{@literal </bean>}
	{@literal <bean id="expressionWatchdog" class="org.eclipse.scanning.sequencer.watchdog.ExpressionWatchdog" init-method="activate">}
	{@literal 	<property name="model"        ref="expressionModel"/>}
    {@literal   <property name="bundle"       value="org.eclipse.scanning.sequencer" /> <!-- Delete for real spring? -->}
	{@literal </bean>}

 * 
 * @author Matthew Gerring
 *
 */
public class ExpressionWatchdog extends AbstractWatchdog implements IPositionListener {
	
	private static Logger logger = LoggerFactory.getLogger(ExpressionWatchdog.class);
	

	private IExpressionEngine engine;
	private IPosition         lastCompletedPoint;


	private List<IScannable<?>>       scannables;
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
			checkExpression(true);
					
		} catch (Exception ne) {
			logger.error("Cannot process position "+evt, ne);
		}	
	}
	private boolean checkExpression(boolean requirePause) throws Exception {
		Boolean ok = engine.evaluate();
		
		if (requirePause) {
			if (!ok) {
				logger.debug("Expression Watchdog pausing on "+device.getName());
				bean.setMessage(model.getMessage());
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
		return ok;
	}
	
	@ScanStart
	public void start(ScanBean bean) throws ScanningException {
		setBean(bean);
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

				engine.addLoadedVariable(scannable.getName(), scannable.getPosition());
		    }
		    
		    // Check it
		    boolean ok = checkExpression(false);
		    if (!ok) throw new ScanningException(model.getMessage()+". The expression '"+model.getExpression()+"' is false and a scan may not be run!");
		    
		    // Listen to it
		    for (IScannable<?> scannable : scannables) {
			    ((IPositionListenable)scannable).addPositionListener(this);
			}
		    
		} catch (ScanningException ne) {
			throw ne; // If there is something badly wrong a proper scanning exception will be prepared and thrown
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
	
	
	private BundleContext bcontext;
	
	public IExpressionService getExpressionService() {
		if (expressionService==null) {
			ServiceReference<IExpressionService> ref = bcontext.getServiceReference(IExpressionService.class);
			if (ref!=null) expressionService = bcontext.getService(ref);
		}
		return expressionService;
	}
	public void setExpressionService(IExpressionService eservice) {
		ExpressionWatchdog.expressionService = eservice;
	}
	public static void setTestExpressionService(ServerExpressionService eservice) {
		expressionService = eservice;
	}
	
	public void start(ComponentContext context) {
		this.bcontext = context.getBundleContext();
	}

}
