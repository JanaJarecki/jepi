package evaluationbasics.compilation;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;

import evaluationbasics.reports.DiagnostedClass;
import evaluationbasics.reports.DiagnostedMethodClass;
import evaluationbasics.analysis.ASTHelper;
import evaluationbasics.exceptions.NoValidClassException;
import evaluationbasics.exceptions.TooManyMethodsException;
import evaluationbasics.exceptions.WrongNumberOfProvidedJavaElementsException;

import evaluationbasics.reports.DiagnostedTest;
import evaluationbasics.xml.TestData;

import org.eclipse.jdt.core.dom.*;


/**
 * Die Klasse bietet mehrere Methoden an die den Kern des Kompilierens vereinfachen.
 *
 * @author Roman Bange
 * @version 1.0
 */
public final class CompilationBox {


  private final JavaCompiler compiler;
  private final DiagnosticCollector diagnostics;
  private final MemClassLoader classLoader;
  private final JavaFileManager fileManager;
  private final List<String> options;

  public JavaCompiler getCompiler() {
    return compiler;
  }

  public DiagnosticCollector getDiagnostics() {
    return diagnostics;
  }

  public MemClassLoader getClassLoader() {
    return classLoader;
  }

  public JavaFileManager getFileManager() {
    return fileManager;
  }

  public CompilationBox() {

    compiler = ToolProvider.getSystemJavaCompiler();
    if (compiler == null)
      throw new IllegalArgumentException("Compiler cannot be found!");

    diagnostics = new DiagnosticCollector();
    classLoader = new MemClassLoader();
    fileManager = new MemJavaFileManager(compiler, classLoader, diagnostics);

//        String testNGURL = "file://home/ilias/Downloads/testng.jar";
//        String hamcrestURL = "file://home/ilias/Downloads/hamcrest.jar";

    classLoader.addClass("TestData",TestData.class);

    options = Arrays.asList(new String[]{
        //"-classpath", testNGURL + ":" + hamcrestURL
    });
  }

  public void close() {
    try {
      fileManager.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public DiagnostedTest compileClassWithTest(String classCode, String testCode, String person, String testMethodName) throws WrongNumberOfProvidedJavaElementsException, ClassNotFoundException {
    assert (!(classCode == null || classCode.equals("")));
    assert (!(testCode == null || testCode.equals("")));

    String className = getPublicClassName(classCode);
    String testName = getSingleClassName(testCode);

//        String testSuiteCode = generateTestNGSuiteCode(testName); // OLD VERSION
    String testSuiteCode = generateSingleTestNGSuiteCode(testName,testMethodName);
    String testSuiteName = generateTestNGSuiteClassName();

    StringJavaFileObject[] classes = {
        new StringJavaFileObject(className, classCode),
        new StringJavaFileObject(testName, testCode),
        new StringJavaFileObject(testSuiteName, testSuiteCode)
    };

    boolean compileSuccess = compileClasses(Arrays.asList(classes));

    Class<?> classClass = null;
    Class<?> testClass = null;
    Class<?> testSuiteClass = null;
    if (compileSuccess) {
      classClass = classLoader.findClass(className);
      testClass = classLoader.findClass(testName);
      testSuiteClass = classLoader.findClass(testSuiteName);
    }
    DiagnostedTest dc = new DiagnostedTest(compileSuccess, diagnostics,
        className, classCode, classClass,
        testName, testCode, testClass,
        testSuiteName, testSuiteCode, testSuiteClass);
    return dc;
  }


  public DiagnostedClass compileClass(String classCode) throws ClassNotFoundException, WrongNumberOfProvidedJavaElementsException {
    assert (!(classCode == null || classCode.equals("")));
    String className = getSingleClassName(classCode);
    return compileClass(classCode, className);
  }

  private String getSingleClassName(String classCode) throws WrongNumberOfProvidedJavaElementsException {
    ASTParser parser = ASTParser.newParser(AST.JLS8);
    parser.setSource(classCode.toCharArray());
    parser.setKind(ASTParser.K_COMPILATION_UNIT);

    CompilationUnit cu = (CompilationUnit) parser.createAST(null);

    Vector<String> classNames = new Vector();
    cu.accept(new ASTVisitor() {
      public boolean visit(TypeDeclaration node) {
        MethodDeclaration[] ms = node.getMethods();
        classNames.add(node.getName().toString());
        return true;
      }
    });
    if (classNames.size() != 1) {
      throw new WrongNumberOfProvidedJavaElementsException("Expected exactly 1 java class but found: " + classNames.size());
    }
    return classNames.get(0);
  }

  private String getPublicClassName(String classCode) throws WrongNumberOfProvidedJavaElementsException {
    ASTParser parser = ASTParser.newParser(AST.JLS8);
    parser.setSource(classCode.toCharArray());
    parser.setKind(ASTParser.K_COMPILATION_UNIT);

    CompilationUnit cu = (CompilationUnit) parser.createAST(null);

    Vector<String> classNames = new Vector();
    cu.accept(new ASTVisitor() {
      public boolean visit(TypeDeclaration node) {
        MethodDeclaration[] ms = node.getMethods();
        if ( Modifier.isPublic(node.getModifiers())) {
          classNames.add(node.getName().toString());
        }
        return true;
      }
    });
    if (classNames.size() != 1) {
      throw new WrongNumberOfProvidedJavaElementsException("Expected exactly 1 public java class but found: " + classNames.size());
    }
    return classNames.get(0);
  }


  /**
   * Diese Methode erstellt aus einem in String gegebenen Java-Code eine .java Datei, welche er versucht zu kompilieren.
   *
   * @param classCode Den Code einer Klasse inkl. der zu importierenden Pakete als String
   * @param className Name der gegebenen Klasse
   * @return DiagnostedClass Objekt welches die Informationen zur Klasse bzw. zur Kompilierung enthaelt
   * @throws IllegalArgumentException falls einer der Parameter keinen Inhalt hat
   * @see DiagnostedClass
   * @see DiagnostedMethodClass
   */
  private DiagnostedClass compileClass(String classCode, String className) throws ClassNotFoundException {
    assert (!(classCode == null || classCode.equals("")));
    assert (!(className == null || className.equals("")));

    Iterable<? extends JavaFileObject> units = Collections.singletonList(new StringJavaFileObject(className, classCode));

    CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, units);
    boolean compilationSuccess = task.call();

    Class<?> zClass = null;
    if (compilationSuccess) {
      zClass = classLoader.findClass(className);
    }

    return new DiagnostedClass(compilationSuccess, className, classCode, diagnostics, zClass);
  }


  /**
   * Diese Methode erstellt aus einem in String gegebenen Java-Code eine .java Datei, welche er versucht zu kompilieren.
   *
   * @param units Compilation units.
   * @return DiagnostedClass Objekt welches die Informationen zur Klasse bzw. zur Kompilierung enthaelt
   * @throws IllegalArgumentException falls einer der Parameter keinen Inhalt hat
   * @see DiagnostedClass
   * @see DiagnostedMethodClass
   */
  public boolean compileClasses(Iterable<? extends JavaFileObject> units) {
    assert (units != null);
    CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, units);
    return task.call();
  }

