package evaluationbasics.CompilationHelpers;

import java.util.HashMap;
import java.util.Map;

/**
 * Diese Klasse definiert einen alternativen ClassLoader um eine dateilose Kompilierung zu ermoeglichen.
 *
 */
public class MemClassLoader extends ClassLoader {
    private final Map<String, MemJavaFileObject> classFiles = new HashMap();
    private final Map<String, Class<?>> precompiledClasses = new HashMap();
//    private URLClassLoader urlClassLoader;

    public MemClassLoader() {
        super(ClassLoader.getSystemClassLoader());
//        String testNGURL = "file://home/ilias/Downloads/testng.jar";
//        String hamcrestURL = "file://home/ilias/Downloads/hamcrest.jar";
//        try {
//            urlClassLoader = new URLClassLoader(new URL[] { new URL("jar", "", testNGURL + "!/"), new URL("jar", "", hamcrestURL + "!/")} );
//        } catch (MalformedURLException e) {
//            System.err.println(e);
//        }
    }

    public void addClass(String className, Class<?> clazz) {
        precompiledClasses.put(className,clazz);
    }

    public void addClassFile(MemJavaFileObject memJavaFileObject) {
        classFiles.put(memJavaFileObject.getClassName(), memJavaFileObject);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        MemJavaFileObject fileObject = classFiles.get(name);

        if (fileObject != null) {
            byte[] bytes = fileObject.getClassBytes();
            return defineClass(name, bytes, 0, bytes.length);
        }

        Class<?> clazz = null;
        clazz = precompiledClasses.get(name);
        if ( clazz !=null) {
            return clazz;
        }
        return super.loadClass(name);
    }



    public Class<?> findClass(String name) throws ClassNotFoundException {
        MemJavaFileObject fileObject = classFiles.get(name);

        if (fileObject != null) {
            byte[] bytes = fileObject.getClassBytes();
            return defineClass(name, bytes, 0, bytes.length);
        }

//        try {
//            return urlClassLoader.loadClass(name);
//        } catch ( ClassNotFoundException ignored) {
//        }

        return super.findClass(name);
    }

}
