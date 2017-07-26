package evaluationbasics.security;

import java.security.Permission;

/**
 * Created by ilias on 01.09.16.
 */
public class ServerSecurityManager extends EvaluationSecurityManager {


    public ServerSecurityManager(int port) {
        super(port);
    }

    public void checkPermission(Permission perm) {
        String name = perm.getName();
        if (RuntimePermission.class == perm.getClass()) {
            if ( name.equals("setIO")) {
                return;
            } else {
                super.checkPermission(perm);
                return;
            }
        } else {
            super.checkPermission(perm);
            return;
        }
    }

}
