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

package com.gorillalogic.fonemonkey.web;

import java.lang.reflect.Method;

import org.apache.commons.codec.net.URLCodec;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Message;
import android.view.KeyEvent;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.gorillalogic.fonemonkey.automators.WebViewAutomator;

/* 
 * Hack alert:
 * WebView.addJavaScriptInterface doesn't work if called after a page has already been loaded.
 * AndroidWebDriver relies on addJavaScriptInterface to bridge between JS and Java.
 * Because we might not find out about a WebView until after its initial page has been loaded,
 * we use this mechanism instead whereby we intercept dummy url requests.
 */
public class WebViewClientWrapper extends WebViewClient {
	private WebViewClient client;

	// private WebViewRecorder recorder;

	public WebViewClientWrapper(WebView view) {
		Method meth;
		try {
			// recorder = new WebViewRecorder(webView);
			// webView.addJavascriptInterface(recorder, "mtrecorder");

			meth = WebView.class.getDeclaredMethod("getWebViewClient", (Class<?>[]) null);
			this.client = (WebViewClient) meth.invoke(view, (Object[]) null);
			if (client == null) {
				client = new WebViewClient();
			}
		} catch (Exception e) {
			throw new IllegalStateException("Error getting WebViewClient: " + e.getMessage(), e);
		}
		view.setWebViewClient(this);
		// view.loadUrl("javascript:window.webdriver = {resultMethod: function(result) {window.location = \"http://mtdummy?monkeytalkresult=\" + result}}");

	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		String decoded;
		if (url.contains("mtdummy")) {
			try {
				decoded = new URLCodec().decode(url, "UTF-8");
				// We seem to be double-encoded...
				decoded = new URLCodec().decode(decoded, "UTF-8");
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
			String[] result = decoded.split("monkeytalkresult=");
			if (result.length == 1) {
				WebViewAutomator.reportResult(null);
				return true;
			}
			if (result.length > 1) {
				WebViewAutomator.reportResult(result[result.length - 1]);
				return true;
			}
		}
		return client.shouldOverrideUrlLoading(view, url);
	}

	public void onTooManyRedirects(WebView view, Message cancelMsg, Message continueMsg) {
		client.onTooManyRedirects(view, cancelMsg, continueMsg);
	}

	public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
		client.onReceivedError(view, errorCode, description, failingUrl);
	}

	public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
		client.doUpdateVisitedHistory(view, url, isReload);
	}

	public boolean equals(Object arg0) {
		return client.equals(arg0);
	}

	public int hashCode() {
		return client.hashCode();
	}

	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		client.onPageStarted(view, url, favicon);
		// recorder.setJsAttached(false);
	}

	public void onPageFinished(WebView view, String url) {
		client.onPageFinished(view, url);

		// final String lib = WebViewAutomator.fileToString("monkeytalk.js");
		// String s = "javascript:" + lib;
		// webView.loadUrl(s);

		// webView.loadUrl("javascript:( document.onclick = function (event) { alert('works' + event); } ) ()");
		// probably where we add record logic
		// webView.loadUrl("javascript:( function () { alert('woohoo'); } ) ()");

		WebViewRecorder.attachJs(view);
	}

	public void onLoadResource(WebView view, String url) {
		client.onLoadResource(view, url);

		// final String lib = WebViewAutomator.fileToString("monkeytalk.js");
		// String s = "javascript:" + lib;
		// String s =
		// "javascript:console.log('count: ' + document.getElementsByTagName('*').length);";
		// recorder.webViewDidChange();
	}

	public void onFormResubmission(WebView view, Message dontResend, Message resend) {
		client.onFormResubmission(view, dontResend, resend);
	}

	public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
		client.onReceivedSslError(view, handler, error);
	}

	public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host,
			String realm) {
		client.onReceivedHttpAuthRequest(view, handler, host, realm);
	}

	public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
		return client.shouldOverrideKeyEvent(view, event);
	}

	public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
		client.onUnhandledKeyEvent(view, event);
	}

	public void onScaleChanged(WebView view, float oldScale, float newScale) {
		client.onScaleChanged(view, oldScale, newScale);
	}

	public String toString() {
		return client.toString();
	}

}
