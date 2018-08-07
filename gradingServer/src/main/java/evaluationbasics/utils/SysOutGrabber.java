package evaluationbasics.utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Created by ilias on 26.07.17.
 */
public class SysOutGrabber {
  PrintStream old;
  Boolean isValid = false;
  private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
  private final PrintStream printStream = new PrintStream(baos);

  public SysOutGrabber() {
    old = System.out;
    System.setOut(printStream);
    isValid = true;
  }


  public String getOutput() {
    System.out.flush();
    return baos.toString();
  }

  public void detach() {
    System.setOut(old);
    isValid = false;
  }

}
