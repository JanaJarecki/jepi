package evaluationbasics;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;

import evaluationbasics.compilationHelpers.MemClassLoader;
import evaluationbasics.compilationHelpers.MemJavaFileManager;
import evaluationbasics.compilationHelpers.StringJavaFileObject;

/**
 * Die Klasse bietet mehrere Methoden an die den Kern des Kompilierens vereinfachen.
 * 
 * @author Roman Bange
 * @version 1.0
 */
public final class EvaluationTools {	
	
	/**
	 * Diese Methode erstellt aus einem in String gegebenen Java-Code eine .java Datei, welche er versucht zu kompilieren.
	 * 
	 * @param pClassCode Den Code einer Klasse inkl. der zu importierenden Pakete als String
	 * @param pClassName Name der gegebenen Klasse
	 * 
	 * @return DiagnostedClass Objekt welches die Informationen zur Klasse bzw. zur Kompilierung enthaelt
	 * 
	 * @exception IllegalArgumentException falls einer der Parameter keinen Inhalt hat
	 * @see DiagnostedClass
	 * @see DiagnostedMethodClass
	 */		
	public static final DiagnostedClass compileClass(String pClassCode, String pClassName){
		
		if(pClassCode==null || pClassCode.equals("")) throw new IllegalArgumentException("pClassCode cannot be emtpy");
		else if(pClassName==null || pClassName.equals("")) throw new IllegalArgumentException("pClassName cannot be emtpy");
		
		boolean bValidClass;		
		Class<?> zClass=null;
				
		//Hole Compilerinstanz aus System
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		
		if(compiler==null)
			throw new IllegalArgumentException("Compiler cannot be found!");
		//Initialisiere Diagnose
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		

		MemClassLoader classLoader = new MemClassLoader();
		JavaFileManager fileManager = new MemJavaFileManager(compiler, classLoader, diagnostics);
		
		//StandardJavaFileManager fileManager = compiler.getStandardFileManager( diagnostics, null, null );
		
			
		//Erstellen der .java Datei intern
		Iterable<? extends JavaFileObject> units = Arrays.asList( new StringJavaFileObject(pClassName, pClassCode));
		
		
		CompilationTask task = compiler.getTask( null, fileManager, diagnostics, null, null, units);
		/*Kompilierung durchfuehren 
		 *true-> lauffaehige Klasse
		 *false-> keine Komplierung moeglich
		 */
		
		bValidClass=task.call();
		
		try {
			fileManager.close();
		} catch (IOException e) {e.printStackTrace();}
		
		if(bValidClass){
			try{
				zClass=classLoader.findClass(pClassName);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}
		
		return new DiagnostedClass(bValidClass, pClassName, pClassCode, diagnostics, zClass);
	}
	
	/**
	 * Ruft unter Erstellung einer Template-Klasse um die Methode, compileClass() auf.
	 * Fuer naehere Dokumentation siehe compileMethod(String,String).
	 * 
	 * @param pMethodCode Code der Methode als String
	 * @return Vollstaendiges DiagnostedMethodClass Objekt
	 * @exception IllegalArgumentException Wird geworfen wenn ein leerer Code uebergeben wurde.
	 * @see DiagnostedMethodClass
	 */	
	public static final DiagnostedMethodClass compileMethod(String pMethodCode) throws TooManyMethodsException{
		return compileMethod(pMethodCode,new String[]{});
	}
	
	/**
	 * Ruft unter Erstellung einer Template-Klasse um die Methode, compileClass() auf.
	 * Bei dieser Methode lassen sich noch benoetigte zu importierte Pakete mitgeben.
	 * Um bei einer Stapelbearbeitung die Template-Klasse eindeutig identifizieren zu koennen,
	 * wird hierbei eine ID verwendet, die sich nach der Zeit der Erstellung richtet.
	 * 
	 * 
	 * @param pMethodCode Code der Methode als String
	 * @param pImportedPackages Zu importierende Pakete. Es ist hierbei nur der Pfad anzugeben zB. "javax.swing.JOptionPane" oder "javax.swing.*"
	 * @return Vollstaendiges DiagnostedMethodClass Objekt
	 * @exception IllegalArgumentException Wird geworfen wenn ein leerer Code uebergeben wurde.
	 * @see DiagnostedMethodClass
	 */	
	public static final DiagnostedMethodClass compileMethod(String pMethodCode, String[] pImportedPackages) throws TooManyMethodsException{
		if(pMethodCode==null || pMethodCode.equals("")) throw new IllegalArgumentException("pMethodCode cannot be empty");
		if(pImportedPackages==null) pImportedPackages=new String[]{};
		String t="Methodclass"+String.valueOf(System.currentTimeMillis()).substring(5);
		String zPackages="";
		for(String pPackage : pImportedPackages)
			zPackages=zPackages+"import "+pPackage+";";		
		DiagnostedClass dcMethod=compileClass(zPackages+"public class "+t+"{\npublic "+t+"(){}\n"+pMethodCode+"\n}",t);
		if(dcMethod.isValidClass())
			if(dcMethod.getAsClass().getDeclaredMethods().length==1)
				return new DiagnostedMethodClass(dcMethod,dcMethod.getAsClass().getDeclaredMethods()[0],pMethodCode);
			else
				throw new TooManyMethodsException("There are too many methods in the Code - use compileMethods.");
		return new DiagnostedMethodClass(dcMethod,pMethodCode);
	}
	
	/**
	 * Funktioniert grundlegend wie die compileMethod()-Methoden.
	 * In diesem Fall kann jedoch der Methodenname mitgegeben werden. So ist es moeglich auch mehrere Methoden im Code mitzugeben,
	 * jedoch nur eine als MainMethode zu identifizieren. Es ist so moeglich, dass eine Klasse wie eine DiagnostedMethodClass behandelt werden kann,
	 * die MainMethode aber auf andere selbst deklarierte Helfermethoden innerhalb der Klasse zurueckgreifen kann.
	 * 
	 * !! Einschraenkende Anmerkung:
	 * 
	 * Die Methode durchsucht alle Methoden der Klasse nach ihren Namen. Falls in der Klasse mehrere Methoden gleichen Namens auftreten,
	 * so wird willkuerlich eine Methode gewaehlt. Dies gilt zu vermeiden.
	 * 
	 * @param pMethodsCode Code der Methode als String
	 * @param pImportedPackages Zu importierende Pakete. Es ist hierbei nur der Pfad anzugeben zB. "javax.swing.JOptionPane" oder "javax.swing.*" Falls nicht notwendig: "null"
	 * @param pMethodName Name der gewuenschten MainMethode (case sensitive)
	 * @return Vollstaendiges DiagnostedMethodClass Objekt
	 * @exception IllegalArgumentException Wenn dcClass oder pMethodName leer sind 
	 * 
	 * @see DiagnostedMethodClass
	 */	
	public static final DiagnostedMethodClass compileMethods(String pMethodsCode, String[] pImportedPackages, String pMethodName) throws TooManyMethodsException{
		DiagnostedClass dcMethod=compileMethod(pMethodsCode,pImportedPackages);
		return EvaluationTools.defineDiagnostedMethodClass(dcMethod, pMethodName, null);
	}
	
	/**
	 * siehe compileMethods(String,String[],String)
	 * 
	 * !! Einschraenkende Anmerkung:
	 * 
	 * Die Methode durchsucht alle Methoden der Klasse nach ihren Namen als auch nach den den Paramtertypen.
	 * Falls diese Werte abweichen, so gilt die Kompilierung als fehlgeschlagen.
	 * 
	 * @param pMethodsCode Code der Methode als String
	 * @param pImportedPackages Zu importierende Pakete. Es ist hierbei nur der Pfad anzugeben zB. "javax.swing.JOptionPane" oder "javax.swing.*" Falls nicht notwendig: "null"
	 * @param pMethodName Name der gewuenschten MainMethode (case sensitive)
	 * @return Vollstaendiges DiagnostedMethodClass Objekt oder Invalides DiagnostedMethodClass Objekt, sofern die Methode syntaktisch nicht korrekt war.
	 */	
	public static final DiagnostedMethodClass compileMethods(String pMethodsCode, String[] pImportedPackages, String pMethodName, Class<?>[] pArgTypes) throws TooManyMethodsException{
		DiagnostedClass dcMethod=compileMethod(pMethodsCode,pImportedPackages);
		if(dcMethod.isValidClass())
			try {
				return new DiagnostedMethodClass(dcMethod,dcMethod.getAsClass().getDeclaredMethod(pMethodName, pArgTypes),pMethodsCode);
			} catch (NoSuchMethodException | SecurityException e) {e.printStackTrace();}
		return new DiagnostedMethodClass(dcMethod,pMethodsCode);		
	}

	
	/**
	 * Mithilfe dieser Methode kann aus einer DiagnostedClass eine DiagnostedMethodClass erstellt werden.
	 * Sofern der Name der gewuenschten MainMethode mehrmals vorkommt darf der Parameter argumentTypes nicht null sein.
	 * 
	 * @param dcClass DiagnostedClass Objekt das umgewandelt werden soll
	 * @param pMethodName Name der gewuenschten MainMethode(case sensitive)
	 * @param argumentTypes Die Parametertypen der gewuenschten MainMethode, falls es mehrere Methode mit pMethodName gibt
	 * @return DiagnostedMethodClass Objekt ohne Code in Klartext oder ein invalides DiagnostedMethodClass Objekt
	 * 
	 * @exception IllegalArgumentException Wenn dcClass oder pMethodName leer sind
	 */
	public static final DiagnostedMethodClass defineDiagnostedMethodClass(DiagnostedClass dcClass, String pMethodName, Class<?>[] argumentTypes){
		if(dcClass==null) throw new IllegalArgumentException("dcClass cannot be null");
		else if(pMethodName==null || pMethodName.equals("")) throw new IllegalArgumentException("pMethodName cannot be null");
		if(dcClass.isValidClass()){
			Method[] zMethods=dcClass.getAsClass().getDeclaredMethods();
			for(Method m : zMethods)
				if(m.getName().equals(pMethodName))
					return new DiagnostedMethodClass(dcClass,m,"");
			if(argumentTypes!=null && argumentTypes.length>=1)
				try {
					Method zMethod=dcClass.getAsClass().getDeclaredMethod(pMethodName, argumentTypes);
					return new DiagnostedMethodClass(dcClass,zMethod,"");
				} catch (NoSuchMethodException | SecurityException e) {} //Fehler
		}		
		return new DiagnostedMethodClass(dcClass,"");//Die angegebene Methode ist nicht in der Klasse enthalten	
	}
	
	
	/**
	 * Gibt die Kompilerdiagnostik der analysierten Klasse als String aus.	
	 * 
	 * @param dClass DiagnostedClass Objekt welches von der compileClass()-Methode zurueckgegeben wurde
	 * @return Kompilerdiagnostik als String
	 */
	public static final String diagnosticToString(DiagnostedClass dClass){
		return diagnosticToString(dClass,0);
	}
	
	/**
	 * Gibt die Kompilerdiagnostik der analysierten Methode als String aus.	
	 * 
	 * @param dClass DiagnostedMethodClass Objekt welches von der compileMethod()-Methode zurueckgegeben wurde
	 * @return Kompilerdiagnostik als String
	 */
	public static final String diagnosticToString(DiagnostedMethodClass dClass){
		return diagnosticToString(dClass,3);		
	}
	
	/**
	 * 
	 * @param dClass DiagnostedClass Objekt deren Kompilerdiagnostik ausgelesen werden soll
	 * @param startLine Hiermit laesst sich alle Zeilennummern versetzen.
	 * @return Kompilerdiagnostik als String
	 * @exception IllegalArgumentException Wenn dClass null ist
	 */
	private static final String diagnosticToString(DiagnostedClass dClass, int startLine){
		if(dClass==null) throw new IllegalArgumentException("dClass cannot be null");
		DiagnosticCollector<?> diagnostics=dClass.getDiagnostic();
		if(diagnostics==null) return "Keine Diagnostik vorhanden";
		else{
			String sReturn="";
			for (Diagnostic<?> diag : diagnostics.getDiagnostics() )
			{
			  sReturn=sReturn+"Quelle: "+diag.getSource()+"\n"+
					  			"Code: "+diag.getCode()+"\n"+
					  			"Nachricht: "+diag.getMessage(null)+"\n"+
					  			"Zeile:"+(diag.getLineNumber()+startLine)+"\n"+
					  			"Position/Spalte:"+diag.getPosition()+"/"+
			                                       diag.getColumnNumber()+"\n"+
			                    "Startpostion/Endposition: n"+diag.getStartPosition()+"/"+
			                                                  diag.getEndPosition()+"\n\n";
			}
			return sReturn;
		}		
	}
	
	
	
	/**
	 * siehe callMethodOnInstance(DiagnostedClass,Object,String,Object[])
	 * Bei dieser Methode wird anstatt eine gegebene Instanz zu verwenden. Eine neue ersellt.
	 * 
	 * @param dClass DiagnostedClass Objekt welches von der compileClass()-Methode zurueckgegeben wurde
	 * @param pMethodName Name der Methode die ausgefuehrt werden soll
	 * @param pMethodArgs Array der zu uebergebenen Parameter
	 * @
	 */	
	public static final Object callMethod(DiagnostedClass dClass, String pMethodName, Object[] pMethodArgs) throws Exception{
		return callMethodOnInstance(dClass,dClass.getNewInstance() ,pMethodName,pMethodArgs);
	}
		
	/**
	 * Diese Methode fuehrt eine Methode unter gegebenen Parametern aus. Dies geschieht unter der gegebenen Instanz.
	 * Die Methode gibt im Erfolgsfall den return-Wert der zu aufrufenden Methode zurueck.
	 * Falls keine Parameter erforderlich sind kann entweder eine leere ObjectArray oder null uebergeben werden.
	 * 
	 * !! Einschraenkende Anmerkung:
	 * Zur Identifizierung der Methode in der Klasse werden die Typen der Argumente benoetigt, da diese nicht uebergeben werden,
	 * wird nun geprueft ob fuer die Typen der in den Parametern uebergebenen Parameter eine Methode gefunden wird.
	 * Es ist also von den gegebenen Parametern abhaengig welche Methode aufgerufen wird.
	 * Damit dies so funktioniert, duerfen die Datentypen der auzurufenen Methode nur Wrapperklassen sein (keine primitiven Datentypen)
	 * Ein Workaround hierfuer ist primitiveToWrapper()
	 * 
	 * 
	 * @param dClass DiagnostedClass Objekt welches von der compileClass()-Methode zurueckgegeben wurde
	 * @param pClassInstance Instanz der Klasse
	 * @param pMethodName Name der Methode die ausgefuehrt werden soll
	 * @param pMethodArgs Array der zu uebergebenen Parameter
	 * 
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException Wird geworfen wenn fuer die gegebenen Parameter und den Methodenname keine Methode gefunden wurde
	 * @throws SecurityException Sofern die Klasse Permissions benoetigt welche verboten worden sind.
	 */	
	public static final Object callMethodOnInstance(DiagnostedClass dClass,Object pClassInstance, String pMethodName, Object[] pMethodArgs) throws Exception{
		if(pMethodArgs==null) pMethodArgs=new Object[]{};
		//
		Class<?> argTypes[]= new Class<?>[pMethodArgs.length];
		for(int i=0;i<pMethodArgs.length;i++)
			argTypes[i]=pMethodArgs[i].getClass();
		if(dClass.isValidClass()){
			Method m;
			try {
				m = dClass.getAsClass().getDeclaredMethod(pMethodName,argTypes);
				if(Modifier.isStatic(m.getModifiers())) //falls die Methode static ist
					pClassInstance=null;						
				return m.invoke(pClassInstance,pMethodArgs);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | 
					NoSuchMethodException | SecurityException e) {
				throw e;
			}
		}else
			throw new NoValidClassException("Given Diagnosted Class is not valid.");
	}
	
	
	
	/**
	 * siehe callMethodOnInstance(DiagnostedMethodClass,Object,Object[])
	 * Bei dieser Methode wird anstatt eine gegebene Instanz zu verwenden. Eine neue erstellt.
	 *
	 * @param dClass DiagnostedMethodClass Objekt welches von der compileMethod()-Methode zurueckgegeben wurde
	 * @param pMethodArgs Array der zu uebergebenen Parameter
	 * @return Rueckgabeobjekt der Methode
	 * @throws Exception Fuer die genaue Listung der Exceptions siehe callMethodonInstance
	 */	
	public static final Object callMethod(DiagnostedMethodClass dClass,Object[] pMethodArgs) throws Exception{
		return callMethodOnInstance(dClass,dClass.getNewInstance(),pMethodArgs);
	}
	
	/**
	 * Diese Methode fuehrt eine Methode unter gegebenen Parametern aus. Dies geschieht unter der gegebenen Instanz.
	 * Die Methode gibt im Erfolgsfall den return-Wert der zu aufrufenden Methode zurueck.
	 * Falls keine Parameter erforderlich sind kann entweder eine leere ObjectArray oder null uebergeben werden.
	 * Es ist moeglich eine Methode mit den Modifiern static/private zu uebergeben und aufzurufen. Fuer den Modififer "private" muss 
	 * die ReflectPermission("suppressAccessChecks") gewaehrt werden.
	 * 
	 * Die einschraenkende Anmerkung des Aequivalent mit der DiagnostedClass gilt hier aufgrund der Eigenschaften einer DiagnostedMethodClass nicht.
	 *  
	 * @param dClass DiagnostedClass Objekt welches von der compileClass()-Methode zurueckgegeben wurde
	 * @param pClassInstance Instanz der Klasse
	 * @param pMethodArgs Array der zu uebergebenen Parameter
	 * @return Rueckgabeobjekt der Methode
	 */		
	public static final Object callMethodOnInstance(DiagnostedMethodClass dClass,Object pClassInstance, Object[] pMethodArgs) throws Exception{
		if(pMethodArgs==null) pMethodArgs=new Object[]{};
		if(dClass.isValidClass()){
			try {
				if(Modifier.isStatic(dClass.getMainMethod().getModifiers()))
					pClassInstance=null;
				if(!dClass.getMainMethod().isAccessible())
					dClass.getMainMethod().setAccessible(true);
				return dClass.getMainMethod().invoke(pClassInstance,pMethodArgs);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | SecurityException e) {
				throw e;
			}
		}
		throw new NoValidClassException("Given Diagnosted Class is not valid.");		
	}
		
	
	/**
	 * Diese Methode wandelt in dem gegebenen Code alle primitiven Datentypen in Wrapper-Klassen um.
	 * Dies ist notwendig um bei Methoden mit Parametern die callMethod()-Methode aufrufen zu koennen.
	 * 
	 * !! Einschraenkende Anmerkung:
	 * Die Umwandlung geschieht ueber einen regulaeren Ausdruck und ist damit moeglicherweise fehleranfaellig.
	 * Es wurden enorm viele Moeglichkeiten beruecksichtigt, jedoch ist es in Ausnahmefaellen moeglich, dass die Methode nicht ueberall erstzt.
	 * Sofern nicht anders geregelt dient dies jedoch als praktikabler Workaround.
	 * 
	 * @deprecated
	 * @param pCode
	 * @return den ueberarbeiteten Code mit Wrapperklassen anstatt primitiven Datentypen
	 */
	public static final String primitiveToWrapper(String pCode){
		final String primToWrap[][]={{"boolean","Boolean"},{"byte","Byte"},{"char","Character"},
		{"short","Short"},{"int","Integer"},{"long","Long"},{"float","Float"},{"double","Double"}};
		
		StringBuffer sbuf=new StringBuffer(pCode);
		Matcher matcher;
		boolean bFound;
		for(int i=0;i<primToWrap.length;i++)
			do{
				matcher=Pattern.compile("[,;()\\s]"+primToWrap[i][0]+"[\\[\\s]").matcher(sbuf);
				bFound=matcher.find();
				if(bFound)
					sbuf.replace(matcher.start()+1, matcher.end()-1, primToWrap[i][1]);
			}while(bFound);
		return sbuf.toString();
	}
	
	/*
	 * Diese Methode gibt den Progammingtype zureuck.
	 * Siehe ASTHelper.getProgammingType()
	 * 
	 * Parameters:
	 * 		dcMethod - DiagnostedMethodClass Objekt
	 */	
	public static final Map<String,Boolean> getMethodType(DiagnostedMethodClass dcMethod) throws TooManyMethodsException{
		return ASTHelper.getMethodType(dcMethod.getMethodCode());
	}
	
	
	/**
	 * Die Methode gibt true zurueck falls die Hauptethode den Rueckgabewert 'void' hat.
	 * 
	 * @param dcMethod das zu ueberpruefende DiagnostedMethodClass Objekt
	 * @return 
	 * @throws IllegalArgumentException sofern dcMethod null ist.
	 */
	public static final boolean isVoid(DiagnostedMethodClass dcMethod){
		if(dcMethod==null) throw new IllegalArgumentException("Parameter cannot be null");
		else if(!dcMethod.isValidClass()) throw new IllegalArgumentException("Parameter cannot be an invalid Class");
		return dcMethod.getMainMethod().getReturnType().equals(Void.TYPE);
	}	
	
	
	/**
	 * Diese Methode versucht den richtigen Namen einer als String gegebenen Methode herauszufinden. Falls kein Methodename gefunden wird,
	 * weil zB die Syntax des Methodenkopfs falsch ist, wird ein leerer String zurueckgegeben.
	 * 
	 * !! Einschraenkende Anmerkung:
	 * Die Suche nach dem Namen geschieht mithilfe eines regulaeren Ausdrucks. Deshalb kann die Richtigkeit nicht sichergestellt sein. 
	 * 
	 * @param pMethod Quellcode der Methode
	 * @return Name der Methode
	 */
	public static final String getMethodName(String pMethod){
		Matcher matcher=Pattern.compile("[\\s][a-zA-Z_][\\w]*[\\s]*[(]{1}").matcher(pMethod);
		if(matcher.find())
			return pMethod.substring(matcher.start()+1, matcher.end()-1);
		else
			return "";
	}


	/**
	 * Diese Methode versucht den richtigen Namen einer als String gegebenen Klasse herauszufinden. Falls kein Klassenname gefunden wird,
	 * weil zB die Syntax des Klassenkopfs falsch ist, wird ein leerer String zurueckgegeben.
	 * 
	 * !! Einschraenkende Anmerkung:
	 * Die Suche nach dem Namen geschieht mithilfe eines regulaeren Ausdrucks. Deshalb kann die Richtigkeit nicht sichergestellt sein. 
	 * 
	 * @param pClass Quelltext der Klasse
	 * @return Name der Klasse
	 */
	public static final String getClassName(String pClass){
		Matcher matcher=Pattern.compile("class[\\s][a-zA-Z_][\\w]*[\\s]*([{<(extends)(implements)]{1})").matcher(pClass);
		if(matcher.find())
			return pClass.substring(matcher.start()+6, matcher.end()-1).trim();
		else
			return "";
	}
}
