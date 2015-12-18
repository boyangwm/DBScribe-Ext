package fina2.reportoo.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class ScheduledReportInfo implements Serializable {

    public final static int STATUS_SCHEDULED = 0;
    public final static int STATUS_PROCESSING = 1;
    public final static int STATUS_DONE = 2;
    public final static int STATUS_ERROR = 3;

    private int reportId;
    private String name;
    private String languageName;
    private String creatorUser;
    private boolean folder;
    private java.util.Date scheduleTime;
    private int reportInfoHashCode;
    private int langId;
    private int userId;
    private boolean onDemand;
    private int status;
    private ArrayList children = new ArrayList();

    public ArrayList getChildren() {
        return children;
    }

    public String getCreatorUser() {
        return creatorUser;
    }

    public boolean isFolder() {
        return folder;
    }

    public String getLanguageName() {
        return languageName;
    }

    public String getName() {
        return name;
    }

    public int getReportInfoHashCode() {
        return reportInfoHashCode;
    }

    public int getLangId() {
        return langId;
    }

    public int getReportId() {
        return reportId;
    }

    public Date getScheduleTime() {
        return scheduleTime;
    }

    public int getStatus() {
        return status;
    }

    public int getUserId() {
        return userId;
    }

    public boolean isOnDemand() {
        return onDemand;
    }

    public void setChildren(ArrayList children) {
        this.children = children;
    }

    public void setCreatorUser(String creatorUser) {
        this.creatorUser = creatorUser;
    }

    public void setFolder(boolean folder) {
        this.folder = folder;
    }

    public void setLanguageName(String languageName) {
        this.languageName = languageName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setReportInfoHashCode(int reportInfoHashCode) {
        this.reportInfoHashCode = reportInfoHashCode;
    }

    public void setLangId(int langId) {
        this.langId = langId;
    }

    public void setReportId(int reportId) {
        this.reportId = reportId;
    }

    public void setScheduleTime(Date scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setOnDemand(boolean onDemand) {
        this.onDemand = onDemand;
    }

    public String toString() {
        return this.name;
    }

    public int hashCode() {
        return reportId + langId + reportInfoHashCode + (onDemand ? 1 : 0)
                + ((scheduleTime != null) ? scheduleTime.hashCode() : 0);
    }

    public boolean equals(Object obj) {

        boolean equals = false;
        if (obj instanceof ScheduledReportInfo) {

            ScheduledReportInfo sri = (ScheduledReportInfo) obj;

            if (isFolder() && folder == sri.folder && reportId == sri.reportId) {

                equals = true;
            } else if (reportId == sri.reportId && langId == sri.langId
                    && reportInfoHashCode == sri.reportInfoHashCode
                    && equalsScheduledTimes(sri)) {

                equals = true;
            }
        }
        return equals;
    }

    private boolean equalsScheduledTimes(ScheduledReportInfo sri) {

        boolean equals = false;

        if (onDemand == sri.onDemand
                && (onDemand == true || scheduleTime.equals(sri.scheduleTime))) {
            equals = true;
        }

        return equals;
    }
}
