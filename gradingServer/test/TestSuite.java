import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.BeforeSuite;


public class TestSuite {

    public static void main(String[] args) {

        Class[] classList = new Class[]{
//                TestTestNG.class,
                TestInferenceOfMethodProperties.class,
                TestMethodIsCompilable.class
        };

        TestListenerAdapter tla = new TestListenerAdapter();

        TestNG testNG = new TestNG();
        testNG.addListener(tla);
        testNG.setTestClasses(classList);

        testNG.setVerbose(1);
        testNG.run();
        System.out.println("Failed tests:");
        System.out.println("===============================================");
        tla.getFailedTests().forEach(System.out::println);
        System.out.println("Skipped tests:");
        System.out.println("===============================================");
        tla.getSkippedTests().forEach(System.out::println);
    }

}