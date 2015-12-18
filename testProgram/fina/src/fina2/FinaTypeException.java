package fina2;

/**
 * Fina exception with type/code specification
 */
public class FinaTypeException extends Exception {

    /**
     * Exception types
     */
    public enum Type {

        /* ========================================================== */
        /* Security related types */

        SECURITY_ROLE_CODE_EMPTY("fina2.security.roleCodeEmpty"), SECURITY_ROLE_DESCRIPTION_EMPTY(
                "fina2.security.roleDescriptionEmpty"), SECURITY_EMPTY_LOGIN(
                "fina2.security.emptyLogin"), SECURITY_EMPTY_NAME(
                "fina2.security.emptyName"), SECURITY_NO_RETURN_VERSION_AMEND_FOR_USER(
                "fina2.security.noReturnVersionAmendForUser"),

        /* ========================================================== */
        /* Returns */

        RETURNS_NO_FI_SELECTED("fina2.returns.noFISelected"),
        /* ========================================================== */
        /* FinaTypeException Error Codes */
        FINA2_GENERAL_ERROR("FinaTypeException.GeneralError"), INVALID_DATE_FORMAT(
                "FinaTypeException.InvalidDateFormat"), PERMISSIONS_DENIED(
                "FinaTypeException.PermissionsDenied"), BANK_CRITERION_NOT_FOUND(
                "FinaTypeException.BankCriterionNotFound"), METADATA_COMPARISON_NOT_UNIQUE(
                "FinaTypeException.ComparisonNotUnique"), RETURNS_RETURN_NOT_UNIQUE(
                "FinaTypeException.ReturnNotUnique"), RETURNS_WRONG_MDT(
                "FinaTypeException.WrongMTD"), RETURNS_DELETING_RETURN_ERROR(
                "FinaTypeException.DeletingReturnError"), SECURITY_PASSWORD_NOT_MATCH(
                "FinaTypeException.PasswordDontMatch"), SECURITY_PASSWORD_TOO_SHORT(
                "FinaTypeException.PasswordTooShort"), SECURITY_PASSWORD_CHAR_NUM_REQUIRED(
                "FinaTypeException.PasswordCharNumRequired"), RETURN_SCHEDULE_NOT_UNIQUE(
                "FinaTypeException.ScheduleNotUnique"), SECURITY_ROLE_NOT_EMPTY(
                "FinaTypeException.RoleNotEmpty"), CODE_NOT_UNIQUE(
                "FinaTypeException.CodeNotUnique"), REQUIRED(
                "FinaTypeException.Required"),
         FINA2_WEB_REJECT("fina2.web.exception.reject"),
         FINA2_WEB_INVALID_RETURN("fina2.web.exception.invalidReturn"),
         FINA2_DUPLICATE_REPORT_FOLDER_NAME("fina2.duplicate.report.folder.name"),
         DEFINITION_CODE_NOT_UNIQUE("fina2.returns.returnDefinitionCodeNotUnique");

        /* ========================================================== */
        /* Type instance */

        /** Contains the message id in resource containers */
        private final String messageUrl;

        /** Creates the instance of the type */
        private Type(String messageUrl) {
            this.messageUrl = messageUrl;
        }
    };

    /** A type of current exception */
    private Type type;
    private String[] params;

    /** Creates the exception of given type */
    public FinaTypeException(Type type) {
        this.type = type;
    }

    /** Creates the exception of given type and parameters*/
    public FinaTypeException(Type type, String[] params) {
        this.type = type;
        this.params = params;
    }

    /** Returns the exception message url */
    public String getMessageUrl() {
        return type.messageUrl;
    }

    public String[] getParams() {
        return params;
    }
}