  /**
   * Ruft unter Erstellung einer Template-Klasse um die Methode, compileClass() auf.
   * Fuer naehere Dokumentation siehe compileMethod(String,String).
   *
   * @param methodCode Code der Methode als String
   * @return Vollstaendiges DiagnostedMethodClass Objekt
   * @throws IllegalArgumentException Wird geworfen wenn ein leerer Code uebergeben wurde.
   * @see DiagnostedMethodClass
   */
  public DiagnostedMethodClass compileMethod(String methodCode) throws TooManyMethodsException, ClassNotFoundException {
    return compileMethod(methodCode, new String[]{});
  }

  /**
   * Ruft unter Erstellung einer Template-Klasse um die Methode, compileClass() auf.
   * Bei dieser Methode lassen sich noch benoetigte zu importierte Pakete mitgeben.
   * Um bei einer Stapelbearbeitung die Template-Klasse eindeutig identifizieren zu koennen,
   * wird hierbei eine ID verwendet, die sich nach der Zeit der Erstellung richtet.
   *
   * @param methodCode     Code der Methode als String
   * @param importPackages Zu importierende Pakete. Es ist hierbei nur der Pfad anzugeben zB. "javax.swing.JOptionPane" oder "javax.swing.*"
   * @return Vollstaendiges DiagnostedMethodClass Objekt
   * @throws IllegalArgumentException Wird geworfen wenn ein leerer Code uebergeben wurde.
   * @see DiagnostedMethodClass
   */
  private DiagnostedMethodClass compileMethod(String methodCode, String[] importPackages)
      throws TooManyMethodsException, ClassNotFoundException {
    assert (!(methodCode == null || methodCode.isEmpty()));
    assert (importPackages != null);
    int offset = 0;

    String className = "AssProgMethodClass" + String.valueOf(System.currentTimeMillis()).substring(5);

    String importStatements = "";
    for (String pPackage : importPackages) {
      offset += 1;
      importStatements = importStatements + "import " + pPackage + ";\n";
    }

    String classCode = importStatements +
        "public class " + className + "{\n" +
        methodCode + "\n" +
        "}";
    offset += 1;

    DiagnostedClass dcMethod = compileClass(classCode, className);

    if (dcMethod.isValidClass()) {
      Method[] methods = dcMethod.getAsClass().getDeclaredMethods();
      if (methods.length == 1)
        return new DiagnostedMethodClass(dcMethod, methodCode, methods[0], offset);
      else
        throw new TooManyMethodsException("There are too many methods found: " + Arrays.toString(methods));
    }
    return new DiagnostedMethodClass(dcMethod, methodCode, offset);
  }

