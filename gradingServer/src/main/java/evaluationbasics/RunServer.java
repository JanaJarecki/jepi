package evaluationbasics;

import evaluationbasics.server.EvaluationServer;

import java.util.Arrays;

/**
 * Created by ilias on 29.08.16.
 */
public class RunServer {

  public static void main(String[] args) {
    int port = 8088;
    String[] acceptedAddresses = new String[]{};

    // try to parse a port number and allowed addresses
    if (args.length > 0) {
      try {
        port = Integer.parseInt(args[0]);
        acceptedAddresses = Arrays.copyOfRange(args, 1, args.length);
      } catch (NumberFormatException e) {
        acceptedAddresses = Arrays.copyOfRange(args, 0, args.length);
      }
    }

    // check supplied port
    if ((port < 1024 && port != 0) || port > 65535) {
      System.err.println("The port number " + port + " is not allowed.");
      System.err.println("Please provide a port between 1024 and 65535.");
      return;
    }

    new EvaluationServer(port, acceptedAddresses);
  }

}
