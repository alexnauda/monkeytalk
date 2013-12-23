package com.gorillalogic.monkeyconsole.componentview.model;

import java.util.ArrayList;
import java.util.List;

public class UIContainer extends UIComponent {
	protected List<UIComponent> children;
	
	public List<UIComponent> getChildren() {
		return children;
	}
	private static IModelVisitor adder = new Adder();
	private static IModelVisitor remover = new Remover();
	
	private void initLists() {
		children = new ArrayList<UIComponent>();
	}
	
	private void addChild(UIComponent child) {
		children.add(child);
	}
	
	private static class Adder implements IModelVisitor {
		public void visitUIComponent(UIComponent component, Object argument) {
			((UIContainer) argument).addChild(component);
		}

		/*
		 * @see ModelVisitorI#visitMovingBox(MovingBox, Object)
		 */
		public void visitUIContainer(UIContainer child, Object argument) {
			((UIContainer) argument).addChild(child);
		}

	}

	private static class Remover implements IModelVisitor {

		/*
		 * @see ModelVisitorI#visitBook(MovingBox, Object)
		 */
		public void visitUIComponent(UIComponent component, Object argument) {
			((UIContainer) argument).removeChild(component);
		}

		/*
		 * @see ModelVisitorI#visitMovingBox(MovingBox, Object)
		 */
		public void visitUIContainer(UIContainer child, Object argument) {
			((UIContainer) argument).removeChild(child);
			child.addListener(NullDeltaListener.getSoleInstance());
		}
	}
	
	public UIContainer(String labelString, boolean isVisible) {
		super(labelString, isVisible);
		initLists();
	}
	
	public void remove(Model toRemove) {
		toRemove.accept(remover, this);
	}
	
	protected void removeChild(UIComponent child) {
		children.remove(child);
		child.addListener(NullDeltaListener.getSoleInstance());
		fireRemove(child);
	}
	
	protected void removeChild(UIContainer child) {
		children.remove(child);
		child.addListener(NullDeltaListener.getSoleInstance());
		fireRemove(child);	
	}

	public void add(Model toAdd) {
		toAdd.accept(adder, this);
	}
	
	/** Answer the total number of items the
	 * receiver contains. */
	public int size() {
		return getChildren().size();
	}
	/*
	 * @see Model#accept(ModelVisitorI, Object)
	 */
	public void accept(IModelVisitor visitor, Object passAlongArgument) {
		visitor.visitUIContainer(this, passAlongArgument);
	}

}
