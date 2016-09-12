package evaluationbasics.XML;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;

import evaluationbasics.Exceptions.WrongNumberOfProvidedJavaElementsException;
import evaluationbasics.Reports.CodeUnit;
import evaluationbasics.Reports.DiagnostedTest;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;


public class XMLConstructor {

    Element root;

    public XMLConstructor() {
        root = new Element("response");
    }

    public XMLConstructor(String rootName) {
        root = new Element(rootName);
    }

    public Document getDocument() {
        root.detach();
        return new Document(root);
    }


    public void responseToCompileTest(DiagnostedTest test) {
        assert(test != null);
        Element element = new Element("compiletestng");
        List<?> diagnostics = test.getDiagnostic().getDiagnostics();
        if (diagnostics.size()>0) {
            String error = diagnostics.get(0).toString();
            String className = error.substring(1, error.indexOf('.'));
            if (!test.getClassName().equals(className)) {
                writeAnalysisOfCompilationError(test, element, error);
            } else {
                writeDiagnostic(test.getDiagnostic(), element);
            }
        } else {
            writeDiagnostic(test.getDiagnostic(),element);
        }
        root.addContent(element);
    }

    private void writeAnalysisOfCompilationError(DiagnostedTest test, Element element, String error) {
        String cause = "";
        Pattern pc = Pattern.compile("error: (.*)\n");
        Matcher mc = pc.matcher(error);
        if (mc.find()) {
            cause = mc.group(1);
        }

        switch (cause) {
            case "cannot find symbol":
                writeConnotFindSymbolAnalysis(element, error);
                break;
            default:
                writeDiagnostic(test.getDiagnostic(), element);
        }
    }

    private void writeConnotFindSymbolAnalysis(Element element, String error) {
        Pattern p = Pattern.compile("symbol: (.*)\n");
        Matcher m = p.matcher(error);
        boolean found = m.find();
        String[] what = m.group(1).trim().split(" ");
        String message = "Expected symbol is missing in your code.";
        message += "\n\tThe missing symbol should be a " + what[0] + ".";
        message += "\n\tThe name should be \"" + what[1] + "\".";
        Element msg = new Element("message");
        msg.setText(message);
        Element diagnostic = new Element("diagnostic");
        diagnostic.addContent(msg);
        diagnostic.setAttribute("id", "1");
        element.addContent(new Element("diagnostics").addContent(diagnostic));
    }

    public void responseToRunTest(List<TestData> tests) {
        Element element = new Element("runtestng");
        writeTests(tests, element);
        root.addContent(element);
    }

    public void responseToRunStudentTest(List<ParamGroup> groups) {
        Element element = new Element("runstudenttestng");
        writeRunTestResult(groups, element);
        root.addContent(element);
    }

    private void writeTests(List<TestData> tests, Element parent) {
        Element element = new Element("tests");
        for (TestData td : tests) {
            Element test = new Element("test");
            test.setAttribute("id", String.valueOf(td.id));
            test.setAttribute("name", td.name);
            test.setAttribute("description", td.description);
            test.setAttribute("passed", String.valueOf(td.passed));
            test.setAttribute("passedPartially", String.valueOf(td.passedPartially));
            test.setAttribute("pPoints", String.valueOf(td.points));
            test.setAttribute("reachedPoints", String.valueOf(td.reachedPoints));
            element.addContent(test);
        }
        parent.addContent(element);
    }


    public void responseToCompileMethod(List<ParamGroup> results, CodeUnit code) {
        Element element = new Element("compilemethod");
        writeDiagnostic(code.diagnostics, element, code.lineOffset);
        if (code.compileable) {
            writeMethodType(code, element);
            writeCompareResult(results, element);
        }
        root.addContent(element);
    }


    public void responseToRunMethod(List<ParamGroup> results, CodeUnit code) {
        Element element = new Element("runmethod");
        writeDiagnostic(code.diagnostics, element, code.lineOffset);
        if (code.compileable) {
            writeMethodType(code, element);
            writeRunResult(results, element);
        }
        root.addContent(element);
    }


    public void respondseToCompareMethods(List<ParamGroup> studentResults, CodeUnit studentsCode) {
        Element element = new Element("comparemethods");
        writeDiagnostic(studentsCode.diagnostics, element, studentsCode.lineOffset);
        if (studentsCode.compileable) {
            writeMethodType(studentsCode, element);
            writeCompareResult(studentResults, element);
        }
        root.addContent(element);
    }

