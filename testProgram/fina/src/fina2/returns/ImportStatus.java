package fina2.returns;

public enum ImportStatus {

    UPLOADED("fina2.returns.import.status.uploaded"), 
    IN_PROGRESS("fina2.returns.import.status.in_progress"), 
    QUEUED("fina2.returns.import.status.queued"), 
    REJECTED("fina2.returns.import.status.rejected"), 
    IMPORTED("fina2.returns.import.status.imported"),
    DECLINED("fina2.returns.import.status.declined"),
    ERRORS("fina2.returns.errors");

    private String code;

    private ImportStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
