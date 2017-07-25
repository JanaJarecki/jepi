package evaluationbasics.Evaluators;

/**
 * Created by ilias on 06.10.16.
 */

import evaluationbasics.XML.XMLConstructor;
import org.jdom2.Document;
import org.jdom2.Element;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeoutException;


public class EvaluationProcess {

  public static Document exec(Element request, String evaluator, final int TIMEOUT)
      throws
      InvocationTargetException,
      IllegalAccessException,
      ClassNotFoundException,
      TimeoutException
  {
    int GRANULARITY = 50;
    String JAVA_CMD = System.getenv("JAVA_HOME");
    String CLASSPATH = System.getProperty("java.class.path");
    String CURRENTDIR = System.getProperty("user.dir");
    String SECURITY_FILE_PATH = System.getProperty("java.security.policy");

    ProcessBuilder builder = new ProcessBuilder(JAVA_CMD + File.separator + "bin" + File.separator + "java",
//        "-Xdebug -Xrunjdwp=transport=dt_socket,server=y,suspend=y,address=5005",
        "-cp", CLASSPATH,
        "-Djava.security.policy=" + SECURITY_FILE_PATH,
        evaluator);
    try {
      Process child = builder.start();
      OutputStream output = child.getOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(output);
      oos.writeObject(request);
      oos.flush();

      InputStream input = child.getInputStream();
      ObjectInputStream ois = new ObjectInputStream(input);
      int total = 0;
      while (total < TIMEOUT && input.available() == 0) {
        try {
          Thread.sleep(GRANULARITY);
        } catch (InterruptedException e) {
        }
        total = total + GRANULARITY;
      }
      try {
        if (input.available() != 0) {
          try {
            Document response = (Document) ois.readObject();
            String studentsOutput = (String) ois.readObject();
            System.out.println(studentsOutput);
            return response;
          } catch (IOException e) {
            XMLConstructor xml = new XMLConstructor();
            xml.error("could not read back Object", e);
            return xml.getDocument();
          }
        } else {
          throw new TimeoutException("timed out after " + TIMEOUT + "ms");
        }
      } finally {
        child.destroy();
      }
    } catch (IOException e) {
      XMLConstructor xml = new XMLConstructor();
      xml.error("could not start builder", e);
      return xml.getDocument();
    }
  }

}