package liveevaluation;

import java.io.IOException;
import java.io.StringReader;
import java.net.Socket;

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


    private XMLConstructor xml;

    public EvaluationRequest(Socket client) {

        xml = new XMLConstructor();
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
        Document doc;

        try {
            String str = EvaluationHelper.getStringFromInputStream(CLIENT.getInputStream());
            doc = builder.build(new StringReader(str));

        } catch (JDOMException e) {
            xml.error(ERROR_CODE.XML_PARSING_ERROR);
            sendResponse(xml.getDocument());
            return;
        } catch (IOException e) {
            xml.error(ERROR_CODE.INPUTSTREAM_IO_ERROR);
            sendResponse(xml.getDocument());
            return;
        }

        System.out.println();
        System.out.println("#######################");
        System.out.println("# XML request #########");
        System.out.println(new XMLOutputter(Format.getPrettyFormat()).outputString(doc));

        Element eRoot = doc.getRootElement();
        Element eType = eRoot.getChild("type");

        switch (eType.getValue()) {
            case "function_original":
                FunctionEvaluator.eval(eRoot, xml);
                break;

            case "testng":
                TestNGEvaluator.eval(eRoot, xml);
                break;

            default:
                xml.error(ERROR_CODE.QUESTION_TYPE_NOT_KNOWN);
        }

        //@todo check for nullpointer
        if ( xml == null) {
            System.out.println("xml");
        }
        if ( xml.getDocument() == null ) {
            System.out.println("document");
        }
        if ( xml.getDocument().getContentSize() == 0) {
            xml.error(ERROR_CODE.COULD_NOT_CREATE_RESPONSEXML);
        }

        sendResponse(xml.getDocument());
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
            xml.error(ERROR_CODE.TIMEOUT);
            this.setUncaughtExceptionHandler(new StudentUncaughtExceptionHandler.UselessUncaughtExceptionHandler());
            this.sendResponse(xml.getDocument());
        }
    }

}
