package org.eclipse.scanning.test.event.queues;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(org.junit.runners.Suite.class)
@SuiteClasses({
	AtomQueueServiceDummyTest.class,
	QueueProcessCreatorTest.class,
	QueueTest.class
})
public class Suite {

}
