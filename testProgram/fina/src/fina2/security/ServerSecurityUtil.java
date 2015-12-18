package fina2.security;

import java.rmi.RemoteException;

import javax.ejb.Handle;

import fina2.FinaTypeException.Type;

/**
 * Contains set of security utility methods for server side. The class contains
 * only static methods and can't have instances.
 */
public class ServerSecurityUtil {

    /** Private constructor to avoid creating of the instances */
    private ServerSecurityUtil() {
    }

    /**
     * Checks whether a given user has the given permissions. If not
     * PermissionDeniedException is thrown.
     */
    public static void checkUserPermissions(Handle userHandle,
            String... permissions) throws fina2.FinaTypeException,
            RemoteException {

        User user = (fina2.security.User) userHandle.getEJBObject();
        int permcounter=0;
        for (String permStr : permissions) {
            if (!user.hasPermission(permStr)) {
            	permcounter++;
            }	
            if(permcounter==permissions.length) { 
            // User doesn't have current permission
                throw new fina2.FinaTypeException(Type.PERMISSIONS_DENIED,
                        new String[] { permStr });
            }
        }

        // Finished. The given user has all given permissions
    }
}
