package org.eclipse.scanning.test.scan.preprocess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.scanning.api.scan.process.IPreprocessor;
import org.eclipse.scanning.example.preprocess.ExamplePreprocessor;
import org.junit.After;
import org.junit.Before;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class PreprocessPluginTest extends PreprocessTest {

	private static BundleContext context;

	private ServiceRegistration<IPreprocessor> registration;

	public static void activate(BundleContext context) {
		PreprocessPluginTest.context = context;
	}

	@Override
	@Before
	public void before() {
		IPreprocessor newPreprocessor = new ExamplePreprocessor();
		registration = context.registerService(IPreprocessor.class, newPreprocessor, null);

		preprocessor = context.getService(context.getServiceReference(IPreprocessor.class));
		assertNotNull(preprocessor);

		// ensure we have the preprocessor we registered (this might not be the case if other plug-ins have registered
		// other IPreprocessors)
		assertEquals(newPreprocessor, preprocessor);
	}

	@After
	public void after() {
		registration.unregister();
		registration = null;
		preprocessor = null;
	}
}
