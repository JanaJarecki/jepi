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
            throws InvocationTargetException, IllegalAccessException, ClassNotFoundException, TimeoutException {
        int GRANULARITY = 50;
        String JAVA_CMD = System.getenv("JAVA_HOME");
        String CLASSPATH = System.getProperty("java.class.path");
        String CURRENTDIR = System.getProperty("user.dir");

        ProcessBuilder builder = new ProcessBuilder(JAVA_CMD + File.separator + "bin" + File.separator + "java",
//                    "-Xdebug -Xrunjdwp=transport=dt_socket,server=y,suspend=y,address=5005",
                "-cp", CLASSPATH,
                "-Djava.security.policy=" + CURRENTDIR + File.separator + "security.policy",
                evaluator);
        try {
            Process child = builder.start();
            try {
                OutputStream output = child.getOutputStream();
                try {
                    ObjectOutputStream oos = new ObjectOutputStream(output);
                    try {
                        oos.writeObject(request);
                        oos.flush();

                        InputStream input = child.getInputStream();
                        try {
                            ObjectInputStream ois = new ObjectInputStream(input);
                            int total = 0;
                            try {
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
                                            return response;
                                        } catch (IOException e) {
                                            XMLConstructor xml = new XMLConstructor();
                                            xml.error("could not read back Object",e);
                                            return xml.getDocument();
                                        }
                                    } else {
                                        throw new TimeoutException("timed out after " + TIMEOUT + "ms");
                                    }
                                } catch (IOException e) {
                                    XMLConstructor xml = new XMLConstructor();
                                    xml.error("could not look for availability",e);
                                    return xml.getDocument();
                                }
                            } catch (IOException e) {
                                XMLConstructor xml = new XMLConstructor();
                                xml.error("could not look for availability - while",e);
                                return xml.getDocument();
                            }
                        } catch (IOException e) {
                            XMLConstructor xml = new XMLConstructor();
                            xml.error("could not create new objectInputStream",e);
                            return xml.getDocument();
                        }

                    } catch (IOException e) {
                        XMLConstructor xml = new XMLConstructor();
                        xml.error("could not write object to stream and flush",e);
                        return xml.getDocument();
                    }

                } catch (IOException e) {
                    XMLConstructor xml = new XMLConstructor();
                    xml.error("could not creatre objectOutputStream",e);
                    return xml.getDocument();
                }
            } finally {
                child.destroy();
            }
        } catch (IOException e) {
            XMLConstructor xml = new XMLConstructor();
            xml.error("could not start builder",e);
            return xml.getDocument();
        }
        }

    }