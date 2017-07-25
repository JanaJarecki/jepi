/**
 * Created by ilias on 16.08.16.
 */

import evaluationbasics.AnalysisHelpers.ASTHelper;
import evaluationbasics.XML.TestData;
import org.testng.*;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.internal.IResultListener;
import org.testng.internal.IResultListener2;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;


public class TestTestNG {
    @DataProvider( name = "first")
    public static Object[][] _MyValues() {
        return new Object[][]{
                {"public int id(int a) { return a; }", new boolean[]{false, false}},
                {"public int id(int a) { if (a==1) return 1; return id(a-1); }", new boolean[]{true, false}},
                {"public int id(int a) { int sum = 0; for(int i=0;i<a;++i) sum+=a; return sum; }", new boolean[]{false, true}},
                {"public int id(int a) { if (a==1) return 1; int sum = 0; for(int i=0;i<a;++i) sum+=a; return sum+id(a-1); }", new boolean[]{true, true}}
        };
    }


    @DataProvider(name = "second")
    public static Object[][] _MyValues2() {
        return new Object[][]{
                {"public int id(int a) { return a; }", new boolean[]{false, false}},
                {"public int id(int a) { if (a==1) return 1; return id(a-1); }", new boolean[]{true, false}},
                {"public int id(int a) { int sum = 0; for(int i=0;i<a;++i) sum+=a; return sum; }", new boolean[]{false, true}},
                {"public int id(int a) { if (a==1) return 1; int sum = 0; for(int i=0;i<a;++i) sum+=a; return sum+id(a-1); }", new boolean[]{false, true}}
        };
    }

    @Test( dataProvider = "first")
    public void testFirst(String code, boolean[] types) {
        testMethod(code,types);
    }

    @Test( dataProvider = "second")
    public void testSecond(String code, boolean[] types) {
        testMethod(code,types);
    }

    public void testMethod(String code, boolean[] types) {
        Map<String, Boolean> type;
        try {
            type = ASTHelper.getMethodType(code);
        } catch (Exception e) {
            throw new RuntimeException("Test failed");
        }
        Assert.assertEquals((boolean)type.get("recursive"),types[0]);
        Assert.assertEquals((boolean)type.get("loop"),types[1]);
    }

    public static void main (String[] args) {
        Vector<TestData> tests = new Vector();
        tests.add(new TestData(0,"testFirst","Erster Test", 5));
        tests.add(new TestData(1,"testSecond","Zweiter Test", 3));
}

    public static void RunTests(List<TestData> tests) {

        TestListenerAdapter tla = new TestListenerAdapter();
        TestNG testng = new TestNG();
        testng.setUseDefaultListeners(false);
        testng.setVerbose(0);
        testng.setTestClasses(new Class[] { TestTestNG.class });
        testng.addListener((ITestNGListener)tla);
        testng.run();

        try {
            Set<String> partiallyPassedTests = new HashSet();
            for ( ITestResult result : tla.getPassedTests() ) partiallyPassedTests.add(result.getName());

            Set<String> failedTests = new HashSet();
            for ( ITestResult result : tla.getFailedTests() ) failedTests.add(result.getName() );

            Set<String> fullyPassedTests = new HashSet<String>(partiallyPassedTests);

            fullyPassedTests.removeAll(failedTests);
            partiallyPassedTests.removeAll(fullyPassedTests);
            failedTests.removeAll(partiallyPassedTests);

            for ( String x: failedTests) writeToTest(x,tests,-1);
            for ( String x: partiallyPassedTests) writeToTest(x,tests,0);
            for ( String x: fullyPassedTests) writeToTest(x,tests,1);
        } catch (Exception e) {
            System.out.println("runTest => "+e);
        }
    }

    public static void writeToTest(String name, List<TestData> list, int result) {
        for ( TestData t: list) {
            if (t.name.equals(name)) {
                t.passed = result > 0;
                t.passedPartially = result >= 0;
                t.reachedPoints = (t.passed) ? t.points : 0;
            }
        }
    }


}