  /**
   * Funktioniert grundlegend wie die compileMethod()-Methoden.
   * In diesem Fall kann jedoch der Methodenname mitgegeben werden. So ist es moeglich auch mehrere Methoden im Code mitzugeben,
   * jedoch nur eine als MainMethode zu identifizieren. Es ist so moeglich, dass eine Klasse wie eine DiagnostedMethodClass behandelt werden kann,
   * die MainMethode aber auf andere selbst deklarierte Helfermethoden innerhalb der Klasse zurueckgreifen kann.
   * <p>
   * !! Einschraenkende Anmerkung:
   * <p>
   * Die Methode durchsucht alle Methoden der Klasse nach ihren Namen. Falls in der Klasse mehrere Methoden gleichen Namens auftreten,
   * so wird willkuerlich eine Methode gewaehlt. Dies gilt zu vermeiden.
   *
   * @param mCode   Code der Methode als String
   * @param imports Zu importierende Pakete. Es ist hierbei nur der Pfad anzugeben zB. "javax.swing.JOptionPane" oder "javax.swing.*" Falls nicht notwendig: "null"
   * @param mName   Name der gewuenschten MainMethode (case sensitive)
   * @return Vollstaendiges DiagnostedMethodClass Objekt
   * @throws IllegalArgumentException Wenn dcClass oder pMethodName leer sind
   * @see DiagnostedMethodClass
   */
  public DiagnostedMethodClass compileMethods(String mCode, String[] imports, String mName)
      throws TooManyMethodsException, ClassNotFoundException {
    DiagnostedClass dcMethod = compileMethod(mCode, imports);
    return defineDiagnostedMethodClass(dcMethod, mName, null);
  }

  /**
   * siehe compileMethods(String,String[],String)
   * <p>
   * !! Einschraenkende Anmerkung:
   * <p>
   * Die Methode durchsucht alle Methoden der Klasse nach ihren Namen als auch nach den den Paramtertypen.
   * Falls diese Werte abweichen, so gilt die Kompilierung als fehlgeschlagen.
   *
   * @param pMethodsCode      Code der Methode als String
   * @param pImportedPackages Zu importierende Pakete. Es ist hierbei nur der Pfad anzugeben zB. "javax.swing.JOptionPane" oder "javax.swing.*" Falls nicht notwendig: "null"
   * @param pMethodName       Name der gewuenschten MainMethode (case sensitive)
   * @return Vollstaendiges DiagnostedMethodClass Objekt oder Invalides DiagnostedMethodClass Objekt, sofern die Methode syntaktisch nicht korrekt war.
   */
  public DiagnostedMethodClass compileMethods(String pMethodsCode, String[] pImportedPackages, String pMethodName, Class<?>[] pArgTypes)
      throws TooManyMethodsException, ClassNotFoundException {
    DiagnostedClass diagnosedClass = compileMethod(pMethodsCode, pImportedPackages);
    if (diagnosedClass.isValidClass())
      try {
        Method method = diagnosedClass.getAsClass().getDeclaredMethod(pMethodName, pArgTypes);
        return new DiagnostedMethodClass(diagnosedClass, pMethodsCode, method, 0);
      } catch (NoSuchMethodException | SecurityException e) {
        e.printStackTrace();
      }
    return new DiagnostedMethodClass(diagnosedClass, pMethodsCode, 0);
  }


  /**
   * Mithilfe dieser Methode kann aus einer DiagnostedClass eine DiagnostedMethodClass erstellt werden.
   * Sofern der Name der gewuenschten MainMethode mehrmals vorkommt darf der Parameter argumentTypes nicht null sein.
   *
   * @param dcClass       DiagnostedClass Objekt das umgewandelt werden soll
   * @param pMethodName   Name der gewuenschten MainMethode(case sensitive)
   * @param argumentTypes Die Parametertypen der gewuenschten MainMethode, falls es mehrere Methode mit pMethodName gibt
   * @return DiagnostedMethodClass Objekt ohne Code in Klartext oder ein invalides DiagnostedMethodClass Objekt
   * @throws IllegalArgumentException Wenn dcClass oder pMethodName leer sind
   */
  private DiagnostedMethodClass defineDiagnostedMethodClass(DiagnostedClass dcClass, String pMethodName, Class<?>[] argumentTypes) {
    if (dcClass == null) throw new IllegalArgumentException("dcClass cannot be null");
    else if (pMethodName == null || pMethodName.equals(""))
      throw new IllegalArgumentException("pMethodName cannot be null");
    if (dcClass.isValidClass()) {
      Method[] zMethods = dcClass.getAsClass().getDeclaredMethods();
      for (Method m : zMethods)
        if (m.getName().equals(pMethodName))
          return new DiagnostedMethodClass(dcClass, "", m, 0);
      if (argumentTypes != null && argumentTypes.length >= 1)
        try {
          Method zMethod = dcClass.getAsClass().getDeclaredMethod(pMethodName, argumentTypes);
          return new DiagnostedMethodClass(dcClass, "", zMethod, 0);
        } catch (NoSuchMethodException | SecurityException ignored) {
        } //Fehler
    }
    return new DiagnostedMethodClass(dcClass, "", 0);//Die angegebene Methode ist nicht in der Klasse enthalten
  }


