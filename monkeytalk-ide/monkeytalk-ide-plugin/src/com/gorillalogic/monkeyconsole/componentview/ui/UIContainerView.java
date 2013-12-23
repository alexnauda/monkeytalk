package com.gorillalogic.monkeyconsole.componentview.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gorillalogic.monkeyconsole.componentview.model.Model;
import com.gorillalogic.monkeyconsole.componentview.model.UIComponent;
import com.gorillalogic.monkeyconsole.componentview.model.UIContainer;
import com.gorillalogic.monkeyconsole.editors.utils.MonkeyTalkImagesEnum;
import com.gorillalogic.monkeyconsole.plugin.FoneMonkeyPlugin;


/**
 * Insert the type's description here.
 * @see ViewPart
 */
public class UIContainerView extends ViewPart implements ITreeViewerListener {
	protected TreeViewer treeViewer;
	protected Text searchBox;
	protected UIContainerLabelProvider labelProvider;
	protected UIContainerContentProvider contentProvider;
	protected boolean showHidden = true;
	protected List<UIComponent> searchMatches = new ArrayList<UIComponent>();
	
	protected UIContainer root;
	
	/**
	 * The constructor.
	 */
	public UIContainerView() {
	}

	/*
	 * @see IWorkbenchPart#createPartControl(Composite)
	 */
	public void createPartControl(Composite parent) {
		/* Create a grid layout object so the text and treeviewer
		 * are layed out the way I want. */
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.verticalSpacing = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 2;
		parent.setLayout(layout);
		
		createSearchBox(parent);
		
		createTreeViewer(parent);
		
		// Create menu, toolbars, filters, sorters.
		createActions();
		createMenus();
		createToolbar();
		hookListeners();
		
		treeViewer.setInput(getInitalContainer());
		
		int ops = DND.DROP_COPY | DND.DROP_MOVE;
		Transfer[] transfers = new Transfer[] { TextTransfer.getInstance()};
		treeViewer.addDragSupport(ops, transfers, new GadgetDragListener(treeViewer));
	}
	
	private void createTreeViewer(Composite parent) {
		// Create the tree viewer as a child of the composite parent
		treeViewer = new TreeViewer(parent);
		treeViewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);

		contentProvider=new UIContainerContentProvider();
		treeViewer.setContentProvider(contentProvider);
		labelProvider = new UIContainerLabelProvider();
		treeViewer.setLabelProvider(labelProvider);
		
		// search filter
		treeViewer.addFilter(new ViewerFilter(){

			@Override
			public boolean select(Viewer arg0, Object parentElement, Object element) {
				if(element instanceof UIComponent){
					UIComponent u = (UIComponent) element;
					
					if(!searchMatches(u,searchBox.getText().toLowerCase())) {
						return false;
					}
				}
				return true;
			}});
		
		// visible filter
		treeViewer.addFilter(new ViewerFilter(){			
			@Override
			public boolean select(Viewer arg0, Object parentElement, Object element) {
				if(element instanceof UIComponent){
					UIComponent u = (UIComponent) element;
					
					if(!u.isVisible() && !showHidden) {
						return false;
					}
				}
				return true;
			}});
		treeViewer.setUseHashlookup(true);
		
