package liveevaluation;

import java.net.ServerSocket;

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
	 * Startet den Server.
	 * 
	 * Es ist die Uebergabe es gewuenschten Ports sowie die weitere Uebergabe von erwuenschten externen IP-Addressen moeglich.
	 * @param args args[0] Uebergabe des gewuenschten Ports. 0 fuer automatische Suche nach verfuegbaren Port. Array ist beliebig weit auffuellbar mit IP-Addressen.
	 */
	public static void main(String[] args){
		int port;
		String[] acceptedAddresses;
		if(args.length>0){
			try{
				port=Integer.parseInt(args[0]);
				acceptedAddresses=new String[ACCEPTED_STR_ADDRESSES.length+args.length-1];
				System.arraycopy(ACCEPTED_STR_ADDRESSES, 0, acceptedAddresses, 0, ACCEPTED_STR_ADDRESSES.length);
				System.arraycopy(args, 1, acceptedAddresses, ACCEPTED_STR_ADDRESSES.length, args.length-1);
			}catch(NumberFormatException e){
				port=DEFAULTPORT;
				acceptedAddresses=new String[ACCEPTED_STR_ADDRESSES.length+args.length];
				System.arraycopy(ACCEPTED_STR_ADDRESSES, 0, acceptedAddresses, 0, ACCEPTED_STR_ADDRESSES.length);
				System.arraycopy(args, 0, acceptedAddresses, ACCEPTED_STR_ADDRESSES.length, args.length);
			}
			new EvaluationServer(port,acceptedAddresses);
		}else			
			new EvaluationServer();
	}


	/**
	 * Konstruktor fuer Standardport und Standardaddressen
	 * @see EvaluationServer (int,String[])
	 */
	public EvaluationServer(){
		new EvaluationServer(DEFAULTPORT,ACCEPTED_STR_ADDRESSES);
	}

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
			System.setSecurityManager(new EvaluationSecurityManager(port));			
			new EvaluationRequestListener(serverSocket,acceptedAddresses).start();
		}

		
	}
}
