package com.gorillalogic.monkeyconsole.editors.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.gorillalogic.cloud.ideversion.CloudConstants;
import com.gorillalogic.cloud.ideversion.FileUtils;
import com.gorillalogic.cloud.ideversion.HttpUtils;
import com.gorillalogic.monkeyconsole.plugin.FoneMonkeyPlugin;
import com.gorillalogic.monkeyconsole.preferences.PreferenceConstants;

public class CloudServices {
	static String learnMoreUrl = "http://www.gorillalogic.com/cloud/overview";
	public static String registerUrl = "https://www.gorillalogic.com/cloud-register";
	static String acceptTermsUrl = "https://www.gorillalogic.com/cloudmonkey/license-agreement";
	static String logEventsUrl = "https://cloud.gorillalogic.com" + CloudConstants.LOG_EVENT;
	static String userToken = "";
	
	protected static String getControllerProtocol() {
		String protocol = FoneMonkeyPlugin.getDefault().getPreferenceStore()
			.getString(PreferenceConstants.P_CONTROLLER_PROTOCOL);
		if (protocol == null || protocol.length()==0) {
			protocol = "https";
		}
		return protocol;
	}

	protected static String getControllerPort(String protocol) {
		if (protocol.equals("http")) {
			String s=FoneMonkeyPlugin.getDefault().getPreferenceStore()
					.getString(PreferenceConstants.P_CONTROLLER_PORT);
			if (s!=null && s.length()>0) {
				return s;
			} else {
				return "80";
			}
		} else {
			String s = FoneMonkeyPlugin.getDefault().getPreferenceStore()
					.getString(PreferenceConstants.P_CONTROLLER_SSL_PORT);
			if (s!=null && s.length()>0) {
				return s;
			} else {
				return "443";
			}
		}
	}

	protected static String getControllerUrlRoot() {
		String protocol = CloudServices.getControllerProtocol(); 
		return protocol + "://"
				+ CloudServices.getControllerHost() + ":"
				+ CloudServices.getControllerPort(protocol);
	}
	
	public static String getToken() throws CloudServiceException {
		if (userToken == null || userToken.length() == 0
				|| checkTokenValidityAgainstServer(userToken)) {
			userToken = login(
					FoneMonkeyPlugin.getDefault().getPreferenceStore()
							.getString(PreferenceConstants.P_CLOUDUSR),
					FoneMonkeyPlugin.getDefault().getPreferenceStore()
							.getString(PreferenceConstants.P_CLOUDPASS));
		}
		return userToken;
	}

	public static String getTokenAndUsername() throws CloudServiceException {
		return "\"token\":\""
				+ getToken()
				+ "\",\"username\":\""
				+ FoneMonkeyPlugin.getDefault().getPreferenceStore()
						.getString(PreferenceConstants.P_CLOUDUSR) + "\"";
	}

	public static JSONObject getDeviceTypes() throws CloudServiceException {
		String errorMessage = "CloudMonkey Appliance at " + CloudServices.getControllerHost()
				+ " could not be reached.";
		try {
			String url = CloudServices.getControllerUrlRoot() + CloudConstants.INFO_PARAMS;
			JSONObject jo = new JSONObject(HttpUtils.get(url));
			// log("getDeviceTypes() elapsed=" + (System.currentTimeMillis() - start) + "ms");
			return jo;
		} catch (JSONException e) {
			throw new CloudServiceException(errorMessage, e);
		} catch (IOException e) {
			throw new CloudServiceException(errorMessage, e);
		}
	}

	// Ping is an alias for getDeviceTypes(). This simply hits a valid CMA endpoint to establish
	// availability.
	public static void ping() throws CloudServiceException {
		getDeviceTypes();
	}

	/**
	 * Log an event with the requested tag in a separate thread
	 * 
	 * @param tag
	 *            the tag for the event. should be a legal event tag, i.e. expected server-side.
	 *            should never be null
	 */
	public static void logEventAsync(String tag) {
		logEventAsync(tag, null);
	}

	/**
	 * Log an event with the requested tag and data in a separate thread
	 * 
	 * @param tag
	 *            the tag for the event. should be a legal event tag, i.e. expected server-side.
	 *            should never be null
	 * @param data
	 *            optional JSON-encoded data providing additional context for the event (e.g. jobId
	 *            or user)
	 */
	public static void logEventAsync(String tag, String data) {
		logEventAsync(tag, data, null);
	}

