package org.eclipse.scanning.points;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * 
 * A simple classloader which can be used as a composite for 
 * the purposes of Jython loading.
 * 
 * @author Matthew Gerring
 *
 */
class CompositeClassLoader extends ClassLoader {

    private final List<ClassLoader> classLoaders;
 
    private CompositeClassLoader() {
        this.classLoaders = new Vector<>(7);
    }
    
    /**
     * Creates a CompositeClassLoader with the argument passed in as the first loader.
     * @param orig
     */
    public CompositeClassLoader(ClassLoader orig) {
    	this();
        addFirst(orig); 
    }
   
    /**
     * Call to add a loader at the end of this loader
     * @param classLoader
     */
    public void addLast(ClassLoader classLoader) {
        if (classLoader == null) throw new IllegalArgumentException("The classloader may not be null!");
        classLoaders.add(classLoader);
    }
    /**
     * Call to add a loader at the start of this loader
     * @param classLoader
     */
    public void addFirst(ClassLoader classLoader) {
        if (classLoader == null) return;
        classLoaders.add(0, classLoader);
    }
    
    @Override
    protected final Class<?> findClass(String name) throws ClassNotFoundException {
    	for (ClassLoader classLoader : classLoaders) {
            try {
                final Class<?> c = classLoader.loadClass(name);
                if (c!=null) return c;
            } catch (Throwable notFound) {
                // This is allowable
            	continue;
            }
        }
    	return super.findClass(name);
    }
    
    @Override
    protected final URL findResource(String name) {
    	for (ClassLoader classLoader : classLoaders) {
            try {
            	URL url = classLoader.getResource(name);
            	if (url!=null) return url;
            } catch (Throwable notFound) {
                // This is allowable
            	continue;
            }
        }
    	return super.findResource(name);
    }

    @Override
    protected final Enumeration<URL> findResources(String name) throws IOException {
    	for (ClassLoader classLoader : classLoaders) {
            try {
            	Enumeration<URL> ret = classLoader.getResources(name);
            	if (ret!=null) return ret;
            } catch (Throwable notFound) {
                // This is allowable
            	continue;
            }
        }
        return super.findResources(name);
    }

    
    @Override
    public Class loadClass(String name) throws ClassNotFoundException {
        
    	for (ClassLoader classLoader : classLoaders) {
            try {
                return classLoader.loadClass(name);
            } catch (Throwable notFound) {
                // This is allowable
            	continue;
            }
        }
        
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader != null) {
            return contextClassLoader.loadClass(name);
        } else {
            throw new ClassNotFoundException(name);
        }
    }

}
