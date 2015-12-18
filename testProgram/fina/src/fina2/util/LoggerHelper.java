package fina2.util;

import java.text.DecimalFormat;

import org.apache.log4j.Logger;

public class LoggerHelper {

    private Logger log;
    private String objectType;
    private DecimalFormat df = new DecimalFormat("000000");

    private final String USERNAME = "USER";
    private final String OBJECT_ID = "OID";
    private final String OBJECT_TYPE = "OBT";
    private final String PROPERTY_NAME = "PNA";
    private final String PROPERTY_VALUE = "PVL";
    private final String PROPERTY_OLD_VALUE = "POV";
    private final String CREATE_ACTION = "ACT=\"CRT\"";
    private final String AMEND_ACTION = "ACT=\"AMD\"";
    private final String DELETE_ACTION = "ACT=\"DEL\"";

    private boolean create;

    public LoggerHelper(Class clazz, String objectType) {

        this.log = Logger.getLogger(clazz);
        this.objectType = objectType;
    }

    public Logger getLogger() {
        return this.log;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public void logObjectCreate(int objectId, String user) {

        create = true;

        /**
         * USER="admin" ACT="CRT" OBT="LANGUAGE" OID="000456"
         */
        StringBuffer buff = new StringBuffer();

        append(USERNAME, user, buff);
        buff.append(" ");

        buff.append(CREATE_ACTION);
        buff.append(" ");

        append(OBJECT_TYPE, objectType, buff);
        buff.append(" ");

        append(OBJECT_ID, getFormattedObjectId(objectId), buff);
        buff.append(" ");

        log.info(buff.toString());
    }

    public void logObjectCreate(long objectId, String user) {

        create = true;

        /**
         * USER="admin" ACT="CRT" OBT="LANGUAGE" OID="000456"
         */
        StringBuffer buff = new StringBuffer();

        append(USERNAME, user, buff);
        buff.append(" ");

        buff.append(CREATE_ACTION);
        buff.append(" ");

        append(OBJECT_TYPE, objectType, buff);
        buff.append(" ");

        append(OBJECT_ID, getFormattedObjectId(objectId), buff);
        buff.append(" ");

        log.info(buff.toString());
    }

    
    public void logObjectStore() {

        create = false;
    }

    public void logObjectRemove(int objectId, String user) {

        /**
         * USER="admin" ACT="DEL" OBT="LANGUAGE" OID="000456"
         */

        StringBuffer buff = new StringBuffer();

        append(USERNAME, user, buff);
        buff.append(" ");

        buff.append(DELETE_ACTION);
        buff.append(" ");

        append(OBJECT_TYPE, objectType, buff);
        buff.append(" ");

        append(OBJECT_ID, getFormattedObjectId(objectId), buff);
        buff.append(" ");

        log.info(buff.toString());
    }
    public void logObjectRemove(long objectId, String user) {

        /**
         * USER="admin" ACT="DEL" OBT="LANGUAGE" OID="000456"
         */

        StringBuffer buff = new StringBuffer();

        append(USERNAME, user, buff);
        buff.append(" ");

        buff.append(DELETE_ACTION);
        buff.append(" ");

        append(OBJECT_TYPE, objectType, buff);
        buff.append(" ");

        append(OBJECT_ID, getFormattedObjectId(objectId), buff);
        buff.append(" ");

        log.info(buff.toString());
    }
    public void logPropertySet(String propertyName, String newValue,
            String oldValue, int objectId, String user) {

        /**
         *  USER="admin" ACT="AMD/CRT" OBT="LANGUAGE" OID="000456" PNA="XML" PVL="1251" POL="1252"
         */

        StringBuffer buff = new StringBuffer();

        append(USERNAME, user, buff);
        buff.append(" ");

        buff.append(create == true ? CREATE_ACTION : AMEND_ACTION);
        buff.append(" ");

        append(OBJECT_TYPE, objectType, buff);
        buff.append(" ");

        append(OBJECT_ID, getFormattedObjectId(objectId), buff);
        buff.append(" ");

        append(PROPERTY_NAME, propertyName, buff);
        buff.append(" ");

        append(PROPERTY_VALUE, newValue, buff);
        buff.append(" ");

        if (!create && oldValue != null && !("".equals(oldValue.trim()))) {
            append(PROPERTY_OLD_VALUE, oldValue, buff);
        }

        log.info(buff.toString());
    }

    public void logPropertySet(String propertyName, String newValue,
            String oldValue, long objectId, String user) {

        /**
         *  USER="admin" ACT="AMD/CRT" OBT="LANGUAGE" OID="000456" PNA="XML" PVL="1251" POL="1252"
         */

        StringBuffer buff = new StringBuffer();

        append(USERNAME, user, buff);
        buff.append(" ");

        buff.append(create == true ? CREATE_ACTION : AMEND_ACTION);
        buff.append(" ");

        append(OBJECT_TYPE, objectType, buff);
        buff.append(" ");

        append(OBJECT_ID, getFormattedObjectId(objectId), buff);
        buff.append(" ");

        append(PROPERTY_NAME, propertyName, buff);
        buff.append(" ");

        append(PROPERTY_VALUE, newValue, buff);
        buff.append(" ");

        if (!create && oldValue != null && !("".equals(oldValue.trim()))) {
            append(PROPERTY_OLD_VALUE, oldValue, buff);
        }

        log.info(buff.toString());
    }

    
    public void logPropertySet(String propertyName, int newValue, int oldValue,
            int objectId, String user) {

        logPropertySet(propertyName, String.valueOf(newValue),
                oldValue == -1 ? null : String.valueOf(oldValue), objectId,
                user);
    }
    public void logPropertySet(String propertyName, int newValue, int oldValue,
            long objectId, String user) {

        logPropertySet(propertyName, String.valueOf(newValue),
                oldValue == -1 ? null : String.valueOf(oldValue), objectId,
                user);
    }
    
    public void logPropertySet(String propertyName, long newValue, long oldValue,
            long objectId, String user) {

        logPropertySet(propertyName, String.valueOf(newValue),
                oldValue == -1 ? null : String.valueOf(oldValue), objectId,
                user);
    }
    
    public void logPropertyValue(String propertyName, String value,
            int objectId, String user) {
        /**
         * USER="admin" ACT="DEL" OBT="LANGUAGE" OID="000456"  PNA="DESCRIPTION" PVL="Tajik"
         */

        StringBuffer buff = new StringBuffer();

        append(USERNAME, user, buff);
        buff.append(" ");

        buff.append(DELETE_ACTION);
        buff.append(" ");

        append(OBJECT_TYPE, objectType, buff);
        buff.append(" ");

        append(OBJECT_ID, getFormattedObjectId(objectId), buff);
        buff.append(" ");

        append(PROPERTY_NAME, propertyName, buff);
        buff.append(" ");

        append(PROPERTY_VALUE, value, buff);

        log.info(buff.toString());
    }

    public void logPropertyValue(String propertyName, String value,
            long objectId, String user) {
        /**
         * USER="admin" ACT="DEL" OBT="LANGUAGE" OID="000456"  PNA="DESCRIPTION" PVL="Tajik"
         */

        StringBuffer buff = new StringBuffer();

        append(USERNAME, user, buff);
        buff.append(" ");

        buff.append(DELETE_ACTION);
        buff.append(" ");

        append(OBJECT_TYPE, objectType, buff);
        buff.append(" ");

        append(OBJECT_ID, getFormattedObjectId(objectId), buff);
        buff.append(" ");

        append(PROPERTY_NAME, propertyName, buff);
        buff.append(" ");

        append(PROPERTY_VALUE, value, buff);

        log.info(buff.toString());
    }

    
    public void logPropertyValue(String propertyName, int value, int objectId,
            String user) {

        logPropertyValue(propertyName, String.valueOf(value), objectId, user);
    }

    public void logPropertyValue(String propertyName, long value, int objectId,
            String user) {

        logPropertyValue(propertyName, String.valueOf(value), objectId, user);
    }
    
    private String getFormattedObjectId(long objectId) {

        return df.format(objectId);
    }

    private void append(String property, String value, StringBuffer buff) {
        buff.append(property);
        buff.append("=\"");
        buff.append(value);
        buff.append("\"");
    }
}
