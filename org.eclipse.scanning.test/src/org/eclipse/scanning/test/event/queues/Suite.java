package org.eclipse.scanning.test.event.queues;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@Ignore
@RunWith(org.junit.runners.Suite.class)
@SuiteClasses({
	AtomQueueServiceDummyTest.class,
	QueueProcessCreatorTest.class,
	QueueTest.class
})
public class Suite {

}
