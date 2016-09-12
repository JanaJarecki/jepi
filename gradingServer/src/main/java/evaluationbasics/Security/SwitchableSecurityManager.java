package evaluationbasics.Security;

import java.security.Permission;

public class SwitchableSecurityManager extends EvaluationSecurityManager {

    private static final ToggleSecurityManagerPermission TOGGLE_PERMISSION = new ToggleSecurityManagerPermission();

    ThreadLocal<Boolean> enabledFlag = null;

    public SwitchableSecurityManager(int port, final boolean enabledByDefault) {
        super(port);

        enabledFlag = new ThreadLocal<Boolean>() {

            @Override
            protected Boolean initialValue() {
                return enabledByDefault;
            }

            @Override
            public void set(Boolean value) {
                SecurityManager securityManager = System.getSecurityManager();
                if (securityManager != null) {
                    securityManager.checkPermission(TOGGLE_PERMISSION);
                }
                super.set(value);
            }
        };
    }

    @Override
    public void checkPermission(Permission permission) {
        if (shouldCheck(permission)) {
            super.checkPermission(permission);
        }
    }

    @Override
    public void checkPermission(Permission permission, Object context) {
        if (shouldCheck(permission)) {
            super.checkPermission(permission, context);
        }
    }

    private boolean shouldCheck(Permission permission) {
        return isEnabled() || permission instanceof ToggleSecurityManagerPermission;
    }

    public void enable() {
        enabledFlag.set(true);
    }

    public void disable() {
        enabledFlag.set(false);
    }

    public boolean isEnabled() {
        return enabledFlag.get();
    }

}