package uk.ac.diamond.daq.activemq.connector.test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * An Answer which simulates loading classes by name.
 * <p>
 * It will search the classes given in the constructor for one whose name matches the first method argument, and throw
 * a ClassNotFoundException if no classes match.
 *
 * @author Colin Palmer
 *
 */
public class MockClassLoaderAnswer implements Answer<Class<?>> {

	final Set<Class<?>> classes;

	public MockClassLoaderAnswer(Class<?>... classes) {
		this.classes = new HashSet<>();
		this.classes.addAll(Arrays.asList(classes));
	}

	@Override
	public Class<?> answer(InvocationOnMock invocation) throws Throwable {
		String className = (String) invocation.getArguments()[0];
		for (Class<?> clazz : classes) {
			if (clazz.getName().equals(className)) {
				return clazz;
			}
		}
		throw new ClassNotFoundException("Class " + className + " not found");
	}
}