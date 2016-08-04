package evaluationbasics;
import java.lang.reflect.Method;

import javax.tools.DiagnosticCollector;



/**
 * Die Klasse DiagnostedMethodClass ist eine Erweiterung der DiagnostedClass mit Schwerpunkt auf eine einzelne Methode.
 * Es handelt sich hierbei ebenfalls nur um eine Datenhaltungsklasse.
 * Dies erspart viel Arbeit wenn, wie im Falle des Bewertungssystem (Stand Dez14) hauptsaechlich um die Klassen mit lediglich einer Methode handelt.
 * 
 * Die Klasse wird um ein Feld MainMethod erweitert welche die Hauptmethode darstellt.
 * Hauptanwendungen sind Klassen mit nur einer Methode, wo in Folge auch der MethodenCode gesondert abgelegt werden kann.
 * Die Klasse kann aber auch genutzt werden um aus mehreren Methoden eine Hauptmethode herauszufiltern.(siehe EvaluationTools.compileMethods)
 * 
 * Es ist zudem moeglich aus einer DiagnostedClass eine DiagnostedMethodClass zu machen (siehe EvaluationTools.defineDiagnostedMethodClass).
 * 
 * @author Roman Bange
 * @see EvaluationTools.defineDiagnostedMethodClass
 * @see DiagnostedClass
 *
 */
public class DiagnostedMethodClass extends DiagnostedClass {
	private final Method zMainMethod;
	private final String sMethodCode;


	/**
	 * Erstellt eine komplett neue DiagnostedMethodClass.
	 * 
	 * @param pValidClass
	 * @param pClassName
	 * @param pClassCode
	 * @param pDiagnostic
	 * @param pClass
	 * @param pMainMethod
	 * @param pMethodCode
	 * @see DiagnostedClass
	 * @See DiagnostedMethodClass
	 */
	public DiagnostedMethodClass(boolean pValidClass, String pClassName, String pClassCode, DiagnosticCollector<?> pDiagnostic, Class<?> pClass, Method pMainMethod, String pMethodCode){
		super(pValidClass,pClassName,pClassCode,pDiagnostic,pClass);
		zMainMethod=pMainMethod;
		sMethodCode=pMethodCode;
	}
	/**
	 * Erstellt eine DiagnostedMethodClass aus einer DiagnostedClass
	 * 
	 * @param pDiagClass Zugrunde liegende DiagnostedClass
	 * @param pMainMethod Ausgewiesene Hauptmethode
	 * @param pMethodCode Ausgewiesene Methode als Quelltext
	 */
	public DiagnostedMethodClass(DiagnostedClass pDiagClass,Method pMainMethod, String pMethodCode){
		this(pDiagClass.isValidClass(),pDiagClass.getClassName(),pDiagClass.getClassCode(),pDiagClass.getDiagnostic(),pDiagClass.getAsClass(),pMainMethod,pMethodCode);			
	}
	
	/**
	 * Erstellt eine invalide DiagnostedMethodClass.
	 *  
	 * @param pDiagClass
	 * @param pMethodCode
	 * @see EvaluationTools.compileMethod
	 */
	public DiagnostedMethodClass(DiagnostedClass pDiagClass,String pMethodCode){
		this(false,pDiagClass.getClassName(),pDiagClass.getClassCode(),pDiagClass.getDiagnostic(),pDiagClass.getAsClass(),null,pMethodCode);			
	}
	
	public final Method getMainMethod(){return zMainMethod;}
	public final String getMethodCode(){return sMethodCode;}
	/**
	 * Gibt die Art der DiagnostedMethodClass an.
	 * @return true, sofern die DiagnostedMethodClass ohne expilizte Angabe der Hauptmethode als Quelltext erstellt wurde
	 */
	public final boolean isConvertedDiagnostedClass(){return sMethodCode.equals("");}
}
