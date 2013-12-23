package com.gorillalogic.monkeyconsole.emulator;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

import swing2swt.layout.BorderLayout;
import swing2swt.layout.FlowLayout;

import com.gorillalogic.monkeyconsole.editors.utils.TitleAreaDialogStyledTextMessage;

import com.gorillalogic.monkeyconsole.plugin.FoneMonkeyPlugin;
import com.gorillalogic.monkeyconsole.preferences.PreferenceConstants;

public class StartEmulatorDialog extends TitleAreaDialogStyledTextMessage {

	public IPreferenceStore preferenceStore;
	Label lblYourApk;
	Label lblStatus;
	private Text apkFileNameText;
	Button btnSubmit;
	Button btnBrowseForApp;
	ToolTip tip;
	File selectedApk=null;
	
	String lastStatus="";

	public File getSelectedApk() {
		return selectedApk;
	}
	
	protected void syncDisplay() {
		apkFileNameText.setText(selectedApk!=null?selectedApk.getAbsolutePath():"");
		lblStatus.setText(lastStatus);
	}
	
	public StartEmulatorDialog(Shell parentShell, String apkName) {
		super(parentShell);
		if (apkName!=null && apkName.length()>0) {
			selectedApk = new File(apkName);
		}
	}

	private void setLearnMoreMessage() {
		setMessage("Use any APK file with MonkeyTalk with our Instrumentor."
				+ " Just point to your APK file." + " <a>Learn More</a>\n",
				IMessageProvider.INFORMATION);
		this.getMessageLabel().addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (event.text.equalsIgnoreCase("Learn More")) {

					IWebBrowser browser;
					try {
						browser = PlatformUI.getWorkbench().getBrowserSupport()
								.createBrowser("Job 1");
						//browser.openURL(new URL(AndroidInstrumentor.LEARN_MORE_URL));
					} catch (PartInitException e) {
						e.printStackTrace();
					//} catch (MalformedURLException e) {
					//	e.printStackTrace();
					}
					setReturnCode(CANCEL);
					close();
				} else if (event.text.equalsIgnoreCase("Preferences")) {
					PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(null,
							"com.gorillalogic.monkeyconsole.preferences.FonemonkeyPreferencePage",
							null, null);
					dialog.open();
				}
			}
		});
	}
	
	@Override
	public void create() {
		super.create();
		// Set the title
		setTitle("Instrument APK " + getApkName());

		// Set the message
		this.setLearnMoreMessage();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		preferenceStore = FoneMonkeyPlugin.getDefault().getPreferenceStore();
		this.setHelpAvailable(false);

		parent.getShell().setSize(600, 200);
		parent.setLayout(new BorderLayout());

		Composite filePickerComposite = new Composite(parent, SWT.NONE);
		filePickerComposite.setLayoutData(BorderLayout.CENTER);
		GridLayout filePickerCompositeGridLayout=new GridLayout(4, false);
		filePickerCompositeGridLayout.horizontalSpacing = 10;
		filePickerComposite.setLayout(filePickerCompositeGridLayout);

		lblYourApk = new Label(filePickerComposite, SWT.NONE);
		lblYourApk.setText("APK to Instrument");
		GridData lblYourApkGridData = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		lblYourApk.setLayoutData(lblYourApkGridData);

		apkFileNameText = new Text(filePickerComposite, SWT.BORDER);
		apkFileNameText.setText(getApkName());
		GridData apkFileNameTextGridData = new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1);
		apkFileNameTextGridData.widthHint = 330;
		apkFileNameText.setLayoutData(apkFileNameTextGridData);

		btnBrowseForApp = new Button(filePickerComposite, SWT.NONE);
		GridData btnBrowseForAppGridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		btnBrowseForApp.setLayoutData(btnBrowseForAppGridData);
		btnBrowseForApp.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		btnBrowseForApp.setText("Browse...");
		btnBrowseForApp.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				tip.setVisible(false);
				lblStatus.setText("");
				FileDialog dlg = new FileDialog(btnBrowseForApp.getShell(), SWT.OPEN);
				// Set the file extension filter based on Android/iOS selection
				String[] filterExtensions = new String[1];
				filterExtensions[0] = "apk";
				dlg.setFilterExtensions(filterExtensions);
				dlg.setText("Open");
				String path = dlg.open();
				if (path != null) {
					apkFileNameText.setText(path);
				}
			}
		});
		ImageHyperlink browseHelpImage = new ImageHyperlink(filePickerComposite, SWT.CENTER);
		// imageHyperLink.setBackgroundImage(section.getBackgroundImage());
		browseHelpImage.setToolTipText("Click me for help");
		browseHelpImage.setImage(JFaceResources.getImage(Dialog.DLG_IMG_HELP));
		tip = new ToolTip(filePickerComposite.getShell(), SWT.BALLOON);
		tip.setMessage("Enter the path to your Android application - a \".apk\" file");
		browseHelpImage.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent arg0) {}
			@Override
			public void focusLost(FocusEvent arg0) {
				tip.setVisible(false);
			}
		});
		browseHelpImage.addMouseListener(new MouseListener() {
			@Override
			public void mouseDoubleClick(MouseEvent arg0) {}
			@Override
			public void mouseDown(MouseEvent arg0) {
				if (tip.isVisible()) {
					tip.setVisible(false);
					return;
				}
				Text actionWidget = (Text) apkFileNameText;
				Point loc = actionWidget.toDisplay(actionWidget.getLocation());
				tip.setLocation(loc.x - actionWidget.getBorderWidth(), loc.y);
				tip.setVisible(true);
			}
			@Override
			public void mouseUp(MouseEvent arg0) {}
		});
		
		lblStatus = new Label(filePickerComposite, SWT.NONE);
		lblStatus.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 4, 1));

		Composite submitCancelComposite = new Composite(parent, SWT.NONE);
		submitCancelComposite.setLayoutData(BorderLayout.SOUTH);
		submitCancelComposite.setLayout(new FlowLayout());

		btnSubmit = new Button(submitCancelComposite, SWT.NONE);
		btnSubmit.setText("Instrument this app");
		btnSubmit.setData(new Integer(OK));
		btnSubmit.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (isValidInput()) {
					okPressed();
				}
			}
		});
		Shell shell = parent.getShell();
		if (shell != null) {
			shell.setDefaultButton(btnSubmit);
		}
		
		Button btnCancel = new Button(submitCancelComposite, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {}
		});
		btnCancel.setText("Cancel");
		btnCancel.setData(new Integer(CANCEL));
		btnCancel.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setReturnCode(CANCEL);
				close();
			}
		});
		
		parent.pack();
		return parent;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent2) {
		// Don't do anything, we'll handle this in build contents because otherwise the layouts get
		// messy
	}

	private boolean isValidInput() {
		boolean valid = true;

		if (apkFileNameText.getText().length() == 0) {
			setErrorMessage("Please provide a path to your app");
			valid = false;
		} else if (!(new File(apkFileNameText.getText()).exists())) {
			setErrorMessage("no file found at '" + apkFileNameText + "'");
			valid = false;
		}
		return valid;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void okPressed() {
		selectedApk = new File(apkFileNameText.getText()); 
		super.okPressed();
	}

	private String getApkName() {
		String apkName = "";
		if (selectedApk!=null) {
			apkName = selectedApk.getAbsolutePath();
		} else {
			String pref = preferenceStore.getString(PreferenceConstants.C_APKNAME);
			if (pref!=null) {
				apkName = pref;
			}
		} 
		return apkName;
	}
}
