package uk.ac.diamond.json.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(org.junit.runners.Suite.class)
@SuiteClasses({

	BundleAndClassNameIdResolverTest.class,
	JsonMarshallerNonOSGiTest.class,
	JsonMarshallerOSGiBundleTest.class

})
public class Suite {
}
