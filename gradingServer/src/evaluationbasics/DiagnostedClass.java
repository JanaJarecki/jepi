package evaluationbasics;
import java.lang.reflect.InvocationTargetException;

import javax.tools.DiagnosticCollector;


/**
 * Bei der DiagnostedClass handelt es sich um eine Datenhaltungsklasse welche die Ergebnisse der Kompilierung enthaelt.
 * Es handelt sich hierbei um eine reine Datenhaltungsklasse. Die Daten werden also nicht veraendert.
 * @author Roman Bange
 *
 */
public class DiagnostedClass {
	private final boolean bValidClass;
	private final String sClassCode;
	private final DiagnosticCollector<?> diagnostic;
	private final Class<?> zClass;
	private final String sClassName;
	
	public DiagnostedClass(boolean pValidClass, String pClassName, String pClassCode, DiagnosticCollector<?> pDiagnostic, Class<?> pClass){
		bValidClass=pValidClass;
		sClassName=pClassName;
		sClassCode=pClassCode;
		diagnostic=pDiagnostic;
		zClass=pClass;
		
	}
	
	/*
	 * getter-Methoden der Konstanten
	 */
	public final boolean isValidClass(){return bValidClass;}
	public final String getClassName(){return sClassName;}
	public final String getClassCode(){return sClassCode;}
	public final DiagnosticCollector<?> getDiagnostic(){return diagnostic;}
	public final Class<?> getAsClass(){return zClass;}
	

	/**
	 *Gibt eine neue Instanz der Klasse aus.
	 * Falls dies nicht moeglich ist wird 'null' zurueckgegeben.
	 * 
	 * Anmerkung:
	 * Um diese Methode einfacher verwenden zu koennen, werden alle moeglichen Fehler abgefangen und in der Konsole ausgegeben. 
	 * @return Neue Instanz der Klasse oder sofern Fehlschlag 'null'
	 */
	public final Object getNewInstance(){
		if(zClass!=null)
			try {
				return zClass.getConstructor().newInstance();
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException  e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return null;
	}
	

	public String toString(){return "Funktionierende Klasse: "+bValidClass+"\n"
								+	"Name der Klasse: "+sClassName+"\n"
								+	"Diagnostik vorhanden: "+(diagnostic!=null)+"\n"
								+	"Class Objekt vorhanden: "+(zClass!=null)+"\n"
								+	"Gegebener Code der Klasse: "+sClassCode+"\n";}
	

}
