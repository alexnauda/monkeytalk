package com.gorillalogic.monkeyconsole.componentview.model;
public abstract class Model {
	protected UIContainer parent;
	protected String labelString;
	protected IDeltaListener listener = NullDeltaListener.getSoleInstance();
	
	protected void fireAdd(Object added) {
		listener.add(new DeltaEvent(added));
	}

	protected void fireRemove(Object removed) {
		listener.remove(new DeltaEvent(removed));
	}
	
	public void setLabelString(String labelString) {
		this.labelString = labelString;
	}
	
	public UIContainer getParent() {
		return parent;
	}
	
	/* The receiver should visit the toVisit object and
	 * pass along the argument. */
	public abstract void accept(IModelVisitor visitor, Object passAlongArgument);
	
	public String getLabelString() {
		return labelString;
	}
	
	public void addListener(IDeltaListener listener) {
		this.listener = listener;
	}
	
	public Model(String labelString) {
		this.labelString = labelString;
	}
	
	public Model() {
	}	
	
	public void removeListener(IDeltaListener listener) {
		if(this.listener.equals(listener)) {
			this.listener = NullDeltaListener.getSoleInstance();
		}
	}
}
