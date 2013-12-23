package com.gorillalogic.monkeyconsole.editors.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.Section;

import swing2swt.layout.BorderLayout;
import swing2swt.layout.FlowLayout;

import com.gorillalogic.cloud.ideversion.TaskConstants;
import com.gorillalogic.monkeyconsole.editors.utils.devicematrixtable.tree.AndroidVersion;
import com.gorillalogic.monkeyconsole.editors.utils.devicematrixtable.tree.DeviceMatrixTreeViewContentProvider;
import com.gorillalogic.monkeyconsole.editors.utils.devicematrixtable.tree.DeviceMatrixTreeViewLableProvider;
import com.gorillalogic.monkeyconsole.editors.utils.devicematrixtable.tree.DeviceMatrixTreeViewModel;
import com.gorillalogic.monkeyconsole.editors.utils.devicematrixtable.tree.Resolution;
import com.gorillalogic.monkeyconsole.plugin.FoneMonkeyPlugin;
import com.gorillalogic.monkeyconsole.preferences.PreferenceConstants;

public class RunInCloudDialog extends TitleAreaDialogStyledTextMessage {

	public IPreferenceStore preferenceStore;
	private Text apkFileNameText;
	private CheckboxTreeViewer deviceMatrixViewer;
	private String jobName;
	private String apkName;
	private String thinktime;
	private String timeout;
	private String jobType = TaskConstants.ANDROID;
	private List<String> deviceMatrixString;
	Button btnSelectAll;
	private Tree tree;
	Button btnBrowseForApp;
	private String scriptName = "";
	Composite grpIos;
	Composite grpAndroid;
	private final FormToolkit formToolkit = new FormToolkit(Display.getDefault());
	private Text usernameTXT;
	private Text passwordTXT;
	Label lblYourApk;
	Form form;
	Button radioAndroidDev;
	Button radioIosDev;
	Shell shellLocal;
	Section sctnAdvanced;
	private Text scriptNameTXT;
	private Text jobNameTXT;
	private Text thinktimeTXT;
	private Text timeoutTXT;
	Button btnSubmit;
	Composite mainContentPanel;
	Section sctnPicker;
	ToolTip tip;

