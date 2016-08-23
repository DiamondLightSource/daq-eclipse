package org.eclipse.scanning.points;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Based on JythonObjectFactory
 * See: http://www.jython.org/jythonbook/en/1.0/JythonAndJavaIntegration.html#more-efficient-version-of-loosely-coupled-object-factory
 */
public class ScanPointGeneratorFactory {
	
	private static final Logger logger = LoggerFactory.getLogger(ScanPointGeneratorFactory.class);
	

	/**
	 * Call to load Jython asynchronously to avoid the
	 * long wait time that happens when points are first generated.
	 * 
	 * Call this method to load jython in a daemon thread such that
	 * when it is first used, for instance in the UI, it will execute
	 * fast because the interpreter has classloaded.
	 */
	static void init() {
		
		final Thread background = new Thread() {
			public void run() {
				// Loading one causes Jython to class load.
				ScanPointGeneratorFactory.JLineGenerator1DFactory();
			}
		};
		background.setDaemon(true);
		background.setName("Jython loader thread");    // Always name threads.
		background.setPriority(Thread.MIN_PRIORITY+2); // Background but some urgency more than least
		background.start();
	}
	
	
	// This class compiles Jython objects and maps them to an IPointGenerator so they can be
	// used easily in Java. More specifically, it creates the Jython ScanPointGenerator interface
	// classes found in the scripts folder of this package (org.eclipse.scanning.points)
	
	// These are the constructors for each Jython SPG interface. To add a new one just replace, 
	// for example, "JArrayGenerator" with your new class and give the constructor a new name
	// like "<YourClass>Factory"
    public static JythonObjectFactory JLineGenerator1DFactory() {
        return new JythonObjectFactory(Iterator.class, "jython_spg_interface", "JLineGenerator1D");
    }
	
    private static void addLast(CompositeClassLoader composite, String bundleName) {
    	try {
    		ClassLoader cl = getBundleLoader(bundleName);
    		composite.addLast(cl);
	    } catch (NullPointerException ne) {
	    	// Allowed, the bundles do not have to be there we are just trying to be helpful
	    	// in loading classes without making hard dependencies on them.

    	}
	}

	public static JythonObjectFactory JLineGenerator2DFactory() {
        return new JythonObjectFactory(Iterator.class, "jython_spg_interface", "JLineGenerator2D");
    }
	
    public static JythonObjectFactory JArrayGeneratorFactory() {
        return new JythonObjectFactory(Iterator.class, "jython_spg_interface", "JArrayGenerator");
    }
	
    public static JythonObjectFactory JSpiralGeneratorFactory() {
        return new JythonObjectFactory(Iterator.class, "jython_spg_interface", "JSpiralGenerator");
    }
	
    public static JythonObjectFactory JLissajousGeneratorFactory() {
        return new JythonObjectFactory(Iterator.class, "jython_spg_interface", "JLissajousGenerator");
    }
	
    public static JythonObjectFactory JCompoundGeneratorFactory() {
        return new JythonObjectFactory(SerializableIterator.class, "jython_spg_interface", "JCompoundGenerator");
    }
	
    public static JythonObjectFactory JRandomOffsetMutatorFactory() {
        return new JythonObjectFactory(PyObject.class, "jython_spg_interface", "JRandomOffsetMutator");
    }
    
    // This class creates Java objects from Jython classes
    public static class JythonObjectFactory {
    	
        
    	private final Class<?> javaClass;
        private final PyObject pyClass;
        
        // This constructor passes through to the other constructor with the SystemState
        JythonObjectFactory(Class<?> javaClass, String moduleName, String className) {

        	setupSystemState();
        	PySystemState state = Py.getSystemState();
          
            this.javaClass = javaClass;
            PyObject importer = state.getBuiltins().__getitem__(Py.newString("__import__"));
            PyObject module = importer.__call__(Py.newString(moduleName));
            pyClass = module.__getattr__(className);
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
    	
    	if (!loc.exists()) throw new IllegalArgumentException(loc+" does not exist!");
    	File find = new File(loc, name);
        if (find.exists()) return find;
        
        for (File child : loc.listFiles()) {
		    if (child.getName().startsWith(name)) {
		    	return child;
		    }
		}
        return null;
	}

	private static volatile boolean setupPythonState = false;

	private static void setupSystemState() {
		
		if (setupPythonState) return;
		setupPythonState = true;
		
 		createPythonPath(); // Must do this first
 		
    	PySystemState state = Py.getSystemState();
        ClassLoader jythonClassloader = createJythonClassLoader(state);
	   	state.setClassLoader(jythonClassloader);
    	addScriptPaths(state);
	}

