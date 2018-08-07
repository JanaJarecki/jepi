package evaluationbasics.evaluators;

/**
 * Created by ilias on 06.10.16.
 */

import evaluationbasics.xml.XMLConstructor;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeoutException;


public class EvaluationProcessStarter {

    public static Document exec(Element request, String evaluator, final int TIMEOUT)
        throws
        InvocationTargetException,
        IllegalAccessException,
        ClassNotFoundException,
        TimeoutException
    {
        int GRANULARITY = 50;
        int TOTAL_TIME = 0;
        long THREAD_ID = Thread.currentThread().getId();

        String JAVA_HOME = System.getProperty("java.home");
        String CLASSPATH = System.getProperty("java.class.path");
        String SECURITY_FILE_PATH = System.getProperty("java.security.policy");
        String EXECUTABLE = JAVA_HOME + File.separator + "bin" + File.separator + "java";
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            EXECUTABLE += ".exe";
        }

        ProcessBuilder builder = new ProcessBuilder(EXECUTABLE ,
//        "-Xdebug -Xrunjdwp=transport=dt_socket,server=y,suspend=y,address=5005",
            "-cp", CLASSPATH,
            "-Djava.security.policy=" + SECURITY_FILE_PATH,
            evaluator);
        try {
            Process child = builder.start();
            OutputStream output = child.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(output);

            InputStream input = child.getInputStream();

            oos.writeObject(request);
            oos.flush();

            ObjectInputStream ois = new ObjectInputStream(input);

            while ( (TOTAL_TIME < TIMEOUT) && (input.available() == 0)) {
                try {
                    Thread.sleep(GRANULARITY);
                } catch (InterruptedException e) {
                }
                TOTAL_TIME += GRANULARITY;
            }

            try {
                if (input.available() != 0) {
                    try {
                        Document response = (Document) ois.readObject();
                        return response;
                    } catch (IOException e) {
                        XMLConstructor xml = new XMLConstructor();
                        xml.error("could not read back Object", e);
                        return xml.getDocument();
                    }
                } else {
                    throw new TimeoutException("timed out after " + TOTAL_TIME + "ms");
                }
            } finally {
                child.destroy();
                child.destroyForcibly();
            }
        } catch (IOException e) {
            XMLConstructor xml = new XMLConstructor();
            xml.error("could not start builder", e);
            return xml.getDocument();
        }
    }

}