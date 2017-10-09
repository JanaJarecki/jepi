package evaluationbasics.server;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import evaluationbasics.evaluators.EvaluationProcessStarter;
import evaluationbasics.evaluators.FunctionEvaluator;
import evaluationbasics.evaluators.TestNGEvaluator;
import evaluationbasics.exceptions.PrintToSystemErrorExceptionHandler;
import evaluationbasics.exceptions.StudentUncaughtExceptionHandler;
import evaluationbasics.exceptions.ERROR_CODE;
import evaluationbasics.evaluators.EvaluationHelper;
import evaluationbasics.xml.XMLConstructor;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;


/**
 * Handle any request in a separate thread.
 */
public class EvaluationRequest extends Thread {

    private final Socket CLIENT;
    private final RequestTimer TIMEOUTCOUNTER;
    //private final String[] forbiddenMethods={"exit","gc","setSecurityManager","clearProperty","inheritedChannel","load","loadLibrary","setErr","setIn","setOut","setProperties","setProperty" //System
    //										   ,"sendResponse","connect","bind","getChannel","getInputStream","getOutputStream" //Socket
    //										   ,"accept","getInetAddress","setSocketFactory"}; //ServerSocket

    public EvaluationRequest(Socket client) {
        CLIENT = client;
        this.setUncaughtExceptionHandler(new StudentUncaughtExceptionHandler(client));
        TIMEOUTCOUNTER = new RequestTimer(this);
    }


    /**
     * The run method handles the incoming request and dispatches to the correct function for the question type.
     */
    public final void run() {
        TIMEOUTCOUNTER.start();

        SAXBuilder builder = new SAXBuilder();
        Document request;
        Document response = null;
        XMLConstructor writer = new XMLConstructor();

        try {
            String str = EvaluationHelper.getStringFromInputStream(CLIENT.getInputStream());
            request = builder.build(new StringReader(str));

            System.out.println("### XML-Request ###\n");
            System.out.println(new XMLOutputter(Format.getPrettyFormat()).outputString(request));
            System.out.println("\n--- END ---\n");

            Element eRoot = request.getRootElement();
            Element eType = eRoot.getChild("type");

            switch (eType.getValue()) {
                case "function_original":
//          response = FunctionEvaluator.processRequestInMainThread(eRoot); // for debugging
                    response = EvaluationProcessStarter.exec(eRoot,"evaluationbasics.evaluators.FunctionEvaluator",EvaluationServerConfig.REQUEST_TIMELIMIT);
                    break;

                case "testng":
//          response = TestNGEvaluator.processRequestInMainThread(eRoot); // for debugging
                    response = EvaluationProcessStarter.exec(eRoot,"evaluationbasics.evaluators.TestNGEvaluator",EvaluationServerConfig.REQUEST_TIMELIMIT);
                    break;

                default:
                    writer.error(ERROR_CODE.QUESTION_TYPE_NOT_KNOWN);
            }
        } catch (JDOMException e) {
            writer.error(ERROR_CODE.XML_PARSING_ERROR,e);
        } catch ( TimeoutException e) {
            writer.error(ERROR_CODE.TIMEOUT);
        } catch (IOException e) {
            writer.error("Problem with client input stream"+"\n"+e.toString());//ERROR_CODE.INPUTSTREAM_IO_ERROR);
        } catch ( InvocationTargetException e) {
        } catch ( IllegalAccessException e ) {
        } catch ( ClassNotFoundException e ) {
        } catch ( Exception e) {
        } finally {
            if ( writer.getDocument().getContentSize() == 0) {
                writer.error(ERROR_CODE.COULD_NOT_CREATE_RESPONSEXML);
            }
            if (response == null) {
                response = writer.getDocument();
            }
        }

        sendResponse(response);
    }

    protected final void sendResponse(Document response) {
        System.out.println("@@@ XML-Response @@@\n");
        System.out.println(new XMLOutputter(Format.getPrettyFormat()).outputString(response));
        System.out.println("\n--- END ---\n");

        try {
            EvaluationHelper.setStringToOutputStream(CLIENT.getOutputStream(), new XMLOutputter().outputString(response));
            if (!CLIENT.isClosed())
                CLIENT.close();
            TIMEOUTCOUNTER.askToStop();
        } catch (IOException e) {
            //Fehler
        }
    }

    public final void timeoutShutdown() {
        if (!CLIENT.isClosed()) {
            XMLConstructor writer = new XMLConstructor();
            writer.error(ERROR_CODE.TIMEOUT);
            setUncaughtExceptionHandler(new PrintToSystemErrorExceptionHandler());

            sendResponse(writer.getDocument());
        }
    }

}
