package evaluationbasics.compilationHelpers;

import javax.tools.*;
import java.util.Arrays;

/**
 * Created by ilias on 15.08.16.
 */
public class Compilation {
    private CompilableClass _mmw;
    private boolean _compilable;
    private String _error;
    private DiagnosticCollector<JavaFileObject> _diagnostics;

    public Compilation(CompilableClass mmw) {
        _mmw = mmw;

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if(compiler==null)
            throw new IllegalArgumentException("Compiler cannot be found!");

        _diagnostics = new DiagnosticCollector<JavaFileObject>();

        MemClassLoader classLoader = new MemClassLoader();

        JavaFileManager fileManager = new MemJavaFileManager(compiler, classLoader, _diagnostics);

        //StandardJavaFileManager fileManager = compiler.getStandardFileManager( diagnostics, null, null );

        //Erstellen der .java Datei intern
        Iterable<? extends JavaFileObject> units = Arrays.asList( new StringJavaFileObject(mmw.getClassName(), _mmw.getClassCode()));

        JavaCompiler.CompilationTask task = compiler.getTask( null, fileManager, _diagnostics, null, null, units);

        _compilable=task.call();
    }

    public String getCode() {
        return _mmw.getClassCode();
    }

    public boolean isCompilable() {
        return _compilable;
    }

    public String getError() {
        return _error;
    }

    public DiagnosticCollector<JavaFileObject>  getDiagnostics() {
        return _diagnostics;
    }
}
