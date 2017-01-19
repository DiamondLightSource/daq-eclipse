package org.eclipse.scanning.test.validation;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(org.junit.runners.Suite.class)
@SuiteClasses({
    CompoundModelTest.class,
    ModelTest.class,
    ScanRequestValidationTest.class
})
public class Suite {

}