	private static ClassLoader createJythonClassLoader(PySystemState state) {
		
    	ClassLoader jythonClassloader = ScanPointGeneratorFactory.class.getClassLoader();
    	
    	try { // For non-unit tests, attempt to use the OSGi classloader of this bundle.
    		String jythonBundleName = System.getProperty("org.eclipse.scanning.jython.osgi.bundle.name", "uk.ac.diamond.jython");
    		CompositeClassLoader composite = new CompositeClassLoader(state.getClassLoader());
    		composite.addLast(ScanPointGeneratorFactory.class.getClassLoader());
    		addLast(composite, jythonBundleName);
    		jythonClassloader = composite;

    	} catch (Throwable ne) {
    		if (logger!=null) {
    			logger.debug("Problem loading jython bundles!", ne);
    		} else {
    			ne.printStackTrace();
    		}
    		// Legal, if static classloader does not work in tests, there will be
    		// errors. If bundle classloader does not work in product, there will be errors.
    		// Typically the message is something like: 'cannot find module org.eclipse.scanning.api'
    	}
    	return jythonClassloader;
	}


	private static void createPythonPath() {
		try {
	    	String jythonBundleName = System.getProperty("org.eclipse.scanning.jython.osgi.bundle.name", "uk.ac.diamond.jython");
	        File loc = getBundleLocation(jythonBundleName); // TODO Name the jython OSGi bundle without Diamond in it!
	        File jythonDir = find(loc, "jython");
	        
	        Properties props = new Properties();
	    	props.put("python.home", jythonDir.getAbsolutePath());
	    	props.put("python.console.encoding", "UTF-8"); // Used to prevent: console: Failed to install '': java.nio.charset.UnsupportedCharsetException: cp0.
	    	props.put("python.security.respectJavaAccessibility", "false"); //don't respect java accessibility, so that we can access protected members on subclasses
	    	props.put("python.import.site","false");

	    	Properties preprops = System.getProperties();
	    	
	    	PySystemState.initialize(preprops, props);
	    
	    } catch (Throwable ne) {
	    	System.out.print("Problem loading jython bundles!");
	    	ne.printStackTrace();
	    	logger.debug("Problem loading jython bundles!", ne);
	    }
	}
 
    private static void addScriptPaths(PySystemState state) {
    	        	
        try {
        	// Search for the Libs directory which should have been expanded out either
        	// directly into the bundle or into the 'jython2.7' folder.
	    	String jythonBundleName = System.getProperty("org.eclipse.scanning.jython.osgi.bundle.name", "uk.ac.diamond.jython");

            File loc = getBundleLocation(jythonBundleName); // TODO Name the jython OSGi bundle without Diamond in it!
	        File jythonDir = find(loc, "jython");
           	state.path.add(new PyString(jythonDir.getAbsolutePath())); // Resolves the collections
	        File lib       = find(jythonDir, "Lib");
	        state.path.add(new PyString(lib.getAbsolutePath())); // Resolves the collections
           
           	System.out.println(state.path);
            
        } catch (Exception ne) {
        	System.out.println("Problem setting jython path to include scripts");
        	ne.printStackTrace();
        	logger.debug("Problem setting jython path to include scripts!", ne);
        }

        final File pointsLocation = getBundleLocation("org.eclipse.scanning.points");
        state.path.add(new PyString(pointsLocation.getAbsolutePath() + "/scripts/"));
        
	}

	private static ClassLoader getBundleLoader(String name) {
    	Bundle bundle = Platform.getBundle(name);
    	BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);
    	return bundleWiring.getClassLoader();
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
	 * @return File
	 */
	private static File getBundleLocation(final String bundleName) {
		
		try {
			final Bundle bundle = Platform.getBundle(bundleName);
			if (bundle == null) {
				throw new IOException();
			}
			return FileLocator.getBundleFile(bundle);
		}
		catch (IOException e) {
			File dir = new File("../"+bundleName);
			if (dir.exists()) return dir;
			dir = new File("../../daq-eclipse.git/"+bundleName);
			if (dir.exists()) return dir;
			
			// These paths refer to finding things in the travis build 
			// They will not resolve from the IDE or binary.
			dir = new File("../../org.eclipse.scanning/"+bundleName);
			if (dir.exists()) return dir;
			dir = new File("../../diamond-jython/"+bundleName);
			if (dir.exists()) return dir;
			dir = new File("../../../diamond-jython/"+bundleName);
			if (dir.exists()) return dir;
		}
		return null;
	}

}