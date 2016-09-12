package evaluationbasics.Security;

import java.lang.reflect.ReflectPermission;
import java.net.NetPermission;
import java.net.SocketPermission;
import java.security.Permission;
import java.security.SecurityPermission;
import java.util.PropertyPermission;
import java.io.FilePermission;

/**
 * Die Klasse bildet das Kernelement des Sicherheitskonzeptes. Siehe Dokumentation
 * <p>
 * Erlaubte Faelle siehe checkPermission()
 *
 * @author Roman Bange
 */
class EvaluationSecurityManager extends SecurityManager {
    protected int propertyOccurance = 0;
    private final int SERVERPORT;
    private String project_path = null;
    private String java_path = null;
    private final String SEPERATOR = System.getProperty("file.separator");
    private final String[] CLASS_PATH;
    private final String[] EXT_DIRS;

    public EvaluationSecurityManager(int port) {
        super();
        project_path = EvaluationSecurityManager.class.getResource("EvaluationSecurityManager.class").getPath().replace("/", SEPERATOR);
        java_path = System.getProperty("java.home");

        try {
            if (project_path.startsWith("file:")) {
                project_path = project_path.substring(5);
            }
            if (project_path.contains("Bewertungssystem")) {
                project_path = project_path.substring(0, project_path.indexOf("Bewertungssystem")) + "Bewertungssystem";
            } else {
                project_path = project_path.substring(0, project_path.indexOf("bewertungssystem")) + "bewertungssystem";
            }
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            java_path = java_path.substring(0, java_path.lastIndexOf(SEPERATOR));
        } catch (IndexOutOfBoundsException e) {
        }

        CLASS_PATH = System.getProperty("java.class.path").split(":");
        EXT_DIRS = System.getProperty("java.ext.dirs").split(":");
        SERVERPORT = port;
    }

    /**
     * Momentan erlaubte Faelle:
     * <p>
     * Das verbinden mit Servern (fuer lokalen Testfall) zu entfernen in final-Version!
     * <p>
     * Das Horchen auf dem Serverport
     * <p>
     * FilePermission:
     * -nur read Zugriff
     * -Zugriff auf Projectpfad ./bewertungssystem/*
     * -Zugriff auf Javahomepfad ./{javaname}/*
     * -Zugriff auf Klassenpfaede (mitgegeben als Argumente) / "java.java.path"
     * -Zugriff auf externe Pakete (mitgegebn als Argumente) / "java.ext.dirs"
     * -
     * <p>
     * PropertyPermission:
     * -"read" immer erlaubt
     * -"user.timezone" mit "write"-Berechtigungen - Ursprung nicht gefunden
     * -"*" -Ursprung nicht gefunden / nur 2 mal erlaubt! Programmstart?
     * <p>
     * ReflectPermission:
     * -"suppresAccessChecks" - Ursprung nicht gefunden / um auf private-Methoden zuzugreifen?
     * <p>
     * SecurityPermission:
     * -"getProperty" - um die Methode getProperty aufrufen zu koennen
     * -"putProviderProperty" - Ursprung nicht gefunden
     * <p>
     * NetPermission:
     * -alles erlaubt
     * <p>
     * RuntimePermission:
     * -"loadLibrary"
     * -"accessDeclaredMembers" - Zugriff auf private Attribute/Methoden
     * -"writeFileDescriptor" - Ursprung nicht gefunden
     * -"readFileDescriptor" - Ursprung nicht gefunden
     * -"createClassLoader" - Notwendig zur Kompilierung
     * -"closeClassLoader" - Notwendig zur Kompilierung
     * -"accessClassInPackage" - Ursprung nicht gefunden
     *
     * @param perm die zu ueberpruefende Methode
     */
    public void checkPermission(Permission perm) {
        String name = perm.getName();
        if (FilePermission.class == perm.getClass()) {
            if (!perm.getActions().equals("read")) {
                super.checkPermission(perm);
                //Windows:
            } else if (name.startsWith(project_path) || name.startsWith(project_path.substring(1)) || name.startsWith(java_path) || name.startsWith(SEPERATOR + java_path)) {
                return;
            } else if (name.endsWith("Sun\\Java\\lib\\ext".replace("\\", SEPERATOR))) { // nicht sicher !?
                return;
                //Linux:
            } else if (name.equals("/dev/random") || name.equals("/dev/urandom")) {
                return;
            } else {
                for (String extdirs : EXT_DIRS) { //Ersetzt Sun\java... bei Windows?
                    if (name.startsWith(extdirs)) {
                        return;
                    }
                }
            }
            for (String classPath : CLASS_PATH) {
                if (name.startsWith(classPath)) {
                    return;
                }
            }
            super.checkPermission(perm);
        } else if (SocketPermission.class == perm.getClass()) {
            super.checkPermission(perm);
        } else if (PropertyPermission.class == perm.getClass()) {
            if (name.equals("*")) {
                if (propertyOccurance <= 2) {
                    propertyOccurance++;
                } else {
                    super.checkPermission(perm);
                }
            } else if (perm.getName().equals("user.timezone")) { //TEST ???
                return;
            } else if (!perm.getActions().equals("read")) {
                super.checkPermission(perm);
            }
        } else if (ReflectPermission.class == perm.getClass()) {
            if (!name.equals("suppressAccessChecks")) {
                super.checkPermission(perm);
            }
        } else if (RuntimePermission.class == perm.getClass()) {
            if (name.startsWith("loadLibrary") || name.equals("accessDeclaredMembers") || name.equals("writeFileDescriptor") ||
                    name.equals("readFileDescriptor") || name.equals("createClassLoader") || name.equals("closeClassLoader") ||
                    name.startsWith("accessClassInPackage")) {
            } else {
                super.checkPermission(perm);
            }
        } else if (SecurityPermission.class == perm.getClass()) {
            if (name.startsWith("getProperty") || name.startsWith("putProviderProperty")) {
            } else {
                super.checkPermission(perm);
            }
        } else if (NetPermission.class == perm.getClass()) {
        } else {
            super.checkPermission(perm);
        }
    }

    //Dauerhaft fuer Serverbetrieb
    public void checkListen(int port) {
        if (port != SERVERPORT)
            super.checkListen(port);
    }

    public void checkAccept(String host, int port) {
        //super.checkAccept(host, port);
    }


}



