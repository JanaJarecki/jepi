package evaluationbasics.exceptions;

/**
 * Created by ilias on 29.08.16.
 */
public class WrongNumberOfProvidedJavaElementsException extends Exception {
    private static final long serialVersionUID = -879654698465487L;

    public WrongNumberOfProvidedJavaElementsException(String str) {
        super(str);
        super.setStackTrace(Thread.currentThread().getStackTrace());
    }
}
