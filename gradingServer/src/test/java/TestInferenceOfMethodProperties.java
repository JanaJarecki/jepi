
import evaluationbasics.analysis.ASTHelper;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;


public class TestInferenceOfMethodProperties {

    @DataProvider( name = "FunctionClassesWithCode")
    public static Object[][] _MyValues() {
        return new Object[][]{
                        {"non-recursive, non-iterative method", "public int id(int a) { return a; }", new boolean[]{false, false}},
                        {"recursive, non-iterative method", "public int id(int a) { if (a==1) return 1; return id(a-1); }", new boolean[]{true, false}},
                        {"non-recursive, iterative method", "public int id(int a) { int sum = 0; for(int i=0;i<a;++i) sum+=a; return sum; }", new boolean[]{false, true}},
                        {"recursive, iterative method", "public int id(int a) { if (a==1) return 1; int sum = 0; for(int i=0;i<a;++i) sum+=a; return sum+id(a-1); }", new boolean[]{true, true}},
        };
    }

    @Test( dataProvider = "FunctionClassesWithCode")
    public void addTest(String code, boolean[] expected) {
        Map<String, Boolean> type;
        try {
            type = ASTHelper.getMethodType(code);
        } catch (Exception e) {
            throw new RuntimeException("Test failed");
        }
        Assert.assertEquals((boolean)type.get("recursive"), expected[0]);
        Assert.assertEquals((boolean)type.get("loop"), expected[1]);
    }
}