package com.gorillalogic.monkeyconsole.cloudview.ui;

import java.util.Date;

public class JobRow {
	
	private int id;
	private String jobName;
	private Date startTime;
	private String status;
	private String message;
	private String actions;
	
    public JobRow(){
    	this(0, "", new Date(), "", "", "");
    }
	public JobRow(int id, String jobName, Date startTime, String status,
			String message, String actions) {
		super();
		this.id = id;
		this.jobName = jobName;
		this.startTime = startTime;
		this.status = status;
		this.message = message;
		this.actions = actions;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getActions() {
		return actions;
	}

	public void setActions(String actions) {
		this.actions = actions;
	}
	@Override
	public String toString() {
		return this.jobName;
	}
	
}
