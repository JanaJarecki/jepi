package evaluationbasics.server;

import evaluationbasics.security.SwitchableSecurityManager;

import java.net.ServerSocket;
import java.util.Arrays;
import java.util.List;

/**
 * The evaluation server for the Java Evaluation Plugin for ILIAS (JEPI)
 */
public class EvaluationServer {
	/**
	 * Opens a socket on the port, sets the security manager and creates a listener.
	 * @param port port to be used
	 * @param acceptedAddresses accepted addresses for an ILIAS
	 */
	public EvaluationServer(int port, String [] acceptedAddresses){

		SwitchableSecurityManager ssm = new SwitchableSecurityManager(port,false);
		System.setSecurityManager(ssm);

		List<String> adresses = Arrays.asList(acceptedAddresses);

		ServerSocket serverSocket=null;

		try {
			serverSocket = new ServerSocket(port);
			port=serverSocket.getLocalPort();
			System.out.println("Ther server listens on port: "+port);
			System.out.println("Please type 'exit' to stop the server.");
		} catch (Exception e) {
			System.out.println("The server could not be started. Used port was: "+port);
			System.out.println(e);
			serverSocket=null;
		}

		if(serverSocket!=null){
			new EvaluationRequestListener(serverSocket,adresses).start();
		}
	}


	public final static EvaluationServerConfig config = new EvaluationServerConfig();

}

class EvaluationServerConfig {
	// Timeout time for long running solution from students
	public static final int REQUEST_TIMELIMIT =2000;
}
