package liveevaluation;

import java.util.LinkedList;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;

import liveevaluation.Student.CodeUnit;
import liveevaluation.Student.ParamGroup;
import liveevaluation.Student.Params;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;

/**
 * Die Klasse dient als Helfer fuer die Konstruktion der Rueckgabe XML-Dokuments.
 * 
 * 
 * @author Roman Bange
 */
public class XMLConstructor {
	
	/**
	 * Dient zur Rueckgabe von extrem harten Fehlern zB wenn der Client nicht im akzeptierten IP-Bereich liegt.
	 * 
	 * @param pMessage Errorcode oder die zu uebergebene Fehlernachricht
	 * @return Document in Form eines Error-XML-Dokuments
	 */
	public static Document errorResponse(String pMessage){
		return new Document(new Element("response").addContent(new Element("error").addContent(pMessage)));
	}
	
	/**
	 * Diese Methode konstruiert das Standard Rueckgabe XML-Dokument, indem sie die in Student modifizierten Datenklassen ausliest.
	 * 
	 * @param type Art der Anfrage
	 * @param pCodeUnits Die getesteten CodeUnits
	 * @param requestRoot das Rootelement der Anfrage um die Parameter zurueckzugeben
	 * @return Document Format siehe Dokumentation oder XMLexamples
	 */
	public static Document response(String type, LinkedList<CodeUnit> pCodeUnits, Element requestRoot){
		Element root=new Element("repsonse");
		Document doc=new Document(root);
		//Haengt die Parameter an die Response XML
		if(requestRoot!=null && (type.equals("runMethod")|| type.equals("compareMethod"))){
			List<Element> l=requestRoot.getChildren("paramgroup");
			for(Element pg : l)
				root=root.addContent(pg.clone());
		}
		for(CodeUnit tempCU : pCodeUnits){
			//Codeid,Compileable,Diagnostik
			Element eCode=new Element("code")	.setAttribute(new Attribute("codeid",String.valueOf(tempCU.id)))
												.addContent(new Element("compileable").addContent(String.valueOf(tempCU.compileable)))
												.addContent(diagnosticToElement(tempCU.diagnostics,tempCU.lineOffset));
			if(!tempCU.error.equals(""))
				eCode=eCode.addContent(new Element("error").addContent(tempCU.error));
			//Writingtype
			if(type.endsWith("Method") && (tempCU.methodtype!= null || !tempCU.methodtypeError.equals(""))){
				Element methodtype=new Element("methodtype");
				if(tempCU.methodtypeError.equals("") && tempCU.methodtype!= null){
					methodtype.addContent(new Element("recursive").addContent(String.valueOf(tempCU.methodtype[0])));
					methodtype.addContent(new Element("iterative").addContent(String.valueOf(tempCU.methodtype[1])));
				}else
					methodtype.addContent(new Element("error").addContent(tempCU.methodtypeError));
				eCode=eCode.addContent(methodtype);
			}
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
								
			root.addContent(eCode);					
		}
		return doc;		
	}
	/**
	 * Diese Methode wrappt die Diagnostik in ein XML-Format das leichter auszulesen ist
	 * 
	 * @param dc DiagnosticCollectorobjekt
	 * @param startLine Versatz fuer korrekte Zeilenangabe bei Methoden
	 * @return Element unter dem die Diagnostik haengt
	 */
	private static Element diagnosticToElement(DiagnosticCollector<?> dc, int startLine){
		Element rootDiag=new Element("diagnostics");
		int id_counter=1;
		if(dc!=null)
			for (Diagnostic<?> diag : dc.getDiagnostics() )
			{
				Element eDiag=new Element("diagnostic").setAttribute(new Attribute("id",String.valueOf(id_counter)));
				eDiag=eDiag	.addContent(new Element("source").addContent(diag.getSource().toString()))
							.addContent(new Element("code").addContent(diag.getCode().toString()))
							.addContent(new Element("message").addContent(diag.getMessage(null).toString()))
							.addContent(new Element("line").addContent(String.valueOf(diag.getLineNumber()+startLine)))
							.addContent(new Element("position").addContent(diag.getPosition()+"/"+diag.getColumnNumber()))
							.addContent(new Element("startposition").addContent(String.valueOf(diag.getStartPosition())))
							.addContent(new Element("endposition").addContent(String.valueOf(diag.getEndPosition())));					
				rootDiag=rootDiag.addContent(eDiag);
				id_counter++;	
			}
		return rootDiag;		
	}

}