    public void error(String message) {
        Element err = new Element("error");
        error(message, err);
        root.addContent(err);
    }

    public void error(int errorId) {
        Element err = new Element("error");
        error(errorId, err);
        root.addContent(err);
    }

    public void error(Exception e) {
        if ( e instanceof WrongNumberOfProvidedJavaElementsException ) {
            error("Could not determine the correct Java Element.\n"+e.getMessage());
        }
        error(e.toString());
    }

    private void error(String message, Element parent) {
        parent.addContent(new Element("error").addContent(message));
    }

    private void error(int errorId, Element parent) {
        parent.addContent(new Element("error").addContent("" + errorId));
    }

    private void writeMethodType(CodeUnit cu, Element parent) {
        Element element = new Element("methodtypediagnostics");
        if (cu.methodtypeError.equals("")) {
            if (cu.methodtype != null) {
                for (String key : cu.methodtype.keySet()) {
                    element.addContent(new Element(key).addContent(String.valueOf(cu.methodtype.get(key))));
                }
            }
        } else {
            error("MethodTypeDiagnosticError: " + cu.methodtypeError, element);
        }
        parent.addContent(element);
    }

    private void writeCompareResult(List<ParamGroup> results, Element parent) {
        Element element = new Element("compareresults");
        for (ParamGroup grp : results) {
            Element group = new Element("paramgroup");
            group.setAttribute("id", String.valueOf(grp.id));
            group.setAttribute("points", String.valueOf(grp.points));
            group.setAttribute("reachedPoints", String.valueOf(grp.reachedPoints));
            group.setAttribute("error", grp.error);
            group.setAttribute("equals", String.valueOf(grp.equals));
            group.setAttribute("name", String.valueOf(grp.name));
            element.addContent(group);
        }
        parent.addContent(element);
    }

    private void writeRunResult(List<ParamGroup> results, Element parent) {
        Element element = new Element("runresults");
        for (ParamGroup grp : results) {
            Element group = new Element("paramgroup");
            for (Params params : grp.params) {
                Element par = new Element("params");
                par.setAttribute("id", "" + params.id);
                par.setAttribute("params", Arrays.toString(params.values));
                if ( params.error.isEmpty() ) {
                    par.setAttribute("value", params.zReturn.toString());
                } else {
                    par.setAttribute("error", "true");
                    par.setText(params.error);
                }
                group.addContent(par);
            }
            element.addContent(group);
        }
        parent.addContent(element);
    }

    private void writeRunTestResult(List<ParamGroup> results, Element parent) {
        Element element = new Element("runresults");
        for (ParamGroup grp : results) {
            Element group = new Element("paramgroup");
            for (Params params : grp.params) {
                Element par = new Element("params");
                par.setAttribute("id", "" + params.id);
                par.setAttribute("params", Arrays.toString(params.values));
                par.setText(params.zReturn.toString());
                group.addContent(par);
            }
            element.addContent(group);
        }
        parent.addContent(element);
    }

    /**
     * Diese Methode wrappt die Diagnostik in ein XML-Format das leichter auszulesen ist
     *
     * @param dc CodeUnit
     * @return Element unter dem die Diagnostik haengt
     */
    private void writeDiagnostic(DiagnosticCollector<?> dc, Element response) {
        writeDiagnostic(dc, response, 0);
    }

    private void writeDiagnostic(DiagnosticCollector<?> dc, Element response, int lineOffset) {
        Element rootDiag = new Element("diagnostics");
        int id_counter = 1;
        if (dc != null)
            for (Diagnostic<?> diag : dc.getDiagnostics()) {
                Element eDiag = new Element("diagnostic").setAttribute(new Attribute("id", String.valueOf(id_counter)));
                eDiag.addContent(new Element("source").addContent(diag.getSource().toString()));
                eDiag.addContent(new Element("code").addContent(diag.getCode().toString()));
                eDiag.addContent(new Element("message").addContent(diag.getMessage(null).toString()));
                eDiag.addContent(new Element("line").addContent(String.valueOf(diag.getLineNumber() - lineOffset)));
                eDiag.addContent(new Element("position").addContent(diag.getPosition() + "/" + diag.getColumnNumber()));
                eDiag.addContent(new Element("startposition").addContent(String.valueOf(diag.getStartPosition())));
                eDiag.addContent(new Element("endposition").addContent(String.valueOf(diag.getEndPosition())));
                rootDiag = rootDiag.addContent(eDiag);
                id_counter++;
            }
        response.addContent(rootDiag);
    }

