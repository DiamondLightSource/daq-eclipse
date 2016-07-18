package org.eclipse.scanning.points;

import java.util.Properties;

import org.eclipse.scanning.api.points.IPointGenerator;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

/**
 * Jython Object Factory using PySystemState. Copied from:
 * http://www.jython.org/jythonbook/en/1.0/JythonAndJavaIntegration.html#more-
 * efficient-version-of-loosely-coupled-object-factory
 */
public class JythonPointGeneratorFactory {

    public static JythonObjectFactory newLineGeneratorFactory() {
        return new JythonObjectFactory(IPointGenerator.class, "linegenerator", "LineGenerator");
        //return new JythonObjectFactory(IPointGenerator.class, "collections", "LineGenerator");

    }
    // IPointGenerator

    public static class JythonObjectFactory {
        private final Class interfaceType;
        private final PyObject klass;

        // Constructor obtains a reference to the importer, module, and the
        // class name
        public JythonObjectFactory(PySystemState state, Class interfaceType, String moduleName, String className) {Properties postProperties = new Properties();

            // The following line fixes a Python import error seemingly arising
            // from using Jython in an OSGI environment.
            // See http://bugs.jython.org/issue2355 .
            postProperties.put("python.import.site", "false");
    
            PythonInterpreter.initialize(System.getProperties(), postProperties, new String[0]);
            PythonInterpreter pi = new PythonInterpreter();
            
            pi.exec("import sys");
            pi.exec("sys.path.append('/scratch/workspaces/workspace_git/daq-eclipse.git/org.eclipse.scanning.points/scripts/')");
        
            System.out.println(state.path);

            state.path.clear(); //(0, new PyString("/scratch/workspaces/workspace_git/daq-eclipse.git/org.eclipse.scanning.points/scripts/scanpointgenerator/"));
            //state.setCurrentWorkingDir("/scratch/workspaces/workspace_git/daq-eclipse.git/org.eclipse.scanning.points/scripts/scanpointgenerator");
            System.out.println(state.path);
            //state..(new PyString("/scratch/workspaces/workspace_git/daq-eclipse.git/org.eclipse.scanning.points/scripts/"));

            this.interfaceType = interfaceType;
            PyObject importer = state.getBuiltins().__getitem__(Py.newString("__import__"));
            PyObject module = importer.__call__(Py.newString(moduleName));
            klass = module.__getattr__(className);
            System.err.println("module=" + module + ",class=" + klass);
        }

        // This constructor passes through to the other constructor
        public JythonObjectFactory(Class interfaceType, String moduleName, String className) {
            this(new PySystemState(), interfaceType, moduleName, className);
        }

        // All of the followng methods return
        // a coerced Jython object based upon the pieces of information
        // that were passed into the factory. The differences are
        // between them are the number of arguments that can be passed
        // in as arguents to the object.

        public Object createObject() {
            return klass.__call__().__tojava__(interfaceType);
        }

        public Object createObject(Object arg1) {
            return klass.__call__(Py.java2py(arg1)).__tojava__(interfaceType);
        }

        public Object createObject(Object arg1, Object arg2) {
            return klass.__call__(Py.java2py(arg1), Py.java2py(arg2)).__tojava__(interfaceType);
        }

        public Object createObject(Object arg1, Object arg2, Object arg3) {
            return klass.__call__(Py.java2py(arg1), Py.java2py(arg2), Py.java2py(arg3)).__tojava__(interfaceType);
        }

        public Object createObject(Object args[], String keywords[]) {
            PyObject convertedArgs[] = new PyObject[args.length];
            for (int i = 0; i < args.length; i++) {
                convertedArgs[i] = Py.java2py(args[i]);
            }

            return klass.__call__(convertedArgs, keywords).__tojava__(interfaceType);
        }

        public Object createObject(Object... args) {
            return createObject(args, Py.NoKeywords);
        }

    }
}