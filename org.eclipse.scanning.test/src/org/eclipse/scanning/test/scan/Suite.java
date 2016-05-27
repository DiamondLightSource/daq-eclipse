package org.eclipse.scanning.test.scan;

import org.eclipse.scanning.test.scan.preprocess.PreprocessTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(org.junit.runners.Suite.class)
@SuiteClasses({

	ScanTest.class,
	TopupTest.class,
	BenchmarkScanTest.class,
	ScanFinishedTest.class,
	PreprocessTest.class

})
public class Suite {
}
