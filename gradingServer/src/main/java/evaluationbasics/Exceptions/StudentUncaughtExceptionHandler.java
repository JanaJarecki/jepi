package evaluationbasics.Exceptions;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.Socket;

import evaluationbasics.Evaluators.EvaluationHelper;
import evaluationbasics.XML.XMLConstructor;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class StudentUncaughtExceptionHandler implements
        UncaughtExceptionHandler {

    private final Socket CLIENT;

    public StudentUncaughtExceptionHandler(Socket pClient) {
        CLIENT = pClient;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        System.out.println("StudentUncaughtExceptionHandler.uncaughtException");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String output = sw.toString();
        try {
            sw.close();
            pw.close();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        XMLConstructor response = new XMLConstructor();
        response.error(output);

        System.out.println("# XML xml #############");
        System.out.println(new XMLOutputter(Format.getPrettyFormat()).outputString(response.getDocument()));
        System.out.println("-----------------------");

        if (!CLIENT.isClosed()) {
            try {
                EvaluationHelper.setStringToOutputStream(CLIENT.getOutputStream(), response.getDocument().toString());
                CLIENT.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

    }

}
