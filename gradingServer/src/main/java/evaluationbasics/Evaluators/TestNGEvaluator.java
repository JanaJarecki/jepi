package evaluationbasics.Evaluators;

import evaluationbasics.CompilationHelpers.CompilationBox;
import evaluationbasics.Reports.DiagnostedTest;
import evaluationbasics.Exceptions.EmptyCodeException;
import evaluationbasics.Exceptions.WrongNumberOfProvidedJavaElementsException;
import evaluationbasics.Exceptions.ERROR_CODE;
import evaluationbasics.XML.*;
import org.jdom2.Document;
import org.jdom2.Element;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static evaluationbasics.XML.XMLParser.parseParameterGroups;

/**
 * Created by ilias on 26.08.16.
 */
public class TestNGEvaluator {

    public static Document eval(Element request) {
        XMLConstructor response = new XMLConstructor();
        TestNGEvaluator eval = new TestNGEvaluator(response);
        eval.dispatchTestAction(request);
        return response.getDocument();
    }

    private XMLConstructor xml;

    private TestNGEvaluator(XMLConstructor response) {
        this.xml = response;
    }

    /**
     * Function handles the original question type. The passed functions are wrapped in a class and then compared if at
     * least two are given.
     *
     * @param request XML Root element of the request
     * @return The respose xml document containing the evaluation.
     */
    private void dispatchTestAction(Element request) {
        Element eAction = request.getChild("action");
        String actionRequested = eAction.getValue().toLowerCase();
        switch (actionRequested) {
            case "compiletestng":
                complationTest(request,"test");
                break;

            case "compilestudenttestng":
                complationTest(request, "student");
                break;

            case "runtestng":
                runTests(request,"test");
                break;

            case "runstudenttestng":
                runStudentTests(request,"student");
                break;

            case "feedbackstudenttestng":
                feedbackTests(request,"student");
                break;

            default:
                xml.error(ERROR_CODE.ACTION_NOT_KNOWN);
        }
    }

    private void runTests(Element request, String person) {
        try {
            List<TestData> tests = XMLParser.parseTests(request);
            DiagnostedTest dc = complationTest(request, person);
            if (dc != null && dc.isValidClass()) {
                EvaluationHelper.runInstanceMethod(dc.getTestSuiteClass(), "RunTests", new Object[]{tests});
                xml.responseToRunTest(tests);
            }
        } catch ( TimeoutException e )  {
            xml.error("The execution took too long: "+e);
        } catch ( IOException e )  {

        } catch ( ClassNotFoundException e )  {

        } catch ( org.jdom2.DataConversionException e) {
            xml.error("Found wrong datatype in XML: "+e);
        } catch ( IllegalAccessException e) {
            xml.error("The method RunTests was not accessible."+e);
        } catch ( InstantiationException e) {
            xml.error("Class Initialization error:"+e);
        } catch ( WrongNumberOfProvidedJavaElementsException e) {
            xml.error(e.getMessage());
        } catch ( InvocationTargetException e) {
            String stackTrace1 = "";
            for ( StackTraceElement s : e.getStackTrace()) stackTrace1+=s.toString()+"\n";
            String stackTrace2 = "";
            for ( StackTraceElement s : e.getCause().getStackTrace()) stackTrace2+=s.toString()+"\n";
            xml.error("Target invocation error: "+e.getMessage()+"\n"+stackTrace1);
            xml.error("Target invocation error cause: "+e.getCause()+"\n"+stackTrace2);
        }
    }

