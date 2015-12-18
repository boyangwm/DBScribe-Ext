package fina2.returns;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;

public class ReturnXMLValidator implements ValidationEventHandler {

    private boolean valid;

    private StringBuffer valErrMsg;

    public ReturnXMLValidator() {
        this.valErrMsg = new StringBuffer();
        this.valid = true;
    }

    public boolean handleEvent(ValidationEvent ve) {
        // Ignore warnings
        if (ve.getSeverity() != ValidationEvent.WARNING) {
            ValidationEventLocator vel = ve.getLocator();
            valErrMsg.append("line: ").append(vel.getLineNumber());
            valErrMsg.append(", column: ").append(vel.getColumnNumber());
            valErrMsg.append(", message: ").append(ve.getMessage());
            valErrMsg.append("\n");

            valid = false;
        }
        return true;
    }

    public boolean isValid() {
        return valid;
    }

    public String getValidationErrorMessage() {
        String msg = null;
        if (!valid) {
            msg = valErrMsg.toString();
        }
        return msg;
    }
}
