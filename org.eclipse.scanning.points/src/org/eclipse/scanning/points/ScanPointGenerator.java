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
	    
	    String strName = String.format("'%s'", name);
	    String strUnits = String.format("'%s'", units);
	    String strStart = String.valueOf(start);
        String strStop = String.valueOf(stop);
        String strNumPoints = String.valueOf(numPoints);
	    
	    @SuppressWarnings("unchecked")
        List<IPosition> points = (List<IPosition>) pi.eval("list(create_line("
                + strName + ","
                + strUnits + ","
                + strStart + ","
                + strStop + ","
                + strNumPoints
                + "))");
	    
		return points;
	}
    
    public List<IPosition> create2DLinePoints(String[] names, String units, double[] start, double[] stop, int numPoints) {
        
        String strNames = Arrays.toString(names);
        String strUnits = String.format("'%s'", units);
        String strStart = Arrays.toString(start);
        String strStop = Arrays.toString(stop);
        String strNumPoints = String.valueOf(numPoints);
        
        @SuppressWarnings("unchecked")
        List<IPosition> points = (List<IPosition>) pi.eval("list(create_2D_line("
                + strNames + ", "
                + strUnits + ", "
                + strStart + ", "
                + strStop + ", "
                + strNumPoints
                + "))");
        
        return points;
    }

    public List<IPosition> createRasterPoints(HashMap<String, Object> inner_line, HashMap<String, Object> outer_line, boolean alternateDirection) {
        
        String strAlternateDirection;
        if (alternateDirection) {
            strAlternateDirection = "alternate_direction=True";
        }
        else {
            strAlternateDirection = "alternate_direction=False";
        }
        
        String units = String.format("'%s'", inner_line.get("units"));
        String name = String.format("'%s'", inner_line.get("name"));
        String start = String.valueOf(inner_line.get("start"));
        String stop = String.valueOf(inner_line.get("stop"));
        String num_points = String.valueOf(inner_line.get("num_points"));
        
        pi.exec("inner = dict("
                + "name=" + name + ", "
                + "units=" + units + ", "
                + "start=" + start + ", "
                + "stop=" + stop + ", "
                + "num_points=" + num_points + ", "
                + ")");

        units = String.format("'%s'", outer_line.get("units"));
        name = String.format("'%s'", outer_line.get("name"));
        start = String.valueOf(outer_line.get("start"));
        stop = String.valueOf(outer_line.get("stop"));
        num_points = String.valueOf(outer_line.get("num_points"));

        pi.exec("outer = dict("
                + "name=" + name + ", "
                + "units=" + units + ", "
                + "start=" + start + ", "
                + "stop=" + stop + ", "
                + "num_points=" + num_points + ", "
                + ")");
        
        @SuppressWarnings("unchecked")
        List<IPosition> points = (List<IPosition>) pi.eval("list(create_raster("
                + "inner,"
                + "outer,"
                + strAlternateDirection + ", "
                + "))");
        
        return points;
    }
    
    public List<IPosition> createSpiralPoints(String[] names, String units, double[] centre, double radius, double scale, boolean alternateDirection) {

        String strAlternateDirection;
        if (alternateDirection) {
            strAlternateDirection = "alternate_direction=True";
        }
        else {
            strAlternateDirection = "alternate_direction=False";
        }
        String strNames = Arrays.toString(names);
        String strUnits = String.format("'%s'", units);
        String strCentre = Arrays.toString(centre);
        String strRadius = String.valueOf(radius);
        String strScale = String.valueOf(scale);
        
        @SuppressWarnings("unchecked")
        List<IPosition> points = (List<IPosition>) pi.eval("list(create_spiral("
                + strNames + ", "
                + strUnits + ", "
                + strCentre + ", "
                + strRadius + ", "
                + strScale + ", "
                + strAlternateDirection
                + "))");
        
        return points;
    }
}
