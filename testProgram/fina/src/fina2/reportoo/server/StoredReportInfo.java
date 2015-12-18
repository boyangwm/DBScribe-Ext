package fina2.reportoo.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class StoredReportInfo implements Serializable {

    private int reportId;
    private String name;
    private String languageName;
    private String creatorUser;
    private boolean folder;
    private java.util.Date storeDate;
    private int reportInfoHashCode;
    private int langId;
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

    public Date getStoreDate() {
        return storeDate;
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

    public void setStoreDate(Date storeDate) {
        this.storeDate = storeDate;
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

    public String toString() {
        return this.name;
    }
}
