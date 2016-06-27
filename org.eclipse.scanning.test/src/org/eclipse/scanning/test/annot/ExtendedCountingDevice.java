package org.eclipse.scanning.test.annot;

import java.util.ArrayList;
import java.util.List;

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
	
	private List<IPosition>              positions = new ArrayList<>();
	private List<IRunnableDeviceService> services  = new ArrayList<>();
	
    @ScanStart
    public void moveToNonObstructingLocation(IRunnableDeviceService rservice) throws Exception {
    	// Do a floating point op
        double v1 = 2;
        double v2 = 2;
        double s = v1*v2;
        count(Thread.currentThread().getStackTrace());
        services.add(rservice); // Normally same one each time
    }
    
    @PointStart
    public void checkNextMoveLegal(IPosition pos) throws Exception {
    	// Do a floating point op
        double v1 = 2;
        double v2 = 2;
        double s = v1*v2;
        count(Thread.currentThread().getStackTrace());
        positions.add(pos);
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
       positions.clear();
       services.clear();
       if (value!=null) throw new RuntimeException("Unexpected non-null value");
    }

	public List<IPosition> getPositions() {
		return positions;
	}

	public List<IRunnableDeviceService> getServices() {
		return services;
	}
}
