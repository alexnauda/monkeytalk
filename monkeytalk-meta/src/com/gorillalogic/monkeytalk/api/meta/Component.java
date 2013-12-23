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
package com.gorillalogic.monkeytalk.api.meta;

import java.util.ArrayList;
import java.util.List;

/**
 * The meta API object for a component.
 */
public class Component extends BaseMeta {
	private String extendz;
	private List<Action> actions;
	private List<Property> properties;

	/**
	 * Instantiate a new {@code Component} with the given name, description,
	 * super class, the list of actions, and the list of properties.
	 * 
	 * @param name
	 *            the name
	 * @param description
	 *            the description
	 * @param extendz
	 *            the super class (as a String)
	 * @param actions
	 *            the list of actions
	 *            @param properties
	 *            the list of properties
	 */
	public Component(String name, String description, String extendz,
			List<Action> actions, List<Property> properties) {
		super(name, description);
		this.extendz = extendz;
		this.actions = actions;
		this.properties = properties;
	}

	/**
	 * Get the super class (aka the parent class) of the current meta API
	 * object.
	 * 
	 * @return the super class meta API object (aka the parent class)
	 */
	public Component getSuper() {
		return API.getComponent(extendz);
	}

	/**
	 * Get the complete list of actions available on the current component. As
	 * implemented, this method recurses all the way up the meta API object
	 * hierarchy and adds all parent actions as it goes.
	 * 
	 * @return the complete list of {@code Action} objects
	 */
	public List<Action> getActions() {
		List<Action> list = new ArrayList<Action>();
		if (actions != null) {
			list.addAll(actions);
		}
		if (getSuper() != null) {
			for (Action a : getSuper().getActions()) {
				boolean overrideExists = false;
				for (Action b : list) {
					if (b.getName().equalsIgnoreCase(a.getName())) {
						overrideExists = true;
						break;
					}
				}
				if (!overrideExists) {
					list.add(a);
				}
			}
		}
		return list;
	}

	/**
	 * Get the complete list of action names.
	 * 
	 * @return the complete list of action names
	 */
	public List<String> getActionNames() {
		List<String> list = new ArrayList<String>();
		for (Action a : getActions()) {
			list.add(a.getName());
		}
		return list;
	}

	/**
	 * Get an {@code Action} object by name. Returns null if not found.
	 * 
	 * @param action
	 *            the action name
	 * @return the action
	 */
	public Action getAction(String action) {
		if (action != null) {
			for (Action a : getActions()) {
				if (a.getName().equalsIgnoreCase(action)) {
					return a;
				}
			}
		}
		return null;
	}

	/**
	 * Add the given action to the list of actions.
	 * 
	 * @param action
	 *            the action
	 */
	public void addAction(Action action) {
		if (action != null) {
			actions.add(action);
		}
	}
	
	/**
	 * Get the complete list of properties available on the current component. As
	 * implemented, this method recurses all the way up the meta API object
	 * hierarchy and adds all parent properties as it goes.
	 * 
	 * @return the complete list of {@code Property} objects
	 */
	public List<Property> getProperties() {
		List<Property> list = new ArrayList<Property>();
		if (properties != null) {
			list.addAll(properties);
		}
		if (getSuper() != null) {
			for (Property p : getSuper().getProperties()) {
				boolean overrideExists = false;
				for (Property q : list) {
					if (q.getName().equalsIgnoreCase(p.getName())) {
						overrideExists = true;
						break;
					}
				}
				if (!overrideExists) {
					list.add(p);
				}
			}
		}
		return list;
	}
	
	/**
	 * Get the complete list of property names.
	 * 
	 * @return the complete list of property names
	 */
	public List<String> getPropertyNames() {
		List<String> list = new ArrayList<String>();
		for (Property p : getProperties()) {
			list.add(p.getName());
		}
		return list;
	}

	/**
	 * Get an {@code Property} object by name. Returns null if not found.
	 * 
	 * @param property
	 *            the property name
	 * @return the property
	 */
	public Property getProperty(String property) {
		if (property != null) {
			for (Property p : getProperties()) {
				if (p.getName().equalsIgnoreCase(property)) {
					return p;
				}
			}
		}
		return null;
	}

	/**
	 * Add the given property to the list of properties.
	 * 
	 * @param property
	 *            the property
	 */
	public void addProperty(Property property) {
		if (property != null) {
			properties.add(property);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString()).append("\n");
		for (Action a : getActions()) {
			sb.append("  ").append(a.toString(false)).append("\n");
		}
		for (Property p : getProperties()) {
			sb.append("  ").append(p.toString(false)).append("\n");
		}
		return sb.toString();

	}
}