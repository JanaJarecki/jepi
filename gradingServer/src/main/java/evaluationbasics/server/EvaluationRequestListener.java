package evaluationbasics.server;

import evaluationbasics.exceptions.ERROR_CODE;
import evaluationbasics.evaluators.EvaluationHelper;
import evaluationbasics.xml.XMLConstructor;
import org.jdom2.output.XMLOutputter;

import java.io.IOException;
import java.net.*;

/**
* Socket listener handling client requests if client has access rights.
 */
class EvaluationRequestListener extends Thread {
    private final byte[][] ACCEPTED_BYTE_ADDRESSES;
    private final ServerSocket SERVER_SOCKET;
    private final ConsoleExitListener CONSOLE_LISTENER;

    /**
     * Creates the socket listener and a console listener to timeoutShutdown the server.
     * @param serverSocket socket from which client requests are processed.
     * @param acceptedAddresses list of addresses that are allowed to access the server.
     */
    public EvaluationRequestListener(ServerSocket serverSocket, Iterable<String> acceptedAddresses) {
        ACCEPTED_BYTE_ADDRESSES = IPAddressListBuilder.parseIPAddresses(acceptedAddresses);
        SERVER_SOCKET = serverSocket;
        CONSOLE_LISTENER = new ConsoleExitListener(this);
        try {
            this.setName("EvaluationRequestListener");
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * Event loop accepting clients.
     */
    public void run() {
        Socket client = null;
        CONSOLE_LISTENER.start();
        try {
            while (true) {
                client = SERVER_SOCKET.accept();
                if (checkClientsAccessPermission(client)) {
                    new EvaluationRequest(client).start();
                } else {
                    reportAccessDeniedToClient(client);
                }
            }
        } catch (SocketException e) {
            //server wurde geschlossen
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        shutdownServer();
    }

    /**
     * Shutdown the server and terminates the console listener.
     */
    private void shutdownServer() {
        closeServer();
        if ( CONSOLE_LISTENER.isAlive() )
            CONSOLE_LISTENER.setStopFlag();
    }

    /**
     * reports the client that it has no access permission.
     * @param client
     */
    private void reportAccessDeniedToClient(Socket client) {
        try {
            XMLConstructor message = new XMLConstructor();
            message.error(ERROR_CODE.CLIENT_ADRESS_NOT_ALLOWED);
            EvaluationHelper.setStringToOutputStream(client.getOutputStream(), new XMLOutputter().outputString(message.getDocument()));
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            System.err.println("SE");
            e.printStackTrace();
        }
    }

    /**
     * Tries to detach the server.
     */
    public void closeServer() {
        if (SERVER_SOCKET!=null && !SERVER_SOCKET.isClosed()) {
            try {
                SERVER_SOCKET.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Checks wether the client has the right to access the server.
     * @return
     * @throws UnknownHostException
     */
    private boolean checkClientsAccessPermission(Socket client) throws UnknownHostException {
        return client.getLocalAddress().isLoopbackAddress()
            || isListedIAddress(client.getLocalAddress())
            || client.getLocalAddress().equals(InetAddress.getLocalHost());
    }

    /**
     * Checks if the ip-address is in the list of accepted addresses.
     *
     * @param iAddress the ip to check
     * @return true if the ip is accepted, otherwise false
     */
    private boolean isListedIAddress(InetAddress iAddress) {
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
