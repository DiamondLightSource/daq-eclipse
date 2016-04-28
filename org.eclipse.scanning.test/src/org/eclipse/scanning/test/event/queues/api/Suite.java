package org.eclipse.scanning.test.event.queues.api;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(org.junit.runners.Suite.class)
@SuiteClasses({
	QueueNameMapTest.class,
	SizeLimitedRecorderTest.class
})
public class Suite {

}
