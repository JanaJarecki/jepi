import evaluationbasics.compilation.CompilableClass;
import evaluationbasics.compilation.MyMethodWrapper;
import evaluationbasics.compilation.Compilation;


import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestMethodIsCompilable {

    @DataProvider( name = "CodeWithIsCompilableAnnotation")
    public static Object[][] _MyValues() {
        return new Object[][] {
                {"good code", "public int id(int a) {\n  return a;\n}", true, ""},
                {"missing return value", "public id(int a) {\n  return a;\n}", false, ""},
                {"missing parameter type", "public int id(a) {\n  return a;\n}", false, ""},
                {"missing parameter type and missing ; ", "public int id(a) {\n  return a\n}", false, ""},
                {"missing ; ", "public int id(int a) {\n  return a\n}", false, ""},
                {"missing } ", "public int id(int a) {\n  return a;", false, ""}
        };
    }

    @Test
    public void addTest(String msg, String code, String compilable) {
        CompilableClass mmw = new MyMethodWrapper(code);
        Compilation cc = new Compilation(mmw);
        Assert.assertEquals(cc.isCompilable(),compilable);
    }
}
