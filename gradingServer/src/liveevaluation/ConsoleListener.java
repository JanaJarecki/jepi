package liveevaluation;

/**
 * Aufgabe:
 * Dauerhafte Ueberwachung der Konsole auf Programmabbruchswort: "exit"
 *
 *
 * @author roman
 *
 */
class ConsoleListener extends Thread{
    private final EvaluationRequestListener CLIENTLISTENER;
    private boolean stop=false;
    public ConsoleListener(EvaluationRequestListener pClientListener){
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
