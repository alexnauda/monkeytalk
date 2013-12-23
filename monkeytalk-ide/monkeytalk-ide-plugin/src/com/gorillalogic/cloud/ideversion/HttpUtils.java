package com.gorillalogic.cloud.ideversion;

import java.io.IOException;
import java.io.InputStream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.NTLMSchemeFactory;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import org.eclipse.core.runtime.Status;
import com.gorillalogic.monkeyconsole.plugin.FoneMonkeyPlugin;
import com.gorillalogic.monkeyconsole.preferences.PreferenceConstants;

public class HttpUtils {

	private static final int CONNECTION_TIMEOUT = 15000; // how long to wait for the server to
															// accept the HTTP connection request
	private static final int SO_TIMEOUT = 15000; // how long to wait for the first/next byte of data
													// to be returned, once a connection has been
													// established (?)

	private HttpUtils() {
	}

	public static String get(String url) throws IOException {
		InputStream in = null;

		try {
			HttpClient base = new DefaultHttpClient();

			SSLContext ctx = SSLContext.getInstance("TLS");
			X509TrustManager tm = new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] xcs, String string)
						throws CertificateException {

				}

				public void checkServerTrusted(X509Certificate[] xcs, String string)
						throws CertificateException {
				}

				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				@Override
				public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
						String authType) throws java.security.cert.CertificateException {
					// TODO Auto-generated method stub

				}

				@Override
				public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
						String authType) throws java.security.cert.CertificateException {
					// TODO Auto-generated method stub

				}
			};
			ctx.init(null, new TrustManager[] { tm }, null);
			SSLSocketFactory ssf = new SSLSocketFactory(ctx);
			ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			ClientConnectionManager ccm = base.getConnectionManager();
			SchemeRegistry sr = ccm.getSchemeRegistry();
			sr.register(new Scheme("https", ssf, 443));

			HttpParams myParams = base.getParams();
			HttpConnectionParams.setConnectionTimeout(myParams, CONNECTION_TIMEOUT);
			HttpConnectionParams.setSoTimeout(myParams, SO_TIMEOUT);
			HttpClient client = new DefaultHttpClient(ccm, myParams);
			HttpGet get = new HttpGet(url);

			HttpResponse resp = client.execute(get);
			in = resp.getEntity().getContent();
			return FileUtils.readStream(in);
		} catch (Exception ex) {
			throw new IOException("GET failed", ex);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ex) {
					// ignore
				}
			}
		}
	}

	public static String post(String url, String json) throws IOException {
		InputStream in = null;
		try {
			// Tried downcasting to the AbstractHttpClient
			//
			// HttpClient 4.2 Docs --
			// http://hc.apache.org/httpcomponents-client-4.2.x/httpclient/apidocs/index.html
			AbstractHttpClient base = new DefaultHttpClient();

			SSLContext ctx = SSLContext.getInstance("TLS");
			X509TrustManager tm = new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] xcs, String string)
						throws CertificateException {

				}

				public void checkServerTrusted(X509Certificate[] xcs, String string)
						throws CertificateException {
				}

				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				@Override
				public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
						String authType) throws java.security.cert.CertificateException {
					// TODO Auto-generated method stub

				}

				@Override
				public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
						String authType) throws java.security.cert.CertificateException {
					// TODO Auto-generated method stub

				}
			};
			ctx.init(null, new TrustManager[] { tm }, null);
			SSLSocketFactory ssf = new SSLSocketFactory(ctx);
			ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			ClientConnectionManager ccm = base.getConnectionManager();
			SchemeRegistry sr = ccm.getSchemeRegistry();
			sr.register(new Scheme("https", ssf, 443));

			HttpParams myParams = base.getParams();
			HttpConnectionParams.setConnectionTimeout(myParams, CONNECTION_TIMEOUT);
			HttpConnectionParams.setSoTimeout(myParams, SO_TIMEOUT);
			HttpClient client = new DefaultHttpClient(ccm, myParams);
			HttpPost post = new HttpPost(url);
			post.setEntity(new StringEntity(json, "UTF-8"));
			post.setHeader("Content-type", "application/json;charset=utf-8");

			setupProxy(client);
			
			HttpContext localContext = new  BasicHttpContext(); // helps debug if needed
			// showHttpAuthDiagnostics("BEFORE CALL", client, post, localContext);
			HttpResponse resp = client.execute(post, localContext);
			// showHttpAuthDiagnostics("AFTER CALL", client, post, localContext);
			
			in = resp.getEntity().getContent();
			String body = FileUtils.readStream(in);
			return (resp.getStatusLine() != null
					&& resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK ? body : null);
		} catch (Exception ex) {
			throw new IOException("POST failed", ex);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ex) {
					// ignore
				}
			}
		}
	}
	
	public static void showHttpAuthDiagnostics(String id, HttpClient client, 
			HttpPost post, HttpContext localContext) {
		String diags = getHttpAuthDiagnostics(id,client,post,localContext);
		// System.out.println(diags);
		log(diags);
	}

	public static String getHttpAuthDiagnostics(String id, HttpClient client, 
			HttpPost post, HttpContext localContext) {
		StringBuilder sb = new StringBuilder();
		String nullExpected = "";
		if (id.equals("BEFORE CALL")) {
			nullExpected=" (as expected)";
		} else {
			nullExpected=" (not expected)";
		}
		
		sb.append("==================> HttpClient Diagnostics: ").append(id).append(" <====================\n");
		
		org.apache.http.auth.AuthState proxyAuthState = (org.apache.http.auth.AuthState) localContext.getAttribute(org.apache.http.client.protocol.ClientContext.PROXY_AUTH_STATE);
		if (proxyAuthState==null) {
			sb.append("Proxy auth state null " + nullExpected + "\n");
		} else {
			sb.append("Proxy auth state: " + proxyAuthState.getState()).append('\n');
			sb.append("Proxy auth scheme: " + proxyAuthState.getAuthScheme()).append('\n');
			sb.append("Proxy auth credentials: " + proxyAuthState.getCredentials()).append('\n');
		}
		
		org.apache.http.auth.AuthState targetAuthState = (org.apache.http.auth.AuthState) localContext.getAttribute(org.apache.http.client.protocol.ClientContext.TARGET_AUTH_STATE);
		if (targetAuthState==null) {
			sb.append("Target auth state null " + nullExpected + "\n");
		} else {
			sb.append("Target auth state: " + targetAuthState.getState()).append('\n');
			sb.append("Target auth scheme: " + targetAuthState.getAuthScheme()).append('\n');
			sb.append("Target auth credentials: " + targetAuthState.getCredentials()).append('\n');
		}
		
		CredentialsProvider cp = ((AbstractHttpClient)client).getCredentialsProvider();
		if (cp==null) {
			sb.append("CredentialsProvider was null!!!!!!!!!!!\n");
		} else {
			sb.append("CredentialsProvider=" + cp).append('\n');	
		}
		
		sb.append("================> END HttpClient Diagnostics: ").append(id).append(" <==================\n");
		return sb.toString();
	}	
	
	public static void setupProxy(HttpClient client) {
		// Set variables from the PreferenceStore
		boolean useProxy = FoneMonkeyPlugin.getDefault().getPreferenceStore()
				.getBoolean(PreferenceConstants.P_USE_PROXY);
		boolean useProxyAuthentication = FoneMonkeyPlugin.getDefault().getPreferenceStore()
				.getBoolean(PreferenceConstants.P_USE_PROXY_AUTHENTICATION);
		String proxyHost = FoneMonkeyPlugin.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.P_PROXY_HOST);
		int proxyPort = FoneMonkeyPlugin.getDefault().getPreferenceStore()
				.getInt(PreferenceConstants.P_PROXY_PORT);
		String proxyUsername = FoneMonkeyPlugin.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.P_PROXY_USERNAME);
		String proxyPassword = FoneMonkeyPlugin.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.P_PROXY_PASSWORD);

		if (useProxy) {
			if (proxyHost != null && proxyHost.length() > 0) {
				proxyHost = proxyHost.trim();

				HttpHost proxy = new org.apache.http.HttpHost(proxyHost, proxyPort);
				
				client.getParams().setParameter(
							org.apache.http.conn.params.ConnRoutePNames.DEFAULT_PROXY, proxy);

				if (useProxyAuthentication) {
					client.getParams().setParameter(
							org.apache.http.client.params.AllClientPNames.HANDLE_AUTHENTICATION,
							true);
					CredentialsProvider credsProvider=((AbstractHttpClient) client).getCredentialsProvider();
					if (credsProvider==null) {
						credsProvider = new BasicCredentialsProvider();
						((AbstractHttpClient) client).setCredentialsProvider(credsProvider);
					}
					
					UsernamePasswordCredentials creds = new UsernamePasswordCredentials(
							proxyUsername, proxyPassword);
					credsProvider.setCredentials(new AuthScope(proxyHost, proxyPort), creds);
					
					// Handle Windows NTLM authentication
					((AbstractHttpClient) client).getAuthSchemes().register("ntlm", new NTLMSchemeFactory());
				}
			}
		} else {
			return;
		}
	}

	private static void log(String s) {
		String pluginId = FoneMonkeyPlugin.getDefault().getBundle().getSymbolicName();
		FoneMonkeyPlugin.getDefault().getLog().log(new Status(Status.INFO,pluginId,s));
	}
	
}
