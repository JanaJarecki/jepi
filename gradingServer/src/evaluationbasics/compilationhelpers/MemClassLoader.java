package evaluationbasics.compilationhelpers;
import java.util.HashMap;
import java.util.Map;

/**
 * Diese Klasse definiert einen alternativen ClassLoader um eine dateilose Kompilierung zu ermoeglichen.
 * @author Roman Bange
 * @see EvaluationTools.compileClass()
 */
public class MemClassLoader extends ClassLoader{
	  private final Map<String, MemJavaFileObject> classFiles = new HashMap<String, MemJavaFileObject>();

	  public MemClassLoader(){
	    super( ClassLoader.getSystemClassLoader() );
	  }

	  public void addClassFile(MemJavaFileObject memJavaFileObject){
	    classFiles.put( memJavaFileObject.getClassName(), memJavaFileObject );
	  }

	  public Class<?> findClass( String name ) throws ClassNotFoundException{
	    MemJavaFileObject fileObject = classFiles.get( name );

	    if ( fileObject != null )
	    {
	      byte[] bytes = fileObject.getClassBytes();
	      return defineClass( name, bytes, 0, bytes.length );
	    }

	    return super.findClass( name );
	  }

}
