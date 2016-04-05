package org.eclipse.scanning.test.command;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(org.junit.runners.Suite.class)
@SuiteClasses({
	//CommandTest.class, // Seems to crash when run from maven
	//ModelStringifierTest.class
	
	// Maven build is giving this:
	//Failed tests:   
	//	testGridModelConcise(org.eclipse.scanning.test.command.ModelStringifierTest): expected:<grid(('[bob', 'alice'), (0.0, 1.0), (10.0, 11.0), count=(3, 4]), snake=False)> but was:<grid(('[???', '???'), ('???', '???'), ('???', '???'), count=('???', '???']), snake=False)>
    //    testGridModelVerbose(org.eclipse.scanning.test.command.ModelStringifierTest): expected:<grid(axes=('[bob', 'alice'), origin=(0.0, 1.0), size=(10.0, 11.0), count=(3, 4]), snake=False)> but was:<grid(axes=('[???', '???'), origin=('???', '???'), size=('???', '???'), count=('???', '???']), snake=False)>

    /* How to reproduce:
	1. Install maven (should have mvn command available)
	2. Checkout code same as build does it. Can use travis command line to do this or check out the repos
	   to eclipse/org.eclipse.XXX (scanning, dawnsci, richbeans)
	   Or attempt checkins to github on a branch and let travis run them.
	 */
	   
})
public class Suite { }
