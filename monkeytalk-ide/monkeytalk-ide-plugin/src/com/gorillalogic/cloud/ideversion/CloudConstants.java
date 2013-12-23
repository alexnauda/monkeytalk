package com.gorillalogic.cloud.ideversion;

public class CloudConstants {
	public static final String DEFAULT_CONTROLLER_HOST = System
			.getenv("MONKEYTALK_IDE_CLOUD_CONTROLLER_HOST") != null ? System
			.getenv("MONKEYTALK_IDE_CLOUD_CONTROLLER_HOST") : "cloud.gorillalogic.com";

	public static final int DEFAULT_CONTROLLER_PUBLIC_PORT = System
			.getenv("MONKEYTALK_IDE_CLOUD_CONTROLLER_PORT") != null ? Integer.parseInt(System
			.getenv("MONKEYTALK_IDE_CLOUD_CONTROLLER_PORT")) : 8080;

	public static final int DEFAULT_CONTROLLER_SSL_PORT = System
			.getenv("MONKEYTALK_IDE_CLOUD_CONTROLLER_SSL_PORT") != null ? Integer.parseInt(System
			.getenv("MONKEYTALK_IDE_CLOUD_CONTROLLER_SSL_PORT")) : 4430;

	public static final String DEFAULT_CONTROLLER_PROTOCOL = 
					(System.getenv("MONKEYTALK_IDE_CLOUD_CONTROLLER_PROTOCOL") != null ?
					 System.getenv("MONKEYTALK_IDE_CLOUD_CONTROLLER_PROTOCOL") : "https");

	// public
	public static final String JOB_LAUNCH = "/job/launch/";
	public static final String JOB_STATUS = "/job/status/";
	public static final String JOB_RESULTS = "/job/results/";
	public static final String USER_LOGIN = "/user/login/";
	public static final String USER_LOGOUT = "/user/logout/";
	public static final String USER_HISTORY = "/user/history/";
	public static final String INFO_PARAMS = "/info/params/";
	public static final String INFO_TYPES = "/info/types/";
	public static final String LOG_EVENT = "/log/event/";

	private CloudConstants() {
	}
}
