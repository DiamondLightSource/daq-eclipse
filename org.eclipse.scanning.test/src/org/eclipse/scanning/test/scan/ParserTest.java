package org.eclipse.scanning.test.scan;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.api.scan.IParser;
import org.eclipse.scanning.api.scan.IParserService;
import org.eclipse.scanning.command.ParserServiceImpl;
import org.junit.Before;
import org.junit.Test;

public class ParserTest {

	private static final double PRECISION = 1e-8;

	private IParserService parserService;

	@Before
	public void setUp() {
		parserService = new ParserServiceImpl();
		// TODO also have a Plugin test that uses OSGi-DS
	}

	@Test
	public void testParserService() {
		assertThat(parserService, is(notNullValue()));
	}

	private void assertStepModel(StepModel stepModel, String name, double start, double stop, double step) {
		assertThat(stepModel, is(notNullValue()));
		assertThat(stepModel.getName(), is(equalTo(name)));
		assertThat(stepModel.getStart(), closeTo(start, PRECISION));
		assertThat(stepModel.getStop(), closeTo(stop, PRECISION));
		assertThat(stepModel.getStep(), closeTo(step, PRECISION));
	}

	@Test
	public void testCreateParser() throws Exception {
		final String scanCommand = "scan x 0 10 0.5 detector 0.1";
		// TODO how do we know what kind of parser this is?
		final IParser<?> parser = parserService.createParser(scanCommand);
		assertThat(parser.getCommand(), is(equalTo(scanCommand)));
		assertThat(parser.getScannableNames(), contains("x"));
		final StepModel xModel = (StepModel) parser.getModel("x");
		assertStepModel(xModel, "x", 0, 10, 0.5);
		assertThat(parser.getDetectorNames(), contains("detector"));
		assertThat(parser.getExposures().keySet(), contains("detector"));
		assertThat((Double) parser.getExposures().get("detector"), closeTo(0.1, PRECISION));
	}

}
