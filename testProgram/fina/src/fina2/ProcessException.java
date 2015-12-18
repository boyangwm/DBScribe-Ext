package fina2;

import fina2.returns.ProcessItem;

@SuppressWarnings("serial")
public class ProcessException extends RuntimeException {

	public enum Reason {
		NumberFormat, ComparisonRule, WrongDateFormat
	}

	private ProcessItem processItem;

	private Reason reason;

	public ProcessException(String message, ProcessItem processItem, Reason reason) {
		this(message, null, processItem, reason);
	}

	public ProcessException(String message, Throwable cause, ProcessItem processItem, Reason reason) {
		super(message, cause);
		this.processItem = processItem;
		this.reason = reason;
	}

	public ProcessItem getProcessItem() {
		return processItem;
	}

	public void setProcessItem(ProcessItem processItem) {
		this.processItem = processItem;
	}

	public Reason getReason() {
		return reason;
	}

	public void setReason(Reason reason) {
		this.reason = reason;
	}

}
