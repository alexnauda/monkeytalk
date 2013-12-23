package com.gorillalogic.monkeyconsole.emulator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;

import com.gorillalogic.monkeytalk.utils.FileUtils;

public class StartEmulatorAction extends Action {
	
	protected static String STARTEMULATOR_ERROR_TITLE = "Start Emulator Error";
	protected static String DEFAULT_EMULATOR_PATH = ".monkeytalk/MonkeyTalkDemoEmulator";
	protected static String DEFAULT_EMULATOR_MARKER = "MonkeyTalkDemoEmulator.avd";
	protected static String DEFAULT_EMULATOR_EXECUTABLE_CONTAINING_DIR = "MonkeyTalkDemoEmulator.avd";
	protected static String EMULATOR_EXECUTABLE_MAC="emulator";
	protected static String EMULATOR_EXECUTABLE_WIN="emulator.exe";
	protected static String LATEST_EMULATOR_DOWNLOAD_URL="http://www.gorillalogic.com/sites/default/files/MonkeyTalkDemoEmulator.zip";
	Shell shell;
	PrintStream consoleStream = null;
	
	protected static Process theEmulatorProcess = null; 

	public StartEmulatorAction(Shell shell) {
		this.shell = shell;
	}

	public void run() {
		File emulatorMarker = null;
		try {
			emulatorMarker=checkForEmulator();
			if (emulatorMarker!=null) {
				// the thing is already downloaded
				URL newerVersion=checkForNewerVersion(emulatorMarker);
				if (newerVersion!=null) {
					if (askToDownload(true)) {
						downloadEmulator(newerVersion); // starts Job
						return;
					}
				}
			} else {
				// they don't have the emulator yet
				if (askToDownload(false)) {
					downloadEmulator(getLatestVersion()); // starts job
				}
				return;
			}
			
			runStartEmulator();
			
		} catch (Exception e) {
			MessageBox dialog = getMessageBox(SWT.ICON_ERROR | SWT.OK);
			dialog.setText(STARTEMULATOR_ERROR_TITLE);
			String exceptionMessage = e.getMessage();
			if (exceptionMessage==null) {
				exceptionMessage = "an unknown error occurred: " + e.getClass().getName();
			}
			dialog.setMessage(exceptionMessage);
			dialog.open();
			return;
		}
	}
	
	protected void runStartEmulator() throws IOException {

		if (theEmulatorProcess!=null) {
			MessageBox dialog = getMessageBox(SWT.ICON_ERROR | SWT.OK);
			dialog.setText(STARTEMULATOR_ERROR_TITLE);
			dialog.setMessage("Emulator is already runing.");
			dialog.open();
			return;
		}
		
		File emulatorMarker=checkForEmulator();
		if (emulatorMarker==null) {
			throw new IOException("Could not locate builtin emulator!");
		}
		
		// get the job parameters
		//StartEmulatorDialog dialog = new StartEmulatorDialog(getDialogParent(), null);
		//dialog.create();
		//if (dialog.open() != Window.OK) {
			// they cancelled
		//	return;
		//}
					
		// if the current file is a script, generate a suite for it
		//final File selectedApk = dialog.getSelectedApk();
		final File selectedApk = null;;

		Job job = new StartEmulatorJob(this.getDefaultEmulatorDir(), selectedApk);
		job.setUser(true);
		job.schedule();
		
	}

	private MessageBox getMessageBox(int flags) {
		MessageBox dialog = new MessageBox(getDialogParent(), flags);
		return dialog;
	}
	
	private Shell getDialogParent() {
		return shell;
	}
	
	private class StartEmulatorJob extends Job {
		File selectedApk;
		File emulatorDir;
				
		public StartEmulatorJob(File emulatorDir, File selectedApk) {
			super("Starting Emulator" + (selectedApk==null ? "" : (": " + selectedApk.getName())) );
			this.selectedApk = selectedApk;
			this.emulatorDir = emulatorDir;
		}
		
