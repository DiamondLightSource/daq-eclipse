package org.eclipse.scanning.test.command;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(org.junit.runners.Suite.class)
@SuiteClasses({
	ScanRequestCreationTest.class,
	ModelStringifierTest.class
})
public class Suite { }
