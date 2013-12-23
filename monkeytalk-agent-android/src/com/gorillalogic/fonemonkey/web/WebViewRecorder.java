package com.gorillalogic.fonemonkey.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.webkit.WebView;

import com.gorillalogic.fonemonkey.Log;
import com.gorillalogic.fonemonkey.Recorder;
import com.gorillalogic.fonemonkey.automators.WebViewAutomator;
import com.gorillalogic.monkeytalk.Command;

public class WebViewRecorder {
	public int elementCount;
	private WebView webView;
	private boolean jsAttached;

	public WebViewRecorder(WebView webView) {
		super();
		this.webView = webView;
		this.webView.addJavascriptInterface(this, "mtrecorder");

		// webView.setOnTouchListener(new View.OnTouchListener() {
		// @Override
		// public boolean onTouch(View v, MotionEvent event) {
		// if (event.getAction() == MotionEvent.ACTION_UP) {
		// // Log.d("webview", "element: " + " x:" + event.getX() + " y:" + event.getY());
		// // Log.d("webview", "raw: " + " x:" + event.getRawX() + " y:" +
		// // event.getRawY());
		//
		// WebView webView = (WebView) v;
		// float zoom = webView.getScale();
		// int x = (int) (event.getX() / zoom) - webView.getScrollX();
		// int y = (int) (event.getY() / zoom) - webView.getScrollY();
		// // Log.d("webview", "js: " + " x:" + x + " y:" + y);
		// webView.loadUrl("javascript:( function () { MonkeyTalk.recordTap(" + x + ", "
		// + y + "); } ) ()");
		// }
		//
		// return false;
		// }
		// });
	}

	public int getElementCount() {
		return elementCount;
	}

	public void setElementCount(int count) {
		this.elementCount = count;
	}

	public boolean isJsAttached() {
		return jsAttached;
	}

	public void setJsAttached(boolean jsAttached) {
		this.jsAttached = jsAttached;
	}

	public static void attachJs(WebView webView) {
		// if (!isJsAttached()) {
		final String xpathLib = WebViewAutomator.fileToString("wgxpath.install.js");
		final String lib = WebViewAutomator.fileToString("monkeytalk-web.js");
		String s = "javascript:" + lib;
		String js = "javascript:" + xpathLib;
		webView.loadUrl(s);
		webView.loadUrl(js);
		// setJsAttached(true);
		// }
	}

	public void webViewDidChange() {
		// if (!isJsAttached()) {
		// Log.d("recorder", "---- attach js");
		// attachJs();
		// setJsAttached(true);
		// }
	}

	public void elementCountCallback(String result) {
		// if (!isJsAttached()) {
		// Log.d("recorder", "count: " + result);
		// attachJs();
		// setJsAttached(true);
		// }
	}

	public void recordJson(String json) {
		Log.log(json);

		try {
			JSONObject jsonObject = new JSONObject(json);
			String componentType = jsonObject.getString("component");
			String monkeyID = jsonObject.getString("monkeyId");
			String action = jsonObject.getString("action");
			String args = jsonObject.getString("args");

			if (args.length() == 0)
				args = null;

			Map<String, String> modifiers = new HashMap<String, String>();
			Command cmd = new Command(componentType, monkeyID, action,
					args != null ? Arrays.asList(args) : new ArrayList<String>(), modifiers);

			Recorder.recordCommand(cmd);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// AutomationManager.record(action, view, null)
		// AutomationManager.record(AutomatorConstants.ACTION_SELECT, group, label);
	}
}