	/**
	 * Log an event with the requested tag, data, and timestamp in a separate thread
	 * 
	 * @param tag
	 *            the tag for the event. should be a legal event tag, i.e. expected server-side.
	 *            should never be null
	 * @param data
	 *            optional JSON-encoded data providing additional context for the event (e.g. jobId
	 *            or user)
	 * @param timestamp
	 *            timestamp for the event; if null, one will be generated
	 */
	public static void logEventAsync(String tag, String data, Date timestamp) {
		final String ftag = tag;
		final String fdata = data;
		final Date ftimestamp = timestamp;
		new Thread(new Runnable() {
			public void run() {
				try {
					logEvent(ftag, fdata, ftimestamp);
				} catch (Exception e) {
					// log("exception caught in logEventAsync(), ignoring. Stack trace for diagnostic purposes:", e);
				}
			}
		}).start();
	}

	public static JSONObject logEvent(String tag) throws CloudServiceException {
		return logEvent(tag, null);
	}

	public static JSONObject logEvent(String tag, String data) throws CloudServiceException {
		return logEvent(tag, data, null);
	}

	/**
	 * Log an event to the server with the given tag, data, and timestamp
	 * 
	 * @param tag
	 *            the tag for the event. should be a legal event tag, i.e. expected server-side.
	 *            should never be null
	 * @param data
	 *            optional JSON-encoded data providing additional context for the event (e.g. jobId
	 *            or user)
	 * @param timestamp
	 *            timestamp for the event; if null, one will be generated
	 * 
	 * @return the response from the server
	 */
	public static JSONObject logEvent(String tag, String data, Date timestamp)
			throws CloudServiceException {
		boolean eventConsent = false;
		if (FoneMonkeyPlugin.getDefault().getPreferenceStore()
				.contains(PreferenceConstants.P_LOGEVENTCONSENT)) {
			eventConsent = FoneMonkeyPlugin.getDefault().getPreferenceStore()
					.getBoolean(PreferenceConstants.P_LOGEVENTCONSENT);
		}
		if (!eventConsent) {
			return new JSONObject();
		}
		return logEventInternal(tag, data, timestamp);
	}

	private static JSONObject logEventInternal(String tag, String data, Date timestamp)
			throws CloudServiceException {
		try {
			if (data == null || data.length() == 0) {
				data = "{}";
			}
			if (timestamp == null) {
				timestamp = new Date();
			}
			JSONObject mainObject = new JSONObject();
			JSONObject dataWrapper = new JSONObject();
			dataWrapper.put("id", -1);
			dataWrapper.put("tag", tag);
			SimpleDateFormat formatter;

			formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
			dataWrapper.put("timestamp", formatter.format(timestamp));
			dataWrapper.put("data", data);
			mainObject.put("data", dataWrapper);
			mainObject.put("message", "logEvent");

			String json = HttpUtils.post(CloudServices.logEventsUrl, mainObject.toString());
			JSONObject jo = new JSONObject(json);
			// log("logEvent() elapsed=" + (System.currentTimeMillis() - start) + "ms");
			return jo;
		} catch (IOException ex) {
			//log("error caught in logEvent()", ex);
			return new JSONObject();
		} catch (JSONException ex) {
			log("error caught in logEvent()", ex);
			return new JSONObject();
		} catch (Exception e) {
			log("error caught in logEvent()", e);
			return new JSONObject();
		}
	}

	/**
	 * OptOut the current user from Event Reporting
	 */
	public static void optOutAsync() {
		new Thread(new Runnable() {
			public void run() {
				try {
					optOut();
				} catch (Exception e) {
					log("exception caught in optOutAsync(), ignoring. Stack trace for diagnostic purposes:",
							e);
				}
			}
		}).start();
	}

	public static void optOut() throws CloudServiceException {
		logEventInternal(
				"MONKEYTALK_IDE_LOGEVENT_OPT_OUT",
				"username="
						+ FoneMonkeyPlugin.getDefault().getPreferenceStore()
								.getString(PreferenceConstants.P_CLOUDUSR), new Date());
	}

	/**
	 * OptIn the current user from Event Reporting
	 */
	public static void optInAsync() {
		new Thread(new Runnable() {
			public void run() {
				try {
					optIn();
				} catch (Exception e) {
					log("exception caught in OptInAsync(), ignoring. Stack trace for diagnostic purposes:",
							e);
				}
			}
		}).start();
	}

	public static void optIn() throws CloudServiceException {
		logEventInternal(
				"MONKEYTALK_IDE_LOGEVENT_OPT_IN",
				"username="
						+ FoneMonkeyPlugin.getDefault().getPreferenceStore()
								.getString(PreferenceConstants.P_CLOUDUSR), new Date());
	}

	public static boolean checkTokenValidityAgainstServer(String token)
			throws CloudServiceException {
		return true;
	}

