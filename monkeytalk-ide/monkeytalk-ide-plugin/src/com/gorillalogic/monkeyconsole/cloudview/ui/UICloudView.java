package com.gorillalogic.monkeyconsole.cloudview.ui;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gorillalogic.cloud.ideversion.CloudConstants;
import com.gorillalogic.cloud.ideversion.Message;
import com.gorillalogic.monkeyconsole.editors.utils.CloudServiceException;
import com.gorillalogic.monkeyconsole.editors.utils.CloudServices;
import com.gorillalogic.monkeyconsole.editors.utils.MonkeyTalkImagesEnum;
import com.gorillalogic.monkeyconsole.editors.utils.MonkeyTalkUtils;
import com.gorillalogic.monkeyconsole.plugin.FoneMonkeyPlugin;
import com.gorillalogic.monkeyconsole.preferences.PreferenceConstants;

/**
 * Insert the type's description here.
 * 
 * @see ViewPart
 */
@SuppressWarnings("restriction")
public class UICloudView extends ViewPart {
	TableViewer tableViewer;
	protected Text text;
	protected UICloudLableProvider labelProvider;
	protected JobRow root;
	public final int JOB_LIST_PAUSE_TIME = 5000; // milliseconds
	protected Thread tableRefreshThread = null;

	/**
	 * The constructor.
	 */
	public UICloudView() {
		startTableRefreshThread(5000);
	}

	private TableViewerColumn createTableViewerColumn(String title, int bound, final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		return viewerColumn;
	}

	/*
	 * @see IWorkbenchPart#createPartControl(Composite)
	 */
	public void createPartControl(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		// Add the TableViewer
		tableViewer = new TableViewer(composite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.BORDER);
		tableViewer.setContentProvider(new UICloudContentProvider());
		// tableViewer.setInput(this.getInitalInput()); // ---- don't do this in UI thread, let
		// RefreshThread take care of it.

		Table table = tableViewer.getTable();
		table.setLayoutData(new GridData(GridData.FILL_BOTH));

		TableColumnLayout layoutt = new TableColumnLayout();
		composite.setLayout(layoutt);
		// /
		TableViewerColumn idColumn = this.createTableViewerColumn("ID", 15, 0);
		idColumn.setLabelProvider(new CloudLableProviderID(tableViewer.getTable()));
		layoutt.setColumnData(idColumn.getColumn(), new ColumnWeightData(5));

		TableViewerColumn jobNamColumn = this.createTableViewerColumn("Job Name", 100, 1);
		jobNamColumn.setLabelProvider(new CloudLableProviderJobName(tableViewer.getTable()));
		layoutt.setColumnData(jobNamColumn.getColumn(), new ColumnWeightData(30));

		TableViewerColumn startDateColumn = this.createTableViewerColumn("Submitted", 300, 2);
		startDateColumn.setLabelProvider(new CloudLableProviderStartDate(tableViewer.getTable()));
		layoutt.setColumnData(startDateColumn.getColumn(), new ColumnWeightData(20));

		TableViewerColumn statusColumn = this.createTableViewerColumn("Status", 100, 3);
		statusColumn.setLabelProvider(new CloudLableProviderStatus(tableViewer.getTable()));
		layoutt.setColumnData(statusColumn.getColumn(), new ColumnWeightData(10));

		TableViewerColumn messageColumn = this.createTableViewerColumn("Message", 100, 3);
		messageColumn.setLabelProvider(new CloudLableProviderMessage(tableViewer.getTable()));
		layoutt.setColumnData(messageColumn.getColumn(), new ColumnWeightData(50));

		// TableViewerColumn linkColumn = this.createTableViewerColumn("Actions", 100, 4);
		// linkColumn.setLabelProvider( new ActionLabelProvider( tableViewer.getTable() ));
		// linkColumn.getColumn().addListener(SWT.Selection, new Listener() {
		// public void handleEvent(Event event) {
		// System.out.println("You have selected: "+event.text);
		// }
		// });
		// layoutt.setColumnData(linkColumn.getColumn(), new ColumnWeightData(20));

		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.getTable().setLinesVisible(true);

		// Create menu, toolbars, filters, sorters.
		createActions();
		createMenus();
		createToolbar();
		hookListeners();
	}

