package liveevaluation;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;

import org.jdom2.Attribute;
import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.testng.junit.JUnit3TestClass;


public class XMLConstructor {

    Element root;
    public XMLConstructor() {
        root = new Element("response");
    }

    public XMLConstructor( String rootName ) {
        root = new Element(rootName);
    }

    public Document getDocument() {
        root.detach();
        return new Document(root);
    }


    public void compileMethodResponse(List<ParamGroup> results, CodeUnit code) {
        Element element = new Element("compilemethod");
        diagnosticToElement(code,element);
        if ( code.compileable ) {
            methodTypeToElement(code, element);
            compareResultToElement(results, element);
        }
        root.addContent( element );
    }


    public void runMethodResponse(List<ParamGroup> results, CodeUnit code) {
        Element element = new Element("runmethod");
        diagnosticToElement(code,element);
        if ( code.compileable ) {
            methodTypeToElement(code, element);
            runResultToElement(results, element);
        }
        root.addContent( element );
    }


    public void compareMethodResponse(List<ParamGroup> studentResults, CodeUnit studentsCode) {
        Element element = new Element("comparemethods");
        diagnosticToElement(studentsCode,element);
        if( studentsCode.compileable) {
            methodTypeToElement(studentsCode, element);
            compareResultToElement(studentResults, element);
        }
        root.addContent( element );
    }

    public void error(String message) {
        Element err = new Element("error");
        error(message,err);
        root.addContent(err);
    }

    public void error(int errorId) {
        Element err = new Element("error");
        error(errorId,err);
        root.addContent(err);
    }

    private void error(String message, Element parent){
        System.out.println();
        parent.addContent( new Element("error").addContent(message));
	}

    private void error(int errorId, Element parent) {
        parent.addContent( new Element("error").addContent(""+errorId) );
	}

    private void methodTypeToElement(CodeUnit cu, Element parent) {
        Element element = new Element("methodtypediagnostics");
        if  ( cu.methodtypeError.equals("") ) {
            if ( cu.methodtype!= null ) {
                for (String key : cu.methodtype.keySet()) {
                    element.addContent(new Element(key).addContent(String.valueOf(cu.methodtype.get(key))));
                }
            }
        } else {
            error("MethodTypeDiagnosticError: "+cu.methodtypeError, element);
        }
        parent.addContent(element);
    }

    private void compareResultToElement(List<ParamGroup> results, Element parent) {
        Element element = new Element("compareresults");
        for ( ParamGroup grp : results) {
            Element group = new Element("paramgroup");
            group.setAttribute("id",String.valueOf(grp.id));
            group.setAttribute("points",String.valueOf(grp.points));
            group.setAttribute("reachedPoints",String.valueOf(grp.reachedPoints));
            group.setAttribute("error",grp.error);
            group.setAttribute("equals",String.valueOf(grp.equals));
            group.setAttribute("name",String.valueOf(grp.name));
            element.addContent(group);
        }
        parent.addContent(element);
    }

    private void runResultToElement(List<ParamGroup> results, Element parent) {
        Element element = new Element("runresults");
        for ( ParamGroup grp : results) {
            Element group = new Element("paramgroup");
            for ( Params params : grp.params) {
                Element par = new Element("params");
                par.setAttribute("id",""+params.id);
                par.setAttribute("params", Arrays.toString(params.values));
                par.setAttribute("value", params.zReturn.toString());
                group.addContent(par);
            }
            element.addContent(group);
        }
        parent.addContent(element);
    }



    /**
	 * Diese Methode konstruiert das Standard Rueckgabe XML-Dokument, indem sie die in EvaluationRequest modifizierten Datenklassen ausliest.
	 * 
	 * @param type Art der Anfrage
	 * @param pCodeUnits Die getesteten CodeUnits
	 * @param requestRoot das Rootelement der Anfrage um die Parameter zurueckzugeben
	 * @return Document Format siehe Dokumentation oder XMLexamples
	 */
	public void response(String type, LinkedList<CodeUnit> pCodeUnits, Element requestRoot){
        System.out.println("XMLConstructor.response");
        //Haengt die Parameter an die Response XML
		if(requestRoot!=null && (type.equals("runMethod")|| type.equals("compareMethod"))){
			List<Element> l=requestRoot.getChildren("paramgroup");
			for(Element pg : l)
				root.addContent(pg.clone());
		}
		for(CodeUnit tempCU : pCodeUnits){
			responseMethodDiagnostics(type, tempCU, requestRoot);
		}
	}

