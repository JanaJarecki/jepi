package evaluationbasics.server;

import evaluationbasics.exceptions.PrintToSystemErrorExceptionHandler;

/**
 * RequestTimer tries to isStopped or askToStop the evaluation request after a given time.
 */
public class RequestTimer extends Thread {

  private final EvaluationRequest evaluationRequest;
  private final long TIMELIMIT = EvaluationServer.config.REQUEST_TIMELIMIT;
  private final long TIMEFRAME = TIMELIMIT / 20;

  private boolean isStopped = false;

  /**
   * Creates new RequestTimer.
   * @param evaluationRequest
   */
  public RequestTimer(EvaluationRequest evaluationRequest){
    this.setUncaughtExceptionHandler(new PrintToSystemErrorExceptionHandler());
    this.evaluationRequest = evaluationRequest;
  }

  @SuppressWarnings("deprecation")
  public void run(){
    long deadline = System.currentTimeMillis() + TIMELIMIT;

    while(!isStopped && (System.currentTimeMillis() < deadline) ){
      try {
        sleep(TIMEFRAME);
      } catch (InterruptedException e) {}
    }

    evaluationRequest.timeoutShutdown();
    try {
      sleep(2000);
    } catch (InterruptedException e) {}

    if(evaluationRequest.isAlive()) {
      // todo: find replacement for deprecated method call
      evaluationRequest.stop();
    }

  }

  /**
   * Gibt ein Zeichen zur sanften Beendigung des TimeoutCounter
   */
  public void askToStop(){
    isStopped = true;
  }

}
