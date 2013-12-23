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
package com.gorillalogic.monkeyconsole.editors;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorActionBarContributor;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.json.JSONObject;

import com.gorillalogic.monkeyconsole.actions.DropDownMenuAction;
import com.gorillalogic.monkeyconsole.actions.PlayOnCloudAction;
import com.gorillalogic.monkeyconsole.editors.utils.CloudServices;
import com.gorillalogic.monkeyconsole.editors.utils.ConnectionTypesEnum;
import com.gorillalogic.monkeyconsole.editors.utils.MonkeyTalkController;
import com.gorillalogic.monkeyconsole.editors.utils.MonkeyTalkImagesEnum;
import com.gorillalogic.monkeyconsole.editors.utils.MonkeyTalkUtils;
import com.gorillalogic.monkeyconsole.editors.utils.TimeSetupWizard;
import com.gorillalogic.monkeyconsole.plugin.FoneMonkeyPlugin;
import com.gorillalogic.monkeyconsole.preferences.PreferenceConstants;
import com.gorillalogic.monkeyconsole.server.RecordListener;
import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;

/**
 * Manages the installation/deinstallation of global actions for multi-page editors. Responsible for
 * the redirection of global actions to the active editor. Multi-page contributor replaces the
 * contributors for the individual editors in the multi-page editor.
 */
public class FoneMonkeyTestContributor extends MultiPageEditorActionBarContributor {
	private IEditorPart activeEditorPart;

	private DropDownMenuAction connectionDropdown;
	static private Action playToolItem;
	static private Action playOnCloudAction;
	static private Action stopToolItem;
	static private Action recordToolItem;
	static private Action clearToolItem;
	static private Action componentTreeToolItem;

	private Action noDeviceSelected;
	private Action connectNetworked;
	private Action connectCloud;
	private Action connectNetworkedIos;

	private Action connectToiOSSimulator;
	private Action connectToAndroidEmulatorTethered;
	private Action toggleMove;
	private Action toggleDrag;
	private Action toggleSwipe;
	private Action toggleTapCoords;
	private static TimerTask __timerTask;
	Process flexsocketlistener;

	private List<Object> actions = new ArrayList<Object>();
	private FoneMonkeyTestEditor editor;
	private AbstractDecoratedTextEditor abstractDecoratedTextEditor;
	private FoneMonkeyJSEditor jsEditor;
	// //
	private DropDownMenuAction filterDropdown;
	private IToolBarManager manager;