	private final ViewerFilter iosOnlyDevOnly = new ViewerFilter() {

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (element instanceof AndroidVersion) {
				AndroidVersion ver = (AndroidVersion) element;
				if (ver.getName().contains("iOS")) {
					for (Resolution res : ver.getTodos()) {
						if (res.getParam().contains("(Device)")) {
							// if any params are device, return true
							return true;
						}
					}
				}
			} else if (element instanceof Resolution) {
				// only return true if device
				return ((Resolution) element).getParam().contains("(Device)");
			}
			return false;
		}
	};

	private final ViewerFilter androidOnlyDevOnly = new ViewerFilter() {

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (element instanceof AndroidVersion) {
				if (((AndroidVersion) element).getName().contains("iOS")
						|| !((AndroidVersion) element).getName().contains("Android Device"))
					return false;
				else
					return true;
			} else {
				if (((AndroidVersion) parentElement).getName().contains("iOS"))
					return false;
				else
					return true;
			}
		}
	};

	public RunInCloudDialog(Shell parentShell, String scriptName) {
		super(parentShell);
		this.scriptName = scriptName;
	}

	public String mineUserData() {
		return "username=" + this.preferenceStore.getString(PreferenceConstants.P_CLOUDUSR);
	}

	@Override
	public void create() {
		super.create();
		// Set the title
		setTitle("Submit " + scriptName + " to CloudMonkey Appliance:");

		// Set the message
		setMessage("The CloudMonkey Appliance runs your MonkeyTalk scripts across many devices."
				+ " \n Upload your app and tests, and we'll do the rest." + " <a>Learn More</a>",
				IMessageProvider.INFORMATION);
		this.getMessageLabel().addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (event.text.equalsIgnoreCase("Learn More")) {

					IWebBrowser browser;
					try {
						CloudServices.logEventAsync(
								LoggedCloudEventTypes.RUNCLOUD_LEARN_MORE_PRESSED.toString(),
								mineUserData().toString(), new Date());
						browser = PlatformUI.getWorkbench().getBrowserSupport()
								.createBrowser("Job 1");
						browser.openURL(new URL(CloudServices.learnMoreUrl));
					} catch (PartInitException e) {
						e.printStackTrace();
					} catch (MalformedURLException e) {
						e.printStackTrace();
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
	protected Control createDialogArea(Composite parent) {
		preferenceStore = FoneMonkeyPlugin.getDefault().getPreferenceStore();
		this.shellLocal = parent.getShell();
		parent.getShell().setSize(570, 625);
		this.setHelpAvailable(false);

		parent.setLayout(new BorderLayout());

		mainContentPanel = new Composite(parent, SWT.NONE);
		mainContentPanel.setLayoutData(BorderLayout.CENTER);
		mainContentPanel.setLayout(new BorderLayout(0, 0));

		Composite composite_3 = new Composite(mainContentPanel, SWT.NONE);
		composite_3.setLayoutData(BorderLayout.NORTH);
		composite_3.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		radioAndroidDev = new Button(composite_3, SWT.RADIO);
		radioAndroidDev.setSelection(true);
		radioAndroidDev.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				form.setText("Android Job Submission");
				lblYourApk.setText("Your apk:");
				tip.setMessage("Enter the path to your MonkeyTalk-enabled application. "
						+ "This is the .apk file stored in the bin folder of your Android project.");
				apkFileNameText.setText(preferenceStore.getString(PreferenceConstants.C_APKNAME));
				btnSelectAll.setSelection(false);
				ViewerFilter[] l = deviceMatrixViewer.getFilters();
				for (int i = 0; i < l.length; i++) {
					deviceMatrixViewer.removeFilter(l[i]);
				}
				deviceMatrixViewer.addFilter(androidOnlyDevOnly);

				// trick tree into populating all items
				deviceMatrixViewer.expandAll();
				deviceMatrixViewer.collapseAll();
			}
		});
		radioAndroidDev.setText("Android Devices");

		radioIosDev = new Button(composite_3, SWT.RADIO);
		radioIosDev.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				form.setText("iOS Job Submission");
				lblYourApk.setText("Your app");
				tip.setMessage("Enter the path to your MonkeyTalk-enabled application. "
						+ "This is the \".ipa\" file that is created to deploy your app. "
						+ "The app must be provisioned for the CloudMonkey devices and deployment.");
				apkFileNameText.setText(preferenceStore.getString(PreferenceConstants.C_APPNAME));
				btnSelectAll.setSelection(false);

				ViewerFilter[] l = deviceMatrixViewer.getFilters();
				for (int i = 0; i < l.length; i++) {
					deviceMatrixViewer.removeFilter(l[i]);
				}
				deviceMatrixViewer.addFilter(iosOnlyDevOnly);

				// trick tree into populating all items
				deviceMatrixViewer.expandAll();
				deviceMatrixViewer.collapseAll();
			}
		});
		radioIosDev.setText("iOS Devices");

		Composite acceptTermsPanel = new Composite(mainContentPanel, SWT.NONE);
		acceptTermsPanel.setLayoutData(BorderLayout.SOUTH);
		acceptTermsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		Link link = new Link(acceptTermsPanel, SWT.NONE);

		link.setText("By submitting your tests to the CloudMonkey service,\nyou agree to accept the CloudMonkey <a>Terms of Service</a>");
		link.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {

				IWebBrowser browser;
				try {
					browser = PlatformUI.getWorkbench().getBrowserSupport().createBrowser("Job 1");
					browser.openURL(new URL(CloudServices.acceptTermsUrl));
				} catch (PartInitException e) {
					e.printStackTrace();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
				setReturnCode(CANCEL);
				close();
			}
		});

		form = formToolkit.createForm(mainContentPanel);
		form.setLayoutData(BorderLayout.CENTER);
		formToolkit.paintBordersFor(form);
		form.setText("Android Configuration");

		sctnPicker = formToolkit
				.createSection(form.getBody(), Section.EXPANDED | Section.TITLE_BAR);
		sctnPicker.setBounds(0, 0, 570, 350);
		sctnPicker.setEnabled(true);
		formToolkit.paintBordersFor(sctnPicker);
		sctnPicker.setText("Picker");

		Composite composite_5 = new Composite(sctnPicker, SWT.NONE);
		formToolkit.adapt(composite_5);
		formToolkit.paintBordersFor(composite_5);
		sctnPicker.setClient(composite_5);
		composite_5.setLayout(new BorderLayout(0, 0));

		deviceMatrixViewer = new CheckboxTreeViewer(composite_5, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.BORDER);

		deviceMatrixViewer.setContentProvider(new DeviceMatrixTreeViewContentProvider());
		deviceMatrixViewer.setLabelProvider(new DeviceMatrixTreeViewLableProvider());

		// Provide the input to the ContentProvider
		deviceMatrixViewer.setInput(new DeviceMatrixTreeViewModel());

		// trick tree into populating all items
		deviceMatrixViewer.expandAll();
		deviceMatrixViewer.collapseAll();

		tree = (Tree) deviceMatrixViewer.getControl();
		tree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TreeItem item = (TreeItem) e.item;
				item.setExpanded(true);
				if (item.getItemCount() > 0) {
					for (int y = 0; y < item.getItems().length; y++) {
						item.getItems()[y].setChecked(item.getChecked());
					}
				}
			}
		});
		deviceMatrixViewer.addFilter(androidOnlyDevOnly);
		formToolkit.adapt(tree);
		formToolkit.paintBordersFor(tree);

		btnSelectAll = new Button(composite_5, SWT.CHECK);
		btnSelectAll.setLayoutData(BorderLayout.SOUTH);
		formToolkit.adapt(btnSelectAll, true, true);
		btnSelectAll.setText("Select All");
		btnSelectAll.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent theEvent) {
				boolean setChecked = ((Button) (theEvent.widget)).getSelection();
				deviceMatrixViewer.expandAll();
				for (int i = 0; i < tree.getItems().length; i++) {
					TreeItem item = tree.getItem(i);
					if (item.getData() instanceof AndroidVersion) {
						item.setChecked(setChecked);
						for (int y = 0; y < item.getItems().length; y++) {
							item.getItems()[y].setChecked(setChecked);
						}
					}
				}

			}
		});

		Composite composite_7 = new Composite(composite_5, SWT.NONE);
		composite_7.setLayoutData(BorderLayout.NORTH);
		formToolkit.adapt(composite_7);
		formToolkit.paintBordersFor(composite_7);
		composite_7.setLayout(new GridLayout(4, false));

		lblYourApk = new Label(composite_7, SWT.NONE);
		formToolkit.adapt(lblYourApk, true, true);
		lblYourApk.setText("Your APK");

		apkFileNameText = new Text(composite_7, SWT.BORDER);
		GridData gd_text_6 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_text_6.widthHint = 330;
		apkFileNameText.setLayoutData(gd_text_6);

		apkFileNameText.setText(preferenceStore.getString(PreferenceConstants.C_APKNAME));
		formToolkit.adapt(apkFileNameText, true, true);

		btnBrowseForApp = new Button(composite_7, SWT.NONE);
		btnBrowseForApp.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		formToolkit.adapt(btnBrowseForApp, true, true);
		btnBrowseForApp.setText("Browse...");
		btnBrowseForApp.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				tip.setVisible(false);
				FileDialog dlg = new FileDialog(btnBrowseForApp.getShell(), SWT.OPEN);
				// Set the file extension filter based on Android/iOS selection
				String[] filterExtensions = new String[1];
				if(radioIosDev.getSelection()) {
					filterExtensions[0] = "ipa";
				} else {
					filterExtensions[0] = "apk";
				}
				dlg.setFilterExtensions(filterExtensions);
				dlg.setText("Open");
				String path = dlg.open();
				if (path == null)
					return;
				apkFileNameText.setText(path);
			}
		});
		ImageHyperlink browseHelpImage = new ImageHyperlink(composite_7, SWT.CENTER);
		// imageHyperLink.setBackgroundImage(section.getBackgroundImage());
		browseHelpImage.setToolTipText("Click me for help");
		browseHelpImage.setImage(JFaceResources.getImage(Dialog.DLG_IMG_HELP));
		tip = new ToolTip(composite_7.getShell(), SWT.BALLOON);
		tip.setMessage("Enter the path to your MonkeyTalk-enabled application. This is the .apk file stored in the bin folder of your Android project.");
		browseHelpImage.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent arg0) {
			}

			@Override
			public void focusLost(FocusEvent arg0) {
				tip.setVisible(false);

			}

		});
		browseHelpImage.addMouseListener(new MouseListener() {
			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
			}

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
			public void mouseUp(MouseEvent arg0) {
			}
		});

		sctnPicker.setExpanded(true);
		sctnPicker.redraw();

		sctnAdvanced = formToolkit.createSection(form.getBody(), Section.TWISTIE
				| Section.TITLE_BAR);
		sctnAdvanced.setText("Advanced");
		sctnAdvanced.addExpansionListener(new IExpansionListener() {
			public void expansionStateChanged(ExpansionEvent ee) {
				if (ee.getState()) {
					sctnPicker.setBounds(0, 0, 570, 200);
					sctnAdvanced.setBounds(0, 260, 570, 100);
				} else {
					sctnPicker.setBounds(0, 0, 570, 350);
					sctnAdvanced.setBounds(0, 350, 570, 25);
				}

			}

			public void expansionStateChanging(ExpansionEvent arg0) {
			}
		});
		sctnAdvanced.setBounds(0, 350, 570, 25);
		formToolkit.paintBordersFor(sctnAdvanced);

		Composite composite_6 = new Composite(sctnAdvanced, SWT.NONE);
		formToolkit.adapt(composite_6);
		formToolkit.paintBordersFor(composite_6);
		sctnAdvanced.setClient(composite_6);
		composite_6.setLayout(new BorderLayout(0, 0));

		Group grpCloudmonkeyJobPreferences = new Group(composite_6, SWT.NONE);
		grpCloudmonkeyJobPreferences.setLayoutData(BorderLayout.NORTH);
		grpCloudmonkeyJobPreferences.setText("CloudMonkey Job Preferences");
		formToolkit.adapt(grpCloudmonkeyJobPreferences);
		formToolkit.paintBordersFor(grpCloudmonkeyJobPreferences);
		grpCloudmonkeyJobPreferences.setLayout(new GridLayout(4, false));

		Label lblScriptToRun = new Label(grpCloudmonkeyJobPreferences, SWT.NONE);
		lblScriptToRun.setText("Script to Run:");

		scriptNameTXT = new Text(grpCloudmonkeyJobPreferences, SWT.BORDER);
		scriptNameTXT.setText(scriptName);
		scriptNameTXT.setLayoutData(new GridData(100, 15));
		formToolkit.adapt(scriptNameTXT, true, true);

		Label lblThinktime = new Label(grpCloudmonkeyJobPreferences, SWT.NONE);
		lblThinktime.setText("Thinktime:");

		thinktimeTXT = new Text(grpCloudmonkeyJobPreferences, SWT.BORDER);

		thinktimeTXT.setText(FoneMonkeyPlugin.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.P_THINKTIME));
		thinktimeTXT.setLayoutData(new GridData(100, 15));
		formToolkit.adapt(thinktimeTXT, true, true);

		Label lblJobName = new Label(grpCloudmonkeyJobPreferences, SWT.NONE);

		lblJobName.setText("Job Name:");
		lblJobName.setVisible(false);
		jobNameTXT = new Text(grpCloudmonkeyJobPreferences, SWT.BORDER);
		jobNameTXT.setText(scriptName);
		jobNameTXT.setLayoutData(new GridData(100, 15));
		jobNameTXT.setVisible(false);
		formToolkit.adapt(jobNameTXT, true, true);

		Label lblTimeout = new Label(grpCloudmonkeyJobPreferences, SWT.NONE);
		lblTimeout.setText("Timeout:");

		timeoutTXT = new Text(grpCloudmonkeyJobPreferences, SWT.BORDER);
		timeoutTXT.setText(FoneMonkeyPlugin.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.P_DEFAULTTIMEOUT));
		timeoutTXT.setLayoutData(new GridData(100, 15));
		formToolkit.adapt(timeoutTXT, true, true);

		Composite composite_1 = new Composite(parent, SWT.NONE);
		composite_1.setLayoutData(BorderLayout.SOUTH);
		composite_1.setLayout(new FlowLayout());

		btnSubmit = new Button(composite_1, SWT.NONE);
		btnSubmit.setText("Submit to CloudMonkey");
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

		Button btnCancel = new Button(composite_1, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

			}
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
		if (jobNameTXT.getText().length() == 0) {
			setErrorMessage("Please provide a Job Name");
			valid = false;
		}

		if (deviceMatrixViewer.getCheckedElements().length == 0) {
			setErrorMessage("Please select at least one device to test on.");
			valid = false;
		}

		if (isOsConflict()) {
			setErrorMessage("Please select only iOS or Android configurations, not both");
			valid = false;
		}

		if (apkFileNameText.getText().length() == 0) {

			setErrorMessage("Please provide a path to your application");

			valid = false;

		} else if (!(new File(apkFileNameText.getText()).exists())) {
			setErrorMessage("The path to your applicaiton is invalid");

			valid = false;
		}
		if (thinktimeTXT.getText().replaceAll("\\d+", "").length() > 0) {
			setErrorMessage("Default thinktime must be numeric");
			valid = false;
		}
		if (timeoutTXT.getText().replaceAll("\\d+", "").length() > 0) {
			setErrorMessage("Default timeout must be numeric");
			valid = false;
		}
		IPreferenceStore store = FoneMonkeyPlugin.getDefault().getPreferenceStore();
		if (store.getString(PreferenceConstants.P_CLOUDUSR).trim().length() == 0
				|| store.getString(PreferenceConstants.P_CLOUDPASS).trim().length() == 0) {
			setErrorMessage("You have not provided a MonkeyCloud username and password in the MonkeyTalk <a>Preferences</a>.");
			valid = false;
		}
		return valid;
	}

	private boolean isOsConflict() {
		Object[] checked = deviceMatrixViewer.getCheckedElements();
		boolean hasIos = false;
		boolean hasAndroid = false;
		for (int i = 0; i < checked.length; i++) {

			if (checked[i] instanceof Resolution) {
				Resolution a = (Resolution) checked[i];
				String osVersion = a.getAndroidVersion();
				if (osVersion != null && osVersion.startsWith(TaskConstants.IOS)) {
					hasIos = true;
				} else {
					hasAndroid = true;
				}
			}

		}
		return (hasIos && hasAndroid);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	// Copy textFields because the UI gets disposed
	// and the Text Fields are not accessible any more.
	private void saveInput() {
		jobName = jobNameTXT.getText();
		apkName = apkFileNameText.getText();
		thinktime = thinktimeTXT.getText();
		timeout = timeoutTXT.getText();
		deviceMatrixString = new ArrayList<String>();
		Object[] checked = deviceMatrixViewer.getCheckedElements();
		for (int i = 0; i < checked.length; i++) {
			if (checked[i] instanceof Resolution) {
				Resolution a = (Resolution) checked[i];
				deviceMatrixString.add(a.getParam());
				String osVersion = a.getAndroidVersion();
				if (osVersion != null && osVersion.startsWith(TaskConstants.IOS)) {
					jobType = TaskConstants.IOS;
				}
			}

		}
		if ((radioAndroidDev != null && radioAndroidDev.getSelection())) {
			FoneMonkeyPlugin.getDefault().getPreferenceStore()
					.setValue(PreferenceConstants.C_APKNAME, apkName);
		}
		if ((radioIosDev != null && radioIosDev.getSelection())) {
			FoneMonkeyPlugin.getDefault().getPreferenceStore()
					.setValue(PreferenceConstants.C_APPNAME, apkName);
		}

		if (usernameTXT != null) {
			FoneMonkeyPlugin.getDefault().getPreferenceStore()
					.setValue(PreferenceConstants.P_CLOUDUSR, usernameTXT.getText());
		}
		if (passwordTXT != null) {
			FoneMonkeyPlugin.getDefault().getPreferenceStore()
					.setValue(PreferenceConstants.P_CLOUDPASS, passwordTXT.getText());
		}
	}

	@Override
	protected void okPressed() {
		saveInput();
		super.okPressed();
	}

	public String getJobName() {
		return jobName;
	}

	public String getApkName() {
		return apkName;
	}

	public String getThinktime() {
		return thinktime;
	}

	public String getTimeout() {
		return timeout;
	}

	public List<String> getDeviceMatrixString() {
		return deviceMatrixString;
	}

	public String getJobType() {
		return jobType;
	}

}
