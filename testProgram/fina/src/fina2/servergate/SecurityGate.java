package fina2.servergate;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Handle;

import fina2.Main;
import fina2.security.Role;
import fina2.security.RoleHome;
import fina2.security.RolePK;
import fina2.security.RoleSession;
import fina2.security.RoleSessionHome;
import fina2.security.SecurityItem;
import fina2.security.TreeSecurityItem;
import fina2.security.User;
import fina2.security.UserHome;
import fina2.security.UserPK;
import fina2.security.UserSession;
import fina2.security.UserSessionHome;

/**
 * Contains the set of security utility methods. The class contains only static
 * methods and can't have instances.
 */
public class SecurityGate {

    /** Private constructor to avoid creating of the instances */
    private SecurityGate() {
    }

    //
    // General user management methods
    //

    /** Returns the list of all permissions from the server */
    public static List<SecurityItem> getAllPermissions() throws Exception {
        UserSession session = getUserSession();
        return session.getAllPermissions(Main.main.getLanguageHandle());
    }

    /** Returns the list of all return versions from the server */
    public static List<SecurityItem> getAllReturnVersions() throws Exception {
        UserSession session = getUserSession();
        return session.getAllReturnVersions(Main.main.getLanguageHandle());
    }

    /** Returns the tree of all returns from the server */
    public static Map<Integer, TreeSecurityItem> getAllReturns()
            throws Exception {
        UserSession session = getUserSession();
        return session.getAllReturns(Main.main.getLanguageHandle());
    }

    /** Returns the tree of all reports from the server */
    public static Map<Integer, TreeSecurityItem> getAllReports()
            throws Exception {
        UserSession session = getUserSession();
        return session.getAllReports(Main.main.getLanguageHandle());
    }

    /** Returns the list of all roles from the server */
    public static List<SecurityItem> getAllRoles() throws Exception {

        UserSession session = getUserSession();
        return session.getAllRoles(Main.main.getLanguageHandle());
    }

    /** Returns the list of all users from the server */
    public static List<SecurityItem> getAllUsers() throws Exception {
        UserSession session = getUserSession();
        return session.getAllUsers(Main.getCurrentLanguage());
    }

    /** Returns a tree of all banks from the server */
    public static Map<Integer, TreeSecurityItem> getAllBanks() throws Exception {
        UserSession session = getUserSession();
        return session.getAllBanks(Main.main.getLanguageHandle());
    }

    //
    // User methods
    //

    /** Returns user session */
    public static UserSession getUserSession() throws Exception {

        UserSessionHome home = (UserSessionHome) fina2.Main.getRemoteObject(
                "fina2/security/UserSession", UserSessionHome.class);

        return home.create();
    }

    /** Creates a new user */
    public static User createUser() throws Exception {
        UserHome home = (UserHome) fina2.Main.getRemoteObject(
                "fina2/security/User", UserHome.class);

        return home.create();
    }

    /** Returns a user with given PK from the server */
    public static User getUser(UserPK userPK) throws IllegalArgumentException,
            Exception {

        if (userPK == null) {
            /* The user PK must be specified */
            String error = "UserPK must be specified";
            throw new IllegalArgumentException(error);
        }

        UserHome home = (UserHome) fina2.Main.getRemoteObject(
                "fina2/security/User", UserHome.class);

        return home.findByPrimaryKey(userPK);
    }

    /** Returns a given user roles from the server */
    public static List<SecurityItem> getUserRoles(UserPK userPK)
            throws Exception {

        UserSession session = getUserSession();
        Handle languageHandle = Main.main.getLanguageHandle();

        return session.getUserRoles(userPK, languageHandle);
    }

    /** Updates a given user roles */
    public static void setUserRoles(UserPK userPK, Set<Integer> roles)
            throws Exception {
        UserSession session = getUserSession();
        session.setUserRoles(userPK, roles);
    }

    /** Returns a tree of given user banks from the server */
    public static Map<Integer, TreeSecurityItem> getUserBanks(UserPK userPK)
            throws Exception {

        UserSession session = getUserSession();
        Handle languageHandle = Main.main.getLanguageHandle();

        return session.getUserBanks(userPK, languageHandle);
    }

    /** Returns user banks only from the server */
    public static Map<Integer, TreeSecurityItem> getUserBanksOnly(UserPK userPK)
            throws Exception {

        UserSession session = getUserSession();
        Handle languageHandle = Main.main.getLanguageHandle();

        return session.getUserBanksOnly(userPK, languageHandle);
    }

    /** Updates a given user banks */
    public static void setUserBanks(UserPK userPK, Set<Integer> banks)
            throws Exception {
        UserSession session = getUserSession();
        session.setUserBanks(userPK, banks);
    }

    /** Returns a given user permissions from the server */
    public static List<SecurityItem> getUserPermissions(UserPK userPK)
            throws Exception {

        UserSession session = getUserSession();
        Handle languageHandle = Main.main.getLanguageHandle();

        return session.getUserPermissions(userPK, languageHandle);
    }

    /** Updates a given user permissions */
    public static void setUserPermissions(UserPK userPK,
            Set<Integer> permissions) throws Exception {
        UserSession session = getUserSession();
        session.setUserPermissions(userPK, permissions);
    }

