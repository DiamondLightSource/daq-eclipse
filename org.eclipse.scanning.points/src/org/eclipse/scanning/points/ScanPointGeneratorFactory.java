package org.eclipse.scanning.points;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;
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
        	
            try { // For non-unit tests, attempt to use the OSGi classloader of this bundle.
	        	Bundle bundle = Platform.getBundle("org.eclipse.scanning.points");
	        	BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);
	        	ClassLoader classLoader = bundleWiring.getClassLoader();
	        	state.setClassLoader(classLoader);
            } catch (Exception ne) {
            	// Legal, if static classloader does not work in tests, there will be
            	// errors. If bundle classloader does not work in product, there will be errors.
            	// Typically the message is something like: 'cannot find module org.eclipse.scanning.api'
            }
        	
            File loc = getBundleLocation("org.eclipse.scanning.points");
            state.path.add(new PyString(loc.getAbsolutePath() + "/scripts/"));
            
            try {
            	// Search for the Libs directory which should have been expanded out either
            	// directly into the bundle or into the 'jython2.7' folder.
	            loc = getBundleLocation("uk.ac.diamond.jython"); // TODO Name the jython OSGi bundle without Diamond in it!
	           	           
	            File lib = find(loc, "Lib");
	            if (lib.exists()) state.path.add(new PyString(lib.getAbsolutePath())); // Resolves the collections
	            
            } catch (Exception ignored) {
            	// We do it anyway, depending on the jython version, collections may be found.
            	// Test decks can run without this step happening because their config
            	// adds a static jython to the classpath.
            }
            
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
    
    
    private static File find(File loc, String name) {
    	
    	File find = new File(loc, name);
        if (find.exists()) return find;
        
        find = new File(loc.getAbsolutePath()+"/jython2.7/"+name);
        if (find.exists()) return find;
        
        for (File child : loc.listFiles()) {
		    if (child.isDirectory() && child.getName().startsWith("jython")) {
		    	final File cfild = new File(child, name);
		    	if (cfild.exists()) return cfild; 
		    }
		}
        return null;
	}

	private static String bundlePath = null;
    
    public String getBundlePath() {
    	return bundlePath;
    }
 
	public static void setBundlePath(String newPath) {
    	bundlePath = newPath;
    }
    
    /**
	 * @param bundleName
	 * @return File; can return null if bundle is not found
	 */
	public static File getBundleLocation(final String bundleName) {
		
		if (bundlePath!=null) return new File(bundlePath);
		
		final Bundle bundle = Platform.getBundle(bundleName);
		if (bundle == null) {
			return null;
		}
		try {
			return FileLocator.getBundleFile(bundle);
		} catch (IOException e) {
			return null;
		}
	}

}