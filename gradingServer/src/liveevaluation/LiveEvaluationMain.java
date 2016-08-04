package liveevaluation;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.jdom2.output.XMLOutputter;

/**
 * In der Klasse ist der Server fuer das Bewertungssystem definiert.
 * 
 * @author Roman Bange
 * @version 1.0
 */
public class LiveEvaluationMain {
	/*
	 * Konstantendeklaration
	 */
	
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
			new LiveEvaluationMain(port,acceptedAddresses);
		}else			
			new LiveEvaluationMain();
	}
	/**
	 * Konstruktor fuer Standardport und Standardaddressen
	 * @see LiveEvaluationMain(int,String[])
	 */
	public LiveEvaluationMain(){
		new LiveEvaluationMain(DEFAULTPORT,ACCEPTED_STR_ADDRESSES);
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
	 * Startet ClientListener
	 * 
	 * @param port Gewuenschter Port
	 * @param acceptedAddresses Fuer Clients zu akzeptierende IPAddressen
	 */
	@SuppressWarnings("unused") //aufgrund der StudenttimeoutMeldung
	public LiveEvaluationMain(int port,String [] acceptedAddresses){
		//Portueberpruefung
		if((port < 1024 && port != 0) || port > 65535){
			System.err.println("Der eingegebene Parameter ist nicht zulaessig. Geben Sie einen Port zwischen 1024 bis 65535 ein. Eine 0 generiert einen zufaelligen Port.");
			return;
		}		
			
		ServerSocket serverSocket=null;		
		try {
			serverSocket = new ServerSocket(port);
			port=serverSocket.getLocalPort();
			System.out.println("Der Server wurde auf Port "+port+" gestartet."+System.getProperty("line.separator")+"Zum Beenden 'exit' eingeben."+System.getProperty("line.separator"));
		} catch (Exception e) {
			System.err.println("Server konnte nicht gestartet werden:"+System.getProperty("line.separator")+"Port: "+port+System.getProperty("line.separator")+e+System.getProperty("line.separator"));
			serverSocket=null;
		}
		
		if(LiveEvaluationMain.STUDENTTIMEOUT<500)
			System.out.println("Die erlaubte Bearbeitungsdauer eines Student betraegt unter 0.5 Sekunden. Funktionsweise nicht gewaehrleistet!");
		
		if(serverSocket!=null){
			System.setSecurityManager(new EvaluationSecurityManager(port));			
			new ClientListener(serverSocket,acceptedAddresses).start();
		}

		
	}		
	
	
	/* 
	 * Threads
	 */	
	
	
	/**
	 * Thread fuer Kernserveraktivitaet
	 * 
	 * Aufgaben:
	 * Starten des ConsoleListener
	 * Warten auf Clients
	 * Ueberpruefen von Clients(IPCheck)
	 * Schliessen des Servers 
	 * 
	 * @author roman
	 */
	private class ClientListener extends Thread{
		private final byte[][]	ACCEPTED_BYTE_ADDRESSES;
		private final ServerSocket SERVER;
		private final ConsoleListener CL;
		public ClientListener(ServerSocket pServer, String[] acceptedAddresses){
			ACCEPTED_BYTE_ADDRESSES= IPAddressListBuilder.parseIPAddresses(acceptedAddresses);	
			SERVER=pServer;
			CL=new ConsoleListener(this);
			//Debug
			try{this.setName("ClientListener");}catch(SecurityException e){e.printStackTrace();}
		}
		
		public void run(){
			Socket client=null;
			CL.start();
			try {
				while(true){
					client=SERVER.accept();
					if(client.getLocalAddress().isLoopbackAddress() || isAcceptedAddress(client.getLocalAddress()) || client.getLocalAddress().equals(InetAddress.getLocalHost()))
						new Student(client).start();
					else{
						try{
							EvaluationHelper.setStringToOutputStream(client.getOutputStream(), new XMLOutputter().outputString(XMLConstructor.errorResponse("104")));
							client.close();
						}catch(IOException e){
							e.printStackTrace();
						}catch (SecurityException e){
							System.err.println("SE");
						}
					}
				}
			} catch(SocketException e){
				//Server wurde geschlossen
			}catch (IOException e) {			
				e.printStackTrace();
			}catch (ClassCastException e){
				e.printStackTrace();
			}
			
			closeServer();
			if(CL.isAlive())
				CL.kill();
				
		}
		/**
		 * Versucht den Server ordnungsgemaess zu schliessen.
		 */
		public void closeServer(){
			if(SERVER!=null){
				if(!SERVER.isClosed())
				try {
					SERVER.close();
					System.out.printf("%s", "Der Server wurde ordnungsgemaess geschlossen."+System.getProperty("line.separator"));
				} catch (IOException e) {e.printStackTrace();}
			}else
				System.console().printf("%s", "Der Server konnte nicht ordnungsgemaess geschlossen werden."+System.getProperty("line.separator"));
		}	
		/**
		 * Die Methode prueft eine IP-Addresse auf ihre Akzeptanz im erlaubten IP-Bereich
		 * 
		 * @param iAddress die zu ueberpruefende IP-Addresse
		 * @return true, sofern die IP-Addresse akzeptiert wird, andernfalls false
		 */
		private boolean isAcceptedAddress(InetAddress iAddress){
			for(byte[] accIAddress: ACCEPTED_BYTE_ADDRESSES)
				try {
					if(InetAddress.getByAddress(accIAddress).equals(iAddress))
						return true;
				} catch (UnknownHostException e) {}
			return false;
		}
	}
	
	/**
	 * Aufgabe:
	 * Dauerhafte Ueberwachung der Konsole auf Programmabbruchswort: "exit"
	 * 
	 * 
	 * @author roman
	 *
	 */
	private class ConsoleListener extends Thread{
		private final ClientListener CLIENTLISTENER;
		private boolean stop=false;
		public ConsoleListener(ClientListener pClientListener){
			CLIENTLISTENER=pClientListener;
			//Debug
			try{this.setName("ConsoleListener");}catch(SecurityException e){e.printStackTrace();}
		}
		/**
		 * Beendet den ConsoleListener
		 */
		public void kill(){
			stop=true;
		}
		public void run(){
			if(System.console()==null)
				System.err.println("Es konnte keine an die Virtual Machine gebundene Konsole gefunden werden."+System.getProperty("line.separator")+"Beenden des Servers ueber die Konsole ist nicht moeglich."+System.getProperty("line.separator"));
			else
				while(!stop)
					if(System.console().readLine().equals("exit")){
						CLIENTLISTENER.closeServer();
						return;
					}
		}
	}

	
}
