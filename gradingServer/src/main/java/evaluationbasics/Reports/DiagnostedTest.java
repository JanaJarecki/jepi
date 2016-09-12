package evaluationbasics.Reports;

import javax.tools.DiagnosticCollector;

/**
 * Created by ilias on 01.09.16.
 */
public class DiagnostedTest extends DiagnostedClass {

    private String testClassName;
    private String testClassCode;
    private Class<?> testClass;
    private String testSuiteClassName;
    private String testSuiteClassCode;
    private Class<?> testSuiteClass;

    public DiagnostedTest(boolean validCode,
                   DiagnosticCollector<?> diagnostics,
                   String solutionClassName,
                   String solutionClassCode,
                   Class<?> solutionClass,
                   String testClassName,
                   String testClassCode,
                   Class<?> testClass,
                   String testSuiteClassName,
                   String testSuiteClassCode,
                   Class<?> testSuiteClass
                   ) {
        super(validCode, solutionClassName, solutionClassCode, diagnostics, solutionClass);
        this.testClassName = testClassName;
        this.testClassCode = testClassCode;
        this.testClass = testClass;
        this.testSuiteClassName  = testSuiteClassName;
        this.testSuiteClassCode = testSuiteClassCode;
        this.testSuiteClass = testSuiteClass;
    }

    public boolean isValidCode() {
        return isValidClass();
    }

    public String getTestClassName() {
        return testClassName;
    }

    public String getTestClassCode() {
        return testClassCode;
    }

    public Class<?> getTestClass() {
        return testClass;
    }

    public String getTestSuiteClassName() {
        return testSuiteClassName;
    }

    public String getTestSuiteClassCode() {
        return testSuiteClassCode;
    }

    public Class<?> getTestSuiteClass() {
        return testSuiteClass;
    }
}
