package org.eclipse.scanning.test.epics;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(org.junit.runners.Suite.class)
@SuiteClasses({
	PVDataSerializationTest.class,
	EpicsV4ConnectorTest.class
})
public class Suite {

	
}
