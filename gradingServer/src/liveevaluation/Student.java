package liveevaluation;

import java.io.IOException;
import java.io.StringReader;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

import org.jdom2.DataConversionException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;


//import evaluationbasics.ASTHelper;
import evaluationbasics.DiagnostedClass;
import evaluationbasics.DiagnostedMethodClass;
import evaluationbasics.EvaluationTools;
import evaluationbasics.TooManyMethodsException;

public class Student extends Thread{
	
	private final Socket CLIENT;
	private final StudentTimeoutCounter TIMEOUTCOUNTER;
	//private final String[] forbiddenMethods={"exit","gc","setSecurityManager","clearProperty","inheritedChannel","load","loadLibrary","setErr","setIn","setOut","setProperties","setProperty" //System
	//										   ,"close","connect","bind","getChannel","getInputStream","getOutputStream" //Socket
	//										   ,"accept","getInetAddress","setSocketFactory"}; //ServerSocket

	
	public Student(Socket client){
		CLIENT=client;
		this.setUncaughtExceptionHandler(new StudentUncaughtExceptionHandler(client));
		TIMEOUTCOUNTER=new StudentTimeoutCounter(this);
	}
	
	public final void run(){
		TIMEOUTCOUNTER.start();
		// Das Dokument erstellen
        SAXBuilder builder = new SAXBuilder();
        Document doc=null, returnDoc=null;
        try{
            String str = EvaluationHelper.getStringFromInputStream(CLIENT.getInputStream());;
        	doc = builder.build(new StringReader(str));
        }catch(JDOMException e){
        	close(XMLConstructor.errorResponse("101"));
        	return;
        }catch(IOException e){
        	close(XMLConstructor.errorResponse("103"));
        	return;
        }
        //Aufsplitten des Dokuments in Elemente
        Element eRoot=doc.getRootElement();            
        Element eAction=eRoot.getChild("action");
        boolean bWrittingType;
        try{
        	bWrittingType=Boolean.parseBoolean(eRoot.getChild("methodtype").getValue());
        }catch(Exception e){
        	bWrittingType=false;
        }
        List<Element> eCodeunits=eRoot.getChildren("element");
        if(eAction==null){
        	close(XMLConstructor.errorResponse("6"));
        	return;
        }else if(eCodeunits.size()==0){
        	close(XMLConstructor.errorResponse("7"));
        	return;
        }
        	
        
        //Ueberpruefe ob alle codes nur Methoden oder nur Klassen sind	            
        boolean methoden=false;
        boolean klassen=false;
        for(Element e: eCodeunits){
        		if(e.getAttribute("type").getValue().equals("method") && !methoden)
        			methoden=true;
        		else if(e.getAttribute("type").getValue().equals("class") && !klassen)
        			klassen=true;
        }
        if(methoden && klassen)
        	returnDoc=XMLConstructor.errorResponse("1"); //Error  <1> : Klassen und Methoden in der XML vorhanden           
        
        
        /*
         * Fall 1:
         * 		Typ: Methode
         * 		Aktion: run
         * 
         * Anmerkung:
         * 	Falls sich mehr als eine Methode in einem Uebergabestring befinden wird die Klasse als nicht valide ausgezeichnet, obwohl sie moeglicherweise
         * 	syntaktisch richtig waere.
         */
        if(eAction.getValue().equals("run") && methoden){
        		LinkedList<CodeUnit> codeunits=this.runAllMethodsOnParams(eRoot, eCodeunits, bWrittingType);
	            returnDoc=XMLConstructor.response("runMethod",codeunits,eRoot);
        }
        /*
         * Fall 2:
         * 		Typ: Methode
         * 		Aktion: compile
         * 
         * Anmerkung:
         * 	Falls sich mehr als eine Methode in einem Uebergabestring befinden wird die Klasse als nicht valide ausgezeichnet, obwohl sie moeglicherweise
         * 	syntaktisch richtig waere.
         */	            
        else if(eAction.getValue().equals("compile") && methoden){
        	LinkedList<CodeUnit> codeunits= new LinkedList<CodeUnit>();
        	for(Element eCodeUnit: eCodeunits){
        		CodeUnit tempCodeUnit=new CodeUnit();
        		DiagnostedMethodClass dcMethod=null;
				try {
					dcMethod = EvaluationTools.compileMethod(eCodeUnit.getChild("code").getValue()); //Packages zu impl
	        		tempCodeUnit.compileable=dcMethod.isValidClass();
	        		tempCodeUnit.diagnostics=dcMethod.getDiagnostic();	
				} catch (TooManyMethodsException e1) {
	        		tempCodeUnit.methodtypeError="51";
				}
        		codeunits.add(tempCodeUnit);
        		try{
        			tempCodeUnit.id=eCodeUnit.getAttribute("id").getIntValue();
        		}catch(DataConversionException e) {tempCodeUnit.id=-1;e.printStackTrace();}          
    			if(tempCodeUnit.compileable && bWrittingType && dcMethod!=null)
    				try {
    					tempCodeUnit.methodtype=EvaluationTools.getMethodType(dcMethod);
    				} catch (TooManyMethodsException e){tempCodeUnit.methodtypeError="51";}
        	}
        	returnDoc=XMLConstructor.response("compileMethod",codeunits,null);
        }
        
        /*
         * Fall 3:
         * 		Typ: Methode
         * 		Aktion: compare
         * 
         * Anmerkung:
         * 	Falls sich mehr als eine Methode in einem Uebergabestring befinden wird die Klasse als nicht valide ausgezeichnet, obwohl sie moeglicherweise
         * 	syntaktisch richtig waere.
         */	            
        else if(eAction.getValue().equals("compare") && methoden){	  
        	
        	//KEIN XML ab hier
    		LinkedList<CodeUnit> codeunits=this.runAllMethodsOnParams(eRoot, eCodeunits, bWrittingType);
    		CodeUnit teacher=null;
    		
    		//Suche "teacher"-Element
    		for(CodeUnit pCodeUnit: codeunits)
    			if((teacher=pCodeUnit).origin.equals("teacher"))
    				break;
    		if(!(teacher==null || !teacher.origin.equals("teacher"))){
        		for(CodeUnit tempCodeUnit: codeunits){           			
        			if(!tempCodeUnit.equals(teacher) && tempCodeUnit.error.equals("")){
        				for(ParamGroup tempParamGroup : tempCodeUnit.paramgroup){
        					ParamGroup tempParamGroupTeacher=null;
        					
        					//Suche TeacherParamGroup mit gleicher id
        					for(ParamGroup p: teacher.paramgroup)
        						if(tempParamGroup.id==p.id){
        							tempParamGroupTeacher=p;break;}
        					if(tempParamGroupTeacher!=null){          
        						boolean paramGroupEquals=true;
            				
                				//Fuer jede Params              				
                				for(Params tempParams : tempParamGroup.params){
                					Params tempParamsTeacher=null;
                					
                					//Finde Teacherparams mit gleicher id
                					for(Params p: tempParamGroupTeacher.params)
                						if(tempParams.id==p.id){
                							tempParamsTeacher=p;break;}
                					if(tempParamsTeacher!=null){		                					
	                					//Vergleich
	                					tempParams.equals=tempParams.zReturn.equals(tempParamsTeacher.zReturn);
	                					
	                					if(paramGroupEquals)
	                						paramGroupEquals=tempParams.equals;    
                					}else{
                						tempParams.error="4"; //Error <4>
                					}
                				}

                				tempParamGroup.equals=paramGroupEquals;
                				
                				if(!paramGroupEquals)
                					tempParamGroup.points=0; //0 Punkte wenn ParamGroup falsch ist  
        					}else{
        						tempParamGroup.error="3"; //Error <3>
        					}
        					
        				}
        					
        				
        			}    				            				
        		}
        		returnDoc=XMLConstructor.response("compareMethod",codeunits,eRoot);
    		}else
    			returnDoc=XMLConstructor.errorResponse("2"); //Error <2>: Kein Teacher Element vorhanden
        }
        /*
         * Fall 4:
         * 		Typ: Klasse
         * 		Aktion: compile
         * 
         * !! Einschraenkende Anmerkung:
         * Da bislang in der Schnittstelle kein Klassenname uebergeben wird, basiert die Umsetzung dieses Falls auf der Methode EvalTools.getClassName()
         * 
         */
        else if(eAction.getValue().equals("compile") && klassen){
        	LinkedList<CodeUnit> codeunits=new LinkedList<CodeUnit>();
			for(Element codeunit: eCodeunits){
				CodeUnit tempCodeUnit=new CodeUnit();
				String classCode=codeunit.getChild("code").getValue();
				DiagnostedClass dcClass=EvaluationTools.compileClass(classCode, EvaluationTools.getClassName(classCode));
				try {
					tempCodeUnit.id=codeunit.getAttribute("id").getIntValue();
				} catch (DataConversionException e) {tempCodeUnit.id=-1;e.printStackTrace();}
				tempCodeUnit.diagnostics=dcClass.getDiagnostic();
				tempCodeUnit.compileable=dcClass.isValidClass();
				codeunits.add(tempCodeUnit); 
			}
        	returnDoc=XMLConstructor.response("compileClass",codeunits,null);	            	
        }
        
        
		/*
		 * Ausgabe
		 */
        if(returnDoc!=null)
			close(returnDoc);
        else
        	close(XMLConstructor.errorResponse("102"));
	}
		
