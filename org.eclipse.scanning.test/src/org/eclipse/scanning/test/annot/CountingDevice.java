package org.eclipse.scanning.test.annot;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scanning.api.AbstractScannable;
import org.eclipse.scanning.api.annotation.scan.LevelStart;
import org.eclipse.scanning.api.annotation.scan.ScanEnd;
import org.eclipse.scanning.api.annotation.scan.ScanStart;
import org.eclipse.scanning.api.points.IPosition;

/**
 * 
 * Could use Mockito but always causes compilation issues
 *
 */
public class CountingDevice extends AbstractScannable<Double> {
	
	protected Double value;
	protected Map<String, Integer> counts = new HashMap<>();
	
	public CountingDevice() {
		
	}
	
	@LevelStart
    public final void prepare() throws Exception {
        count(Thread.currentThread().getStackTrace());
	}
	
    @ScanStart
    public final void prepareVoltages() throws Exception {
    	// Do a floating point op
        double v1 = 2;
        double v2 = 2;
        double s = v1*v2;
        count(Thread.currentThread().getStackTrace());
    }
    @ScanEnd
    public void dispose() {
       value = null;
       count(Thread.currentThread().getStackTrace());
    }
	@Override
	public Double getPosition() throws Exception {
		return value;
	}
	@Override
	public void setPosition(Double value, IPosition position) throws Exception {
		this.value = value;
	}
	
	protected void count(StackTraceElement[] ste) {
		String methodName = getMethodName(ste);
		Integer count = counts.get(methodName);
		if (count==null) count = 0;
		count = count+1;
		counts.put(methodName, count);
	}
	
	public int getCount(String method) {
		if (!counts.containsKey(method)) return 0;
		return counts.get(method);
	}
	
	protected static final String getMethodName ( StackTraceElement ste[] ) {  
		   
	    String methodName = "";  
	    boolean flag = false;  
	   
	    for ( StackTraceElement s : ste ) {  
	   
	        if ( flag ) {  
	   
	            methodName = s.getMethodName();  
	            break;  
	        }  
	        flag = s.getMethodName().equals( "getStackTrace" );  
	    }  
	    return methodName;  
	}

}