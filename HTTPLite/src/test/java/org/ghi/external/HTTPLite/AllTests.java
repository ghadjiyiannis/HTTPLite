package org.ghi.external.HTTPLite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ FileUtilTest.class, IntegrationTest.class, ConnectedWorkerTest.class })
public class AllTests {

}
