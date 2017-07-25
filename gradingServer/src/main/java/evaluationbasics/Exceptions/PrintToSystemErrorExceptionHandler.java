package evaluationbasics.Exceptions;

/**
 * Created by ilias on 29.08.16.
 */
public class PrintToSystemErrorExceptionHandler implements Thread.UncaughtExceptionHandler {
  @Override
  public void uncaughtException(Thread t, Throwable e) {
    System.err.println(e.getStackTrace());
  }
}