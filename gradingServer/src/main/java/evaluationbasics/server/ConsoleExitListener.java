package evaluationbasics.server;

/**
 * Listens the console if the user supplies 'exit' to terminate the server.
 *
 */
class ConsoleExitListener extends Thread{
    private final EvaluationRequestListener CLIENTLISTENER;
    private boolean stopFlag =false;

    public ConsoleExitListener(EvaluationRequestListener pClientListener){
        CLIENTLISTENER = pClientListener;
        try {
            this.setName("ConsoleExitListener");
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void setStopFlag(){
        stopFlag =true;
    }

    public void run(){
        if(System.console()==null) {
            System.err.println("No running console could be detected.");
            System.err.println("The server can not be stopped using the console.");
            return;
        }
        while (!stopFlag) {
            if (System.console().readLine().equals("exit")) {
                CLIENTLISTENER.closeServer();
                return;
            }
        }
    }
}
