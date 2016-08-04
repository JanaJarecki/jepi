package evaluationbasics;
/**
 * Wird geworfen bei einer ungueltigen Nutzung der compileMethod()-Methode geworfen.
 * Wird zudem geworfen wenn versucht wird eine Klasse mit mehreren Methoden eine Rekursions/Iterationspruefung durchzufuehren.
 * @author Roman Bange
 */
public class TooManyMethodsException extends Exception {
	private static final long serialVersionUID = -3732312084766119514L;
	public TooManyMethodsException(String str){super(str);}
}