	/**
	 * Creates a multi-page contributor.
	 */
	public FoneMonkeyTestContributor() {
		super();

		playOnCloudAction = new PlayOnCloudAction(this);

		final RecordListener recordListener = new RecordListener() {

			// @Override - breaks tycho compile
			public void onRecord(Command command, JSONObject json) {
				if (command.getAction().equalsIgnoreCase("drag")) {
					command = convertToGesture(command);
				} else if (command.getAction().equalsIgnoreCase("tap")) {
					if (!toggleTapCoords.isChecked()) {
						// Strip coordinates
						command.setArgsAndModifiers("");
					}

				}
				if (activeEditorPart instanceof ITextEditor) {
					MonkeyTalkUtils.runOnGUI(new Runnable() {
						public void run() {
							editor.convertFromMonkeyTalk();
						}
					}, editor.getSite().getShell().getDisplay());
					FoneMonkeyPlugin.getDefault().getController().addARow(command, true);
				} else {
					FoneMonkeyPlugin.getDefault().getController().addARow(command, true);
				}

			}

			private Command convertToGesture(Command command) {

				if (toggleMove.isChecked()) {
					command.setAction("move");
					return command;

				} else if (toggleDrag.isChecked()) {
					try {
						int x1 = Integer.parseInt(command.getArgs().get(0));
						int y1 = Integer.parseInt(command.getArgs().get(1));
						int x2 = Integer.parseInt(command.getArgs().get(
								command.getArgs().size() - 2));
						int y2 = Integer.parseInt(command.getArgs().get(
								command.getArgs().size() - 1));
						command.setArgsAndModifiers(x1 + " " + y1 + " " + x2 + " " + y2);
					} catch (NumberFormatException e) {
					}
					return command;

				} else if (toggleSwipe.isChecked()) {
					try {
						int x1 = Integer.parseInt(command.getArgs().get(0));
						int y1 = Integer.parseInt(command.getArgs().get(1));
						int x2 = Integer.parseInt(command.getArgs().get(
								command.getArgs().size() - 2));
						int y2 = Integer.parseInt(command.getArgs().get(
								command.getArgs().size() - 1));
						if (Math.abs(x1 - x2) > Math.abs(y1 - y2)) { // action is on the
																		// x axis
							if (x1 > x2)
								command.setArgsAndModifiers("left");
							else
								command.setArgsAndModifiers("right");
						} else {
							if (y1 > y2)
								command.setArgsAndModifiers("up");
							else
								command.setArgsAndModifiers("down");
						}
						command.setAction("swipe");
					} catch (NumberFormatException e) {
					}
					return command;
				}
				return command;
			}
		};
		recordToolItem = new Action() {
			public void run() {
				if (FoneMonkeyPlugin.getDefault().getController().isRecordingON()) {
					return;
				}

				__timerTask = new TimerTask() {
					int currentRecordImage = 0;

					public void run() {

						if (++currentRecordImage > 18)
							currentRecordImage = 1;
						recordToolItem.setImageDescriptor(FoneMonkeyPlugin
								.getImageDescriptor("icons/coolbaricons/recordingimages2/s"
										+ (currentRecordImage < 10 ? "0" : "") + currentRecordImage
										+ ".gif"));

					}
				};
				final Timer timer = new Timer();
				timer.schedule(__timerTask, 0, // initial delay
						1 * 100); // subsequent rate

				MonkeyTalkController ctlr = FoneMonkeyPlugin.getDefault().getController();
				ctlr.startRecording();
				startRecordServer();
			}

			public void startRecordServer() {
				FoneMonkeyPlugin.getDefault().getController().getRecordServer()
						.setRecordListener(recordListener);
			}
		};
		stopToolItem = new Action() {
			public void run() {
				if (FoneMonkeyPlugin.getDefault().getController().isRecordingON()) {
					FoneMonkeyPlugin.getDefault().getController().stopRecording();
					__timerTask.cancel();
					recordToolItem.setImageDescriptor(MonkeyTalkImagesEnum.RECORDING.image);
				} else {
					FoneMonkeyPlugin.getDefault().getController().stopReplay();
				}
			}
		};
	}

	/**
	 * Returns the action register with the given text editor.
	 * 
	 * @return IAction or null if editor is null.
	 */
	protected IAction getAction(ITextEditor editor, String actionID) {
		return (editor == null ? null : editor.getAction(actionID));
	}

	@Override
	public void setActiveEditor(IEditorPart targetEditor) {
		Action previouslyConnectedConnection = null;
		if (null != connectionDropdown)
			previouslyConnectedConnection = connectionDropdown.getSelectedAction();
		removeContributedCrap();
		createActions(((FileEditorInput) targetEditor.getEditorInput()).getFile()
				.getFileExtension());
		contributeToToolBar();
		boolean wasConnected = false;
		if ((editor != null || jsEditor != null)
				&& FoneMonkeyPlugin.getDefault().getController() != null)
			wasConnected = FoneMonkeyPlugin.getDefault().getController().isCurrentlyConnected();

		super.setActiveEditor(targetEditor);
		if (targetEditor instanceof FoneMonkeyTestEditor) { // It should always
															// be this, but I am
															// just going to
															// double check
			if (stopToolItem != null && editor != null
					&& FoneMonkeyPlugin.getDefault().getController() != null)
				stopToolItem.run();
			this.editor = (FoneMonkeyTestEditor) targetEditor;
			FoneMonkeyPlugin.getDefault().getController().setCurrentlyConnected(wasConnected);
			if (null != previouslyConnectedConnection)
				connectionDropdown.setSelectedAction(previouslyConnectedConnection);
			targetEditor.setFocus();
			FoneMonkeyPlugin
					.getDefault()
					.getController()
					.setExtention(
							((FileEditorInput) targetEditor.getEditorInput()).getFile()
									.getFileExtension());
			FoneMonkeyPlugin.getDefault().getController().setPlaybackControlsState();
		}
		if (targetEditor instanceof FoneMonkeyJSEditor) { // It should always
			this.jsEditor = (FoneMonkeyJSEditor) targetEditor;
			// this.editor = (FoneMonkeyTestEditor) targetEditor;
			FoneMonkeyPlugin.getDefault().getController().setCurrentlyConnected(wasConnected);
			if (null != previouslyConnectedConnection)
				connectionDropdown.setSelectedAction(previouslyConnectedConnection);
			targetEditor.setFocus();
			FoneMonkeyPlugin
					.getDefault()
					.getController()
					.setExtention(
							((FileEditorInput) targetEditor.getEditorInput()).getFile()
									.getFileExtension());
			FoneMonkeyPlugin.getDefault().getController().setPlaybackControlsState();
			abstractDecoratedTextEditor = (AbstractDecoratedTextEditor) targetEditor;
			FoneMonkeyPlugin.getDefault().getController()
					.setJSContextualData(abstractDecoratedTextEditor);
			activeEditorPart = targetEditor;
		}

	}

