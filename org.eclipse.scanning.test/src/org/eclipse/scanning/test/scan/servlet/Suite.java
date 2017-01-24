package org.eclipse.scanning.test.scan.servlet;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(org.junit.runners.Suite.class)
@SuiteClasses({

	ScanProcessTest.class,
	ScanServletTest.class
})
public class Suite {
}