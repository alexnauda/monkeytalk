package com.gorillalogic.monkeyconsole.componentview.model;



public interface IModelVisitor {
	public void visitUIContainer(UIContainer container, Object passAlongArgument);
	public void visitUIComponent(UIComponent component, Object passAlongArgument);
}
