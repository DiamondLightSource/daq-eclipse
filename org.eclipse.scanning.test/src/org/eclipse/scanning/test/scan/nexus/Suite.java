package org.eclipse.scanning.test.scan.nexus;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(org.junit.runners.Suite.class)
@SuiteClasses({

	AttributeTest.class,
	BasicScanTest.class,
	ConstantVelocityTest.class,
	DarkCurrentTest.class,
	MandelbrotAcquireTest.class,
	MandelbrotExampleTest.class,
	MandelbrotRemoteTest.class,
	MonitorTest.class,
	ScanMetadataTest.class,
	ScanProcessingTest.class,
	ScanPointsWriterTest.class,
	LinearScanTest.class

})
public class Suite {
}