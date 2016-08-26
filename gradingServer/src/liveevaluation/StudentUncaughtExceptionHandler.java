package liveevaluation;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.Socket;

import org.jdom2.output.XMLOutputter;

public class StudentUncaughtExceptionHandler implements
		UncaughtExceptionHandler {
	
	private final Socket CLIENT;
	public StudentUncaughtExceptionHandler(Socket pClient){
		CLIENT=pClient;
	}
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		System.out.println("StudentUncaughtExceptionHandler.uncaughtException");
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String output=sw.toString();
		try {
			sw.close();
			pw.close();
		} catch (IOException e2) {
			e2.printStackTrace();
		}		
		XMLConstructor response = new XMLConstructor();
		response.error(output);
		
		if(!CLIENT.isClosed())
			//Beendet den Stream ordnungsgemaess mit der Ausgabe einer Error-XML
			try {
				EvaluationHelper.setStringToOutputStream(CLIENT.getOutputStream(),response.getDocument().toString());
				CLIENT.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		
	}

	public static class UselessUncaughtExceptionHandler implements UncaughtExceptionHandler{
		@Override
		public void uncaughtException(Thread t, Throwable e) {}		
	}

}
