package com.gorillalogic.monkeyconsole.componentview.model;

public class UIComponent extends Model {
	private boolean isVisible=true; 
	
	public UIComponent(String labelString) {
		super(labelString);
	}
	
	public UIComponent(String labelString, boolean isVisible) {
		super(labelString);
		this.isVisible=isVisible;
	}
	
	/*
	 * @see Model#accept(ModelVisitorI, Object)
	 */
	public void accept(IModelVisitor visitor, Object passAlongArgument) {
		visitor.visitUIComponent(this, passAlongArgument);
	}

	public boolean isVisible() {
		return isVisible;
	}
	
}
