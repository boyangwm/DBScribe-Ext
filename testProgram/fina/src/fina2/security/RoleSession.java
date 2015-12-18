package fina2.security;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJBObject;
import javax.ejb.Handle;

/**
 * RoleSession interface
 */
public interface RoleSession extends EJBObject {

    //
    // Permissions
    //

    List<SecurityItem> getRolePermissions(RolePK rolePK, Handle languageHandle)
            throws RemoteException;

    void setRolePermissions(RolePK rolePK, Set<Integer> permissions)
            throws RemoteException;

    //
    // Returns
    //

    Map<Integer, TreeSecurityItem> getRoleReturns(RolePK rolePK,
            Handle languageHandle) throws RemoteException;

    void setRoleReturns(RolePK rolePK, Set<Integer> returns)
            throws RemoteException;

    //
    // Reports
    //

    Map<Integer, TreeSecurityItem> getRoleReports(RolePK rolePK,
            Handle languageHandle) throws RemoteException;

    void setRoleReports(RolePK rolePK, Set<Integer> reports)
            throws RemoteException;

    //
    // Return versions
    //

    List<SecurityItem> getRoleReturnVersions(RolePK rolePK,
            Handle languageHandle) throws RemoteException;

    void setRoleReturnVersions(RolePK rolePK, List<SecurityItem> versions)
            throws RemoteException;

    //
    // Users
    //

    List<SecurityItem> getRoleUsers(RolePK rolePK, Handle languageHandle)
            throws RemoteException;
}
