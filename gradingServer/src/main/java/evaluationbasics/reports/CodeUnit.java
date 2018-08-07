package evaluationbasics.reports;

import evaluationbasics.xml.ParamGroup;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by ilias on 22.08.16.
 */
public class CodeUnit {
    public DiagnosticCollector<?> diagnostics = new DiagnosticCollector<JavaFileObject>();
    ;
    public int lineOffset = 0; //Zur Korrektur der Linienangaben in der Diagnostik
    public boolean compileable = false;
    public String origin = "";
    public int id;
    public boolean isSecurityLeak = false;

    //Methodtype
    public Map<String, Boolean> methodtype = null;
    public String methodtypeError = "";

    public LinkedList<ParamGroup> paramgroup; //Beinhaltet die Return-Werte
    public Object noParamsReturn = null; //Return-Wert fuer Methoden ohne Parameter
    public String error = "";

    //equals() nach id
    public boolean equals(Object obj) {
        if (obj.getClass() == this.getClass())
            return id == ((CodeUnit) obj).id;
        else
            return false;
    }
}
