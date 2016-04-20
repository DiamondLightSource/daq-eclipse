package org.eclipse.scanning.test.event.queues.beans;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(org.junit.runners.Suite.class)
@SuiteClasses({
	DummyAtomTest.class,
	MonitorAtomTest.class,
	MoveAtomTest.class,
	ScanAtomTest.class,
	SubTaskBeanTest.class,
	TaskBeanTest.class
})
public class Suite {

}
