package fina2;

@SuppressWarnings("serial")
public class FinaGuiUpdateException extends Exception {
	/**
	 * Exception types
	 */
	public enum Type {
		PROPERTY_NOT_FOUND("finaGuiUpdateException.propertyNotFound"), INVALID_PROPERTY(
				"finaGuiUpdateException.invalidProperty"), FILE_NOT_FOUND(
				"finaGuiUpdateException.fileNotFound"), INVALID_FILE(
				"finaGuiUpdateException.invalidFile"), VERSION_NOT_FOUND(
				"finaGuiUpdateException.versionNotFound");

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
	public FinaGuiUpdateException(Type type) {
		this.type = type;
	}

	/** Creates the exception of given type and parameters */
	public FinaGuiUpdateException(Type type, String[] params) {
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
