package com.gorillalogic.monkeyconsole.actions;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Date;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.json.JSONException;
import org.json.JSONObject;

import com.gorillalogic.cloud.ideversion.CloudConstants;
import com.gorillalogic.cloud.ideversion.FileUtils;
import com.gorillalogic.cloud.ideversion.Message;
import com.gorillalogic.monkeyconsole.editors.FoneMonkeyJSEditor;
import com.gorillalogic.monkeyconsole.editors.FoneMonkeyTestContributor;
import com.gorillalogic.monkeyconsole.editors.FoneMonkeyTestEditor;
import com.gorillalogic.monkeyconsole.editors.utils.CloudServiceException;
import com.gorillalogic.monkeyconsole.editors.utils.CloudServices;
import com.gorillalogic.monkeyconsole.editors.utils.LoggedCloudEventTypes;
import com.gorillalogic.monkeyconsole.editors.utils.MonkeyTalkUtils;
import com.gorillalogic.monkeyconsole.editors.utils.RunInCloudDialog;
import com.gorillalogic.monkeyconsole.plugin.FoneMonkeyPlugin;
import com.gorillalogic.monkeyconsole.preferences.PreferenceConstants;

public class PlayOnCloudAction extends Action {
	FoneMonkeyTestContributor contributor;
	FoneMonkeyTestEditor editor = null;
	FoneMonkeyJSEditor jsEditor = null;

	String CLOUDMONKEY_APPLIANCE_ERROR_TITLE = "CloudMonkey Appliance Error";

	public PlayOnCloudAction(FoneMonkeyTestContributor contributor) {
		this.contributor = contributor;
	}

	public void run() {
		loadEditor();
		submitToCloud();

	}

	private void submitToCloud() {
		try {
			try {
				CloudServices.ping();
			} catch (CloudServiceException cse) {
				MessageBox dialog = new MessageBox(getEditorShell(), SWT.ICON_ERROR | SWT.OK);
				dialog.setText(CLOUDMONKEY_APPLIANCE_ERROR_TITLE);
				dialog.setMessage(cse.getMessage());
				dialog.open();
				return;
			}

			IFile inputFile = getEditorInput().getFile();
			String inputFileName = inputFile.getName();
			// get the job parameters
			final RunInCloudDialog dialog = new RunInCloudDialog(getEditorShell(), inputFileName);
			dialog.create();
			CloudServices.logEventAsync(LoggedCloudEventTypes.RUNCLOUD_DIALOG_OPEN.toString(),
					dialog.mineUserData().toString(), new Date());
			if (dialog.open() != Window.OK) {
				CloudServices.logEventAsync(LoggedCloudEventTypes.RUNCLOUD_CANCEL_PRESSED
						.toString(), dialog.mineUserData().toString(), new Date());
				return;
			}
			CloudServices.logEventAsync(LoggedCloudEventTypes.RUNCLOUD_SUBMIT_PRESSED.toString(),
					dialog.mineUserData().toString(), new Date());

			// zip up the project
			File projectDir = getEditorInput().getPath().toFile().getParentFile();

			File zipFile = null;
			try {
				zipFile = FileUtils.zipDirectory(projectDir, false, false,
						Arrays.asList("mt", "mts", "js", "csv", "properties"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			final File zipFileForUpload = zipFile;

			final String currentFileName = inputFileName;
			System.out.println("Suite to run: " + currentFileName);

			System.out.println("Job Type: " + dialog.getJobType());

			File appFile = new File(dialog.getApkName());
			if (appFile.isDirectory()) {
				try {
					appFile = FileUtils.zipDirectory(appFile, true, false);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			final File appFileForUpload = appFile;
			System.out.println("Binary to upload: " + appFile.getPath());

			Job job = new Job("Submitting MonkeyTalk Cloud Job") {
				@Override
				protected IStatus run(IProgressMonitor arg0) {
					try {
						arg0.beginTask("Submitting Job to CloudMonkey Appliance",
								IProgressMonitor.UNKNOWN);
						JSONObject response2;
						try {
							response2 = CloudServices.submitJob(currentFileName, zipFileForUpload,
									appFileForUpload, dialog.getJobName(), dialog.getThinktime(),
									dialog.getTimeout(), dialog.getDeviceMatrixString(),
									dialog.getJobType());
						} catch (CloudServiceException cse) {
							MessageBox dialog = new MessageBox(getEditorShell(), SWT.ICON_ERROR
									| SWT.OK);
							dialog.setText(CLOUDMONKEY_APPLIANCE_ERROR_TITLE);
							dialog.setMessage(cse.getMessage());
							dialog.open();
							return Status.CANCEL_STATUS;
						}
						final JSONObject response = response2;
						// If you want to update the UI
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								try {
									MessageBox dialog2;

									try {
										if (response.getString("message").equalsIgnoreCase(
												Message.ERROR)) {
											dialog2 = new MessageBox(getEditorShell(),
													SWT.ICON_ERROR | SWT.OK);
											dialog2.setMessage(response.getString("data"));
										} else {
											dialog2 = new MessageBox(getEditorShell(),
													SWT.ICON_INFORMATION | SWT.OK);
											String newJobId = ""
													+ response.getJSONObject("data").getInt("id");
											dialog2.setMessage("Job " + newJobId
													+ " has been started.");

											try {
												MonkeyTalkUtils.openBrowser(
														"Job " + newJobId,
														"http://"
																+ CloudServices.getControllerHost()
																+ ":"
																+ CloudServices.getControllerPort()
																+ CloudConstants.JOB_STATUS
																+ "?username="
																+ FoneMonkeyPlugin
																		.getDefault()
																		.getPreferenceStore()
																		.getString(
																				PreferenceConstants.P_CLOUDUSR)
																+ "&token="
																+ CloudServices.getToken() + "&id="
																+ newJobId,
														editor != null ? editor.getEditorSite()
																: jsEditor.getEditorSite());
											} catch (PartInitException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											} catch (MalformedURLException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											} catch (CloudServiceException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}

										}
									} catch (JSONException e1) {
										e1.printStackTrace();
										throw new RuntimeException(e1);
									}
									dialog2.setText("CloudMonkey");
									dialog2.open();
									// ////
								} catch (Exception ex) {
									ex.printStackTrace();
								}
							}
						});

						return Status.OK_STATUS;
					} catch (Exception ex) {
						ex.printStackTrace();
						return Status.OK_STATUS;
					}
				}
			};

			job.setUser(true);
			job.schedule();

			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			IWorkbenchPage page = window.getActivePage();
			try {
				page.showView("com.gorillalogic.monkeyconsole.cloudview.ui.UICloudView");
			} catch (PartInitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	private void loadEditor() {
		editor = contributor.getEditor();
		jsEditor = contributor.getJSEditor();
		if (editor != null) {
			editor.getEditorSite().getWorkbenchWindow().getWorkbench().saveAllEditors(true);
		} else {
			jsEditor.getEditorSite().getWorkbenchWindow().getWorkbench().saveAllEditors(true);
		}
	}

	private Shell getEditorShell() {
		return editor != null ? editor.getEditorSite().getShell() : jsEditor.getEditorSite()
				.getShell();
	}

	private FileEditorInput getEditorInput() {

		return (FileEditorInput) (editor != null ? editor.getEditorInput() : jsEditor
				.getEditorInput());
	}

}