  /**
   * Gibt die Kompilerdiagnostik der analysierten Klasse als String aus.
   *
   * @param dClass DiagnostedClass Objekt welches von der compileClass()-Methode zurueckgegeben wurde
   * @return Kompilerdiagnostik als String
   */
  public static String diagnosticToString(DiagnostedClass dClass) {
    return diagnosticToString(dClass, 0);
  }

  /**
   * Gibt die Kompilerdiagnostik der analysierten Methode als String aus.
   *
   * @param dClass DiagnostedMethodClass Objekt welches von der compileMethod()-Methode zurueckgegeben wurde
   * @return Kompilerdiagnostik als String
   */
  public static String diagnosticToString(DiagnostedMethodClass dClass) {
    return diagnosticToString(dClass, 3);
  }

  /**
   * @param dClass    DiagnostedClass Objekt deren Kompilerdiagnostik ausgelesen werden soll
   * @param startLine Hiermit laesst sich alle Zeilennummern versetzen.
   * @return Kompilerdiagnostik als String
   * @throws IllegalArgumentException Wenn dClass null ist
   */
  private static String diagnosticToString(DiagnostedClass dClass, int startLine) {
    if (dClass == null) throw new IllegalArgumentException("dClass cannot be null");
    DiagnosticCollector<?> diagnostics = dClass.getDiagnostic();
    if (diagnostics == null) return "Keine Diagnostik vorhanden";
    else {
      String sReturn = "";
      for (Diagnostic<?> diag : diagnostics.getDiagnostics()) {
        sReturn = sReturn + "Quelle: " + diag.getSource() + "\n" +
            "Code: " + diag.getCode() + "\n" +
            "Nachricht: " + diag.getMessage(null) + "\n" +
            "Zeile:" + (diag.getLineNumber() + startLine) + "\n" +
            "Position/Spalte:" + diag.getPosition() + "/" +
            diag.getColumnNumber() + "\n" +
            "Startpostion/Endposition: n" + diag.getStartPosition() + "/" +
            diag.getEndPosition() + "\n\n";
      }
      return sReturn;
    }
  }


  /**
   * siehe callMethodOnInstance(DiagnostedClass,Object,String,Object[])
   * Bei dieser Methode wird anstatt eine gegebene Instanz zu verwenden. Eine neue ersellt.
   *
   * @param dClass      DiagnostedClass Objekt welches von der compileClass()-Methode zurueckgegeben wurde
   * @param pMethodName Name der Methode die ausgefuehrt werden soll
   * @param pMethodArgs Array der zu uebergebenen Parameter
   * @
   */
  public static Object callMethod(DiagnostedClass dClass, String pMethodName, Object[] pMethodArgs) throws Exception {
    return callMethodOnInstance(dClass, dClass.getNewInstance(), pMethodName, pMethodArgs);
  }

  /**
   * Diese Methode fuehrt eine Methode unter gegebenen Parametern aus. Dies geschieht unter der gegebenen Instanz.
   * Die Methode gibt im Erfolgsfall den return-Wert der zu aufrufenden Methode zurueck.
   * Falls keine Parameter erforderlich sind kann entweder eine leere ObjectArray oder null uebergeben werden.
   * <p>
   * !! Einschraenkende Anmerkung:
   * Zur Identifizierung der Methode in der Klasse werden die Typen der Argumente benoetigt, da diese nicht uebergeben werden,
   * wird nun geprueft ob fuer die Typen der in den Parametern uebergebenen Parameter eine Methode gefunden wird.
   * Es ist also von den gegebenen Parametern abhaengig welche Methode aufgerufen wird.
   * Damit dies so funktioniert, duerfen die Datentypen der auzurufenen Methode nur Wrapperklassen sein (keine primitiven Datentypen)
   * Ein Workaround hierfuer ist primitiveToWrapper()
   *
   * @param dClass         DiagnostedClass Objekt welches von der compileClass()-Methode zurueckgegeben wurde
   * @param pClassInstance Instanz der Klasse
   * @param pMethodName    Name der Methode die ausgefuehrt werden soll
   * @param pMethodArgs    Array der zu uebergebenen Parameter
   * @throws IllegalAccessException    Exception
   * @throws IllegalArgumentException  Exception
   * @throws InvocationTargetException Exception
   * @throws NoSuchMethodException     Wird geworfen wenn fuer die gegebenen Parameter und den Methodenname keine Methode gefunden wurde
   * @throws SecurityException         Sofern die Klasse Permissions benoetigt welche verboten worden sind.
   */
  private static Object callMethodOnInstance(DiagnostedClass dClass, Object pClassInstance, String pMethodName, Object[] pMethodArgs) throws Exception {
    if (pMethodArgs == null) pMethodArgs = new Object[]{};
    //
    Class<?> argTypes[] = new Class<?>[pMethodArgs.length];
    for (int i = 0; i < pMethodArgs.length; i++)
      argTypes[i] = pMethodArgs[i].getClass();
    if (dClass.isValidClass()) {
      Method m;
      try {
        m = dClass.getAsClass().getDeclaredMethod(pMethodName, argTypes);
        if (Modifier.isStatic(m.getModifiers())) //falls die Methode static ist
          pClassInstance = null;
        return m.invoke(pClassInstance, pMethodArgs);
      } catch (IllegalAccessException | IllegalArgumentException
          | InvocationTargetException |
          NoSuchMethodException | SecurityException e) {
        throw e;
      }
    } else
      throw new NoValidClassException("Given Diagnosted Class is not valid.");
  }