	private final void close(Document pReturnDoc){
		try{			
			EvaluationHelper.setStringToOutputStream(CLIENT.getOutputStream(), new XMLOutputter().outputString(pReturnDoc));
			if(!CLIENT.isClosed())
				CLIENT.close();
			TIMEOUTCOUNTER.kill();
		}catch(IOException e){
			//Fehler
		}
	}
	
	public final void kill(){
		if(!CLIENT.isClosed()){
			this.setUncaughtExceptionHandler(new StudentUncaughtExceptionHandler.UselessUncaughtExceptionHandler());
			this.close(XMLConstructor.errorResponse("105"));
		}
	}
	
	private final LinkedList<CodeUnit> runAllMethodsOnParams(Element root, List<Element> eCodeunits, boolean bWrittingtype){
		DiagnostedMethodClass dcMethod=null;
		LinkedList<CodeUnit> lReturn=new LinkedList<CodeUnit>();
		
		Iterator<Element>i=eCodeunits.iterator();
		
		//Fuer alle "Code" bzw "Element"
		while(i.hasNext()){
			Element codeunit=i.next();
			
			//naechste CodeUnit wird erstellt
			CodeUnit tempCodeUnit= new CodeUnit();
			lReturn.add(tempCodeUnit);  
			
			
			//Kompiliere Methode
			//dcMethod=EvaluationTools.compileMethod(EvaluationTools.primitiveToWrapper(codeunit.getChild("code").getValue()), new String[]{}); //packages zu impl
			
			
			try {
				dcMethod=EvaluationTools.compileMethod(codeunit.getChild("code").getValue());
				tempCodeUnit.compileable=dcMethod.isValidClass();
				tempCodeUnit.diagnostics=dcMethod.getDiagnostic();
			} catch (TooManyMethodsException e1) {
				dcMethod=null;
				tempCodeUnit.methodtypeError="51";				
			}
			
			
					
			tempCodeUnit.origin=codeunit.getAttributeValue("origin");
			if(tempCodeUnit.origin==null) tempCodeUnit.origin="";
			
			//SecurityLeak
		
			boolean isSecurityLeak=false;
			/*
			if(!tempCodeUnit.origin.equals("teacher"))
				isSecurityLeak=ASTHelper.isSecurityLeak(codeunit.getChild("code").getValue(), forbiddenMethods);
			*/
			
			//Setze ID
			try {
				tempCodeUnit.id=codeunit.getAttribute("id").getIntValue();
			} catch (DataConversionException e) {tempCodeUnit.id=-1;e.printStackTrace();}
			
			tempCodeUnit.paramgroup=new LinkedList<ParamGroup>(); //alle ParamGroups Element eines Codes sind hier drin
			
			//LineOffSet
			tempCodeUnit.lineOffset=3; //3 wegen Methode
			
			//Methodtype
			if(tempCodeUnit.compileable && bWrittingtype && dcMethod!=null)
				try {
					tempCodeUnit.methodtype=EvaluationTools.getMethodType(dcMethod);
				} catch (TooManyMethodsException e){tempCodeUnit.methodtypeError="51";}
			
			/*
			 if(isSecurityLeak)
				tempCodeUnit.error="52";
			 */
			if(dcMethod.isValidClass() && EvaluationTools.isVoid(dcMethod))
				tempCodeUnit.error="53";
			
			//Testen der Paramgroups
			Iterator<Element> j=root.getChildren("paramgroup").iterator();
			if(dcMethod!=null && dcMethod.isValidClass() && !isSecurityLeak && !EvaluationTools.isVoid(dcMethod))
				if(j.hasNext())
					while(j.hasNext()){
						
						ParamGroup tempParamGroup= new ParamGroup();
						Element tempElementParamGroup=j.next();
						
						
						LinkedList<Params> paramsList=new LinkedList<Params>();
						
						
						try {
							tempParamGroup.id= tempElementParamGroup.getAttribute("id").getIntValue();
						} catch (DataConversionException e) {tempParamGroup.id=-1;e.printStackTrace();}
						tempParamGroup.params= paramsList;
						try {
							if(tempElementParamGroup.getAttribute("points")!=null)
								tempParamGroup.points= tempElementParamGroup.getAttribute("points").getIntValue();
						} catch (DataConversionException e) {e.printStackTrace();}
						
						tempCodeUnit.paramgroup.add(tempParamGroup);
						
						Iterator<Element> k=tempElementParamGroup.getChildren().iterator();
						
						
						//Testen der Params einer Paramgroup
						while(k.hasNext()){
							Params tempParams= new Params();
							Element tempElementParams=k.next();
							try {
								tempParams.id=tempElementParams.getAttribute("id").getIntValue();
							} catch (DataConversionException e) {tempParams.id=-1;}
							try{
								tempParams.zReturn=EvaluationHelper.runMethodOnParams(dcMethod, EvaluationHelper.paramsToArray(tempElementParams));
							}catch(Exception e){							
								tempParams.zReturn=null;
								if(!e.getMessage().equals("")){
									tempParams.error=e.getMessage();
									tempCodeUnit.error=e.getMessage();
								}
								tempParams.error="100";
							}
							paramsList.add(tempParams);
						}
	     		}
			else //Keine Parameter
				try {
					tempCodeUnit.noParamsReturn=EvaluationHelper.runMethodOnParams(dcMethod, null);
				} catch (Exception e) {e.printStackTrace();} //kann nicht vorkommen
		}  //Ende Schleife
		return lReturn; 		
	}
	
	
	/*
	 * Datenklassen
	 */
	
	
	public class Params{
		public int id;
		public Object zReturn;
		public boolean equals=false;
		public String error="";
	}
	
	public class ParamGroup{
		public int id;
		public int points=-1;
		public LinkedList<Params> params;
		public boolean equals=false;	
		public String error="";	
	}

	public class CodeUnit{
		public DiagnosticCollector<?> diagnostics=new DiagnosticCollector<JavaFileObject>();;
		public int lineOffset=0; //Zur Korrektur der Linienangaben in der Diagnostik
		public boolean compileable=false;
		public String origin="";
		public int id;
		public boolean isSecurityLeak=false;
		
		//Methodtype
		public Map<String,Boolean> methodtype=null;
		public String methodtypeError="";
		
		public LinkedList<ParamGroup> paramgroup; //Beinhaltet die Return-Werte
		public Object noParamsReturn=null; //Return-Wert fuer Methoden ohne Parameter
		public String error="";
		//equals() nach id
		public boolean equals(Object obj){
			if(obj.getClass() == this.getClass())
				return id == ((CodeUnit) obj).id;
			else			
				return false;
		}
	}

}
