package phenote;

import junit.framework.TestSuite;
import junit.textui.TestRunner;
import phenote.util.FileUtilTest;

/**
 * This is the master unit test class that runs all registered unit tests (suite).
 * Add your new unit test here if it is ready to be included in regular builds.
 */
public class UnitTests extends TestSuite {

    public UnitTests() {
        super("Unit Tests");
    }

    public static void main(String args[]) {
        TestRunner.run(suite());
    }

    /**
     * Add unit test class here to the suite.
     */
    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(FileUtilTest.suite());
        return suite;
    }

}