  /**
   * Diese Methode wandelt in dem gegebenen Code alle primitiven Datentypen in Wrapper-Klassen um.
   * Dies ist notwendig um bei Methoden mit Parametern die callMethod()-Methode aufrufen zu koennen.
   * <p>
   * !! Einschraenkende Anmerkung:
   * Die Umwandlung geschieht ueber einen regulaeren Ausdruck und ist damit moeglicherweise fehleranfaellig.
   * Es wurden enorm viele Moeglichkeiten beruecksichtigt, jedoch ist es in Ausnahmefaellen moeglich, dass die Methode nicht ueberall erstzt.
   * Sofern nicht anders geregelt dient dies jedoch als praktikabler Workaround.
   *
   * @param code Java-Code
   * @return den ueberarbeiteten Code mit Wrapperklassen anstatt primitiven Datentypen
   * @deprecated
   */
  public static String primitiveToWrapper(String code) {
    final String primToWrap[][] = {{"boolean", "Boolean"}, {"byte", "Byte"}, {"char", "Character"},
        {"short", "Short"}, {"int", "Integer"}, {"long", "Long"}, {"float", "Float"}, {"double", "Double"}};

    StringBuffer sbuf = new StringBuffer(code);
    Matcher matcher;
    boolean bFound;
    for (String[] aPrimToWrap : primToWrap)
      do {
        matcher = Pattern.compile("[,;()\\s]" + aPrimToWrap[0] + "[\\[\\s]").matcher(sbuf);
        bFound = matcher.find();
        if (bFound)
          sbuf.replace(matcher.start() + 1, matcher.end() - 1, aPrimToWrap[1]);
      } while (bFound);
    return sbuf.toString();
  }

  /*
   * Diese Methode gibt den Progammingtype zureuck.
   * Siehe ASTHelper.getProgammingType()
   *
   * Parameters:
   * 		dcMethod - DiagnostedMethodClass Objekt
   */
  public static Map<String, Boolean> getMethodType(DiagnostedMethodClass dcMethod) throws TooManyMethodsException {
    return ASTHelper.getMethodType(dcMethod.getMethodCode());
  }


  /**
   * Die Methode gibt true zurueck falls die Hauptethode den Rueckgabewert 'void' hat.
   *
   * @param dcMethod das zu ueberpruefende DiagnostedMethodClass Objekt
   * @return True if it is void.
   * @throws IllegalArgumentException sofern dcMethod null ist.
   */
  public static boolean isVoid(DiagnostedMethodClass dcMethod) {
    if (dcMethod == null) throw new IllegalArgumentException("Parameter cannot be null");
    else if (!dcMethod.isValidClass()) throw new IllegalArgumentException("Parameter cannot be an invalid Class");
    return dcMethod.getMainMethod().getReturnType().equals(Void.TYPE);
  }


  /**
   * Diese Methode versucht den richtigen Namen einer als String gegebenen Methode herauszufinden. Falls kein Methodename gefunden wird,
   * weil zB die Syntax des Methodenkopfs falsch ist, wird ein leerer String zurueckgegeben.
   * <p>
   * !! Einschraenkende Anmerkung:
   * Die Suche nach dem Namen geschieht mithilfe eines regulaeren Ausdrucks. Deshalb kann die Richtigkeit nicht sichergestellt sein.
   *
   * @param pMethod Quellcode der Methode
   * @return Name der Methode
   */
  public static String getMethodName(String pMethod) {
    Matcher matcher = Pattern.compile("[\\s][a-zA-Z_][\\w]*[\\s]*[(]{1}").matcher(pMethod);
    if (matcher.find())
      return pMethod.substring(matcher.start() + 1, matcher.end() - 1);
    else
      return "";
  }


