package liveevaluation;

import evaluationbasics.DiagnostedMethodClass;
import evaluationbasics.EmptyCodeException;
import evaluationbasics.TooManyMethodsException;
import javafx.util.Pair;
import org.jdom2.Element;

import java.util.List;

import static liveevaluation.XMLParser.parseParameterGroups;

/**
 * Created by ilias on 26.08.16.
 */
public class TestNGEvaluator {

    public static void eval(Element request, XMLConstructor response) {
        TestNGEvaluator eval = new TestNGEvaluator(response);
        eval.dispatchTestAction(request);
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
            case "compile-test":
                complationTest(request,"test");
                break;

            case "run-test":
                runTestExecution(request,"test");
                break;

            default:
                xml.error(ERROR_CODE.ACTION_NOT_KNOWN);
        }
    }

    private void runTestExecution(Element request, String person) {
        Object test = complationTest(request,person);
        if ( test != null ) {
            Object result = runTest(test);

//        xml.compileTestRunResult(sparams,result);
        }
    }

    private Object complationTest(Element request, String person) {
        try{

            Element parameterOwner = request.getChild(person);
            List<ParamGroup> sparams = parseParameterGroups(parameterOwner);

            Element solutionXML = request.getChild("solution");
            Element testXML = request.getChild("test");

            String solutionCode = XMLParser.getCode(solutionXML);
            String testCode = XMLParser.getCode(testXML);

            Object test = compileTest(solutionCode, testCode);
//            xml.compileTestResponse(sparams,test);

            return test;

        } catch ( EmptyCodeException e) {
            xml.error("Provided code was empty: \n"+e);
        } catch ( org.jdom2.DataConversionException e) {
            xml.error("Found wrong datatype in XML: "+e);
        } catch ( Exception e) {
            xml.errorUnknown(e);
        }
        return null;
    }


    private void runStudentTest(Element request, String person) {
        try {
            Element solutionXML = request.getChild("solution");
            String solutionCode = XMLParser.getCode(solutionXML);

            Object test = compileSolutionPart(solutionCode);
            Object result = runSolutionMain(test);

//        xml.compileTestRunResult(sparams,result);
        } catch ( EmptyCodeException e) {
            xml.error("Provided code was empty: \n"+e);
        } catch ( Exception e) {
            xml.errorUnknown(e);
        }
    }

    private Object compileTest(String solution, String test) {
        return null;
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
