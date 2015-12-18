/*
 * MDTConstants.java
 *
 * Created on October 19, 2001, 11:05 AM
 */

package fina2.returns;

/**
 *
 * @author  David Shalamberidze
 * @version 
 */

public class ReturnConstants {
    public final static int TABLETYPE_MULTIPLE = 1;
    public final static int TABLETYPE_NORMAL = 2;
    public final static int TABLETYPE_VARIABLE = 3;

    public final static int STATUS_CREATED = 1;
    public final static int STATUS_AMENDED = 2;
    public final static int STATUS_IMPORTED = 3;
    public final static int STATUS_PROCESSED = 4;
    public final static int STATUS_VALIDATED = 5;
    public final static int STATUS_RESETED = 6;
    public final static int STATUS_ACCEPTED = 7;
    public final static int STATUS_REJECTED = 8;
    public final static int STATUS_ERRORS = 9;
    public final static int STATUS_LOADED = 10;
    public final static int STATUS_QUEUED = 11;

    public final static String STATUS_CREATED_STR = "fina2.returns.created";
    public final static String STATUS_AMENDED_STR = "fina2.returns.amended";
    public final static String STATUS_IMPORTED_STR = "fina2.returns.imported";
    public final static String STATUS_PROCESSED_STR = "fina2.returns.processed";
    public final static String STATUS_VALIDATED_STR = "fina2.returns.validated";
    public final static String STATUS_RESETED_STR = "fina2.returns.reseted";
    public final static String STATUS_ACCEPTED_STR = "fina2.returns.accepted";
    public final static String STATUS_REJECTED_STR = "fina2.returns.rejected";
    public final static String STATUS_LOADED_STR = "fina2.returns.loaded";
    public final static String STATUS_ERRORS_STR = "fina2.returns.errors";
    public final static String STATUS_QUEUED_STR = "fina2.returns.queued";

    public final static int EVAL_SUM = 1;
    public final static int EVAL_AVERAGE = 2;
    public final static int EVAL_MIN = 3;
    public final static int EVAL_MAX = 4;
    public final static int EVAL_EQUATION = 5;
}
