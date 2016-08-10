package org.eclipse.scanning.points;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PySystemState;

/**
 * Based on JythonObjectFactory
 * See: http://www.jython.org/jythonbook/en/1.0/JythonAndJavaIntegration.html#more-efficient-version-of-loosely-coupled-object-factory
 */
public class ScanPointGeneratorFactory {
	// This class compiles Jython objects and maps them to an IPointGenerator so they can be
	// used easily in Java. More specifically, it creates the Jython ScanPointGenerator interface
	// classes found in the scripts folder of this package (org.eclipse.scanning.points)
	
	// These are the constructors for each Jython SPG interface. To add a new one just replace, 
	// for example, "JArrayGenerator" with your new class and give the constructor a new name
	// like "<YourClass>Factory"
    public static JythonObjectFactory JLineGenerator1DFactory() {
        return new JythonObjectFactory(Iterator.class, "jython_spg_interface", "JLineGenerator1D");
    }
	
    public static JythonObjectFactory JLineGenerator2DFactory() {
        return new JythonObjectFactory(Iterator.class, "jython_spg_interface", "JLineGenerator2D");
    }
	
    public static JythonObjectFactory JArrayGeneratorFactory() {
        return new JythonObjectFactory(Iterator.class, "jython_spg_interface", "JArrayGenerator");
    }
	
    public static JythonObjectFactory JRasterGeneratorFactory() {
        return new JythonObjectFactory(Iterator.class, "jython_spg_interface", "JRasterGenerator");
    }
	
    public static JythonObjectFactory JSpiralGeneratorFactory() {
        return new JythonObjectFactory(Iterator.class, "jython_spg_interface", "JSpiralGenerator");
    }
	
    public static JythonObjectFactory JLissajousGeneratorFactory() {
        return new JythonObjectFactory(Iterator.class, "jython_spg_interface", "JLissajousGenerator");
    }
	
    public static JythonObjectFactory JCompoundGeneratorFactory() {
        return new JythonObjectFactory(Iterator.class, "jython_spg_interface", "JCompoundGenerator");
    }
	
    public static JythonObjectFactory JRandomOffsetMutatorFactory() {
        return new JythonObjectFactory(PyObject.class, "jython_spg_interface", "JRandomOffsetMutator");
    }
    
    // This class creates Java objects from Jython classes
    public static class JythonObjectFactory {
        private final Class javaClass;
        private final PyObject pyClass;
        
        // Constructor obtains a reference to the importer, module, and the class name
        public JythonObjectFactory(PySystemState state, Class javaClass, String moduleName, String className) {
        	
        	Properties postProperties = new Properties();
        	
            File loc = getBundleLocation("org.eclipse.scanning.points");
            state.path.add(new PyString(loc.getAbsolutePath() + "/scripts/"));
            
            this.javaClass = javaClass;
            PyObject importer = state.getBuiltins().__getitem__(Py.newString("__import__"));
            PyObject module = importer.__call__(Py.newString(moduleName));
            pyClass = module.__getattr__(className);
            // System.err.println("module=" + module + ",class=" + klass);
        }
        
        // This constructor passes through to the other constructor with the SystemState
        public JythonObjectFactory(Class javaClass, String moduleName, String className) {
            this(Py.getSystemState(), javaClass, moduleName, className);
        }
        
        // The following methods return a coerced Jython object based upon the pieces of
        // information that were passed into the factory, for various argument structures
        
        public Object createObject() {
            return pyClass.__call__().__tojava__(javaClass);
        }
        public Object createObject(Object arg1) {
            return pyClass.__call__(Py.java2py(arg1)).__tojava__(javaClass);
        }
        public Object createObject(Object arg1, Object arg2) {
            return pyClass.__call__(Py.java2py(arg1), Py.java2py(arg2)).__tojava__(javaClass);
        }
        public Object createObject(Object arg1, Object arg2, Object arg3) {
            return pyClass.__call__(Py.java2py(arg1), Py.java2py(arg2), Py.java2py(arg3)).__tojava__(javaClass);
        }
        public Object createObject(Object args[], String keywords[]) {
            PyObject convertedArgs[] = new PyObject[args.length];
            for (int i = 0; i < args.length; i++) {
                convertedArgs[i] = Py.java2py(args[i]);
            }
            return pyClass.__call__(convertedArgs, keywords).__tojava__(javaClass);
        }
        public Object createObject(Object... args) {
            return createObject(args, Py.NoKeywords);
        }
    }
    
    
    /**
	 * @param bundleName
	 * @return file this can return null if bundle is not found
	 */
	public static File getBundleLocation(final String bundleName) {
		final Bundle bundle = Platform.getBundle(bundleName);
		if (bundle == null) {
			return new File("/scratch/workspaces/workspace_git/daq-eclipse.git/org.eclipse.scanning.points");
		}
		try {
			return FileLocator.getBundleFile(bundle);
		}
		catch (IOException e) {
			return new File("/scratch/workspaces/workspace_git/daq-eclipse.git/org.eclipse.scanning.points");
		}
	}

}