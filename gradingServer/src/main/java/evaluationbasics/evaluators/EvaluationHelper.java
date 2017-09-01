package evaluationbasics.evaluators;

import evaluationbasics.exceptions.NoValidClassException;
import evaluationbasics.exceptions.WrongNumberOfParametersException;
import evaluationbasics.exceptions.WrongNumberOfProvidedJavaElementsException;
import evaluationbasics.reports.DiagnostedClass;
import evaluationbasics.reports.DiagnostedMethodClass;
import evaluationbasics.security.SwitchableSecurityManager;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.rmi.activation.UnknownObjectException;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;


public class EvaluationHelper {


  private static Object saveExecution(Method method, Object instance, Object arg) throws InvocationTargetException, IllegalAccessException, IOException, ClassNotFoundException, TimeoutException {
    SwitchableSecurityManager sm = (SwitchableSecurityManager) System.getSecurityManager();
    sm.enable();
    Object result;
    try {
      result = method.invoke(instance, arg);
    } finally {
      sm.disable();
    }
    return result;

//        return EvaluationProcessStarter.exec(20000,method,instance,arg);
  }

  private static Object saveExecution(Method method, Object instance, Object[] args) throws InvocationTargetException, IllegalAccessException, IOException, ClassNotFoundException, TimeoutException {

    SwitchableSecurityManager sm = (SwitchableSecurityManager) System.getSecurityManager();
    sm.enable();
    Object result;
    try {
      result = method.invoke(instance, args);
    } finally {
      sm.disable();
    }
    return result;

//        return EvaluationProcessStarter.exec(20000,method,instance,args);
  }