  /**
   * Diese Methode versucht den richtigen Namen einer als String gegebenen Klasse herauszufinden. Falls kein Klassenname gefunden wird,
   * weil zB die Syntax des Klassenkopfs falsch ist, wird ein leerer String zurueckgegeben.
   * <p>
   * !! Einschraenkende Anmerkung:
   * Die Suche nach dem Namen geschieht mithilfe eines regulaeren Ausdrucks. Deshalb kann die Richtigkeit nicht sichergestellt sein.
   *
   * @param pClass Quelltext der Klasse
   * @return Name der Klasse
   */
  public static String getClassName(String pClass) {
    Pattern pat = Pattern.compile("class[\\s][a-zA-Z_][\\w]*[\\s]*([{<(extends)(implements)]{1})");
    Matcher matcher = pat.matcher(pClass);
    if (matcher.find())
      return pClass.substring(matcher.start() + 6, matcher.end() - 1).trim();
    else
      return "";
  }


  private static String generateTestNGSuiteClassName() {
    return "EvaluationServerTestMainClass";
  }


  private static String generateSingleTestNGSuiteCode(String className, String testName) {
    return "import java.util.*;\n" +
        "import org.testng.TestListenerAdapter;\n" +
        "import org.testng.TestNG;\n" +
        "import org.testng.ITestNGListener;\n" +
        "import org.testng.ITestResult;\n" +
        "\n" +
        "import org.testng.xml.XmlSuite;\n" +
        "import org.testng.xml.XmlTest;\n" +
        "import org.testng.xml.XmlInclude;\n" +
        "import org.testng.xml.XmlClass;\n" +
        "import evaluationbasics.utils.SysOutGrabber;" +
        "\n" +
        "import java.util.function.Consumer;\n" +
        "import java.util.function.IntFunction;\n" +
        "import static java.util.stream.Collectors.toSet;" +
        "import evaluationbasics.xml.TestData;\n" +
        "import java.util.stream.Stream;" +
        "\n" +
        "import static java.util.stream.Collectors.toSet;\n" +
        "\n" +
        "public class EvaluationServerTestMainClass{\n" +
        "\n" +
        "public static void RunTests(TestData testData) {\n" +
        "\n" +
        "   TestListenerAdapter tla = new TestListenerAdapter();\n" +
        "\n" +
        "    XmlSuite suite = new XmlSuite();\n" +
        "    suite.setName(\"TmpSuite\");\n" +
        "\n" +
        "    XmlTest test = new XmlTest(suite);\n" +
        "    test.setName(\"TmpTest\");\n" +
        "\n" +
        "    XmlInclude inc = new XmlInclude(\""+testName+"\");\n" +
        "    List<XmlInclude> testToExecute = new ArrayList<XmlInclude>();\n" +
        "    testToExecute.add(inc);\n" +
        "\n" +
        "    XmlClass testedClass = new XmlClass( "+className+".class);\n" +
        "    testedClass.setIncludedMethods(testToExecute);\n" +
        "\n" +
        "    List<XmlClass> classes = new ArrayList<XmlClass>();\n" +
        "    classes.add(testedClass);\n" +
        "    test.setXmlClasses(classes);\n" +
        "\n" +
        "    List<XmlSuite> suites = new ArrayList<XmlSuite>();\n" +
        "    suites.add(suite);\n" +
        "\n" +
        "    TestNG tng = new TestNG();\n" +
        "    tng.setXmlSuites(suites);\n" +
        "    tng.addListener((ITestNGListener)tla);\n" +
        "    tng.setUseDefaultListeners(false);\n" +
        "    tng.setVerbose(0);\n" +
        "\n" +
        "    tng.run();\n" +
        "\n" +
        "\n" +
        "\n" +
        "\n" +
        "    try {\n" +
        "      Set<String> partiallyPassedTests = new HashSet<>();\n" +
        "      for ( ITestResult result : tla.getPassedTests() ) partiallyPassedTests.add(result.getName());\n" +
        "\n" +
        "      Set<String> failedTests = new HashSet<>();\n" +
        "      for ( ITestResult result : tla.getFailedTests() ) failedTests.add(result.getName() );\n" +
        "\n" +
        "      Set<String> fullyPassedTests = new HashSet<String>(partiallyPassedTests);\n" +
        "      fullyPassedTests.removeAll(failedTests);\n" +
        "      partiallyPassedTests.removeAll(fullyPassedTests);\n" +
        "      failedTests.removeAll(partiallyPassedTests);\n" +
        "\n" +
        "      for ( String x: failedTests) if (x == \""+testName+"\") writeToTest(x,testData,-1);\n" +
        "      for ( String x: partiallyPassedTests) if (x == \""+testName+"\") writeToTest(x,testData,0);\n" +
        "      for ( String x: fullyPassedTests) if (x == \""+testName+"\") writeToTest(x,testData,1);\n" +
        "    } catch (Exception e) {\n" +
        "    }" +
        "  }\n" +
        "\n" +
        "  public static void writeToTest(String name, TestData t, int result) {\n" +
        "      if (t.name.equals(name)) {\n" +
        "        t.passed = result > 0;\n" +
        "        t.passedPartially = result >= 0;\n" +
        "        t.reachedPoints = (t.passed) ? t.points : 0;\n" +
        "      }\n" +
        "  }" +
        "\n" +
        "}";
  }