	public static String login(String username, String password) throws CloudServiceException {
		String url = CloudServices.getControllerUrlRoot() + CloudConstants.USER_LOGIN;
		String token = "";
		String json = null;
		try {
			json = HttpUtils.post(url, "{ \"username\": \"" + username + "\", \"password\": \""
					+ password + "\" }");
			if (null == json) {
				throw new CloudServiceException(
						"CloudMonkey authorization service is down for maintenance");
			}

			JSONObject jo = new JSONObject(json);
			if (jo.has("message") && jo.getString("message").equalsIgnoreCase("error")) {
				String errorMessage = jo.getString("message");
				if (jo.has("data") && jo.getString("data").length() > 0) {
					errorMessage = jo.getString("data");
				}
				throw new CloudServiceException(errorMessage);
			}

			if (jo.has("data")) {
				token = jo.getJSONObject("data").getString("token");
			}

		} catch (IOException e) {
			// log("error caught in login()", e);
		} catch (JSONException e) {
			log("non-JSON response to login: \"" + json + "\"", e);
		}

		return token;
	}

	public static JSONObject submitJob(String script, File proj, File apk, String jobName,
			String thinktime, String timeout, List<String> params, String jobType)
			throws CloudServiceException {
		String url = CloudServices.getControllerUrlRoot() + CloudConstants.JOB_LAUNCH;
		try {
			long start = System.currentTimeMillis();
			String ret = sendFormPost(url, apk, proj, script, thinktime, timeout, params, jobType);
			log("submitJob() elapsed=" + (System.currentTimeMillis() - start) + "ms");
			return new JSONObject(ret);
		} catch (Exception ex) {
			log("POST failed in submitJob(), throwing exception - stack trace here just in case:",
					ex);
			throw new CloudServiceException("POST failed", ex);

		}

	}

	private static String sendFormPost(String url, File apk, File proj, String script,
			String thinktime, String timeout, List<String> params, String jobType)
			throws IOException {

		HttpClient client = getHttpClientForFormPost();
		HttpUtils.setupProxy(client);
		
		try {
			HttpPost post = new HttpPost(url);

			MultipartEntity multipart = new MultipartEntity();
			multipart.addPart(
					"username",
					new StringBody(FoneMonkeyPlugin.getDefault().getPreferenceStore()
							.getString(PreferenceConstants.P_CLOUDUSR), Charset.forName("UTF-8")));
			multipart.addPart(
					"password",
					new StringBody(FoneMonkeyPlugin.getDefault().getPreferenceStore()
							.getString(PreferenceConstants.P_CLOUDPASS), Charset.forName("UTF-8")));

			if (thinktime != null && thinktime.length() > 0)
				multipart.addPart("thinktime", new StringBody(thinktime, Charset.forName("UTF-8")));
			if (timeout != null && timeout.length() > 0)
				multipart.addPart("timeout", new StringBody(timeout, Charset.forName("UTF-8")));

			multipart.addPart("script", new StringBody(script, Charset.forName("UTF-8")));

			multipart.addPart("type", new StringBody(jobType, Charset.forName("UTF-8")));

			if (apk != null) {
				multipart.addPart("binary", new FileBody(apk));
			}

			if (proj != null) {
				multipart.addPart("project", new FileBody(proj));
			}

			for (String param : params) {
				String encodedParam = java.net.URLEncoder.encode(param, "UTF-8");
				multipart.addPart(encodedParam, new StringBody("on", Charset.forName("UTF-8")));
			}

			post.setEntity(multipart);

			HttpResponse resp = client.execute(post);
			
			HttpEntity out = resp.getEntity();
			InputStream in = out.getContent();
			String responseAsString=FileUtils.readStream(in);;
			
			return responseAsString;
			
		} catch (Exception ex) {
			throw new IOException("POST failed", ex);
		} finally {
			try {
				client.getConnectionManager().shutdown();
			} catch (Exception ex) {
				// ignore
			}
		}
	}
	
	private static HttpClient getHttpClientForFormPost() {
		HttpClient base = new DefaultHttpClient();
		if (CloudServices.getControllerProtocol().toLowerCase().equals("http")) {
			return base;
		} else {
			// some weird SSL stuff, I guess for the file uploads or multi-part?
			SSLContext ctx = null;
			try {
				ctx = SSLContext.getInstance("TLS");
			} catch (NoSuchAlgorithmException e) {
				log("exception in SSL setup for sendFormPost():", e);
			}
			X509TrustManager tm = new X509TrustManager() {

				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				@Override
				public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
						String authType) throws java.security.cert.CertificateException {
				}

				@Override
				public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
						String authType) throws java.security.cert.CertificateException {
				}
			};
			try {
				ctx.init(null, new TrustManager[] { tm }, null);
			} catch (KeyManagementException e) {
				log("exception in SSL setup for sendFormPost():", e);
			}
			SSLSocketFactory ssf = new SSLSocketFactory(ctx);
			ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			ClientConnectionManager ccm = base.getConnectionManager();
			SchemeRegistry sr = ccm.getSchemeRegistry();
			sr.register(new Scheme("https", ssf, 443));

