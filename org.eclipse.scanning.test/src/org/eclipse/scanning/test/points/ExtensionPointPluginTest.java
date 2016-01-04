package org.eclipse.scanning.test.points;

import org.eclipse.scanning.api.points.IGenerator;
import org.eclipse.scanning.api.points.IGeneratorService;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.points.GeneratorServiceImpl;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;

// Run as plugin test
public class ExtensionPointPluginTest {
	
	private IGeneratorService service;
	
	@Before
	public void setup() {
		service = new GeneratorServiceImpl(); // Can't be bothered to set up OSGi for this test.
	}

	// Must be plugin test because uses extension points.
	@Test
	public void testExtendedGenerators() throws Exception {
		
		TestGeneratorModel model = new TestGeneratorModel();
		final IGenerator<TestGeneratorModel, Point> gen = service.createGenerator(model);
		
		assertNotNull(gen); // Simple as that
	}
}
