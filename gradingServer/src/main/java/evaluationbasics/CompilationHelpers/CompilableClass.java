package evaluationbasics.CompilationHelpers;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.util.List;

/**
 * Created by ilias on 15.08.16.
 */
public interface CompilableClass {

    String getClassCode();

    String getClassName();

    String parseError(List<Diagnostic<? extends JavaFileObject>> diagnostics);
}
