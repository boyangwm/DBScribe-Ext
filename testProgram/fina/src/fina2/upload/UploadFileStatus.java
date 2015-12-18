package fina2.upload;

public enum UploadFileStatus {
	
	UPLOADED("fina2.web.uploaded"),
	CONVERTED("fina2.web.converted"),
	REJECTED("fina2.web.rejected"),
	UPLOADED_VALIDATED("fina2.web.uploaded.validated"),
	UPLOADED_NOT_VALIDATED("fina2.web.uploaded.not.validated"),
	UPLOADED_CONVERTED("fina2.web.uploaded.converted"),
	UPLOADED_NOT_CONVERTED("fina2.web.uploaded.not.converted"),
	UPLOADED_PROCESSED("fina2.web.uploaded.processed"),
	UPLOADED_ERROR_PROCESSING("fina2.web.uploaded.error.processing");
	
	private String code;
	
	private UploadFileStatus(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return code;
	}
}
