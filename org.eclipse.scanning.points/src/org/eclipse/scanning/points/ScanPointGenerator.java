package org.eclipse.scanning.points;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.eclipse.scanning.api.points.IPosition;
import org.python.util.PythonInterpreter;

public class ScanPointGenerator {
	public static PythonInterpreter pi;
    
    public ScanPointGenerator() {
    }
    
    static {
        Properties postProperties = new Properties();

        // The following line fixes a Python import error seemingly arising
        // from using Jython in an OSGI environment.
        // See http://bugs.jython.org/issue2355 .
        postProperties.put("python.import.site", "false");

        PythonInterpreter.initialize(System.getProperties(), postProperties, new String[0]);
        pi = new PythonInterpreter();
        
        pi.exec("import sys");
        pi.exec("sys.path.append('/scratch/workspaces/workspace_git/daq-eclipse.git/org.eclipse.scanning.points/scripts/')");
        pi.exec("from jython_spg_interface import *");
    }
	
	public List<IPosition> createLinePoints(String name, String units, double start, double stop, int numPoints) {
	    
	    String strStart = String.valueOf(start);
        String strStop = String.valueOf(stop);
        String strNumPoints = String.valueOf(numPoints);
	    
	    @SuppressWarnings("unchecked")
        List<IPosition> points = (List<IPosition>) pi.eval("list(create_line("
                + name + ","
                + units + ","
                + strStart + ","
                + strStop + ","
                + strNumPoints
                + "))");
	    
		return points;
	}
    
    public List<IPosition> create2DLinePoints(String[] names, String units, double[] start, double[] stop, int numPoints) {
        
        String strNames = Arrays.toString(names);
        String strStart = Arrays.toString(start);
        String strStop = Arrays.toString(stop);
        String strNumPoints = String.valueOf(numPoints);
        
        @SuppressWarnings("unchecked")
        List<IPosition> points = (List<IPosition>) pi.eval("list(create_2D_line("
                + strNames + ","
                + units + ","
                + strStart + ","
                + strStop + ","
                + strNumPoints
                + "))");
        
        return points;
    }
}