	public void responseMethodDiagnostics(String type, CodeUnit tempCU, Element response) {
        System.out.println("XMLConstructor.responseMethodDiagnostics");
        //Codeid,Compileable,Diagnostik
		Element eCode=new Element("code")	.setAttribute(new Attribute("codeid",String.valueOf(tempCU.id)))
                                            .addContent(new Element("compileable").addContent(String.valueOf(tempCU.compileable)));
        diagnosticToElement(tempCU, eCode);
		if(!tempCU.error.equals(""))
            eCode=eCode.addContent(new Element("error").addContent(tempCU.error));

		//Paramgroups
		if(type.equals("runMethod") || type.equals("compareMethod")){
            if(tempCU.noParamsReturn==null) //Methoden mit Parameter
                for(ParamGroup tempPG : tempCU.paramgroup){
                    Element eP= new Element("paramgroup").setAttribute(new Attribute("paramgroupid",String.valueOf(tempPG.id)));
                    if(!tempPG.error.equals(""))
                        eP=eP.addContent(new Element("error").addContent(tempPG.error));
                    if(type.equals("compareMethod"))
                        eP=eP	.addContent(new Element("reachedpoints").addContent(String.valueOf(tempPG.points)))
                                .addContent(new Element("equal").addContent(String.valueOf(tempPG.equals)));
                    for(Params tempP : tempPG.params){
                        Element eParams=new Element("paramsreturn")	.setAttribute(new Attribute("paramsid",String.valueOf(tempP.id)))
                                                                    .addContent(new Element("return").addContent(String.valueOf(tempP.zReturn)));
                        if(!tempP.error.equals(""))
                            eParams=eParams.addContent(new Element("error").addContent(tempP.error));
                        if(type.equals("compareMethod"))
                            eParams=eParams.addContent(new Element("equal").addContent(String.valueOf(tempP.equals)));
                        eP=eP.addContent(eParams);
                    }
                    eCode=eCode.addContent(eP);
                }
            else //Methoden ohne Parameter
                eCode=eCode.addContent(new Element("paramgroup").setAttribute(new Attribute("paramgroupid","0"))
                                                                .addContent(new Element("paramsreturn").setAttribute(new Attribute("paramsid","0")).addContent(String.valueOf(tempCU.noParamsReturn))));
        }
		response.addContent(eCode);
	}

	/**
	 * Diese Methode wrappt die Diagnostik in ein XML-Format das leichter auszulesen ist
	 * 
	 * @param cu CodeUnit
	 * @return Element unter dem die Diagnostik haengt
	 */
	private void diagnosticToElement(CodeUnit cu, Element response){
        System.out.println("XMLConstructor.diagnosticToElement");
        DiagnosticCollector<?> dc = cu.diagnostics;
		Element rootDiag=new Element("diagnostics");
		int id_counter=1;
		if(dc!=null)
			for (Diagnostic<?> diag : dc.getDiagnostics() )
			{
				Element eDiag=new Element("diagnostic").setAttribute(new Attribute("id",String.valueOf(id_counter)));
				eDiag=eDiag	.addContent(new Element("source").addContent(diag.getSource().toString()))
							.addContent(new Element("code").addContent(diag.getCode().toString()))
							.addContent(new Element("message").addContent(diag.getMessage(null).toString()))
							.addContent(new Element("line").addContent(String.valueOf(diag.getLineNumber()-cu.lineOffset)))
							.addContent(new Element("position").addContent(diag.getPosition()+"/"+diag.getColumnNumber()))
							.addContent(new Element("startposition").addContent(String.valueOf(diag.getStartPosition())))
							.addContent(new Element("endposition").addContent(String.valueOf(diag.getEndPosition())));					
				rootDiag=rootDiag.addContent(eDiag);
				id_counter++;	
			}
		response.addContent(rootDiag);
	}

	//@todo remove additional error information for production
    public void errorUnknown(Exception e) {
        StackTraceElement[] ste = e.getStackTrace();
        for(StackTraceElement s : ste) {
            System.out.println(s);
        }
        error("unkwon error: "+e);
    }
}