  static Object runMainMethodWithParams(DiagnostedClass dc, String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException, ClassNotFoundException, TimeoutException {
    assert (dc.isValidClass());

    PrintStream systemOut = System.out;

    ByteArrayOutputStream methodOutput = new ByteArrayOutputStream();
    PrintStream printStream = new PrintStream(methodOutput);

    try {
      Method main = dc.getAsClass().getMethod("main", String[].class);
      if (!Modifier.isStatic(main.getModifiers())) {
        throw new NoSuchMethodException("Could not find a static main method.");
      }
      System.setOut(printStream);
      saveExecution(main, null, (Object) args);
    } finally {
      System.setOut(systemOut);
    }
    return methodOutput.toString();
  }


  static Object runMethodOnParams(DiagnostedMethodClass dcMethod, Object[] pTestArgs) throws NumberFormatException, UnknownObjectException, WrongNumberOfParametersException, NoValidClassException {
    if (dcMethod.isValidClass()) {
      Class<?>[] pClassType = dcMethod.getMainMethod().getParameterTypes();
      if (pClassType.length > 0 && pTestArgs == null) {
        throw new WrongNumberOfParametersException("Expected number of parameters: " + pClassType.length + " but none provided.");
      } else if (pClassType.length != pTestArgs.length) {
        throw new WrongNumberOfParametersException("Expected number of parameters: " + pClassType.length + " but " + pTestArgs.length + " provided.");
      } else {
        for (int i = 0; i < pTestArgs.length; i++) {
          try {
            pTestArgs[i] = EvaluationHelper.stringToType(pClassType[i], pTestArgs[i].toString());
          } catch (NumberFormatException e) {
            throw new NumberFormatException("Exception when parsing parameter[" + i + "]: Could not parse string \"" + pTestArgs[i].toString() + "\" to a number.");
          }
        }
      }
      try {
        return callMethod(dcMethod, pTestArgs);
      } catch (InvocationTargetException e) {
        return e.getTargetException();
      } catch (Exception e) {
        return e;
      }
    }
    throw new NoValidClassException("Given DiagnostedMethodClass is not valid");
  }

  public static Object runInstanceMethod(Class<?> clazz, String methodName, Object[] args) throws WrongNumberOfProvidedJavaElementsException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException, ClassNotFoundException, TimeoutException {
//        assert (dc.isValidClass());
    if (args == null) {
      args = new Object[]{};
    }
    Method method = findMethod(clazz.getMethods(), methodName, args);
    return saveExecution(method, null, args);
  }

  public static Method findMethod(Method[] methods, String name, Object[] args) throws WrongNumberOfProvidedJavaElementsException {
    for (Method m : methods) {
      String methodName = m.getName();
      if (methodName.equals(name)) {
        Class<?>[] parameterTypes = m.getParameterTypes();
        try {
          for (int i = 0; i < args.length; i++) {
            parameterTypes[i].cast(args[i]);
          }
          return m;
        } catch (ClassCastException ignored) {
        }
      }
    }
    throw new WrongNumberOfProvidedJavaElementsException("Could not find class " + name + "with parameters:\n\t" + Arrays.stream(args).map(x -> x.getClass().getSimpleName()).collect(Collectors.joining("\n\t")));
  }

  /*
   * Die Methode versucht einen als String gegebenen Wert in einen spezifischen Typen umzuwandeln.
   * Bisher unterstuetze Datentypen:
   * 	Eindemensinale Arrays unterstuetzter Datentypen
   *	Byte
   *	Double
   *	Short
   *	Integer
   *	Long
   *	Float
   *	Double
   *	String
   *	Boolean
   *
   * Sofern der Typ nicht unterstuetzt wird, wird der String zurueck gegeben.
   *
   * Parameters:
   * 		pClass - Typ in den der String verwandelt werden soll
   * 		pParam - String in welcher umgewandelt werden soll
   */
  private static Object stringToType(Class<?> pClass, String pParam) throws UnknownObjectException, NumberFormatException {
    if (pClass.isArray()) {
      String[] sArray = pParam.split("~");
      Object objArray = Array.newInstance(pClass.getComponentType(), sArray.length);
      for (int i = 0; i < sArray.length; i++)
        Array.set(objArray, i, stringToType(pClass.getComponentType(), sArray[i]));
      return objArray;
    } else if (pClass == Byte.class || pClass == byte.class) {
      return Byte.parseByte(pParam);
    } else if (pClass == Character.class || pClass == char.class) {
      return pParam.charAt(0);
    } else if (pClass == Short.class || pClass == short.class) {
      return Short.parseShort(pParam);
    } else if (pClass == Integer.class || pClass == int.class) {
      return Integer.parseInt(pParam);
    } else if (pClass == Long.class || pClass == long.class) {
      return Long.parseLong(pParam);
    } else if (pClass == Float.class || pClass == float.class) {
      return Float.parseFloat(pParam);
    } else if (pClass == Double.class || pClass == double.class) {
      return Double.parseDouble(pParam);
    } else if (pClass == String.class) {
      return pParam;
    } else if (pClass == Boolean.class || pClass == boolean.class) {
      return Boolean.parseBoolean(pParam);
    } else {
      throw new UnknownObjectException("Do not know how to parse an object from a string with type: " + pClass.getName());
    }
  }

  public static String deepToString(Object e1) {
    String s = "";
    if (e1 instanceof Object[] )
      s = Arrays.deepToString((Object[]) e1);
    else if (e1 instanceof byte[] )
      s = Arrays.toString((byte[]) e1);
    else if (e1 instanceof short[] )
      s = Arrays.toString((short[]) e1);
    else if (e1 instanceof int[] )
      s = Arrays.toString((int[]) e1);
    else if (e1 instanceof long[] )
      s = Arrays.toString((long[]) e1);
    else if (e1 instanceof char[] )
      s = Arrays.toString((char[]) e1);
    else if (e1 instanceof float[] )
      s = Arrays.toString((float[]) e1);
    else if (e1 instanceof double[] )
      s = Arrays.toString((double[]) e1);
    else if (e1 instanceof boolean[] )
      s = Arrays.toString((boolean[]) e1);
    else
      s = e1.toString();
    return s;
  }


  /*
   * Streambehandlung
   */
  public static void setStringToOutputStream(OutputStream out, String output) {
    try {
      out.write(output.getBytes());
      out.flush();
    } catch (IOException e) {
      // @todo how-to handle which errors?
    }
  }


  /**
   * Reads a complete String from an InputStream.
   *
   * @param in
   * @return
   */
  public static String getStringFromInputStream(InputStream in) {
    final int ACCEPT_NBYTE = 1000;
    byte[] b = new byte[ACCEPT_NBYTE];
    StringBuilder sReturn = new StringBuilder();
    int i = 0;
    try {
      while (i != -1) {
        i = in.read(b);
        if (i > 0) {
          byte[] relevant = Arrays.copyOf(b, i);
          sReturn.append(new String(relevant));
          if (i < ACCEPT_NBYTE) {
            break;
          }
        }
      }
    } catch (IOException e) {
      // @todo how-to handle which errors?
    }
    return sReturn.toString();
  }


  /**
   * Diese Methode fuehrt eine Methode unter gegebenen Parametern aus. Dies geschieht unter der gegebenen Instanz.
   * Die Methode gibt im Erfolgsfall den return-Wert der zu aufrufenden Methode zurueck.
   * Falls keine Parameter erforderlich sind kann entweder eine leere ObjectArray oder null uebergeben werden.
   * Es ist moeglich eine Methode mit den Modifiern static/private zu uebergeben und aufzurufen. Fuer den Modififer "private" muss
   * die ReflectPermission("suppressAccessChecks") gewaehrt werden.
   * <p>
   * Die einschraenkende Anmerkung des Aequivalent mit der DiagnostedClass gilt hier aufgrund der Eigenschaften einer DiagnostedMethodClass nicht.
   *
   * @param dClass         DiagnostedClass Objekt welches von der compileClass()-Methode zurueckgegeben wurde
   * @param pClassInstance Instanz der Klasse
   * @param pMethodArgs    Array der zu uebergebenen Parameter
   * @return Rueckgabeobjekt der Methode
   */
  private static Object callMethodOnInstance(DiagnostedMethodClass dClass, Object pClassInstance, Object[] pMethodArgs)
      throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, SecurityException, NoValidClassException, IOException, ClassNotFoundException, TimeoutException {
    if (pMethodArgs == null) pMethodArgs = new Object[]{};
    if (dClass.isValidClass()) {
      Method method = dClass.getMainMethod();
      if (Modifier.isStatic(method.getModifiers()))
        pClassInstance = null;
      if (!method.isAccessible())
        method.setAccessible(true);
      return saveExecution(method, pClassInstance, pMethodArgs);
    }
    throw new NoValidClassException("Given Diagnosted Class is not valid.");
  }

  /**
   * siehe callMethodOnInstance(DiagnostedMethodClass,Object,Object[])
   * Bei dieser Methode wird anstatt eine gegebene Instanz zu verwenden. Eine neue erstellt.
   *
   * @param dClass      DiagnostedMethodClass Objekt welches von der compileMethod()-Methode zurueckgegeben wurde
   * @param pMethodArgs Array der zu uebergebenen Parameter
   * @return Rueckgabeobjekt der Methode
   * @throws Exception Fuer die genaue Listung der exceptions siehe callMethodonInstance
   */
  private static Object callMethod(DiagnostedMethodClass dClass, Object[] pMethodArgs) throws Exception {
    return callMethodOnInstance(dClass, dClass.getNewInstance(), pMethodArgs);
  }
}
