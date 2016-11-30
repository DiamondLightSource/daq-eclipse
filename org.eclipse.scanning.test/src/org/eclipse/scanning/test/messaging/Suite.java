package org.eclipse.scanning.test.messaging;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(org.junit.runners.Suite.class)
@SuiteClasses({
	DeviceRequestMessagingAPITest.class,
	AcquireRequestMessagingAPITest.class,
	PositionerRequestMessagingAPITest.class,
	XcenMessagingAPITest.class,
	ScanBeanMessagingAPITest.class,
	TopicAndQueueNameTest.class,
})
public class Suite {

	
}
