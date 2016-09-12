package evaluationbasics.Reports;

import evaluationbasics.CompilationHelpers.CompilationBox;

import java.lang.reflect.Method;

import javax.tools.DiagnosticCollector;


/**
 * Die Klasse DiagnostedMethodClass ist eine Erweiterung der DiagnostedClass mit Schwerpunkt auf eine einzelne Methode.
 * Es handelt sich hierbei ebenfalls nur um eine Datenhaltungsklasse.
 * Dies erspart viel Arbeit wenn, wie im Falle des Bewertungssystem (Stand Dez14) hauptsaechlich um die Klassen mit lediglich einer Methode handelt.
 * <p>
 * Die Klasse wird um ein Feld MainMethod erweitert welche die Hauptmethode darstellt.
 * Hauptanwendungen sind Klassen mit nur einer Methode, wo in Folge auch der MethodenCode gesondert abgelegt werden kann.
 * Die Klasse kann aber auch genutzt werden um aus mehreren Methoden eine Hauptmethode herauszufiltern.(siehe CompilationBox.compileMethods)
 * <p>
 * Es ist zudem moeglich aus einer DiagnostedClass eine DiagnostedMethodClass zu machen (siehe CompilationBox.defineDiagnostedMethodClass).
 *
 * @author Roman Bange
 * @see CompilationBox.defineDiagnostedMethodClass
 * @see DiagnostedClass
 */
public class DiagnostedMethodClass extends DiagnostedClass {
    private Method mainMethod;
    private String methodCode;
    private int offset;


    /**
     *
     * @param pValidClass
     * @param pClassName
     * @param pClassCode
     * @param pDiagnostic
     * @param pClass
     * @param mainMethod
     * @param methodCode
     * @param offset
     */
    public DiagnostedMethodClass(boolean pValidClass,
                                 String pClassName,
                                 String pClassCode,
                                 DiagnosticCollector<?> pDiagnostic, Class<?> pClass,
                                 Method mainMethod,
                                 String methodCode,
                                 int offset) {
        super(pValidClass, pClassName, pClassCode, pDiagnostic, pClass);
        this.mainMethod = mainMethod;
        this.methodCode = methodCode;
        this.offset = offset;
    }

    /**
     *
     * @param diagnostedClass
     * @param methodCode
     * @param mainMethod
     * @param offset
     */
    public DiagnostedMethodClass(DiagnostedClass diagnostedClass,
                                 String methodCode,
                                 Method mainMethod,
                                 int offset) {
        this(
                diagnostedClass.isValidClass(),
                diagnostedClass.getClassName(),
                diagnostedClass.getClassCode(),
                diagnostedClass.getDiagnostic(),
                diagnostedClass.getAsClass(),
                mainMethod,
                methodCode,
                offset);
    }

    /**
     *
     * @param diagnostedClass
     * @param methodCode
     * @param offset
     */
    public DiagnostedMethodClass(DiagnostedClass diagnostedClass,
                                 String methodCode,
                                 int offset) {
        this(
                false,
                diagnostedClass.getClassName(),
                diagnostedClass.getClassCode(),
                diagnostedClass.getDiagnostic(),
                diagnostedClass.getAsClass(),
                null,
                methodCode,
                offset);
    }

    public Method getMainMethod() {
        return mainMethod;
    }

    public String getMethodCode() {
        return methodCode;
    }

    public int getOffset() {
        return offset;
    }

    /**
     * Gibt die Art der DiagnostedMethodClass an.
     *
     * @return true, sofern die DiagnostedMethodClass ohne expilizte Angabe der Hauptmethode als Quelltext erstellt wurde
     */
    public boolean isConvertedDiagnostedClass() {
        return methodCode.equals("");
    }
}
