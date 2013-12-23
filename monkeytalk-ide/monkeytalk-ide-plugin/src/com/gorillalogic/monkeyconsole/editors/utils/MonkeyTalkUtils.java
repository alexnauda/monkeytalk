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
package com.gorillalogic.monkeyconsole.editors.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.internal.browser.BrowserViewer;
import org.eclipse.ui.internal.browser.WebBrowserEditor;
import org.eclipse.ui.internal.browser.WebBrowserEditorInput;
import org.eclipse.ui.internal.browser.WebBrowserView;
//?? Juno ?? import org.eclipse.rwt.widgets.ExternalBrowser;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ViewPart;

import com.gorillalogic.monkeyconsole.plugin.FoneMonkeyPlugin;
import com.gorillalogic.monkeyconsole.preferences.PreferenceConstants;
import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.api.js.tools.JSMTGenerator;

public class MonkeyTalkUtils {
	public static final String EXTERNAL_BROWSER_ID=FoneMonkeyPlugin.PLUGIN_ID + "_BROWSER";
	
	public static void runOnGUI(Runnable r, Display display) {
		runOnGUI(r, true, display);
	}

	public static void runOnGUI(Runnable r, boolean async, Display display) {
		if (!display.isDisposed()) {
			if (async)
				display.asyncExec(r);
			else
				display.syncExec(r);
		}
	}
	public static void openBrowser(final String name, String url, final IWorkbenchPartSite site) 
							throws PartInitException,MalformedURLException {
		if (shouldUseExternalBrowser()) {
			openExternalBrowser(name, url, site);
		} else {
			openInternalBrowser(name, url, site);
		}
	}
	
	private static boolean shouldUseExternalBrowser() {
		return isWindows();
	}
	
	private static void openExternalBrowser(final String name, String url, final IWorkbenchPartSite site) 
			throws PartInitException,MalformedURLException {
		// ExternalBrowser.open(name,url,ExternalBrowser.LOCATION_BAR | ExternalBrowser.NAVIGATION_BAR | ExternalBrowser.STATUS);
		IWorkbenchBrowserSupport browserSupport =
				PlatformUI.getWorkbench().getBrowserSupport();
		IWebBrowser browser = 
				browserSupport.createBrowser(IWorkbenchBrowserSupport.AS_EXTERNAL, 
											EXTERNAL_BROWSER_ID, "", "");
		browser.openURL(new URL(url));
	}
	
	private static void openInternalBrowser(final String name, String url, final IWorkbenchPartSite site) 
				throws PartInitException,MalformedURLException {
		WebBrowserEditor browserEditor = (WebBrowserEditor) site.getPage()
				.openEditor(
						new WebBrowserEditorInput(new URL(url), BrowserViewer.BUTTON_BAR
						| BrowserViewer.LOCATION_BAR, name),
						WebBrowserEditor.WEB_BROWSER_EDITOR_ID);
		Browser swtBrowser;
		try {
			Field wb = WebBrowserEditor.class.getDeclaredField("webBrowser");
			wb.setAccessible(true);
			BrowserViewer bviewer = (BrowserViewer) wb.get(browserEditor);
			Field b = BrowserViewer.class.getDeclaredField("browser");
			b.setAccessible(true);
			swtBrowser = (Browser) b.get(bviewer);
		} catch (Exception e) {
			throw new IllegalStateException("Error opening web browser", e);
		}
		swtBrowser.addLocationListener(new LocationListener() {

			@Override
			public void changing(LocationEvent event) {
				String location = event.location;
				if (!location.contains("liveview.html")) {
					return;
				}
				Pattern p = Pattern.compile("//(.+):");
				Matcher m = p.matcher(location);
				m.find();
				//String hostname = m.group(1);
				WebBrowserView browserView;
				try {
					browserView = (WebBrowserView) site
							.getPage()
							.showView(WebBrowserView.WEB_BROWSER_VIEW_ID, "Live View",
										IWorkbenchPage.VIEW_ACTIVATE);
				} catch (PartInitException e) {
					throw new IllegalStateException("Unable to show browser", e);
				}

				browserView.setURL(location);
				try {
					Method meth = ViewPart.class.getDeclaredMethod("setPartName", String.class);
					meth.setAccessible(true);
					meth.invoke(browserView, name);
				} catch (Exception e1) {
					System.out.println("Unable to set window name");
					e1.printStackTrace();
				}
				try {
					FoneMonkeyPlugin.getDefault().getController()
							.connectToCloudHost(new URL(location).getHost());
				} catch (MalformedURLException e) {
				}
				event.doit = false;

			}

			@Override
			public void changed(LocationEvent event) {

			}
		});

	}
	
