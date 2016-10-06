package evaluationbasics.Server;

import java.io.IOException;
import java.io.StringReader;
import java.net.Socket;

import evaluationbasics.Evaluators.FunctionEvaluator;
import evaluationbasics.Evaluators.TestNGEvaluator;
import evaluationbasics.Exceptions.DummyExceptionHandler;
import evaluationbasics.Exceptions.StudentUncaughtExceptionHandler;
import evaluationbasics.Exceptions.ERROR_CODE;
import evaluationbasics.Evaluators.EvaluationHelper;
import evaluationbasics.XML.XMLConstructor;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;


//import evaluationbasics.ASTHelper;


public class EvaluationRequest extends Thread {

    private final Socket CLIENT;
    private final StudentTimeoutCounter TIMEOUTCOUNTER;
    //private final String[] forbiddenMethods={"exit","gc","setSecurityManager","clearProperty","inheritedChannel","load","loadLibrary","setErr","setIn","setOut","setProperties","setProperty" //System
    //										   ,"sendResponse","connect","bind","getChannel","getInputStream","getOutputStream" //Socket
    //										   ,"accept","getInetAddress","setSocketFactory"}; //ServerSocket


    public EvaluationRequest(Socket client) {
        CLIENT = client;
        this.setUncaughtExceptionHandler(new StudentUncaughtExceptionHandler(client));
        TIMEOUTCOUNTER = new StudentTimeoutCounter(this);
    }


    /**
     * The run method handles the incoming request and dispatches to the correct function for the question type.
     */
    public final void run() {
        TIMEOUTCOUNTER.start();

        SAXBuilder builder = new SAXBuilder();
        Document request;
        Document response = null;

        try {
            String str = EvaluationHelper.getStringFromInputStream(CLIENT.getInputStream());
            request = builder.build(new StringReader(str));

//        System.out.println();
//        System.out.println("#######################");
//        System.out.println("# XML request #########");
//        System.out.println(new XMLOutputter(Format.getPrettyFormat()).outputString(request));

            Element eRoot = request.getRootElement();
            Element eType = eRoot.getChild("type");

            switch (eType.getValue()) {
                case "function_original":
                    response = FunctionEvaluator.eval(eRoot);
                    break;

                case "testng":
                    response = TestNGEvaluator.eval(eRoot);
                    break;

                default:
                    XMLConstructor writer = new XMLConstructor();
                    writer.error(ERROR_CODE.QUESTION_TYPE_NOT_KNOWN);
                    response = writer.getDocument();
            }
        } catch (JDOMException e) {
            XMLConstructor writer = new XMLConstructor();
            writer.error(ERROR_CODE.XML_PARSING_ERROR);
            response = writer.getDocument();
        } catch (IOException e) {
            XMLConstructor writer = new XMLConstructor();
            writer.error(ERROR_CODE.INPUTSTREAM_IO_ERROR);
            response = writer.getDocument();
        } finally {
            if (response == null || response.getContentSize() == 0) {
                XMLConstructor writer = new XMLConstructor();
                writer.error(ERROR_CODE.COULD_NOT_CREATE_RESPONSEXML);
                response = writer.getDocument();
            }
            sendResponse(response);
        }
    }

    protected final void sendResponse(Document pReturnDoc) {

        System.out.println("- XML xml --------");
        System.out.println(new XMLOutputter(Format.getPrettyFormat()).outputString(pReturnDoc));
        System.out.println("#######################");

        try {
            EvaluationHelper.setStringToOutputStream(CLIENT.getOutputStream(), new XMLOutputter().outputString(pReturnDoc));
            if (!CLIENT.isClosed())
                CLIENT.close();
            TIMEOUTCOUNTER.kill();
        } catch (IOException e) {
            //Fehler
        }
    }

    public final void kill() {
        if (!CLIENT.isClosed()) {

            XMLConstructor writer = new XMLConstructor();
            writer.error(ERROR_CODE.TIMEOUT);
            setUncaughtExceptionHandler(new DummyExceptionHandler());
            sendResponse(writer.getDocument());
        }
    }

}