	/*
	 * @see IWorkbenchPart#dispose()
	 */
	public void dispose() {
		System.out.println("UICloudView: stopping job status refresh thread");
		this.stopTableRefreshThread();
		super.dispose();
	}
	
	protected boolean isError(JSONObject jo, boolean showMessageToUser) {
		try {
			if (jo.getString("message").equalsIgnoreCase(Message.ERROR)) {
				if (!showMessageToUser)
					return true;
				MessageBox dialog2 = new MessageBox(getSite().getShell(), SWT.ICON_ERROR | SWT.OK);
				dialog2.setText("Cloud Service");
				try {
					dialog2.setMessage(jo.getString("data"));
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
				dialog2.open();
				return true;
			}
		} catch (JSONException e2) {
			return true;
		}
		return false;
	}

	protected void hookListeners() {
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				// if the selection is empty clear the label
				if (event.getSelection().isEmpty()) {
					// text.setText("");
					return;
				}
				if (event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) event.getSelection();
					for (Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
						JobRow domain = (JobRow) iterator.next();

						try {
							String name = "Job " + domain.getId();
							if (!domain.getStatus().equalsIgnoreCase("done")) {
								MonkeyTalkUtils.openBrowser(
										name,
										"http://"
												+ CloudServices.getControllerHost()
												+ ":"
												+ CloudServices.getControllerPort()
												+ CloudConstants.JOB_STATUS
												+ "?username="
												+ FoneMonkeyPlugin.getDefault()
														.getPreferenceStore()
														.getString(PreferenceConstants.P_CLOUDUSR)
												+ "&token=" + CloudServices.getToken() + "&id="
												+ domain.getId(), UICloudView.this.getSite());

							} else {
								JSONObject jo = CloudServices.getJobResults("" + domain.getId());
								if (!isError(jo, true)) {
									MonkeyTalkUtils.openBrowser(name, jo.getJSONObject("data")
											.getString("summary"), UICloudView.this.getSite());
								}
							}
						} catch (PartInitException e) {
							e.printStackTrace();
						} catch (MalformedURLException e) {
							e.printStackTrace();
						} catch (JSONException e) {
							e.printStackTrace();
						} catch (CloudServiceException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}

		});
	}

	Action refreshButton = null;

	protected void createActions() {
		refreshButton = new Action() {
			@SuppressWarnings("deprecation")
			public void run() {
				long start = System.currentTimeMillis();
				try {
					refreshButton.setEnabled(false);
					if (tableRefreshThread.isAlive()) {
						tableRefreshThread.interrupt();
					}
					try {
						tableRefreshThread.join(5000L);
					} catch (InterruptedException e) {
						return;
					}
					if (tableRefreshThread.isAlive()) {
						System.out.println("cannot stop tableRefreshThread, killing it...");
						tableRefreshThread.stop();
					}
					System.out.println(new Date()
							+ " UICloudView: refreshButton.run(): restarting tableRefreshThread"
							+ (System.currentTimeMillis() - start) + "ms");
					startTableRefreshThread(0);
				} finally {
					refreshButton.setEnabled(true);
				}
			}
		};
		refreshButton.setText("Refresh");
		refreshButton.setToolTipText("Refresh all items in this list");
		refreshButton.setImageDescriptor(MonkeyTalkImagesEnum.REFRESH.image);
		refreshButton.setId("refresh");
	}

	protected void createMenus() {
		IMenuManager rootMenuManager = getViewSite().getActionBars().getMenuManager();
		rootMenuManager.setRemoveAllWhenShown(true);
	}

