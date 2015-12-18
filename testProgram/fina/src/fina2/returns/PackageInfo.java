package fina2.returns;

import java.util.ArrayList;

public class PackageInfo {

	private ArrayList items = new ArrayList();

	private String name;

	private String bank;

	private String startDate;

	private String endDate;

	private String returnType;

	private String returnVersionCode;

	private String type;

	public PackageInfo() {
		name = "";
	}

	public PackageInfo(String bank, String startDate, String endDate, String returnType, String returnVersionCode, String type) {

		this.bank = bank;
		this.startDate = startDate;
		this.endDate = endDate;
		this.returnType = returnType;
		this.returnVersionCode = returnVersionCode;
		this.type = type;
	}

	public ArrayList getItems() {
		return items;
	}

	public String getBank() {
		return bank;
	}

	public String getEndDate() {
		return endDate;
	}

	public String getReturnType() {
		return returnType;
	}

	public String getReturnVersion() {
		return returnVersionCode;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setItems(ArrayList items) {
		this.items = items;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public int hashCode() {
		return toString().hashCode();
	}

	public String getName() {

		if (this.name == null) {

			StringBuffer buff = new StringBuffer();

			buff.append(bank).append(" ");
			buff.append(endDate).append(" ");
			buff.append(returnType).append(" ");
			buff.append(returnVersionCode).append(" ");
			buff.append(type).append(" ");

			this.name = buff.toString();
		}

		return name;
	}

	public String getReturnVersionCode() {
		return returnVersionCode;
	}

	public String toString() {

		return getName();
	}

	public boolean equals(Object obj) {

		boolean equals = false;

		if (obj instanceof PackageInfo && this.toString().equals(obj.toString())) {
			equals = true;
		}

		return equals;
	}
}
