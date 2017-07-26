package evaluationbasics.compilation;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

/**
 * Created by ilias on 15.08.16.
 */
public class MyMethodWrapper implements CompilableClass {
    private static String _PATTERN = "/*MY_METHOD_TO_BE_WRAPPED_GOES_HERE*/";
    private String _methodeCode;
    private String _classCode;
    private String _className = "MyMethod";

    private String template = ""+
            "public class "+_className+" {\n"+
            "    "+_PATTERN+"\n"+
            "}\n";
    private int _OFFSET = 1;

    public MyMethodWrapper(String methodeCode) {
        this._methodeCode = methodeCode;
        _classCode = template.replace(_PATTERN,_methodeCode);
    }

    @Override
    public String getClassCode() {
        return _classCode;
    }

    @Override
    public String getClassName() {
        return _className;
    }

    @Override
    public String parseError(List<Diagnostic<? extends JavaFileObject>> diagnostics){
        Vector<String> msgs = new Vector<String>(diagnostics.size());
        for ( int i = 0; i<diagnostics.size(); ++i ) {
            Diagnostic<? extends JavaFileObject> diagnostic = diagnostics.get(i);
            long lineNbr = diagnostic.getLineNumber() - _OFFSET;
            String error = diagnostic.getMessage(Locale.ENGLISH);
            msgs.add(i, "at line " + lineNbr + ": " + error);
        }
        return String.join("\n",msgs);
    }

}
