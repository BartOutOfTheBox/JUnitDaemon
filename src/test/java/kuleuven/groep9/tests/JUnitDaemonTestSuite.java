package kuleuven.groep9.tests;

import kuleuven.groep9.tests.classloading.DirectoryNotifierTest;
import kuleuven.groep9.tests.statistics.FreqTest;
import kuleuven.groep9.tests.statistics.LastFailTest;
import kuleuven.groep9.tests.statistics.TestGroupTest;
import kuleuven.groep9.tests.taskqueues.TaskQueueTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;



@RunWith(Suite.class)
@Suite.SuiteClasses({
   DirectoryNotifierTest.class,
   FreqTest.class,
   LastFailTest.class,
   TestGroupTest.class,
   TaskQueueTest.class
})
public class JUnitDaemonTestSuite {   
}  