package evaluationbasics.Server;

import evaluationbasics.Exceptions.ERROR_CODE;
import evaluationbasics.Evaluators.EvaluationHelper;
import evaluationbasics.XML.XMLConstructor;
import org.jdom2.output.XMLOutputter;

import java.io.IOException;
import java.net.*;

/**
 * Thread fuer Kernserveraktivitaet
 * <p>
 * Aufgaben:
 * Starten des ConsoleListener
 * Warten auf Clients
 * Ueberpruefen von Clients(IPCheck)
 * Schliessen des Servers
 *
 * @author roman
 */
class EvaluationRequestListener extends Thread {
    private final byte[][] ACCEPTED_BYTE_ADDRESSES;
    private final ServerSocket SERVER;
    private final ConsoleListener CL;

    public EvaluationRequestListener(ServerSocket pServer, Iterable<String> acceptedAddresses) {
        ACCEPTED_BYTE_ADDRESSES = IPAddressListBuilder.parseIPAddresses(acceptedAddresses);
        SERVER = pServer;
        CL = new ConsoleListener(this);
        //Debug
        try {
            this.setName("EvaluationRequestListener");
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        Socket client = null;
        CL.start();
        try {
            while (true) {
                client = SERVER.accept();
                if (client.getLocalAddress().isLoopbackAddress()
                        || isAcceptedAddress(client.getLocalAddress())
                        || client.getLocalAddress().equals(InetAddress.getLocalHost()) ) {
                    new EvaluationRequest(client).start();
                } else {
                    try {
                        XMLConstructor response = new XMLConstructor();
                        response.error(ERROR_CODE.CLIENT_ADRESS_NOT_ALLOWED);
                        EvaluationHelper.setStringToOutputStream(client.getOutputStream(), new XMLOutputter().outputString(response.getDocument()));
                        client.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (SecurityException e) {
                        System.err.println("SE");
                    }
                }
            }
        } catch (SocketException e) {
            //Server wurde geschlossen
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }

        closeServer();
        if (CL.isAlive())
            CL.kill();

    }

    /**
     * Versucht den Server ordnungsgemaess zu schliessen.
     */
    public void closeServer() {
        if (SERVER != null) {
            if (!SERVER.isClosed())
                try {
                    SERVER.close();
                    System.out.println("Der Server wurde ordnungsgemaess geschlossen.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
        } else {
            System.out.println("Der Server konnte nicht ordnungsgemaess geschlossen werden.");
        }
    }

    /**
     * Die Methode prueft eine IP-Addresse auf ihre Akzeptanz im erlaubten IP-Bereich
     *
     * @param iAddress die zu ueberpruefende IP-Addresse
     * @return true, sofern die IP-Addresse akzeptiert wird, andernfalls false
     */
    private boolean isAcceptedAddress(InetAddress iAddress) {
        for (byte[] accIAddress : ACCEPTED_BYTE_ADDRESSES) {
            try {
                if (InetAddress.getByAddress(accIAddress).equals(iAddress))
                    return true;
            } catch (UnknownHostException e) {
            }
        }
        return false;
    }
}
