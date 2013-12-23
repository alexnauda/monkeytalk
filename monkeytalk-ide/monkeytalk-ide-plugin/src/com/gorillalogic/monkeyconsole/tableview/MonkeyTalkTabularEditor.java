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
package com.gorillalogic.monkeyconsole.tableview;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.FocusCellHighlighter;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

import com.gorillalogic.monkeyconsole.editors.utils.MonkeyTalkController;
import com.gorillalogic.monkeyconsole.editors.utils.MonkeyTalkUtils;
import com.gorillalogic.monkeyconsole.plugin.FoneMonkeyPlugin;
import com.gorillalogic.monkeyconsole.preferences.PreferenceConstants;
import com.gorillalogic.monkeyconsole.tableview.editors.ActionEditingSupport;
import com.gorillalogic.monkeyconsole.tableview.editors.ArgsEditingSupport;
import com.gorillalogic.monkeyconsole.tableview.editors.ComponentEditingSupport;
import com.gorillalogic.monkeyconsole.tableview.editors.MonkeyidEditingSupport;
import com.gorillalogic.monkeyconsole.tableview.editors.ShouldFailEditingSupport;
import com.gorillalogic.monkeyconsole.tableview.editors.ThinktimeEditingSupport;
import com.gorillalogic.monkeyconsole.tableview.editors.TimeoutEditingSupport;
import com.gorillalogic.monkeyconsole.tableview.labelproviders.ActionLabelProvider;
import com.gorillalogic.monkeyconsole.tableview.labelproviders.ArgsLabelProvider;
import com.gorillalogic.monkeyconsole.tableview.labelproviders.ComponentLabelProvider;
import com.gorillalogic.monkeyconsole.tableview.labelproviders.MonkeyidLabelProvider;
import com.gorillalogic.monkeyconsole.tableview.labelproviders.RowNumberLabelProvider;
import com.gorillalogic.monkeyconsole.tableview.labelproviders.ShouldFailLabelProvider;
import com.gorillalogic.monkeyconsole.tableview.labelproviders.ThinktimeLabelProvider;
import com.gorillalogic.monkeyconsole.tableview.labelproviders.TimeoutLabelProvider;
import com.gorillalogic.monkeytalk.Command;

/**
 * This class allows you to create and edit Command objects
 */
public class MonkeyTalkTabularEditor extends EditorPart {
	// Table column names/properties
	public static final String ROW = "Row";
	public static final String COMPONENT = "Component";
	public static final String MONKEYID = "MonkeyID";
	public static final String ACTION = "Action";
	public static final String ARGS = "Arguments";
	public static final String TIMEOUT = "Timeout (ms)";
	public static final String THINKTIME = "ThinkTime (ms)";
	public static final String SHOULDFAIL = "Should Fail";
	public boolean commandKeyDown = false;

	public static final String[] PROPS = { ROW, COMPONENT, MONKEYID, ACTION, ARGS, TIMEOUT,
			THINKTIME, SHOULDFAIL };

	// The data model, this is marked as final because the labelproviders and
	// editors need access to it
	final private java.util.List<TableRow> commands;

	TableViewer tv = null;
	Table table = null;
	String[] limitedComponentSet = null;
	MonkeyTalkController monkeyControls;
	int menueventsCaught = 0;

	public MonkeyTalkController getMonkeyControls() {
		return monkeyControls;
	}

	public void setMonkeyControls(MonkeyTalkController monkeyControls) {
		this.monkeyControls = monkeyControls;
	}

	/**
	 * Constructs a JfaceTableExample
	 */
	public MonkeyTalkTabularEditor(MonkeyTalkController monkeyControls) {
		commands = new ArrayList<TableRow>();
		this.monkeyControls = monkeyControls;
	}

	/**
	 * Constructs a JfaceTableExample
	 */
	public MonkeyTalkTabularEditor(String[] limitedComponentSet, MonkeyTalkController monkeyControls) {
		this.limitedComponentSet = limitedComponentSet;
		commands = new ArrayList<TableRow>();
		this.monkeyControls = monkeyControls;
	}

