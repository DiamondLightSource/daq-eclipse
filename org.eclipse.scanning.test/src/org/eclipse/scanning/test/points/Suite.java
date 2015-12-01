package org.eclipse.scanning.test.points;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(org.junit.runners.Suite.class)
@SuiteClasses({

	GridTest.class, GridTestLarge.class, LinearTest.class, LissajousTest.class, RasterTest.class, RasterTestLarge.class, StepTest.class, CompoundTest.class, CompoundTestLarge.class

})
public class Suite {
}
