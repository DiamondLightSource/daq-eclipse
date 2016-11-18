package org.eclipse.scanning.test.points;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(org.junit.runners.Suite.class)
@SuiteClasses({

	GridTest.class, 
	LinearTest.class, 
	RasterTest.class, 
	StepTest.class, 
	CompoundTest.class, 
	RandomOffsetDecoratorTest.class,
	ScanRankTest.class,
	SpiralTest.class,
	ScanPointGeneratorFactoryTest.class,
	ArrayTest.class,
	RandomOffsetGridTest.class,
	PointServiceTest.class


	// TODO Smoke tests?
	//GridTestLarge.class, 
	//RasterTestLarge.class, 
	//CompoundTestLarge.class

})
public class Suite {
}
