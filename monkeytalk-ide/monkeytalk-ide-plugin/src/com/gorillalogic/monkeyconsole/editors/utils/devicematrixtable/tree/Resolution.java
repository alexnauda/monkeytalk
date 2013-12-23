package com.gorillalogic.monkeyconsole.editors.utils.devicematrixtable.tree;

public class Resolution {
	private String summary = "";
	private String description = "";
	private String androidVersion = "";
	private String param = "";

	public Resolution(String summary) {
		this.summary = summary;
	}

	public Resolution(String summary, String description, String param) {
		this.summary = summary;
		this.description = description;
		this.param = param;
	}

	public String getSummary() {
		return summary.trim();
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAndroidVersion() {
		return androidVersion;
	}

	public void setAndroidVersion(String androidVersion) {
		this.androidVersion = androidVersion;
	}

	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}

	@Override
	public String toString() {
		return "Resolution(summary=" + getSummary() + " description=" + description + " androidVersion="
				+ androidVersion + " param=" + param + ")";
	}
}