	public static void setDefaultUPFromClassPath() throws IOException{
		IPreferenceStore preferenceStore = FoneMonkeyPlugin.getDefault().getPreferenceStore();
		String pass = preferenceStore.getString(PreferenceConstants.P_CLOUDPASS);
		String username = preferenceStore.getString(PreferenceConstants.P_CLOUDUSR);
		if((username+pass).trim().equalsIgnoreCase("")){
			Properties rb = getPropertiesFromClasspath("com/gorillalogic/cloud/ideversion/credentials.properties");
			preferenceStore.setValue(PreferenceConstants.P_CLOUDPASS, rb.getProperty("password"));
			preferenceStore.setValue(PreferenceConstants.P_CLOUDUSR, rb.getProperty("username"));
		}
	}
	
	public static Properties getPropertiesFromClasspath(String propFileName) throws IOException {
	    // loading xmlProfileGen.properties from the classpath
	    Properties props = new Properties();
	    InputStream inputStream = MonkeyTalkUtils.class.getClassLoader()
	        .getResourceAsStream(propFileName);

	    if (inputStream == null) {
	        throw new FileNotFoundException("property file '" + propFileName
	            + "' not found in the classpath");
	    }

	    props.load(inputStream);

	    return props;
	}
	/**
	 * Make a list of strings lower case
	 * @param strings
	 * @return
	 */
	public static List<String> toLowerList(List<String> listToConvert)
	{
		List<String> copiedList = new ArrayList<String>();
		for(String x : listToConvert){
			copiedList.add(x.toLowerCase());
		}
	    return copiedList;
	}
	
	public static void generateJScript(FileEditorInput fei, List<Command> commands, IWorkbenchPartSite editorSite) {
		File f = new File((fei).getPath().toString());
		f = f.getParentFile();

		String js = "";
		try {
			js = JSMTGenerator.createScript((fei).getFile().getProject()
					.getName(), fei.getName(), commands);
		} catch (Exception e) {
			// /couldn't convert
		}
		String fileName = (fei).getName().substring(0,
				(fei).getName().length() - 3)
				+ ".js";
		File outfile = new File(f.getAbsolutePath() + "/" + fileName);
		if (outfile.exists()) {
			// ////ask the user before overwriting!
		}
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(outfile));
			out.write(js);
			out.close();
		} catch (IOException e) {
			//System.out.println("Exception: " + e.getMessage());
		}
		try {
			(fei).getFile().getProject()
					.refreshLocal(IResource.DEPTH_INFINITE, null);

			IEditorPart ieditorpart = editorSite.getPage()
					.getActiveEditor();
			IFile fileToBeOpened = ((IFileEditorInput) ieditorpart
					.getEditorInput()).getFile().getProject().getFile(fileName);

			IEditorInput editorInput = new FileEditorInput(fileToBeOpened);
			editorSite.getPage()
					.openEditor(editorInput,
							"org.eclipse.wst.jsdt.ui.CompilationUnitEditor");
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static boolean isDemoApp(String pathToApp) {
		if (pathToApp==null | pathToApp.length()==0) {
			return false;
		}
		if (pathToApp.contains("Demo") || pathToApp.contains("demo")) {
			// certainly not conclusive, but whatever...
			return true;
		}
		return false;
	}
	
	private static String OS = System.getProperty("os.name").toLowerCase();
	 
	public static String getOs() {
		if (isWindows()) {
			return "windows";
		} else if (isMac()) {
			return("mac");
		} else if (isSolaris()) {
			return "solaris";
		} else if (isLinux()) {
			return "linux";
		} else if (isUnix()) {
			return "unix";
		} else {
			return OS;
		}
	}
	 
	public static boolean isWindows() { 
		return (OS.indexOf("win") >= 0);
	} 
	public static boolean isMac() { 
		return (OS.indexOf("mac") >= 0);
	} 
	public static boolean isUnix() { 
		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 || isSolaris());
	} 
	public static boolean isLinux() { 
		return (OS.indexOf("linux") >= 0);
	}
	public static boolean isSolaris() { 
		return (OS.indexOf("sunos") >= 0);
	}
}