	/*
	 * (non-JavaDoc) Method declared in AbstractMultiPageEditorActionBarContributor.
	 */

	public void setActivePage(IEditorPart part) {

		if (activeEditorPart == part)
			return;

		activeEditorPart = part;
		IActionBars actionBars = getActionBars();
		if (actionBars != null) {

			ITextEditor editor = (part instanceof ITextEditor) ? (ITextEditor) part : null;

			actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(),
					getAction(editor, ITextEditorActionConstants.DELETE));
			actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(),
					getAction(editor, ITextEditorActionConstants.UNDO));
			actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(),
					getAction(editor, ITextEditorActionConstants.REDO));
			actionBars.setGlobalActionHandler(ActionFactory.CUT.getId(),
					getAction(editor, ITextEditorActionConstants.CUT));
			actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(),
					getAction(editor, ITextEditorActionConstants.COPY));
			actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(),
					getAction(editor, ITextEditorActionConstants.PASTE));
			actionBars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(),
					getAction(editor, ITextEditorActionConstants.SELECT_ALL));
			actionBars.setGlobalActionHandler(ActionFactory.FIND.getId(),
					getAction(editor, ITextEditorActionConstants.FIND));
			actionBars.setGlobalActionHandler(IDEActionFactory.BOOKMARK.getId(),
					getAction(editor, IDEActionFactory.BOOKMARK.getId()));
			actionBars.updateActionBars();
		}
	}

	// File Operations

	private void createActions(String extention) {
		actions.removeAll(actions);

		clearToolItem = new Action() {
			public void run() {

				MessageDialog dlg = new MessageDialog(activeEditorPart.getSite().getShell(),
						"Delete All?", MonkeyTalkImagesEnum.CLEAR.image.createImage(),
						"Are you sure you would like to delete all items?", MessageDialog.WARNING,
						new String[] { "Cancel", "OK" }, 1);
				if (dlg.open() == 1)
					FoneMonkeyPlugin.getDefault().getController().clear();
			}
		};
		clearToolItem.setText("Clear");
		clearToolItem.setToolTipText("Clear All");
		clearToolItem.setImageDescriptor(MonkeyTalkImagesEnum.CLEARROW.image);
		clearToolItem.setId("monkeyclear");
		clearToolItem.setEnabled(false);
		actions.add(clearToolItem);

		componentTreeToolItem = new Action() {
			public void run() {
				FoneMonkeyPlugin.getDefault().getController().fetchAndShowComponentTree();
			}
		};
		componentTreeToolItem.setText("View Component Tree");
		componentTreeToolItem.setToolTipText("View Component Tree");
		componentTreeToolItem.setImageDescriptor(MonkeyTalkImagesEnum.TREE.image);
		componentTreeToolItem.setId("refreshtree");
		componentTreeToolItem.setEnabled(false);
		actions.add(componentTreeToolItem);

		actions.add(new Separator());
		// /////////////////////
		// Play Back selection type component
		// /////////////////////
		if (extention.equalsIgnoreCase("mt")) {
			playToolItem = new Action() {
				public void run() {

					if (FoneMonkeyPlugin.getDefault().getController().isRecordingON()) {
						editor.getFmch()
								.writeToConsole(
										"WARNING: PlayAll action invoked, but current console window reports recording ON");
						return;
					}
					if (editor.getActivePage() == 1) {
						editor.convertFromMonkeyTalk();
					}
					editor.getEditorSite().getWorkbenchWindow().getWorkbench().saveAllEditors(true);
					FoneMonkeyPlugin.getDefault().getController().startReplayAll();
				}
			};
			playToolItem.setText("Play All");
			playToolItem.setToolTipText("Play All");
			playToolItem.setEnabled(false);
			playToolItem.setImageDescriptor(MonkeyTalkImagesEnum.PLAY.image);
			playToolItem.setId("monkeyplay");
			actions.add(playToolItem);
		} else if (extention.equalsIgnoreCase("mts")) {
			playToolItem = new Action() {
				public void run() {
					editor.getEditorSite().getWorkbenchWindow().getWorkbench().saveAllEditors(true);
					FoneMonkeyPlugin.getDefault().getController().startSuiteReplay();
				}
			};
			playToolItem.setText("Run As Suite");
			playToolItem.setToolTipText("Run as a Test Suite");
			playToolItem.setImageDescriptor(MonkeyTalkImagesEnum.PLAY.image);
			playToolItem.setId("monkeyplaysuite");
			playToolItem.setEnabled(false);
			actions.add(playToolItem);
		} else if (extention.equalsIgnoreCase("js")) {
			playToolItem = new Action() {
				public void run() {
					try {
						jsEditor.getEditorSite().getWorkbenchWindow().getWorkbench()
								.saveAllEditors(true);
						FoneMonkeyPlugin.getDefault().getController().startJScriptReplay();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			playToolItem.setText("Run Script");
			playToolItem.setToolTipText("Run Script");
			playToolItem.setImageDescriptor(MonkeyTalkImagesEnum.PLAY.image);
			playToolItem.setId("org.eclipse.wst.jsdt.internal.ui.javaeditor.monkeytalk.runaction");
			playToolItem.setEnabled(false);
			actions.add(playToolItem);
		}
		stopToolItem.setText("Stop");
		stopToolItem.setToolTipText("Stop");
		stopToolItem.setEnabled(false);
		stopToolItem.setImageDescriptor(MonkeyTalkImagesEnum.STOP.image);
		stopToolItem.setId("monkeystop");
		actions.add(stopToolItem);

		recordToolItem.setEnabled(false);
		recordToolItem.setText("Record");
		recordToolItem.setToolTipText("Record");
		recordToolItem.setImageDescriptor(MonkeyTalkImagesEnum.RECORDING.image);
		recordToolItem.setId("monkeyrecord");
		actions.add(recordToolItem);

		playOnCloudAction.setText("Submit to Cloud");
		playOnCloudAction.setToolTipText("Submit Job to CloudMonkey");
		playOnCloudAction.setImageDescriptor(MonkeyTalkImagesEnum.PLAYONCLOUD.image);
		playOnCloudAction.setId("monkeyplaysuiteoncloud");
		playOnCloudAction.setEnabled(false);
		actions.add(playOnCloudAction);

		actions.add(new Separator());

		// //////////////////////
		// Connection Component
		// /////////////////////
		List<Action> connectionItems = new ArrayList<Action>();

		// ////////////
		connectionDropdown = new DropDownMenuAction(connectionItems) {

		};
		connectionDropdown.setId("monkeyconnect");
		actions.add(connectionDropdown);

		// ////////////////////////////////////

		connectToAndroidEmulatorTethered = new ConnectionItem(connectionDropdown,
				ConnectionTypesEnum.EMULATOR, "Android Emulator/Tethered Device",
				MonkeyTalkImagesEnum.CONNECTANDROIDEMULATOR.image);
		if (new Boolean(FoneMonkeyPlugin.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.P_INCLUDEANDROID))) {
			connectionItems.add(connectToAndroidEmulatorTethered);
		}

		// ///////////////
		connectToiOSSimulator = new ConnectionItem(connectionDropdown,
				ConnectionTypesEnum.SIMULATOR, "iOS Simulator",
				MonkeyTalkImagesEnum.CONNECTIOSEMMULATOR.image);
		if (new Boolean(FoneMonkeyPlugin.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.P_INCLUDEIOS))) {
			connectionItems.add(connectToiOSSimulator);
		}

		// ////////////////////////////////////////////////////////
		connectNetworked = new ConnectionItem(connectionDropdown,
				ConnectionTypesEnum.NETWORKED_ANDROID, "Networked Android Device...",
				MonkeyTalkImagesEnum.CONNECTNETWORKEDANDROID.image) {
			@Override
			public void doRun(MonkeyTalkController controller) {
				String host = controller.getHost(true);
				if (host != null) {
					this.setText("Device at "
							+ controller.preferenceStore.getString(PreferenceConstants.C_HOST));
					this.setToolTipText("Device at "
							+ controller.preferenceStore.getString(PreferenceConstants.C_HOST));
					controller.connect(ConnectionTypesEnum.NETWORKED_ANDROID);
					controller.setHost(host);
				}
			}
		};
		if (new Boolean(FoneMonkeyPlugin.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.P_INCLUDEANDROID))) {
			connectionItems.add(connectNetworked);
		}
		// ////////////////////////////////////////////////////////
		connectCloud = new ConnectionItem(connectionDropdown, ConnectionTypesEnum.CLOUD_ANDROID,
				"CloudMonkey Android Emulator", MonkeyTalkImagesEnum.CONNECTCLOUDMONKEY.image) {
			public void doRun(MonkeyTalkController controller) {
				String host = controller.getCloudHost();
				if (host != null) {
					connectCloud.setText("CloudMonkey at "
							+ controller.preferenceStore
									.getString(PreferenceConstants.C_CLOUD_HOST));
					connectCloud.setToolTipText("CloudMonkey at "
							+ controller.preferenceStore
									.getString(PreferenceConstants.C_CLOUD_HOST));

					controller.setHost(host);
					controller.connect(ConnectionTypesEnum.CLOUD_ANDROID);
				}
			}
		};
		if (new Boolean(FoneMonkeyPlugin.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.P_INCLUDEANDROID))) {
			// Uncomment to enable interactive record/playback on Cloud
			// connectionItems.add(connectCloud);
		}

		connectNetworkedIos = new ConnectionItem(connectionDropdown,
				ConnectionTypesEnum.NETWORKED_IOS, "Networked iOS Device...",
				MonkeyTalkImagesEnum.CONNECTNETWORKEDIOS.image) {
			public void doRun(MonkeyTalkController controller) {
				String host = controller.getHost(true);
				if (host != null) {
					connectNetworkedIos.setText("Device at "
							+ controller.preferenceStore.getString(PreferenceConstants.C_HOST));
					connectNetworkedIos.setToolTipText("Device at "
							+ controller.preferenceStore.getString(PreferenceConstants.C_HOST));

					controller.connect(ConnectionTypesEnum.NETWORKED_IOS);
					controller.setHost(host);
				}
			}
		};
		if (new Boolean(FoneMonkeyPlugin.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.P_INCLUDEIOS))) {
			connectionItems.add(connectNetworkedIos);
		}
		noDeviceSelected = new ConnectionItem(connectionDropdown, ConnectionTypesEnum.NO_DEVICE,
				"No Device Selected", MonkeyTalkImagesEnum.NOCONNECTION.image);

		connectionItems.add(noDeviceSelected);

		if (connectionDropdown != null && connectionItems.size() > 0) {
			connectionDropdown.setSelectedAction(connectionItems.get(0));
		}
		setupFilterDropdown(actions);
		Action webAction = new Action() {
			public void run() {
				URL webUrl;
				try {
					webUrl = new URL("http://www.gorillalogic.com/testing-tools/support-services");
				} catch (MalformedURLException e) {
					return;
				}
				try {
					IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport()
							.createBrowser("GorillaLogic");
					browser.openURL(webUrl);
				} catch (PartInitException e) {
					return;
				}
			}
		};
		webAction.setToolTipText("Having trouble getting started? Let us help!"); //$NON-NLS-1$
		ImageDescriptor newImage = FoneMonkeyPlugin.getImageDescriptor("icons/gl-icon-16.png");
		webAction.setImageDescriptor(newImage); //$NON-NLS-1$
		webAction.setId("monkeycontactus");
		actions.add(webAction);

		// //////////////////////////////////////////////////////////////////////////
		// / Section for configuration options
		// //////////////////////////////////////////////////////////////////////////

		actions.add(new Separator());

		// This action setup the thinktime and timeout
		Action timeSetupAction = new Action() {
			public void run() {
				TimeSetupWizard wizard = new TimeSetupWizard();
				WizardDialog dialog = new WizardDialog(new Shell(), wizard);
				dialog.create();
				dialog.open();

			}
		};
		timeSetupAction.setToolTipText("Thinktime/Timeout setup");
		timeSetupAction.setImageDescriptor(MonkeyTalkImagesEnum.TIME.image); //$NON-NLS-1$
		timeSetupAction.setId("monkeytimeSetup");
		actions.add(timeSetupAction);

		// This action is to set up if the execution should take screenshots on errors

		Action screenshotOnErrorAction = new Action() {
			public void run() {
				FoneMonkeyPlugin.getDefault().getPreferenceStore()
						.setValue(PreferenceConstants.P_TAKEERRORSCREENSHOTS, this.isChecked());
			}
		};

		screenshotOnErrorAction.setImageDescriptor(MonkeyTalkImagesEnum.SCREENSHOTERROR.image);

		if (FoneMonkeyPlugin.getDefault().getPreferenceStore()
				.getBoolean(PreferenceConstants.P_TAKEERRORSCREENSHOTS)) {
			screenshotOnErrorAction.setChecked(true);
		} else {
			screenshotOnErrorAction.setChecked(false);
		}

		screenshotOnErrorAction.setToolTipText("Take screenshot on error.");
		screenshotOnErrorAction.setId("monkeyScreenshotOnError");

		actions.add(screenshotOnErrorAction);

		// This action is to set up the taking of screenshots while executing commands
		Action takeScreenshot = new Action() {
			public void run() {
				FoneMonkeyPlugin.getDefault().getPreferenceStore()
						.setValue(PreferenceConstants.P_TAKEAFTERSCREENSHOTS, this.isChecked());

			}
		};
		takeScreenshot.setImageDescriptor(MonkeyTalkImagesEnum.SCREENSHOT.image);

		if (FoneMonkeyPlugin.getDefault().getPreferenceStore()
				.getBoolean(PreferenceConstants.P_TAKEAFTERSCREENSHOTS)) {
			takeScreenshot.setChecked(true);
		} else {
			takeScreenshot.setChecked(false);
		}

		takeScreenshot.setToolTipText("Take screenchots on command execution.");
		takeScreenshot.setId("monkeyTakeScreenShot");

		actions.add(takeScreenshot);

	}

	private void setupFilterDropdown(List<Object> actionList) {
		// //////////////////////
		// Filters Component
		// /////////////////////
		ImageDescriptor filterImage = MonkeyTalkImagesEnum.FILTER.image;

		List<Action> items = new ArrayList<Action>();
		filterDropdown = new DropDownMenuAction(items) {
		};
		filterDropdown.setImageDescriptor(filterImage);
		filterDropdown.setToolTipText("Filter Actions");
		actionList.add(new Separator());
		filterDropdown.setId("monkeyfilter");
		actionList.add(filterDropdown);

		toggleMove = new Action() {
			public void run() {
				FoneMonkeyPlugin.getDefault().getController()
						.toggleRecordFilter(AutomatorConstants.ACTION_MOVE);
				toggleDrag.setChecked(false);
				toggleMove.setChecked(true);
				toggleSwipe.setChecked(false);
			}
		};
		toggleMove.setText("Move");
		toggleMove.setToolTipText("Set recording of touch events to be recorded as moves");
		toggleMove.setChecked(true);
		// toggleTouchUpDown.setImageDescriptor(toggleTouchUpDownImage);
		if (editor != null)
			toggleMove.setChecked(FoneMonkeyPlugin.getDefault().getController().getRecordFilter()
					.get(AutomatorConstants.ACTION_MOVE));
		items.add(toggleMove);

		toggleDrag = new Action() {
			public void run() {
				toggleDrag.setChecked(true);
				toggleMove.setChecked(false);
				toggleSwipe.setChecked(false);
			}
		};
		toggleDrag.setText("Drag");
		toggleDrag.setToolTipText("Set recording of touch events to be recorded as drags");
		// toggleGestures.setImageDescriptor(toggleTouchUpDownImage);
		if (editor != null)
			toggleDrag.setChecked(FoneMonkeyPlugin.getDefault().getController().getRecordFilter()
					.get(AutomatorConstants.ACTION_DRAG));
		items.add(toggleDrag);

		toggleSwipe = new Action() {
			public void run() {
				toggleDrag.setChecked(false);
				toggleMove.setChecked(false);
				toggleSwipe.setChecked(true);
			}
		};
		toggleSwipe.setText("Swipe");
		toggleDrag.setChecked(false);
		toggleMove.setChecked(false);
		toggleSwipe.setChecked(true);
		toggleSwipe.setToolTipText("Set recording of touch events to be recorded as swipes");
		items.add(toggleSwipe);

		toggleTapCoords = new Action() {

		};
		toggleTapCoords.setText("Tap Coordinates");
		toggleTapCoords.setChecked(false);
		items.add(toggleTapCoords);

		if (editor != null)
			toggleSwipe.setChecked(FoneMonkeyPlugin.getDefault().getController().getRecordFilter()
					.get(AutomatorConstants.ACTION_SWIPE));
	}

	public MenuManager menu = null;

	public void contributeToMenu(IMenuManager manager) {

	}

	public void contributeToToolBar() {
		manager.add(new Separator());
		for (Object sampleAction : actions) {
			if (sampleAction instanceof Action) {
				((Action) sampleAction).setEnabled(true);
				manager.add((Action) sampleAction);
			} else {
				manager.add((ContributionItem) sampleAction);
			}
		}
		manager.update(true);
	}

	public void removeContributedCrap() {
		for (Object sampleAction : actions) {
			if (sampleAction instanceof Action) {

				manager.remove(((Action) sampleAction).getId());
			} else {
				manager.remove((ContributionItem) sampleAction);
			}
		}
	}

	public void contributeToToolBar(IToolBarManager manager) {
		this.manager = manager;

	}

	public Action getConnectToiOSSimulator() {
		return connectToiOSSimulator;
	}

	public Action getPlayToolItem() {
		return playToolItem;
	}

	public Action getClearToolItem() {
		return clearToolItem;
	}

	public Action getComponentTreeToolItem() {
		return componentTreeToolItem;
	}

	public Action getPlayOnCloudAction() {
		return playOnCloudAction;
	}

	public Action getStopToolItem() {
		return stopToolItem;
	}

	public Action getRecordToolItem() {
		return recordToolItem;
	}

	public DropDownMenuAction getConnectionDropdown() {
		return connectionDropdown;
	}

	public Action getNoDeviceSelected() {
		return noDeviceSelected;
	}

	public Action getConnectToAndroidEmulatorTethered() {
		return connectToAndroidEmulatorTethered;
	}

	public FoneMonkeyTestEditor getEditor() {
		return editor;
	}

	public FoneMonkeyJSEditor getJSEditor() {
		return jsEditor;
	}

	private static class ConnectionItem extends Action {
		DropDownMenuAction connectionDropdown;
		ConnectionTypesEnum connectionType;

		ConnectionItem(DropDownMenuAction connectionDropdown, ConnectionTypesEnum connectionType,
				String labelText, ImageDescriptor imageDescriptor) {
			this.connectionDropdown = connectionDropdown;
			this.connectionType = connectionType;
			this.setText(labelText);
			this.setToolTipText(labelText);
			this.setImageDescriptor(imageDescriptor);
		}

		public void run() {
			MonkeyTalkController controller = FoneMonkeyPlugin.getDefault().getController();
			ConnectionTypesEnum prevConnectionType = controller.getConnectionType();
			boolean wasConnected = controller.isCurrentlyConnected();

			doRun(controller);
			connectionDropdown.setSelectedAction(this);
			controller.setPlaybackControlsState();

			if (controller.isCurrentlyConnected()) {
				ConnectionTypesEnum newConnectionType = controller.getConnectionType();
				if (!wasConnected || !(newConnectionType.equals(prevConnectionType))) {
					CloudServices.logEventAsync(
							"IDE_CONNECT_TO_APP",
							"username="
									+ FoneMonkeyPlugin.getDefault().getPreferenceStore()
											.getString(PreferenceConstants.P_CLOUDUSR)
									+ ",connectionType=" + newConnectionType);
				}
			}
		}

		protected void doRun(MonkeyTalkController controller) {
			controller.connect(connectionType);
		}
	}

}