		@Override
		protected IStatus run(IProgressMonitor arg0) {
			
			arg0.beginTask(this.getName(), IProgressMonitor.UNKNOWN);
			
			// launch the emulator
			int style;
			String message;
			IStatus status;
			try {
				theEmulatorProcess = startEmulator(selectedApk);
				
				if (theEmulatorProcess==null) {
					style=SWT.ICON_ERROR | SWT.OK;
					message="Emulator failed to start!";
					status=Status.CANCEL_STATUS;
				} else {
					style=SWT.ICON_INFORMATION | SWT.OK;
					message="Emulator started!";
					status=Status.OK_STATUS;
				}
				
			} catch (Exception e) {
				style = SWT.ICON_ERROR | SWT.OK;
				message="Error Starting Emulator " + (selectedApk==null ? "" : (": " + selectedApk.getName())) 
						+ "\n" + e.getMessage();
				status=Status.CANCEL_STATUS;
			}
			
			final int _style=style;
			final String _message=message;
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					MessageBox dialog2 = getMessageBox(_style);
					dialog2.setText("Emulator Startup");
					dialog2.setMessage(_message);
					dialog2.open();
				}
			});

			return status;
		}
		
		protected Process startEmulator(File selectedApk) throws IOException {
			String executableName;
			File workingDirectory = new File(emulatorDir,DEFAULT_EMULATOR_EXECUTABLE_CONTAINING_DIR);
			if (isWindows()) {
				executableName=workingDirectory.getAbsolutePath() + File.separator + EMULATOR_EXECUTABLE_WIN;
			} else {
				executableName = "." + File.separator + EMULATOR_EXECUTABLE_MAC;
			}
			/*
			String emulatorExecutable = new File(workingDirectory,DEFAULT_EMULATOR_EXECUTABLE).getAbsolutePath();
			String sysdir  = emulatorDir.getAbsolutePath();
			String system  = new File(emulatorDir,"system.img").getAbsolutePath();
			String kernel  = new File(emulatorDir,"kernel-qemu").getAbsolutePath();
			String partitionSize = "218";
			String ramdisk = new File(emulatorDir,"ramdisk.img").getAbsolutePath();
			String skindir = emulatorDir.getAbsolutePath();
			String skin    = "WVGA800";
			String datadir = emulatorDir.getAbsolutePath();
			*/
			String emulatorExecutable = executableName;
			String sysdir  = "." + File.separator;
			String system  = "system.img";
			String kernel  = "kernel-qemu";
			String partitionSize = "218";
			String ramdisk = "ramdisk.img";
			String skindir = "." + File.separator;
			String skin    = "WVGA800";
			String datadir = "." + File.separator;

			ArrayList<String> commands = new ArrayList<String>();
			commands.add(emulatorExecutable);
			commands.add("-sysdir");
			commands.add(sysdir);
			commands.add("-system");
			commands.add(system);
			commands.add("-kernel");
			commands.add(kernel);
			commands.add("-partition-size");
			commands.add(partitionSize);
			commands.add("-ramdisk");
			commands.add(ramdisk);
			commands.add("-skindir");
			commands.add(skindir);
			commands.add("-skin");
			commands.add(skin);
			commands.add("-datadir");
			commands.add(datadir);		
			
			return runEmulator(workingDirectory, commands);			
		}
	} 
	
	private Process runEmulator(File workingDirectory, List<String> commands) throws IOException {
		ProcessBuilder pb = new ProcessBuilder();
		pb.directory(workingDirectory);
		pb.redirectErrorStream(true);
		pb.command(commands);
		Process p=pb.start();
		if (p!=null) {
			sendOutputToConsole(p);
		}
		return p;
	}
	
	protected void sendOutputToConsole(Process p) {
		final Process pp=p;
		new Thread(
				new Runnable() {
						@Override
						public void run() {
							PrintStream consoleStream = getConsolePrintStream("MonkeyTalk Android Emulator");
							try {
								BufferedInputStream processOutput = new BufferedInputStream(pp.getInputStream());
								int i;
								while ((i=processOutput.read())!=-1) {
									consoleStream.print((char)i);
								}
								theEmulatorProcess=null;
							} catch(Exception e) {
								consoleStream.println("Error reading output of MonkeyTalk emulator:" + e.getMessage());
							} finally {
								if (theEmulatorProcess!=null) {
									consoleStream.println("Emulator exited abnormally!");
									try {
										theEmulatorProcess.destroy();
									} catch (Exception e) {
										// do nothing
									}
									theEmulatorProcess=null;
								}
								consoleStream.close();
							}
						}
				 	}
				).start();
	}
	
	protected File checkForEmulator() {
		File emulatorMarkerFile = new File(getDefaultEmulatorDir(), DEFAULT_EMULATOR_MARKER);
		if (emulatorMarkerFile.exists()) {
			return emulatorMarkerFile;
		}
		return null;
	}
	
	protected File getDefaultEmulatorDir() {
		String[] folders = DEFAULT_EMULATOR_PATH.split("/");
		File defaultEmulatorDir = new File(System.getProperty("user.home"));
		for (String folder : folders) {
			defaultEmulatorDir = new File(defaultEmulatorDir, folder);
		}
		return defaultEmulatorDir;
	}
	
	private PrintStream getConsolePrintStream() {
		return getConsolePrintStream(null);
	}
	private PrintStream getConsolePrintStream(String title) {
		if (consoleStream == null) {			
			IOConsole console = new IOConsole(title!=null?title:"", null);
			console.setConsoleWidth(80);
	
			IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
			IConsole[] existing = manager.getConsoles();
			boolean exists = false;
			for (int i = 0; i < existing.length; i++ ) {
				if(console == existing[i]) {
					exists = true;
				}
			}
			if(!exists) {
				manager.addConsoles(new IConsole[] {console});
			}
			manager.showConsoleView(console);
			IOConsoleOutputStream stream = console.newOutputStream();
			consoleStream = new PrintStream(stream);
		}
		return consoleStream;
	}
	
	private URL getLatestVersion() throws MalformedURLException {
		return checkForNewerVersion(null);
	}
	private URL checkForNewerVersion(File marker) throws MalformedURLException {
		if (marker==null) {
			return new URL(LATEST_EMULATOR_DOWNLOAD_URL);
		}
		return null;
	}
	
	private boolean askToDownload(boolean newer) {
		MessageBox dialog = getMessageBox(SWT.ICON_INFORMATION | SWT.OK | SWT.CANCEL);
		dialog.setText("Download MonkeyTalk Android Emulator");
		String message;
		if (newer) {
			message = "A newer version of the MonkeyTalk Android Emulator is available.\nDownload it now?";
		} else {
			message = "The MonkeyTalk Android Emulator is not installed.\nDownload it now?";
		}
		dialog.setMessage(message);
		int result = dialog.open();
		return (result == SWT.OK); 
	}
	
	private void downloadEmulator(URL url) throws IOException {
		File downloadDir = this.getDefaultEmulatorDir().getParentFile();
		File zipfile = new File(downloadDir,"MonkeyTalkDemoEmulator_download.zip");
		DownloadEmulatorJob job = new DownloadEmulatorJob(url, zipfile);
		job.setUser(true);
		job.schedule();
	}
	
	private class DownloadEmulatorJob extends Job {
		URL url;
		File downloadTarget; 
		public DownloadEmulatorJob(URL url, File downloadTarget) {
			super("Downloading MonkeyTalk Demo Emulator");
			this.url = url;
			this.downloadTarget = downloadTarget;
		}
		
		@Override
		protected IStatus run(IProgressMonitor arg0) {
			try {
				File downloadDir = downloadTarget.getParentFile();
				downloadDir.mkdirs();
				org.apache.commons.io.FileUtils.copyURLToFile(url, downloadTarget);
				com.gorillalogic.monkeytalk.utils.FileUtils.unzipFile(downloadTarget, downloadDir);
			} catch (Exception e) {
				final Exception ee = e;
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						MessageBox dialog2 = getMessageBox(SWT.ICON_ERROR | SWT.OK);
						dialog2.setText("Emulator Download Error");
						dialog2.setMessage(ee.getMessage());
						dialog2.open();
					}
				});
				return Status.CANCEL_STATUS;
			} 
			
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					try {
						runStartEmulator();
					} catch (Exception e) {
						MessageBox dialog2 = getMessageBox(SWT.ICON_ERROR | SWT.OK);
						dialog2.setText("Emulator Startup Error");
						dialog2.setMessage(e.getMessage());
						dialog2.open();
					}
				}
			});
			return Status.OK_STATUS;
		}
	}
	
	protected boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().contains("win");
	}
}