  private static String generateTestNGSuiteCode(String name) {
    return "import java.util.*;\n" +
        "import org.testng.TestListenerAdapter;\n" +
        "import org.testng.TestNG;\n" +
        "import org.testng.ITestNGListener;\n" +
        "import org.testng.ITestResult;\n" +
        "\n" +
        "import java.util.function.Consumer;\n" +
        "import java.util.function.IntFunction;\n" +
        "import static java.util.stream.Collectors.toSet;" +
        "import evaluationbasics.xml.TestData;\n" +
        "import java.util.stream.Stream;" +
        "\n" +
        "import static java.util.stream.Collectors.toSet;\n" +
        "\n" +
        "public class EvaluationServerTestMainClass{\n" +
        "\n" +
        "public static void RunTests(List<TestData> tests) {\n" +
        "\n" +
        "        TestListenerAdapter tla = new TestListenerAdapter();\n" +
        "        TestNG testng = new TestNG();\n" +
        "        testng.setUseDefaultListeners(false);\n" +
        "        testng.setVerbose(0);\n" +
        "        testng.setTestClasses(new Class[] { "+name+".class });\n" +
        "        testng.addListener((ITestNGListener)tla);\n" +
        "        testng.run();\n" +
        "\n" +
        "        try {\n" +
        "            Set<String> partiallyPassedTests = new HashSet<>();\n" +
        "            for ( ITestResult result : tla.getPassedTests() ) partiallyPassedTests.add(result.getName());\n" +
        "\n" +
        "            Set<String> failedTests = new HashSet<>();\n" +
        "            for ( ITestResult result : tla.getFailedTests() ) failedTests.add(result.getName() );\n" +
        "\n" +
        "            Set<String> fullyPassedTests = new HashSet<String>(partiallyPassedTests);" +
        "\n" +
        "            fullyPassedTests.removeAll(failedTests);\n" +
        "            partiallyPassedTests.removeAll(fullyPassedTests);\n" +
        "            failedTests.removeAll(partiallyPassedTests);\n" +
        "\n" +
        "            for ( String x: failedTests) writeToTest(x,tests,-1);\n" +
        "            for ( String x: partiallyPassedTests) writeToTest(x,tests,0);\n" +
        "            for ( String x: fullyPassedTests) writeToTest(x,tests,1);" +
        "        } catch (Exception e) {\n" +
        "        }\n" +
        "    }\n" +
        "\n" +
        "    public static void writeToTest(String name, List<TestData> list, int result) {\n" +
        "        for ( TestData t: list) {\n" +
        "            if (t.name.equals(name)) {\n" +
        "                t.passed = result > 0;\n" +
        "                t.passedPartially = result >= 0;\n" +
        "                t.reachedPoints = (t.passed) ? t.points : 0;\n" +
        "            }\n" +
        "        }\n" +
        "    }\n" +
        "\n" +
        "}";

//        return "\n" +
//                "import evaluationbasics.analysis.ASTHelper;\n" +
//                "import evaluationbasics.xml.TestData;\n" +
//                "import org.testng.*;\n" +
//                "import org.testng.annotations.DataProvider;\n" +
//                "import org.testng.annotations.Test;\n" +
//                "import org.testng.internal.IResultListener;\n" +
//                "import org.testng.internal.IResultListener2;\n" +
//                "\n" +
//                "import java.util.*;\n" +
//                "import java.util.function.Consumer;\n" +
//                "import java.util.function.IntFunction;\n" +
//                "import java.util.stream.Stream;\n" +
//                "\n" +
//                "import static java.util.stream.Collectors.toSet;\n" +
//                "\n" +
//                "\n" +
//                "public class TestTestNG {\n" +
//                "    @DataProvider( name = \"first\")\n" +
//                "    public static Object[][] _MyValues() {\n" +
//                "        return new Object[][]{\n" +
//                "                {\"public int id(int a) { return a; }\", new boolean[]{false, false}},\n" +
//                "                {\"public int id(int a) { if (a==1) return 1; return id(a-1); }\", new boolean[]{true, false}},\n" +
//                "                {\"public int id(int a) { int sum = 0; for(int i=0;i<a;++i) sum+=a; return sum; }\", new boolean[]{false, true}},\n" +
//                "                {\"public int id(int a) { if (a==1) return 1; int sum = 0; for(int i=0;i<a;++i) sum+=a; return sum+id(a-1); }\", new boolean[]{true, true}}\n" +
//                "        };\n" +
//                "    }\n" +
//                "\n" +
//                "\n" +
//                "    @DataProvider(name = \"second\")\n" +
//                "    public static Object[][] _MyValues2() {\n" +
//                "        return new Object[][]{\n" +
//                "                {\"public int id(int a) { return a; }\", new boolean[]{false, false}},\n" +
//                "                {\"public int id(int a) { if (a==1) return 1; return id(a-1); }\", new boolean[]{true, false}},\n" +
//                "                {\"public int id(int a) { int sum = 0; for(int i=0;i<a;++i) sum+=a; return sum; }\", new boolean[]{false, true}},\n" +
//                "                {\"public int id(int a) { if (a==1) return 1; int sum = 0; for(int i=0;i<a;++i) sum+=a; return sum+id(a-1); }\", new boolean[]{false, true}}\n" +
//                "        };\n" +
//                "    }\n" +
//                "\n" +
//                "    @Test( dataProvider = \"first\")\n" +
//                "    public void testFirst(String code, boolean[] types) {\n" +
//                "        testMethod(code,types);\n" +
//                "    }\n" +
//                "\n" +
//                "    @Test( dataProvider = \"second\")\n" +
//                "    public void testSecond(String code, boolean[] types) {\n" +
//                "        testMethod(code,types);\n" +
//                "    }\n" +
//                "\n" +
//                "    public void testMethod(String code, boolean[] types) {\n" +
//                "        Map<String, Boolean> type;\n" +
//                "        try {\n" +
//                "            type = ASTHelper.getMethodType(code);\n" +
//                "        } catch (Exception e) {\n" +
//                "            throw new RuntimeException(\"Test failed\");\n" +
//                "        }\n" +
//                "        Assert.assertEquals((boolean)type.get(\"recursive\"),types[0]);\n" +
//                "        Assert.assertEquals((boolean)type.get(\"loop\"),types[1]);\n" +
//                "    }\n" +
//                "\n" +
//                "    public static void main (String[] args) {\n" +
//                "        Vector<TestData> tests = new Vector();\n" +
//                "        tests.add(new TestData(0,\"testFirst\",5));\n" +
//                "        tests.add(new TestData(1,\"testSecond\",3));\n" +
//                "    }\n" +
//                "\n" +
//                "    public static void RunTests(List<TestData> tests) {\n" +
//                "\n" +
//                "        TestListenerAdapter tla = new TestListenerAdapter();\n" +
//                "        TestNG testng = new TestNG();\n" +
//                "        testng.setUseDefaultListeners(false);\n" +
//                "        testng.setVerbose(0);\n" +
//                "        testng.setTestClasses(new Class[] { TestTestNG.class });\n" +
//                "        testng.addListener((ITestNGListener)tla);\n" +
//                "        testng.run();\n" +
//                "\n" +
//                "        try {\n" +
//                "            Set<String> partiallyPassedTests = tla.getPassedTests().stream().map((x) -> {\n" +
//                "                return x.getName();\n" +
//                "            }).collect(toSet());\n" +
//                "\n" +
//                "            Set<String> failedTests = tla.getFailedTests().stream().map((x) -> {\n" +
//                "                return x.getName();\n" +
//                "            }).collect(toSet());\n" +
//                "\n" +
//                "            Set<String> fullyPassedTests = new HashSet<String>(partiallyPassedTests);\n" +
//                "\n" +
//                "            fullyPassedTests.removeAll(failedTests);\n" +
//                "            partiallyPassedTests.removeAll(fullyPassedTests);\n" +
//                "            failedTests.removeAll(partiallyPassedTests);\n" +
//                "\n" +
//                "            for ( String x: failedTests) writeToTest(x,tests,-1);\n" +
//                "            for ( String x: partiallyPassedTests) writeToTest(x,tests,0);\n" +
//                "            for ( String x: fullyPassedTests) writeToTest(x,tests,1);\n" +
//                "        } catch (Exception e) {\n" +
//                "            System.out.println(\"runTest => \"+e);\n" +
//                "            System.out.println();\n" +
//                "            Arrays.asList(e.getStackTrace()).stream().forEach(x-> System.out.println(x));\n" +
//                "            System.out.println();\n" +
//                "            System.out.println(e.getCause());\n" +
//                "        }\n" +
//                "    }\n" +
//                "\n" +
//                "    public static void writeToTest(String name, List<TestData> list, int result) {\n" +
//                "        for ( TestData t: list) {\n" +
//                "            if (t.name.equals(name)) {\n" +
//                "                t.passed = result > 0;\n" +
//                "                t.passedPartially = result >= 0;\n" +
//                "                t.reachedPoints = (t.passed) ? t.points : 0;\n" +
//                "            }\n" +
//                "        }\n" +
//                "    }\n" +
//                "\n" +
//                "\n" +
//                "}";
  }
}