    /** Returns a tree of given user returns from the server */
    public static Map<Integer, TreeSecurityItem> getUserReturns(UserPK userPK)
            throws Exception {

        UserSession session = getUserSession();
        Handle languageHandle = Main.main.getLanguageHandle();

        return session.getUserReturns(userPK, languageHandle);
    }

    /** Updates a given user returns */
    public static void setUserReturns(UserPK userPK, Set<Integer> returns)
            throws Exception {
        UserSession session = getUserSession();
        session.setUserReturns(userPK, returns);
    }

    /** Returns a tree of given user reports from the server */
    public static Map<Integer, TreeSecurityItem> getUserReports(UserPK userPK)
            throws Exception {

        UserSession session = getUserSession();
        Handle languageHandle = Main.main.getLanguageHandle();

        return session.getUserReports(userPK, languageHandle);
    }

    /** Updates a given user reports */
    public static void setUserReports(UserPK userPK, Set<Integer> reports)
            throws Exception {
        UserSession session = getUserSession();
        session.setUserReports(userPK, reports);
    }

    /** Returns a given user return versions from the server */
    public static List<SecurityItem> getUserReturnVersions(UserPK userPK)
            throws Exception {

        UserSession session = getUserSession();
        Handle languageHandle = Main.main.getLanguageHandle();

        return session.getUserReturnVersions(userPK, languageHandle);
    }

    /** Updates a given user return versions */
    public static void setUserReturnVersions(UserPK userPK,
            List<SecurityItem> versions) throws Exception {
        UserSession session = getUserSession();
        session.setUserReturnVersions(userPK, versions);
    }

    //
    // Role methods
    //

    /** Returns role session */
    public static RoleSession getRoleSession() throws Exception {

        RoleSessionHome home = (RoleSessionHome) fina2.Main.getRemoteObject(
                "fina2/security/RoleSession", RoleSessionHome.class);

        return home.create();
    }

    /** Creates a new role */
    public static Role createRole() throws Exception {
        RoleHome home = (RoleHome) fina2.Main.getRemoteObject(
                "fina2/security/Role", RoleHome.class);

        return home.create();
    }

    /** Returns the role with given PK from the server */
    public static Role getRole(RolePK rolePK) throws IllegalArgumentException,
            Exception {

        if (rolePK == null) {
            /* The role PK must be specified */
            String error = "RolePK must be specified";
            throw new IllegalArgumentException(error);
        }

        RoleHome home = (RoleHome) fina2.Main.getRemoteObject(
                "fina2/security/Role", RoleHome.class);

        return home.findByPrimaryKey(rolePK);
    }

    /** Returns a given role users from the server */
    public static List<SecurityItem> getRoleUsers(RolePK rolePK)
            throws Exception {

        RoleSession session = getRoleSession();
        Handle languageHandle = Main.main.getLanguageHandle();

        return session.getRoleUsers(rolePK, languageHandle);
    }

    /** Returns the role permissions from the server */
    public static List<SecurityItem> getRolePermissions(RolePK rolePK)
            throws Exception {

        RoleSession session = getRoleSession();
        Handle languageHandle = Main.main.getLanguageHandle();

        return session.getRolePermissions(rolePK, languageHandle);
    }

    /** Updates a given role permissions */
    public static void setRolePermissions(RolePK rolePK,
            Set<Integer> permissions) throws Exception {
        RoleSession session = getRoleSession();
        session.setRolePermissions(rolePK, permissions);
    }

    /** Loads a given role reports from the server */
    public static Map<Integer, TreeSecurityItem> getRoleReports(RolePK rolePK)
            throws Exception {

        RoleSession session = getRoleSession();
        Handle languageHandle = Main.main.getLanguageHandle();

        return session.getRoleReports(rolePK, languageHandle);
    }

    /** Updates a given role permissions */
    public static void setRoleReports(RolePK rolePK, Set<Integer> reports)
            throws Exception {
        RoleSession session = getRoleSession();
        session.setRoleReports(rolePK, reports);
    }

    /** Loads a given role returns from the server */
    public static Map<Integer, TreeSecurityItem> getRoleReturns(RolePK rolePK)
            throws Exception {

        RoleSession session = getRoleSession();
        Handle languageHandle = Main.main.getLanguageHandle();

        return session.getRoleReturns(rolePK, languageHandle);
    }

    /** Updates a given role permissions */
    public static void setRoleReturns(RolePK rolePK, Set<Integer> returns)
            throws Exception {
        RoleSession session = getRoleSession();
        session.setRoleReturns(rolePK, returns);
    }

    /** Returns a given role return versions from the server */
    public static List<SecurityItem> getRoleReturnVersions(RolePK rolePK)
            throws Exception {

        RoleSession session = getRoleSession();
        Handle languageHandle = Main.main.getLanguageHandle();

        return session.getRoleReturnVersions(rolePK, languageHandle);
    }

    /** Updates a given role return versions */
    public static void setRoleReturnVersions(RolePK rolePK,
            List<SecurityItem> versions) throws Exception {
        RoleSession session = getRoleSession();
        session.setRoleReturnVersions(rolePK, versions);
    }

}
