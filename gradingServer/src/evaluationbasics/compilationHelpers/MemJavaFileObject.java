package evaluationbasics.compilationHelpers;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;

import javax.tools.SimpleJavaFileObject;

/**
 * Mithilfe dieser Klasse laesst sich ein auf dem Laufwerk in Form einer .class Datei gespeicherter Zielcode durch eine Resscource ersetzen.
 * @author Roman Bange
 * @see EvaluationTools.compileClass()
 */
public class MemJavaFileObject  extends SimpleJavaFileObject
{
	  private final ByteArrayOutputStream baos = new ByteArrayOutputStream( 8192 );
	  private final String className;

	  public MemJavaFileObject(String className){
	    super( URI.create( "string:///" + className + Kind.CLASS.extension ),
	           Kind.CLASS );
	    this.className = className;
	  }

	  public String getClassName(){return className;}

	  public byte[] getClassBytes(){return baos.toByteArray();}

	  public OutputStream openOutputStream(){return baos;}

}
