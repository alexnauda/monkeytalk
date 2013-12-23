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
package com.gorillalogic.fonemonkey.automators;

import java.util.List;

import android.view.View;

public interface IAutomator {

	/**
	 * The base class handled by this IAutomator. The IAutomator will handle all subclasses between
	 * this base class and the first subclass handled by another registered IAutomator.
	 * 
	 * @return the base class
	 * @see {@link AutomationManager#registerClass(String, Class, Class)}
	 */
	public Class<?> getComponentClass();

	/**
	 * The String value that identifies this component member
	 * 
	 * @return
	 */
	public String getMonkeyID();

	/**
	 * Sets the monkeyID. Will be ignored if automator has a read-only monkeyID
	 * 
	 * @return
	 */
	public void setMonkeyID(String id);

	/**
	 * @return the logical typeName of this component
	 */
	public String getComponentType();

	/**
	 * The component being automated
	 * 
	 * @return
	 */
	public Object getComponent();

	/**
	 * The component being automated
	 * 
	 * @param o
	 */
	public void setComponent(Object o);

	/**
	 * Play an action against the automated component
	 * 
	 * @param action
	 * @param args
	 * @return A string value, or null if the action has no return value
	 */
	public String play(String action, String... args);

	/**
	 * Record this operation
	 * 
	 * @param action
	 * @param args
	 */
	public void record(String action, String... args);

	/**
	 * Tests if this child view should be prevented from recording its own events.
	 * 
	 * @param view
	 * @return
	 */
	public boolean hides(View view);

	/**
	 * Record the action with the supplied args. The first argument is the view.
	 * 
	 * @param action
	 * @param args
	 */
	public void record(String action, Object[] args);

	/**
	 * @param componentType
	 * @return true if this automator is mapped to a subtype of this componentType
	 */
	public boolean forSubtypeOf(String componentType);

	/**
	 * @return Alternate componentType names that this automator handles
	 */
	public String[] getAliases();

	/**
	 * 
	 * @return a string representing the logical value of this component.
	 */
	public String getValue();

	/**
	 * The ordinal identifier of this view, derived by determining its position among all currently
	 * displayed components of the same type. (Upper-left-most to Lower-right-most sequence)
	 * 
	 * @return identifier in the form #n
	 */
	int getOrdinal();

	/**
	 * A java.util.List of String objects which can be used as monkeyIDs for this the view
	 * 
	 * @return a java.util.List of String objects which can be used to identify the view
	 */
	List<String> getIdentifyingValues();

	/**
	 * installs any default event listeners needed for this component type if they have not been
	 * explicitly set already
	 * 
	 * @return true if a listener was set, else false
	 */
	public boolean installDefaultListeners();

	/**
	 * @return true if this automator can provide automation for a component with the supplied
	 *         componentType and monkeyID
	 */
	public boolean canAutomate(String componentType, String monkeyID);

	/**
	 * @return true if this automator is an html automator
	 */
	public boolean isHtmlAutomator();

}
