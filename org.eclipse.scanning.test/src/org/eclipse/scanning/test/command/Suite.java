package org.eclipse.scanning.test.command;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(org.junit.runners.Suite.class)
@SuiteClasses({
	ScanRequestCreationTest.class,
	SubmissionTest.class,
	MScanServletTest.class,
	PyExpresserTest.class
})
public class Suite { }
