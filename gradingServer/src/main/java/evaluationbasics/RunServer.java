package evaluationbasics;

import evaluationbasics.Server.EvaluationServer;

import java.util.Arrays;

/**
 * Created by ilias on 29.08.16.
 */
public class RunServer {

    public static void main(String[] args) {
        int port = -1;
        String[] acceptedAddresses = new String[]{};
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
                acceptedAddresses = Arrays.copyOfRange(args,1,args.length);
            } catch (NumberFormatException e) {
                acceptedAddresses = Arrays.copyOfRange(args,0,args.length);
            }
        }
        new EvaluationServer(port,acceptedAddresses);
    }

}
