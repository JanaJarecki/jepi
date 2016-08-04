package evaluationbasics;
/**
 * Wird geworfen falls versucht wird ungueltige Operationen auf eine nicht valide Klasse durchzufuehren.
 * 
 * @author Roman Bange
 */
public class NoValidClassException extends Exception{
	private static final long serialVersionUID = 1840784828685051594L;
	public NoValidClassException(String str){super(str);}
}
