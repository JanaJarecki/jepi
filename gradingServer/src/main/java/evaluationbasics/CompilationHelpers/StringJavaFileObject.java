package evaluationbasics.CompilationHelpers;

import java.net.URI;
import javax.tools.SimpleJavaFileObject;

/**
 * Mithilfe dieser Klasse laesst sich ein auf dem Laufwerk in Form einer .java Datei gespeicherter Quellcode durch eine Resscource ersetzen.
 * 
 * @author Roman Bange
 * @see EvaluationTools.compileClass
 */
public class StringJavaFileObject extends SimpleJavaFileObject {
	  private final CharSequence code;

	  public StringJavaFileObject(String sClassName, String pCode )
	  {
	    super( URI.create( "string:///" + sClassName.replace( '.', '/' ) + Kind.SOURCE.extension ),
	                       Kind.SOURCE );
	    this.code = pCode;
	  }

	  @Override
	  public CharSequence getCharContent( boolean ignoreEncodingErrors )
	  {
	    return code;
	  }
}
