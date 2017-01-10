package org.eclipse.scanning.test.remote;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(org.junit.runners.Suite.class)
@SuiteClasses({
	RemoteRunnableServiceTest.class,
	RemoteScannableServiceTest.class,
	RemoteQueueControllerServiceTest.class
})
public class Suite {

	
}
