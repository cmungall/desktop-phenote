package phenote;

import junit.framework.TestSuite;
import junit.framework.JUnit4TestAdapter;
import junit.textui.TestRunner;
import phenote.util.FileUtilTest;
import phenote.gui.TestPhenote;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is the master unit test class that runs all registered unit tests (suite).
 * Add your new unit test here if it is ready to be included in regular builds.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        FileUtilTest.class,
        TestPhenote.class
        })

public class UnitTests {

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(UnitTests.class);
    }
}