    private void feedbackTests(Element request, String person) {
        try {
            Element solutionXML = request.getChild("solution");
            List<ParamGroup> groups = parseParameterGroups(solutionXML);

            DiagnostedTest dc = complationTest(request,person);
            if ( dc != null && dc.isValidClass() ) {

                List<TestData> tests = XMLParser.parseTests(request);
                EvaluationHelper.runInstanceMethod(dc.getTestSuiteClass(), "RunTests", new Object[]{tests});
                xml.responseToRunTest(tests);
            }
        } catch ( TimeoutException e )  {
            xml.error("The execution took too long: "+e);
        } catch ( IOException e )  {

        } catch ( ClassNotFoundException e )  {

        } catch ( org.jdom2.DataConversionException e) {
            xml.error("Found wrong datatype in XML: "+e);
        } catch ( IllegalAccessException e) {
            xml.error("The main method was not accessible. Probably the class or the main method is missing or has a too strict access modifier.");
        } catch ( InstantiationException e) {
            xml.error("Class Initialization error:"+e);
        } catch ( WrongNumberOfProvidedJavaElementsException e) {
            xml.error(e.getMessage());
        } catch ( InvocationTargetException e) {
            xml.errorInvocationTargetException(e);
        }
    }

    private void runStudentTests(Element request, String person) {
        try {
            Element solutionXML = request.getChild("solution");
            List<ParamGroup> groups = parseParameterGroups(solutionXML);

            DiagnostedTest dc = complationTest(request,person);
            if ( dc != null && dc.isValidClass() ) {
                for ( ParamGroup group : groups) {
                    for (Params param : group.params) {
                        String[] args =  new String[param.values.length];
                        for ( int i = 0; i< param.values.length; ++i) {
                            args[i] = (String) param.values[i];
                        }
                        param.zReturn = EvaluationHelper.runMainMethodWithParams(dc, args);
                    }
                }
                xml.responseToRunStudentTest(groups);

                List<TestData> tests = XMLParser.parseTests(request);
                EvaluationHelper.runInstanceMethod(dc.getTestSuiteClass(), "RunTests", new Object[]{tests});
                xml.responseToRunTest(tests);
            }
        } catch ( TimeoutException e )  {
            xml.error("The execution took too long: "+e);
        } catch ( IOException e )  {

        } catch ( ClassNotFoundException e )  {

        } catch ( NoSuchMethodException e) {
            xml.error("The main method was not accessible. Probably the main method is missing or has a too strict access modifier.");
        } catch ( org.jdom2.DataConversionException e) {
            xml.error("Found wrong datatype in XML: "+e);
        } catch ( IllegalAccessException e) {
            xml.error("The main method was not accessible. Probably the class or the main method is missing or has a too strict access modifier.");
        } catch ( InstantiationException e) {
            xml.error("Class Initialization error:"+e);
        } catch ( WrongNumberOfProvidedJavaElementsException e) {
            xml.error(e.getMessage());
        } catch ( InvocationTargetException e) {
            xml.errorInvocationTargetException(e);
        }
    }


    private DiagnostedTest complationTest(Element request, String person) {
        DiagnostedTest dc = null;
        try{

            Element solutionXML = request.getChild("solution");
            Element testXML = request.getChild("testgroup");

            String solutionCode = XMLParser.getCode(solutionXML);
            String testCode = XMLParser.getCode(testXML);

            dc = compileTest(solutionCode, testCode, person);
            if ( dc != null) {
                xml.responseToCompileTest(dc);
            }
            return dc;

        } catch ( EmptyCodeException e) {
            xml.error("Provided code was empty: \n"+e);
        }
        return dc;
    }

    private DiagnostedTest compileTest(String solution, String test, String person) {
        DiagnostedTest dc = null;
        try {
            CompilationBox cb = new CompilationBox();
            dc = cb.compileClassWithTest(solution, test, person);
        } catch (WrongNumberOfProvidedJavaElementsException e){
            xml.error(e);
        } catch (ClassNotFoundException e) {
            xml.error(e.toString());
        }
        return dc;
    }

    private Object compileSolutionPart(String solution) {
        return null;
    }

    private Object runTest( Object test) {
        return null;
    }

    private Object runSolutionMain( Object test ) {
        return null;
    }
}