		// tree viewer is greedy for space
		GridData layoutData = new GridData();
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.grabExcessVerticalSpace = true;
		layoutData.horizontalAlignment = GridData.FILL;
		layoutData.verticalAlignment = GridData.FILL;
		treeViewer.getControl().setLayoutData(layoutData);
		
	}

	private void createSearchBox(Composite parent) {
		Canvas searchBoxCanvas = new Canvas(parent, SWT.CENTER);
		
		GridData layoutData = new GridData();
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.horizontalAlignment = GridData.FILL;
		searchBoxCanvas.setLayoutData(layoutData);

		/* Create a grid layout object so the text and treeviewer
		 * are layed out the way I want. */
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 2;
		layout.marginHeight = 2;
		searchBoxCanvas.setLayout(layout);
		
		Label searchLabel = new Label(searchBoxCanvas, SWT.RIGHT);
		searchLabel.setText("Search:");
		
		layoutData = new GridData();
		layoutData.grabExcessHorizontalSpace = false;
		layoutData.horizontalAlignment = GridData.FILL;
		searchLabel.setLayoutData(layoutData);
		
		searchBox = new Text(searchBoxCanvas, SWT.SINGLE | SWT.BORDER);
		searchBox.setToolTipText("Search the tree for any text");		
		searchBox.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				searchMatches.clear();
                treeViewer.refresh();
				treeViewer.expandAll();
			}			
		});
		
		layoutData = new GridData();
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.horizontalAlignment = GridData.FILL;
		searchBox.setLayoutData(layoutData);
		
	}
	
	private boolean searchMatches(UIComponent u, String searchString) {
		if (searchMatches.contains(u)) {
			return true;
		}
		if (u.getLabelString().toLowerCase().contains(searchString)) {
			searchMatches.add(u);
			if (u instanceof UIContainer) {
				// keep all descendants of a matching node
				addDescendantsToSearchMatches((UIContainer)u);
			}
			return true;
		} else if (u instanceof UIContainer) {
			for (UIComponent child : ((UIContainer)u).getChildren()) {
				if (searchMatches(child, searchString)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private void addDescendantsToSearchMatches(UIContainer u) {
		for (UIComponent c : u.getChildren()) {
			if (!searchMatches.contains(c)) {
				searchMatches.add(c);
			}
			if (c instanceof UIContainer) {
				addDescendantsToSearchMatches((UIContainer)c);
			}
		}
	}
	
	protected void hookListeners() {
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				// if the selection is empty clear the label
//				if(event.getSelection().isEmpty()) {
//					text.setText("");
//					return;
//				}
				if(event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection)event.getSelection();
					StringBuffer toShow = new StringBuffer();
					for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
						Object domain = (Model) iterator.next();
						String value = labelProvider.getText(domain);
						toShow.append(value);
						toShow.append(", ");
					}
					// remove the trailing comma space pair
					if(toShow.length() > 0) {
						toShow.setLength(toShow.length() - 2);
					}
					//text.setText(toShow.toString());
				}
			}
		});
	}
	
	Action expandAll = null;
	Action collapseAll = null;
	Action toggleVisible = null;
	Action treeRefresh = null;
	protected void createActions() {
		expandAll = new Action() {
			public void run() {
				treeViewer.expandAll();
			}
		};
		expandAll.setText("Expand all");
		expandAll.setToolTipText("Expand all items in this tree");
		expandAll.setImageDescriptor(MonkeyTalkImagesEnum.EXPANDALL.image);
		expandAll.setId("expandall");
	
		collapseAll = new Action() {
			public void run() {
				treeViewer.collapseAll();
			}
		};		
		
		collapseAll.setText("Collapse all");
		collapseAll.setToolTipText("Collapse all items in this tree");
		collapseAll.setImageDescriptor(MonkeyTalkImagesEnum.COLLAPSEALL.image);
		collapseAll.setId("collapseall");
		
		toggleVisible = new Action() {
			
			public void run() {
				toggleVisible.setChecked(!toggleVisible.isChecked());
				showHidden = !showHidden;
				treeViewer.refresh();
				treeViewer.expandAll();
				setToggleVisibleState();
			}
		};
		toggleVisible.setId("toggleVis");
		setToggleVisibleState();
		
		treeRefresh = new Action() {
			public void run() {
				FoneMonkeyPlugin.getDefault().getController().fetchAndShowComponentTree();
				treeViewer.refresh();
				treeViewer.expandAll();
			}
		};		
		
		treeRefresh.setText("Tree Refresh");
		treeRefresh.setToolTipText("Refresh the component tree from the app");
		treeRefresh.setImageDescriptor(MonkeyTalkImagesEnum.TREEREFRESH.image);
		treeRefresh.setId("treeRefresh");
		
	}
	
	protected void setToggleVisibleState() {
		if (showHidden) {
			toggleVisible.setText("Hide Hidden Components");
			toggleVisible.setToolTipText("Hide hidden components");
			toggleVisible.setImageDescriptor(MonkeyTalkImagesEnum.EYE.image);
		} else {
			toggleVisible.setText("Show Hidden Components");
			toggleVisible.setToolTipText("Show hidden components");
			toggleVisible.setImageDescriptor(MonkeyTalkImagesEnum.EYENO.image);
		}
	}
		
	protected void createMenus() {
		IMenuManager rootMenuManager = getViewSite().getActionBars().getMenuManager();
		rootMenuManager.setRemoveAllWhenShown(true);    
	}

	protected void createToolbar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
		toolbarManager.add(expandAll);
		toolbarManager.add(collapseAll);
		toolbarManager.add(toggleVisible);
		toolbarManager.add(treeRefresh);
	}
	
	public  void setInput(JSONObject jasonData) throws JSONException{
		if(jasonData == null) return;
		root = this.getInitalContainer();
		JSONArray ja = new JSONArray();
		
		ja.put(jasonData.getJSONObject("message"));
        root = convertJsonArrayToBoxes(ja, root);
		treeViewer.setInput(root);
	}
	
	public UIContainer convertJsonArrayToBoxes(JSONArray jsonArray, UIContainer root) throws JSONException{
		for(int i = 0; i < jsonArray.length(); i++){
			JSONObject rootNode = jsonArray.getJSONObject(i);			
			String title=formatTitle(rootNode);
			boolean isVisible=isVisible(rootNode);
			if(rootNode.has("children") && rootNode.getJSONArray("children").length() > 0) {
				// String title=rootNode.getString("ComponentType") + "(" +rootNode.getString("monkeyId")+")";
				UIContainer parent = new UIContainer(title, isVisible);
				root.add(parent);
				convertJsonArrayToBoxes(rootNode.getJSONArray("children"),parent);
			} else {
				root.add(new UIComponent(title, isVisible));
			}
		}
		return root;
	}
	
	protected boolean isVisible(JSONObject json) throws JSONException {
		boolean isVisible=true;
		if(json.has("visible")) {
			String viz=json.getString("visible");
			isVisible = (viz!=null && viz.contains("t"));
		}
		return isVisible;
	}
	
	private String formatTitle(JSONObject rootNode) throws JSONException {
		String componentType = rootNode.has("ComponentType") ? rootNode.getString("ComponentType") : "View";
		String monkeyId = "";
		if (rootNode.has("monkeyId")) {
			String mid = rootNode.getString("monkeyId");
			if (!mid.toCharArray().equals("null")) {
				monkeyId = mid.replace("\n", "\\n");
			}
		}
		String vizability = "";
		if(rootNode.has("visible")) {
			String viz=rootNode.getString("visible");
			vizability = (viz!=null && viz.contains("t")) ? "visible" : "hidden";
		}
		String identifiers = "";
		if (rootNode.has("identifiers")) {
			identifiers=formatIdentifiers(rootNode.getJSONArray("identifiers"), monkeyId);
		}
		String ordinal = "";
		if (rootNode.has("ordinal")) {
			Object o = rootNode.get("ordinal");
			if (o instanceof String) {
				ordinal = (String)o;
			} else if (o instanceof Integer) {
				Integer ord = (Integer)o;
				if (ord > 0) {
					ordinal = " #" + (Integer)o;
				}
			}
		}
		String title=componentType
				+ ordinal
				+ " (" 
				+ monkeyId
				+ ") " 
				+ identifiers
				+ " " + rootNode.getString("className") 
				+ " " +  vizability;
		return title;
	}
	
	private String formatIdentifiers(JSONArray identifiersJson, String monkeyId) throws JSONException {
		StringBuilder sb=new StringBuilder();
		for (int j=0; j<identifiersJson.length(); j++) {
			String identifier = identifiersJson.getString(j);
			if (identifier!=null && identifier.length()>0 && !identifier.equals(monkeyId)) {
				if (sb.indexOf(identifier)==-1) {
					if (sb.length()==0) {
						sb.append("[");
					} else {
						sb.append(", ");
					}
					sb.append(identifier.replace("\n","\\n"));
				}
			}
		}
		if (sb.length()>0) {
			sb.append("]");
		}
		return sb.toString();
	}
	
	private UIContainer getInitalContainer() {
		root = new UIContainer("root", true);
		return root;
	}

	/*
	 * @see IWorkbenchPart#setFocus()
	 */
	public void setFocus() {}

	@Override
	public void treeCollapsed(TreeExpansionEvent arg0) {
	}
	
	@Override
	public void treeExpanded(TreeExpansionEvent arg0) {
	}
}