	/**
	 * Creates the main window's contents
	 * 
	 * @param parent
	 *            the main window
	 * @return Control
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		// Add the TableViewer
		tv = new TableViewer(composite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.BORDER);

		tv.setContentProvider(new CommandContentProvider());
		tv.setInput(commands);

		table = tv.getTable();
		table.setLayoutData(new GridData(GridData.FILL_BOTH));

		TableColumnLayout layout = new TableColumnLayout();
		composite.setLayout(layout);

		// Row Number Column
		TableViewerColumn row_col = createTableViewerColumn(ROW, 100, 0);
		row_col.setLabelProvider(new RowNumberLabelProvider(commands));
		layout.setColumnData(row_col.getColumn(), new ColumnWeightData(10));

		// Component Type Column
		TableViewerColumn component_col = createTableViewerColumn(COMPONENT, 100, 1);
		component_col.setLabelProvider(new ComponentLabelProvider());
		component_col.setEditingSupport(new ComponentEditingSupport(tv, limitedComponentSet) {
			public void dataChanged() {
				MonkeyTalkTabularEditor.this.doDataChanged();
			};
		});
		layout.setColumnData(component_col.getColumn(), new ColumnWeightData(30));

		TableViewerColumn monkeyid_col = createTableViewerColumn(MONKEYID, 100, 2);
		monkeyid_col.setLabelProvider(new MonkeyidLabelProvider());
		monkeyid_col.setEditingSupport(new MonkeyidEditingSupport(tv) {
			public void dataChanged() {
				MonkeyTalkTabularEditor.this.doDataChanged();
			};
		});
		layout.setColumnData(monkeyid_col.getColumn(), new ColumnWeightData(25));

		TableViewerColumn action_col = createTableViewerColumn(ACTION, 100, 3);
		action_col.setLabelProvider(new ActionLabelProvider());
		action_col.setEditingSupport(new ActionEditingSupport(tv) {
			public void dataChanged() {
				MonkeyTalkTabularEditor.this.doDataChanged();
			};
		});
		layout.setColumnData(action_col.getColumn(), new ColumnWeightData(30));

		TableViewerColumn args_col = createTableViewerColumn(ARGS, 100, 4);
		args_col.setLabelProvider(new ArgsLabelProvider());
		args_col.setEditingSupport(new ArgsEditingSupport(tv) {
			public void dataChanged() {
				MonkeyTalkTabularEditor.this.doDataChanged();
			};
		});
		layout.setColumnData(args_col.getColumn(), new ColumnWeightData(40));

		TableViewerColumn timeout_col = createTableViewerColumn(TIMEOUT, 100, 5);
		timeout_col.setLabelProvider(new TimeoutLabelProvider());
		timeout_col.setEditingSupport(new TimeoutEditingSupport(tv) {
			public void dataChanged() {
				MonkeyTalkTabularEditor.this.doDataChanged();
			};
		});
		layout.setColumnData(timeout_col.getColumn(), new ColumnWeightData(20));

		TableViewerColumn thinktime_col = createTableViewerColumn(THINKTIME, 100, 6);
		thinktime_col.setLabelProvider(new ThinktimeLabelProvider());
		thinktime_col.setEditingSupport(new ThinktimeEditingSupport(tv) {
			public void dataChanged() {
				MonkeyTalkTabularEditor.this.doDataChanged();
			};
		});
		layout.setColumnData(thinktime_col.getColumn(), new ColumnWeightData(20));

		String extencion = FilenameUtils.getExtension(((FileEditorInput) this.getEditorInput())
				.getPath().toFile().getName());
		if (extencion.equals("mt")) {
			TableViewerColumn shouldfail_col = createTableViewerColumn(SHOULDFAIL, 100, 7);
			shouldfail_col.getColumn().setAlignment(SWT.CENTER);
			shouldfail_col.setLabelProvider(new ShouldFailLabelProvider(tv));
			shouldfail_col.setEditingSupport(new ShouldFailEditingSupport(tv) {
				public void dataChanged() {
					MonkeyTalkTabularEditor.this.doDataChanged();
				};
			});

			layout.setColumnData(shouldfail_col.getColumn(), new ColumnWeightData(15));
		}

		tv.getTable().addListener(SWT.MenuDetect, new Listener() {
			public void handleEvent(Event event) {

				if (menueventsCaught != event.time) {
					new ContextMenu(MonkeyTalkTabularEditor.this, event).show();
					menueventsCaught = event.time;
				}

			}
		});
		table.addListener(SWT.MouseDoubleClick, new Listener() {
			@Override
			public void handleEvent(Event e) {
				TableItem item = table.getItem(new Point(e.x, e.y));
				if (item == null) {

				} else { // click link case
					String comp = ((TableRow) item.getData()).getComponentType();
					if (comp != null
							&& (comp.equalsIgnoreCase("Script") || comp.equalsIgnoreCase("Test")
									|| comp.equalsIgnoreCase("SetUp") || comp
										.equalsIgnoreCase("Teardown"))) {
						try {

							IEditorPart ieditorpart = MonkeyTalkTabularEditor.this.getEditorSite()
									.getPage().getActiveEditor();
							String dotMt = ".mt";
							if (((TableRow) item.getData()).getMonkeyId().contains(".mt")
									|| ((TableRow) item.getData()).getMonkeyId().contains(".js")) {
								dotMt = "";
							}
							IFile fileToBeOpened = ((IFileEditorInput) ieditorpart.getEditorInput())
									.getFile().getProject()
									.getFile(((TableRow) item.getData()).getMonkeyId() + dotMt);

							IEditorInput editorInput = new FileEditorInput(fileToBeOpened);
							MonkeyTalkTabularEditor.this
									.getEditorSite()
									.getPage()
									.openEditor(editorInput,
											"com.gorillalogic.monkeyconsole.editors.FoneMonkeyTestEditor");
							commandKeyDown = false;
						} catch (CoreException e2) {
							// TODO Auto-generated catch block
							e2.printStackTrace();
						}
					}
				}

			}

		});
		table.addListener(SWT.MouseUp, new Listener() {

			@Override
			public void handleEvent(Event e) {
				TableItem item = table.getItem(new Point(e.x, e.y));
				if (item == null) {
					if (e.y < (table.getItemCount() * table.getItemHeight())
							+ table.getItemHeight()) {
						appendRow();
					}
				} else { // click link case
					String comp = ((TableRow) item.getData()).getComponentType();
					if (comp != null
							&& commandKeyDown
							&& tv.getCell(new Point(e.x, e.y)).getColumnIndex() == 2
							&& (comp.equalsIgnoreCase("Script") || comp.equalsIgnoreCase("Test")
									|| comp.equalsIgnoreCase("Run")
									|| comp.equalsIgnoreCase("RunWith")
									|| comp.equalsIgnoreCase("SetUp") || comp
										.equalsIgnoreCase("Teardown"))) {
						try {

							IEditorPart ieditorpart = MonkeyTalkTabularEditor.this.getEditorSite()
									.getPage().getActiveEditor();
							IFile fileToBeOpened = ((IFileEditorInput) ieditorpart.getEditorInput())
									.getFile().getProject()
									.getFile(tv.getCell(new Point(e.x, e.y)).getText() + ".mt");

							IEditorInput editorInput = new FileEditorInput(fileToBeOpened);
							MonkeyTalkTabularEditor.this
									.getEditorSite()
									.getPage()
									.openEditor(editorInput,
											"com.gorillalogic.monkeyconsole.editors.FoneMonkeyTestEditor");
							commandKeyDown = false;
						} catch (CoreException e2) {
							// TODO Auto-generated catch block
							e2.printStackTrace();
						}
					}
				}

			}
		});
		// FOCUS TRAVERSAL
		FocusCellHighlighter highlighter = new FocusCellHighlighter(tv) {

		};

		TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(tv,
				highlighter) {

		};

		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(
				tv) {
			protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
				return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
						|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_CLICK_SELECTION
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.keyCode == SWT.CR)
						|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
			}
		};

		TableViewerEditor.create(tv, focusCellManager, actSupport,
				ColumnViewerEditor.TABBING_HORIZONTAL
						| ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
						| ColumnViewerEditor.TABBING_VERTICAL
						| ColumnViewerEditor.KEYBOARD_ACTIVATION);

		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent arg0) {
				if (arg0.keyCode == SWT.CTRL || arg0.keyCode == SWT.COMMAND) {
					commandKeyDown = true;
				}

			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				if (arg0.keyCode == SWT.CTRL || arg0.keyCode == SWT.COMMAND) {
					commandKeyDown = false;
				}
				if (commandKeyDown && (arg0.character == 'P' || arg0.character == 'p')) {
					if (monkeyControls.isCurrentlyConnected() && !monkeyControls.isRecordingON()
							&& getLimitedComponentSet() == null)
						monkeyControls
								.startReplayRange(tv.getTable().getSelectionIndex(), tv.getTable()
										.getSelectionIndex() + tv.getTable().getSelectionCount());
				}
				System.out.println(arg0.keyCode);
				if (arg0.keyCode == SWT.DEL || arg0.keyCode == 8) {
					deleteRows(getTv().getTable().getSelectionIndices());
				}
			}

		});
		int ops = DND.DROP_COPY | DND.DROP_MOVE;
		Transfer[] transfers = new Transfer[] { TextTransfer.getInstance() };
		tv.addDropSupport(ops, transfers, new GadgetTableDropAdapter(tv));
		return composite;
	}

	private static ViewerCell getNeighbor(ViewerCell currentCell, int directionMask,
			boolean sameLevel) {
		ViewerRow row;

		if ((directionMask & ViewerCell.ABOVE) == ViewerCell.ABOVE) {
			row = currentCell.getViewerRow().getNeighbor(ViewerRow.ABOVE, sameLevel);
		} else if ((directionMask & ViewerCell.BELOW) == ViewerCell.BELOW) {
			row = currentCell.getViewerRow().getNeighbor(ViewerRow.BELOW, sameLevel);
		} else {
			row = currentCell.getViewerRow();
		}

		if (row != null) {
			int columnIndex;
			columnIndex = getVisualIndex(row, currentCell.getColumnIndex());

			int modifier = 0;

			if ((directionMask & ViewerCell.LEFT) == ViewerCell.LEFT) {
				modifier = -1;
			} else if ((directionMask & ViewerCell.RIGHT) == ViewerCell.RIGHT) {
				modifier = 1;
			}

			columnIndex += modifier;

			if (columnIndex >= 0 && columnIndex < row.getColumnCount()) {
				ViewerCell cell = getCellAtVisualIndex(row, columnIndex);
				if (cell != null) {
					while (cell != null && columnIndex < row.getColumnCount() - 1
							&& columnIndex > 0) {
						if (isVisible(cell)) {
							break;
						}

						columnIndex += modifier;
						cell = getCellAtVisualIndex(row, columnIndex);
						if (cell == null) {
							break;
						}
					}
				}

				return cell;
			}
		}
		return null;
	}

	// Reimplementation of ViewerCell-Methods
	private static int getVisualIndex(ViewerRow row, int creationIndex) {
		TableItem item = (TableItem) row.getItem();
		int[] order = item.getParent().getColumnOrder();

		for (int i = 0; i < order.length; i++) {
			if (order[i] == creationIndex) {
				return i;
			}
		}
		return creationIndex;
	}

	private static ViewerCell getCellAtVisualIndex(ViewerRow row, int visualIndex) {
		return getCell(row, getCreationIndex(row, visualIndex));
	}

	private static boolean isVisible(ViewerCell cell) {
		return getWidth(cell) > 0;
	}

	private static int getWidth(ViewerCell cell) {
		TableItem item = (TableItem) cell.getViewerRow().getItem();
		return item.getParent().getColumn(cell.getColumnIndex()).getWidth();
	}

	private static ViewerCell getCell(ViewerRow row, int index) {
		return row.getCell(index);
	}

	private static int getCreationIndex(ViewerRow row, int visualIndex) {
		TableItem item = (TableItem) row.getItem();
		if (item != null && !item.isDisposed() /*
												 * && hasColumns() && isValidOrderIndex
												 * (visualIndex)
												 */) {
			return item.getParent().getColumnOrder()[visualIndex];
		}
		return visualIndex;
	}

	private TableViewerColumn createTableViewerColumn(String title, int bound, final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(tv, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;
	}

	/**
	 * Append a row to the bottom of the table
	 * 
	 * @return true if the row was added, false if not
	 */
	public void appendRow() {
		TableRow c = new TableRow();
		c.setAction("");
		c.setArgsAndModifiers("");
		c.setComponentType("");
		c.setMonkeyId("");
		commands.add(c);
		doDataChanged();
		tv.refresh();

	}

	/**
	 * Append a row to the bottom of the table
	 * 
	 * @return true if the row was added, false if not
	 */
	public void appendRow(Command c) {
		TableRow r = new TableRow(c);

		commands.add(r);
		doDataChanged();
		tv.refresh();
		table.setTopIndex(table.getItemCount() - 1);
	}

	/**
	 * Append a row using coalescing
	 * 
	 * @param c
	 * @param useCooalessing
	 */
	public void appendRow(Command cr, boolean useCooalessing) {
		TableRow newrow = new TableRow(cr);
		if (useCooalessing
				&& getCommands().size() > 0
				&& FoneMonkeyPlugin.getDefault().getPreferenceStore()
						.getString(PreferenceConstants.P_EVENTSTOCOMBINE).toLowerCase()
						.contains(newrow.getAction().toLowerCase())) {
			Command lastRow = getCommands().get(getCommands().size() - 1);
			if (null != lastRow && lastRow.getAction().equalsIgnoreCase(newrow.getAction())
					&& lastRow.getComponentType().equalsIgnoreCase(newrow.getComponentType())
					&& lastRow.getCommandName().equalsIgnoreCase(newrow.getCommandName())
					&& lastRow.getMonkeyId().equalsIgnoreCase(newrow.getMonkeyId())) {
				commands.set(getCommands().size() - 1, new TableRow(newrow));
				doDataChanged();
			} else {
				commands.add(newrow);
				doDataChanged();
			}
		} else {
			commands.add(newrow);
			doDataChanged();
		}
		tv.refresh();
		table.setTopIndex(table.getItemCount() - 1);
	}

	/**
	 * 
	 * @param rowToInsertAbove
	 */
	public void insertRow(int rowToInsertAbove) {
		TableRow c = new TableRow();
		c.setAction("");
		c.setArgsAndModifiers("");
		c.setComponentType("");
		c.setMonkeyId("");
		commands.add(rowToInsertAbove, c);
		doDataChanged();
		tv.refresh();

	}

	/**
	 * Delete the rows provided
	 * 
	 * @param start
	 *            the first row to delete 0 indexed
	 * @param end
	 *            the last row to delete 0 indexed
	 */
	public void deleteRows(int start, int end) {
		for (int i = end; i > start; i--) {
			commands.remove(i);
			doDataChanged();
		}
		tv.refresh();
	}

	/**
	 * Delete a random assortment of rows
	 * 
	 * @param rowsToDelete
	 *            a SORTED low to high collection of rows to be deleted
	 */
	public void deleteRows(int[] rowsToDelete) {
		for (int i = rowsToDelete.length - 1; i >= 0; i--) {
			commands.remove(rowsToDelete[i]);
			doDataChanged();
		}
		tv.refresh();
	}

	/**
	 * Convinence function for deleting one row
	 * 
	 * @param rowNumberthe
	 *            row to be deleted
	 */
	public void deleteRow(int rowNumber) {
		int[] i = new int[1];
		i[0] = rowNumber;
		deleteRows(i);
	}

	/**
	 * Convinence function for deleting all rows
	 */
	public void clear() {
		commands.removeAll(commands);
		doDataChanged();
		tv.refresh();
	}

	@Override
	public void doSave(IProgressMonitor arg0) {
		isDirty = false;

	}

	@Override
	public void doSaveAs() {
		isDirty = false;

	}

	@Override
	public void init(IEditorSite arg0, IEditorInput arg1) throws PartInitException {
		this.setSite(arg0);
		this.setInput(arg1);

	}

	@Override
	public void createPartControl(Composite parent) {
		this.createContents(parent);
	}

	boolean isDirty = false;

	public void doDataChanged() {
		isDirty = true;
		this.firePropertyChange(PROP_DIRTY);
	}

	@Override
	public boolean isDirty() {
		return isDirty;
	}

	public void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
		this.firePropertyChange(PROP_DIRTY);
	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void setFocus() {
		// Must set focus somewhere or Project Navigator gets whacked
		table.setFocus();
		// this.setPlaybackControlsState();

	}
    public void deleteBlankRows(){
		MonkeyTalkUtils.runOnGUI(new Runnable() {
			public void run() {
				for (int i = 0; i < commands.size(); i++) {
					if (commands.get(i).toString().equalsIgnoreCase("* * *")){
						deleteRow(i);
					}
					}
			}
		}, getSite().getShell().getDisplay());

    }
	public int getBlankCommandOffset(int row) {
		int retVal = 0;
		for (int i = 0; i < commands.size(); i++) {
			if (commands.get(i).toString().equalsIgnoreCase("* * *")){
				if(i < row)
				   retVal++;
			}
		}

		return retVal;
	}
	public java.util.List<Command> getCommands() {
		List<Command> retCommands = new ArrayList<Command>();
		for (int i = 0; i < commands.size(); i++) {
			if (!commands.get(i).toString().equalsIgnoreCase("* * *")){
				retCommands.add(commands.get(i));
			}
		}

		return retCommands;
	}

	public void setCommands(java.util.List<Command> commandsparam) {
		commands.removeAll(commands);
		for (Command c : commandsparam) {
			commands.add(new TableRow(c));
		}
		tv.setInput(commands);
		tv.refresh();
	}

	public String getCommandsAsString() {
		String result = "";
		for (Command c : getCommands()) {
			result += c.toString() + "\n";
		}
		if (result.length() > 0) {
			// remove tailing newline character
			result = result.substring(0, result.length() - 1);
		}
		return result;
	}

	public TableViewer getTv() {
		return tv;
	}

	public void setTv(TableViewer tv) {
		this.tv = tv;
	}

	public void setSelection(int i) {
		tv.getTable().setSelection(i);

	}

	public void markRowAsError(int i) {
		// TODO Auto-generated method stub

	}

	public String[] getLimitedComponentSet() {
		return limitedComponentSet;
	}

	public void setLimitedComponentSet(String[] limitedComponentSet) {
		this.limitedComponentSet = limitedComponentSet;
	}
}
