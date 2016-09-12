import evaluationbasics.Security.SwitchableSecurityManager;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Created by ilias on 29.08.16.
 */
public class TestClass {
//
//       public static void main( String... args) {
//           System.out.println("starting test");
//           SwitchableSecurityManager ssm = new SwitchableSecurityManager(1234,false);
//           System.setSecurityManager(ssm);
//
//           SwitchableSecurityManager secman = (SwitchableSecurityManager) System.getSecurityManager();
//           secman.enable();
//
//           PrintStream sysout = System.out;
//           ByteArrayOutputStream methodOutput = new ByteArrayOutputStream();
//           PrintStream printStream = new PrintStream(methodOutput);
//           try {
//               System.setOut(printStream);
//           } catch (Exception e) {
//
//           } finally {
//               secman.disable();
//               System.setOut(sysout);
//           }
//           secman.enable();
//           System.setOut(printStream);
//
//           System.out.println("end of test");
//       }

       public static void main( String ... args) {

       }


}


class MyClass {
    private int value = 42;
    public int getInt() { return value; }
}