			HttpClient client = new DefaultHttpClient(ccm, base.getParams());
			return client;
		}
	}

	public static JSONObject getJobHistory() throws CloudServiceException {

		try {
			String url = CloudServices.getControllerUrlRoot() + CloudConstants.USER_HISTORY;
			String json = HttpUtils.post(
					url,
					"{\"token\":\""
							+ getToken()
							+ "\",\"username\":\""
							+ FoneMonkeyPlugin.getDefault().getPreferenceStore()
									.getString(PreferenceConstants.P_CLOUDUSR) + "\"}");

			if (json == null)
				return null;
			JSONObject ret = new JSONObject(json);
			// log("getJobHistory() elapsed=" + (System.currentTimeMillis() - start) + "ms");
			return ret;
		} catch (IOException e) {
			return new JSONObject();
		} catch (JSONException e) {
			return new JSONObject();
		}
	}

	public static JSONObject getJobStatus(String jobid) throws CloudServiceException {
		String url = CloudServices.getControllerUrlRoot() + CloudConstants.JOB_STATUS;
		try {
			String json = HttpUtils.post(url, "{\"message\":\"jobRequest\",\"data\":{"
					+ getTokenAndUsername() + ",\"id\":" + jobid + "}}");
			JSONObject jo = new JSONObject(json);
			// log("getJobStatus() elapsed=" + (System.currentTimeMillis() - start) + "ms");
			return jo;
		} catch (IOException ex) {
			log("exception in getJobStatus():", ex);
			return new JSONObject();
		} catch (JSONException ex) {
			log("non-JSON response in getJobStatus():", ex);
			return new JSONObject();
		}
	}

	public static JSONObject getJobResults(String jobid) throws CloudServiceException {
		String url = CloudServices.getControllerUrlRoot() + CloudConstants.JOB_RESULTS;
		try {
			String json = HttpUtils.post(url, "{\"message\":\"jobRequest\",\"data\":{"
					+ getTokenAndUsername() + ",\"id\":" + jobid + "}}");
			JSONObject jo = new JSONObject(json);
			// log("getJobResults() elapsed=" + (System.currentTimeMillis() - start) + "ms");
			return jo;
		} catch (IOException ex) {
			log("exception in getJobResults():", ex);
			return new JSONObject();
		} catch (JSONException ex) {
			log("non-JSON response in getJobResults():", ex);
			return new JSONObject();
		}
	}

	public static String readStream(InputStream in) throws IOException {
		StringBuilder sb = new StringBuilder();
		String line = null;
		BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
		while ((line = reader.readLine()) != null) {
			sb.append(line).append('\n');
		}

		return (sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "");
	}

	protected static PrintStream logStream = System.out;

	protected static void log(String s) {
		logStream.println(new Date() + " CloudServices: " + s);
	}

	protected static void log(String s, Exception e) {
		logStream.println(new Date() + " CloudServices: " + s);
		e.printStackTrace(logStream);
	}

	public static String getControllerHost() {
		String controllerHostPref = FoneMonkeyPlugin.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.P_CONTROLLER_HOST);
		if (controllerHostPref != null && controllerHostPref.length() > 0) {
			return controllerHostPref;
		}
		return CloudConstants.DEFAULT_CONTROLLER_HOST;
	}

	public static int getControllerPort() {
		String controllerPortPref = FoneMonkeyPlugin.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.P_CONTROLLER_PORT);
		if (controllerPortPref != null && controllerPortPref.length() > 0) {
			int portPref = -1;
			try {
				portPref = Integer.parseInt(controllerPortPref);
			} catch (NumberFormatException nfe) {
				portPref = -1;
			}
			if (portPref != -1) {
				return portPref;
			}
		}
		return CloudConstants.DEFAULT_CONTROLLER_PUBLIC_PORT;
	}

	public static int getControllerSslPort() {
		String controllerPortPref = FoneMonkeyPlugin.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.P_CONTROLLER_SSL_PORT);
		if (controllerPortPref != null && controllerPortPref.length() > 0) {
			int portPref = -1;
			try {
				portPref = Integer.parseInt(controllerPortPref);
			} catch (NumberFormatException nfe) {
				portPref = -1;
			}
			if (portPref != -1) {
				return portPref;
			}
		}
		return CloudConstants.DEFAULT_CONTROLLER_SSL_PORT;
	}
}
