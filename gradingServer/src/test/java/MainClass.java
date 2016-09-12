import evaluationbasics.CompilationHelpers.CompilationBox;
import evaluationbasics.CompilationHelpers.StringJavaFileObject;
import evaluationbasics.Evaluators.EvaluationHelper;
import evaluationbasics.Evaluators.TestNGEvaluator;
import evaluationbasics.Reports.DiagnostedClass;
import evaluationbasics.XML.TestData;
import evaluationbasics.XML.XMLConstructor;
import org.eclipse.jdt.core.dom.*;

import javax.tools.Diagnostic;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

/**
 * Created by ilias on 23.08.16.
 */
public class MainClass {

//    public static void main ( String[] args) {
//        try {
//            MemClassLoader mcl = new MemClassLoader();
//            Class<?> clazz  = mcl.loadClass("org.testng.Assert");
//            System.out.println(clazz.getName());
//        } catch ( ClassNotFoundException e ){
//            System.err.println(e);
//        } catch ( Exception e ) {
//            System.err.println(e);
//        }
//    }

    public static void main ( String[] args) {
        try {
            String solution = "public class MyClass {\n" +
                    "  public int value = 42;\n" +
                    "  public int getValue() { return value; }\n" +
                    "}";
            String solution2 = "public class MyClass {\n" +
                    "  public int value = 42;\n" +
                    "  public int getValue() { return value; }\n" +
                    "}\n" +
                    "class MyClass2 {\n" +
                    "  public int value = 42;\n" +
                    "  public int getValue() { return value; }\n" +
                    "} ";
            String test = "import org.testng.Assert;\n" +
                    "import org.testng.annotations.Test;\n" +
                    "\n" +
                    "class AssProgQuestionTestNG {\n" +
                    "  @Test\n" +
                    "  public void initialValueTest() {\n" +
                    "    MyClass m = new MyClass();\n" +
                    "    Assert.assertEquals(42,m.getValue());\n" +
                    "  }\n" +
                    "}";





            ASTParser parser = ASTParser.newParser(AST.JLS8);
            parser.setSource(solution.toCharArray());
            parser.setKind(ASTParser.K_COMPILATION_UNIT);

            CompilationUnit cu = (CompilationUnit) parser.createAST(null);


            Vector<String> classNames = new Vector();
            cu.accept(new ASTVisitor() {
                          public boolean visit(TypeDeclaration node) {
                              MethodDeclaration[] ms = node.getMethods();
                              if ( Arrays.asList(ms).stream().anyMatch(m -> (m.getName().toString() == "main")) ) {
                                  classNames.add(node.getName().toString());
                              }
                              return true;
                          }
                      });

            System.out.println();

            String solutionName = "MyClass";
            CompilationBox cp = new CompilationBox();
            DiagnostedClass dc = cp.compileClassWithTest(solution,test,"test");

            List<TestData> tests = Arrays.asList(new TestData[]{
                    new TestData(0,"testFirst","Erster Test",3),
                    new TestData(1,"testSecond","Zweiter Test",3),
                    new TestData(2,"initialValueTest","Default Konstruktor Test", 4)
            });

            EvaluationHelper.runInstanceMethod(dc.getAsClass(), "RunTests", new Object[]{tests});


//            Method m = TestTestNG.class.getMethod("RunTests",List.class);
//            System.out.println(m.getName());
//            m.invoke(null,tests);
            System.out.println(dc.isValidClass());
            for ( TestData td : tests) {
                System.out.println( td.name + " " + td.passed);;
            }

        } catch (Exception e) {
            System.out.println(e);
            System.out.println();
            Arrays.asList(e.getStackTrace()).stream().forEach(x-> System.out.println(x));
            System.out.println();
            System.out.println(e.getCause());
        }
    }

    private static void printDiagnostics(CompilationBox cp, boolean compileSuccess) {
        List<Diagnostic<?>> diags = cp.getDiagnostics().getDiagnostics();
        if( ! compileSuccess ) {
            for (Diagnostic<?> diag : diags) {
                System.out.println(diag.getSource().toString());
                System.out.println(diag.getLineNumber());
                System.out.println(diag.getCode().toString());
                System.out.println(diag.getMessage(null).toString());
                System.out.println(diag.getPosition() + "/" + diag.getColumnNumber());
                System.out.println(diag.getStartPosition());
                System.out.println(diag.getEndPosition());
            }
        }
    }
}