	protected void createToolbar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
		toolbarManager.add(refreshButton);
	}

	private void startTableRefreshThread(long pauseBeforeStarting) {
		if (pauseBeforeStarting > 0) {
			try {
				Thread.sleep(pauseBeforeStarting);
			} catch (InterruptedException e) {
				return;
			}
		}
		tableRefreshThread = new Thread(new Runnable() {
			public void run() {
				while (true) {
					final List<JobRow> data = getInitalInput();
					if (Thread.currentThread().isInterrupted()) {
						break;
					}
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							try {
								tableViewer.setInput(data);
								tableViewer.refresh();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
					// System.out.println(new Date()
					// + " UICloudView: tableRefresh.run() completed, elapsed="
					// + (System.currentTimeMillis() - start) + "ms");
					if (Thread.currentThread().isInterrupted()) {
						break;
					}
					try {
						Thread.sleep(JOB_LIST_PAUSE_TIME);
					} catch (InterruptedException e) {
						break;
					}
				}
			}
		});
		tableRefreshThread.start();
	}
	
	private void stopTableRefreshThread() {
		tableRefreshThread.interrupt();
	}

	public List<JobRow> getInitalInput() {
		JSONObject jo = new JSONObject();
		try {
			jo = CloudServices.getJobHistory();
		} catch (CloudServiceException cse) {
			try {
				jo.put("message", "error");
				jo.put("data", cse.getMessage());
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

		if (Thread.currentThread().isInterrupted()) {
			return null;
		}

		List<JobRow> jobs = new ArrayList<JobRow>();
		if (jo == null || isError(jo, false)) {
			try {
				if (jo.has("data"))
					jobs.add(new JobRow(1, "error", new Date(), jo.getString("data"),
							" view the monkey talk prefs to set username and pass", ""));
				else
					jobs.add(new JobRow(1, "error", new Date(), "Unable to Connect",
							" Can not connect to the cloud.gorillalogic.com servers", ""));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return jobs;
		}

		try {
			JSONArray ja = jo.getJSONObject("data").getJSONArray("jobs");

			for (int i = 0; i < ja.length(); i++) {
				JSONObject joo = ja.getJSONObject(i);
				String jobName = joo.getString("name");
				if (jobName == null || jobName.equalsIgnoreCase("null")) {
					jobName = "Job " + joo.getInt("id");
				}
				String jobMessage = createJobMessage(joo);
				jobs.add(new JobRow(joo.getInt("id"), jobName, new Date(joo.getLong("created")),
						joo.getString("status"), jobMessage, "view"));
			}
			return jobs;
		} catch (JSONException e) {
			return new ArrayList<JobRow>();
		} catch (CloudServiceException e) {
			return new ArrayList<JobRow>();
		}
	}

	protected String createJobMessage(JSONObject job) throws JSONException, CloudServiceException {
		String jobMessage = job.getString("msg");
		if (jobMessage != null && jobMessage.length() > 0 && !jobMessage.equals("null")) {
			return jobMessage;
		}
		String jobStatus = job.getString("status");
		if (jobStatus == null || jobStatus.length() == 0 || jobStatus.equals("done")) {
			return "";
		}

		// check for cancel
		if (Thread.currentThread().isInterrupted()) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		job = CloudServices.getJobStatus(job.getString("id"));

		JSONArray tasks;
		try {
			job = job.getJSONObject("data");
			tasks = job.getJSONArray("tasks");
		} catch (JSONException e) {
			return "";
		}
		int depth = 0;
		if (tasks != null && tasks.length() > 0) {
			SortedMap<String, List<JSONObject>> taskStatusCounters = new TreeMap<String, List<JSONObject>>();
			for (int i = 0; i < tasks.length(); i++) {
				JSONObject task = tasks.getJSONObject(i);
				String taskStatus = task.getString("status");
				List<JSONObject> taskList = taskStatusCounters.get(taskStatus);
				if (taskList == null) {
					taskList = new ArrayList<JSONObject>();
					taskStatusCounters.put(taskStatus, taskList);
				}
				taskList.add(task);

				if (task.has("queueDepth")) {
					int d = task.getInt("queueDepth");
					if (depth < d) {
						depth = d;
					}
				}
			}

			String depthMsg = (depth > 0 ? " (" + depth + " task" + (depth == 1 ? "" : "s")
					+ " ahead of you)" : "");
			for (String taskStatus : taskStatusCounters.keySet()) {
				if (taskStatus.equals("queued")) {
					sb.append(taskStatusCounters.get(taskStatus).size() + " " + taskStatus
							+ depthMsg + ", ");
				} else {
					sb.append(taskStatusCounters.get(taskStatus).size() + " " + taskStatus + ", ");
				}
			}
			sb.setLength(sb.length() - 2);
		}
		return sb.toString();
	}

	/*
	 * @see IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
	}
}
