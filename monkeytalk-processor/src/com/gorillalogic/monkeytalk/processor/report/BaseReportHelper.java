package com.gorillalogic.monkeytalk.processor.report;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.gorillalogic.monkeytalk.utils.FileUtils;
import com.gorillalogic.monkeytalk.utils.TimeUtils;

public abstract class BaseReportHelper {
	public static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("MMMM d, yyyy HH:mm:ss z");
	public static final DecimalFormat DEC_FMT = new DecimalFormat("0.000");
	public static final DecimalFormat INT_FMT = new DecimalFormat("0");

	protected File master;
	protected String masterName;
	protected String templateName;
	private File tempDir;

	public BaseReportHelper(String masterName, String templateName) {
		this.masterName = masterName;
		this.templateName = templateName;
	}

	public File getTempDir() throws IOException {
		if (tempDir == null) {
			tempDir = FileUtils.tempDir();
		}
		return tempDir;
	}

	public String getTemplateContents() throws IOException {
		return FileUtils.readStream(getClass().getResourceAsStream(templateName));
	}

	public File getMaster() throws IOException {
		if (master == null) {
			master = new File(getTempDir(), masterName);
		}
		return master;
	}

	public void log(String s) {
		System.out.println(TimeUtils.formatDate(new Date()) + " " + getClass().getSimpleName()
				+ ": " + s);
	}
}