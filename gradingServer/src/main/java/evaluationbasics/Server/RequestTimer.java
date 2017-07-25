package evaluationbasics.Server;

import evaluationbasics.Exceptions.PrintToSystemErrorExceptionHandler;

/**
 * RequestTimer tries to isStopped or askToStop the evaluation request after a given time.
 */
public class RequestTimer extends Thread{
	private final EvaluationRequest evaluationRequest;
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
		long start=System.currentTimeMillis();

		while(((start+ EvaluationServer.config.REQUEST_TIMELIMIT) > System.currentTimeMillis()) && !isStopped){
			try {
				sleep(EvaluationServer.config.REQUEST_TIMELIMIT / 20);
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
