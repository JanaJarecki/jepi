package evaluationbasics.compilation;

import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;

/**
 * Diese Klasse definiert einen alternativen ClassLoader um eine dateilose Kompilierung zu ermoeglichen.
 *
 * @author Roman Bange
 * @see EvaluationTools.compileClass()
 */
public class MemJavaFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
    private final MemClassLoader classLoader;

    public MemJavaFileManager(JavaCompiler compiler, MemClassLoader classLoader, DiagnosticCollector<JavaFileObject> diag) {
        super(compiler.getStandardFileManager(diag, null, null));

        this.classLoader = classLoader;
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location,
                                               String className,
                                               Kind kind,
                                               FileObject sibling) {
        MemJavaFileObject fileObject = new MemJavaFileObject(className);
        classLoader.addClassFile(fileObject);
        return fileObject;
    }
}
