/*  MonkeyTalk - a cross-platform functional testing tool
    Copyright (C) 2012 Gorilla Logic, Inc.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package com.gorillalogic.monkeytalk.ant;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

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
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.gorillalogic.monkeytalk.CommandWorld;
import com.gorillalogic.monkeytalk.agents.AndroidEmulatorAgent;
import com.gorillalogic.monkeytalk.processor.Globals;
import com.gorillalogic.monkeytalk.processor.PlaybackListener;
import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.processor.PlaybackStatus;
import com.gorillalogic.monkeytalk.processor.Runner;
import com.gorillalogic.monkeytalk.processor.Scope;
import com.gorillalogic.monkeytalk.processor.SuiteListener;
import com.gorillalogic.monkeytalk.processor.report.Report;
import com.gorillalogic.monkeytalk.sender.Response;
import com.gorillalogic.monkeytalk.utils.AndroidUtils;
import com.gorillalogic.monkeytalk.utils.FileUtils;

public class RunTask extends Task {
	private String agent;
	private File script;
	private File suite;
	private String host;
	private int port;
	private File adb;
	private String callbackurl;
	private String jobrefparams;
	private String adbSerial;
	private int adbLocalPort;
	private int adbRemotePort;
	private File reportdir;
	private String text;
	private boolean verbose = false;
	private int timeout;
	private int thinktime;
	private int startup;
	private boolean screenshots = false;
	private boolean screenshotOnError = true;
	private String globals;
	private static final String TEMP_FILE = ".tmp" + CommandWorld.SCRIPT_EXT;

	private final PlaybackListener scriptListener = new PlaybackListener() {
		private String indent = "";

		@Override
		public void onScriptStart(Scope scope) {
			onPrint((adbSerial != null ? adbSerial + ": " : "") + indent.replaceAll(" ", "-")
					+ "-run: " + (scope.getFilename() == null ? "commands" : scope.getFilename()));
			indent += "  ";
		}

		@Override
		public void onScriptComplete(Scope scope, PlaybackResult result) {
			indent = indent.substring(2);
			onPrint((adbSerial != null ? adbSerial + ": " : "") + indent.replaceAll(" ", "-")
					+ "-end: " + (scope.getFilename() == null ? "commands" : scope.getFilename()));
		}

		@Override
		public void onStart(Scope scope) {
			if (!"debug".equalsIgnoreCase(scope.getCurrentCommand().getComponentType())) {
				onPrint((adbSerial != null ? adbSerial + ": " : "") + indent
						+ scope.getCurrentCommand());
			}
		}

		@Override
		public void onComplete(Scope scope, Response response) {
			if (response.getMessage() != null && response.getMessage().length() > 0) {
				onPrint((adbSerial != null ? adbSerial + ": " : "") + indent + "-> "
						+ response.getStatus() + " : " + response.getMessage());
			}
		}

		@Override
		public void onPrint(String message) {
			log(message.endsWith("\n") ? message.substring(0, message.length() - 1) : message);
		}
	};

	private final PlaybackListener scriptListenerForSuite = new PlaybackListener() {
		private String indent = "";

		@Override
		public void onScriptStart(Scope scope) {
			indent += "  ";
		}

		@Override
		public void onScriptComplete(Scope scope, PlaybackResult result) {
			indent = indent.substring(2);
		}

		@Override
		public void onStart(Scope scope) {
			if (verbose) {
				onPrint((adbSerial != null ? adbSerial + ": " : "") + indent
						+ scope.getCurrentCommand());
			}
		}

		@Override
		public void onComplete(Scope scope, Response response) {
			if (verbose && response.getMessage() != null && response.getMessage().length() > 0) {
				onPrint((adbSerial != null ? adbSerial + ": " : "") + indent + "-> "
						+ response.getStatus() + " : " + response.getMessage());
			}
		}

		@Override
		public void onPrint(String message) {
			log(message);
		}
	};

	private final SuiteListener suiteListener = new SuiteListener() {

		@Override
		public void onRunStart(int total) {
		}

		@Override
		public void onRunComplete(PlaybackResult result, Report report) {
		}

		@Override
		public void onTestStart(String name, int num, int total) {
			log((adbSerial != null ? adbSerial + ": " : "") + "  " + num + " : " + name);
		}

		@Override
		public void onTestComplete(PlaybackResult result, Report report) {
			log((adbSerial != null ? adbSerial + ": " : "")
					+ "  -> "
					+ result.getStatus()
					+ (result.getMessage() != null && result.getMessage().length() > 0 ? " : "
							+ result.getMessage() : ""));
		}

		@Override
		public void onSuiteStart(int total) {
			log((adbSerial != null ? adbSerial + ": " : "") + "-start suite (" + total
					+ (total == 1 ? " test" : " tests") + ")");
		}

		@Override
		public void onSuiteComplete(PlaybackResult result, Report report) {
			log((adbSerial != null ? adbSerial + ": " : "") + "-end suite");
			if (callbackurl != null) {
				sendReport();
			}
		}
	};

	public RunTask() {
		port = -1;
		adbLocalPort = -1;
		adbRemotePort = -1;
		timeout = -1;
		thinktime = -1;
		startup = -1;
	}

	public void execute() throws BuildException {
		Runner runner = new Runner(agent, host, port);

		if (adb != null) {
			runner.setAdb(adb);
		} else if (agent != null && agent.equalsIgnoreCase("AndroidEmulator")) {
			File adb = AndroidUtils.getAdb();
			runner.setAdb(adb);
		}
		if (adbSerial != null) {
			runner.setAgentProperty(AndroidEmulatorAgent.ADB_SERIAL_PROP, adbSerial);
		}
		if (adbLocalPort > 0) {
			runner.setAgentProperty(AndroidEmulatorAgent.ADB_LOCAL_PORT_PROP,
					Integer.toString(adbLocalPort));
		}
		if (adbRemotePort > 0) {
			runner.setAgentProperty(AndroidEmulatorAgent.ADB_REMOTE_PORT_PROP,
					Integer.toString(adbRemotePort));
		}

		runner.setReportdir(reportdir);
		runner.setScriptListener(scriptListener);
		runner.setSuiteListener(suiteListener);
		runner.setVerbose(false);
		runner.setGlobalTimeout(timeout);
		runner.setGlobalThinktime(thinktime);
		runner.setGlobalScreenshotOnError(screenshotOnError);
		runner.setTakeAfterScreenshot(screenshots);
		runner.setTakeAfterMetrics(screenshots);
		PlaybackResult result = null;

		try {
			if (script != null && suite != null) {
				throw new BuildException(
						"You cannot specify both script and suite in the run task.");
			} else if (script != null && text != null) {
				throw new BuildException(
						"You cannot specify both script and inline commands in the run task.");
			} else if (suite != null && text != null) {
				throw new BuildException(
						"You cannot specify both suite and inline commands in the run task.");
			}

			if (!runner.waitUntilReady(startup)) {
				throw new BuildException("Unable to startup MonkeyTalk connection - timeout after "
						+ startup + "s");
			}

			if (script != null) {
				if (verbose) {
					log("running script " + script.getName() + "...");
				}
				result = runner.run(script, Globals.parse(globals));
			} else if (suite != null) {
				if (verbose) {
					log("running suite " + suite.getName() + "...");
				}
				runner.setScriptListener(scriptListenerForSuite);
				result = runner.run(suite, Globals.parse(globals));
			} else if (text != null) {
				File tmp = new File(getProject().getBaseDir(), TEMP_FILE);
				String subbed = getProject().replaceProperties(text.trim());

				try {
					FileUtils.writeFile(tmp, subbed);
				} catch (IOException ex) {
					throw new BuildException(ex);
				}

				result = runner.run(tmp, Globals.parse(globals));

				tmp.delete();
			} else {
				throw new BuildException("Nothing to run.");
			}
		} catch (RuntimeException ex) {
			throw new BuildException(ex.getMessage());
		}

		if (result.getStatus() != PlaybackStatus.OK) {
			throw new BuildException(result.toString());
		}

		if (verbose) {
			log("...done");
		}
	}

	public void setAgent(String agent) {
		this.agent = agent;
	}

	public void setScript(File script) {
		if (script.getName().endsWith(CommandWorld.SUITE_EXT)) {
			// user gave us a suite instead of a script, so fix it for them
			this.suite = script;
			this.script = null;
		} else {
			this.script = script;
		}
	}

	public void setSuite(File suite) {
		this.suite = suite;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setAdb(File adb) {
		this.adb = adb;
	}

	public void setAdbSerial(String adbSerial) {
		this.adbSerial = adbSerial;
	}

	public void setAdbLocalPort(int adbLocalPort) {
		this.adbLocalPort = adbLocalPort;
	}

	public void setAdbRemotePort(int adbRemotePort) {
		this.adbRemotePort = adbRemotePort;
	}

	public void setReportdir(File reportdir) {
		this.reportdir = reportdir;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public void setScreenshots(boolean screenshots) {
		this.screenshots = screenshots;
	}

	public void setScreenshotOnError(boolean screenshotOnError) {
		this.screenshotOnError = screenshotOnError;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void setThinktime(int thinktime) {
		this.thinktime = thinktime;
	}

	public void setStartup(int startup) {
		this.startup = startup;
	}

	public void setGlobals(String globals) {
		this.globals = globals;
	}

	public void setCallbackurl(String callbackurl) {
		this.callbackurl = callbackurl;
	}

	public void setJobrefparams(String jobrefparams) {
		this.jobrefparams = jobrefparams;
	}

	public void addText(String text) {
		this.text = text;
	}

	public void sendReport() {
		try {
			File zippedReports = FileUtils.zipDirectory(reportdir, true, false);
			System.out.println(zippedReports.getAbsolutePath());
			Map<String, String> additionalParams = new HashMap<String, String>();
			StringTokenizer st2 = new StringTokenizer(jobrefparams, ",");

			while (st2.hasMoreElements()) {
				String param = (String) st2.nextElement();
				StringTokenizer st3 = new StringTokenizer(param, ":");
				additionalParams.put((String) st3.nextElement(), (String) st3.nextElement());

			}

			sendFormPost(callbackurl, zippedReports, additionalParams);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private String sendFormPost(String url, File proj, Map<String, String> additionalParams)
			throws IOException {

		HttpClient base = new DefaultHttpClient();
		SSLContext ctx = null;

		try {
			ctx = SSLContext.getInstance("TLS");
		} catch (NoSuchAlgorithmException ex) {
			log("exception in sendFormPost():");
		}

		X509TrustManager tm = new X509TrustManager() {
			@Override
			public X509Certificate[] getAcceptedIssuers() {
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
		} catch (KeyManagementException ex) {
			log("exception in sendFormPost():");
		}

		SSLSocketFactory ssf = new SSLSocketFactory(ctx);
		ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		ClientConnectionManager ccm = base.getConnectionManager();
		SchemeRegistry sr = ccm.getSchemeRegistry();
		sr.register(new Scheme("https", ssf, 443));

		HttpClient client = new DefaultHttpClient(ccm, base.getParams());
		try {
			HttpPost post = new HttpPost(url);

			MultipartEntity multipart = new MultipartEntity();
			for (String key : additionalParams.keySet())
				multipart.addPart(key,
						new StringBody(additionalParams.get(key), Charset.forName("UTF-8")));

			if (proj != null) {
				multipart.addPart("uploaded_file", new FileBody(proj));
			}

			post.setEntity(multipart);

			HttpResponse resp = client.execute(post);

			HttpEntity out = resp.getEntity();

			InputStream in = out.getContent();
			return FileUtils.readStream(in);
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
}