    //@todo remove additional error information for production
    public void errorUnknown(Exception e) {
        StackTraceElement[] ste = e.getStackTrace();
        for (StackTraceElement s : ste) {
            System.out.println(s);
        }
        error("unkwon error: " + e);
    }

    public void errorInvocationTargetException(InvocationTargetException e) {
        String message = "Could not execute your code.";
        String error = e.getTargetException().getMessage();


        if (error.contains("java.lang.RuntimePermission")) {
            message += "\nThis could be due to you use a method that you are not allowed to use.";
            Pattern p = Pattern.compile("\"([^\"]*)\"");
            Matcher m = p.matcher(error);
            if (m.find() && m.find()) {
                message += "\nYou do not have the permission \"" + m.group(1) + "\"";
            }
        }


        error(message);
    }


    /**
     * Diese Methode konstruiert das Standard Rueckgabe XML-Dokument, indem sie die in EvaluationRequest modifizierten Datenklassen ausliest.
     *
     * @param type        Art der Anfrage
     * @param pCodeUnits  Die getesteten CodeUnits
     * @param requestRoot das Rootelement der Anfrage um die Parameter zurueckzugeben
     * @return Document Format siehe Dokumentation oder XMLexamples
     */
    public void ZZZresponse(String type, LinkedList<CodeUnit> pCodeUnits, Element requestRoot) {
        //Haengt die Parameter an die Response XML
        if (requestRoot != null && (type.equals("runMethod") || type.equals("compareMethod"))) {
            List<Element> l = requestRoot.getChildren("paramgroup");
            for (Element pg : l)
                root.addContent(pg.clone());
        }
        for (CodeUnit tempCU : pCodeUnits) {
            ZZZresponseMethodDiagnostics(type, tempCU, requestRoot);
        }
    }

    public void ZZZresponseMethodDiagnostics(String type, CodeUnit tempCU, Element response) {
        //Codeid,Compileable,Diagnostik
        Element eCode = new Element("code").setAttribute(new Attribute("codeid", String.valueOf(tempCU.id)))
                .addContent(new Element("compileable").addContent(String.valueOf(tempCU.compileable)));
        writeDiagnostic(tempCU.diagnostics, eCode, tempCU.lineOffset);
        if (!tempCU.error.equals(""))
            eCode = eCode.addContent(new Element("error").addContent(tempCU.error));

        //Paramgroups
        if (type.equals("runMethod") || type.equals("compareMethod")) {
            if (tempCU.noParamsReturn == null) //Methoden mit Parameter
                for (ParamGroup tempPG : tempCU.paramgroup) {
                    Element eP = new Element("paramgroup").setAttribute(new Attribute("paramgroupid", String.valueOf(tempPG.id)));
                    if (!tempPG.error.equals(""))
                        eP = eP.addContent(new Element("error").addContent(tempPG.error));
                    if (type.equals("compareMethod"))
                        eP = eP.addContent(new Element("reachedpoints").addContent(String.valueOf(tempPG.points)))
                                .addContent(new Element("equal").addContent(String.valueOf(tempPG.equals)));
                    for (Params tempP : tempPG.params) {
                        Element eParams = new Element("paramsreturn").setAttribute(new Attribute("paramsid", String.valueOf(tempP.id)))
                                .addContent(new Element("return").addContent(String.valueOf(tempP.zReturn)));
                        if (!tempP.error.equals(""))
                            eParams = eParams.addContent(new Element("error").addContent(tempP.error));
                        if (type.equals("compareMethod"))
                            eParams = eParams.addContent(new Element("equal").addContent(String.valueOf(tempP.equals)));
                        eP = eP.addContent(eParams);
                    }
                    eCode = eCode.addContent(eP);
                }
            else //Methoden ohne Parameter
                eCode = eCode.addContent(new Element("paramgroup").setAttribute(new Attribute("paramgroupid", "0"))
                        .addContent(new Element("paramsreturn").setAttribute(new Attribute("paramsid", "0")).addContent(String.valueOf(tempCU.noParamsReturn))));
        }
        response.addContent(eCode);
    }
}
