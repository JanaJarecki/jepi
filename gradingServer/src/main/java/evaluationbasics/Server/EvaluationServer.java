package evaluationbasics.Server;

import evaluationbasics.Security.SwitchableSecurityManager;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * In der Klasse ist der Server fuer das Bewertungssystem definiert.
 * 
 * @author Roman Bange
 * @version 1.0
 */
public class EvaluationServer {

	
	//Hier koennen weiterere IPAddressen definiert werden, von denen aus auf das Bewertungssystem zugegriffen werden kann
	private final static String[] ACCEPTED_STR_ADDRESSES= new String[]{
		//"255.255.255.255" - Eingabebeispiel
	};			
	private static final int DEFAULTPORT=1234;
	
	//Dauer fuer die ein Studentthread bearbeitet werden darf (in Millisekunden). siehe StudentTimeoutCounter
	public static final int STUDENTTIMEOUT=20000;

	/**
	 * Konstruktur fuer Serverklasse
	 * 
	 * Aufgaben:
	 * Uebernimmt Port-Management
	 * 	-Automatische Portsuche
	 * 	-Portcheck
	 * Startet SecurityManager
	 * Startet Server
	 * Startet EvaluationRequestListener
	 * 
	 * @param port Gewuenschter Port
	 * @param acceptedAddresses Fuer Clients zu akzeptierende IPAddressen
	 */
	@SuppressWarnings("unused") //aufgrund der StudenttimeoutMeldung
	public EvaluationServer(int port, String [] acceptedAddresses){
		//Portueberpruefung
		if (port == -1 ) port = DEFAULTPORT;
		if((port < 1024 && port != 0) || port > 65535){
			System.err.println("Der eingegebene Parameter ist nicht zulaessig.");
			System.err.println("Geben Sie einen Port zwischen 1024 bis 65535 ein.");
			System.err.println("Eine 0 generiert einen zufaelligen Port.");
			return;
		}		
			
		ServerSocket serverSocket=null;

		try {
			serverSocket = new ServerSocket(port);
			port=serverSocket.getLocalPort();
			System.out.println("Der Server wurde auf Port "+port+" gestartet.");
			System.out.println("Zum Beenden 'exit' eingeben.");
		} catch (Exception e) {
			System.err.println("Server konnte nicht gestartet werden:");
			System.out.println("Port: "+port);
			System.out.println(e);
			serverSocket=null;
		}
		
		if(EvaluationServer.STUDENTTIMEOUT<500) {
			System.out.println("Die erlaubte Bearbeitungsdauer eines EvaluationRequest betraegt unter 0.5 Sekunden.");
			System.out.println("Funktionsweise nicht gewaehrleistet!");
		}

		if(serverSocket!=null){
			List<String> adresses = new ArrayList<String>(Arrays.asList(ACCEPTED_STR_ADDRESSES));
			adresses.addAll(Arrays.asList(acceptedAddresses));
			SwitchableSecurityManager ssm = new SwitchableSecurityManager(port,false);
			System.setSecurityManager(ssm);
			new EvaluationRequestListener(serverSocket,adresses).start();
		}

		
	}
}
