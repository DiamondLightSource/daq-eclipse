package org.eclipse.scanning.test.annot;

import org.eclipse.scanning.api.annotation.scan.PointEnd;
import org.eclipse.scanning.api.annotation.scan.PointStart;
import org.eclipse.scanning.api.annotation.scan.ScanEnd;
import org.eclipse.scanning.api.annotation.scan.ScanStart;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.points.IPosition;

/**
 * 
 * Could use Mockito but always causes compilation issues
 *
 */
public class ExtendedCountingDevice extends CountingDevice {
	
    @ScanStart
    public void moveToNonObstructingLocation(IRunnableDeviceService rservice) throws Exception {
    	// Do a floating point op
        double v1 = 2;
        double v2 = 2;
        double s = v1*v2;
        count(Thread.currentThread().getStackTrace());
    }
    
    @PointStart
    public void checkNextMoveLegal(IPosition pos) throws Exception {
    	// Do a floating point op
        double v1 = 2;
        double v2 = 2;
        double s = v1*v2;
        count(Thread.currentThread().getStackTrace());
    }
    
    @PointStart
    public void notifyPosition(IPosition pos) throws Exception {
    	// Do a floating point op
        double v1 = 2;
        double v2 = 2;
        double s = v1*v2;
        count(Thread.currentThread().getStackTrace());
    }
    
    @PointEnd
    public void deleteLocation() {
        count(Thread.currentThread().getStackTrace());
    }
    
    @Override
    @ScanEnd
    public void dispose() {
       super.dispose();
       if (value!=null) throw new RuntimeException("Unexpected non-null value");
    }
}
