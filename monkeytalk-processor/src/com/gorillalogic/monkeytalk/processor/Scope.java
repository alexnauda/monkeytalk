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
package com.gorillalogic.monkeytalk.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.gorillalogic.monkeytalk.Command;

/**
 * Class for managing the context of a running MonkeyTalk script. Every recursive MonkeyTalk command
 * ({@code Script}, {@code Setup}, and {@code Teardown}) creates a new scope. Variables, both
 * built-in and named, are not recursive -- they only exist intra-scope.
 */
public class Scope implements Cloneable {
	private Scope parentScope;
	private String filename;
	private String componentType;
	private String monkeyId;
	private String action;
	private List<String> args;
	private Map<String, String> variables;
	private Command currentCommand;
	private int currentIndex;

	/**
	 * Instantiate an empty scope object.
	 */
	public Scope() {
		this((String) null, null);
	}

	/**
	 * Instantiate a new scope with just the current script filename.
	 * 
	 * @param filename
	 *            the MonkeyTalk script filename
	 */
	public Scope(String filename) {
		this(filename, null);
	}

	/**
	 * Instantiate a new scope with the current script filename and the parent scope (which implies
	 * we are moving down the scope stack).
	 * 
	 * @param filename
	 *            the MonkeyTalk script filename
	 * @param parentScope
	 *            the parent scope (above us on the scope stack)
	 */
	public Scope(String filename, Scope parentScope) {
		this(filename, parentScope, null, null, null, null, null);
	}

	/**
	 * Instantiate a new scope with the parent command and parent scope.
	 * 
	 * @param command
	 *            the parent command
	 * @param parentScope
	 *            the parent scope (above us on the scope stack)
	 */
	public Scope(Command command, Scope parentScope) {
		this(command.getMonkeyId(), parentScope, command.getComponentType(), command.getMonkeyId(),
				command.getAction(), command.getArgs(), null);
	}

	/**
	 * Instantiate a new scope with the parent command, parent scope, and a map of named variables.
	 * The only reason to create a scope with variables is when the script is being data-driven.
	 * 
	 * @param command
	 *            the parent command
	 * @param parentScope
	 *            the parent scope (above us on the scope stack)
	 * @param variables
	 *            the named variables (used when data-driving)
	 */
	public Scope(Command command, Scope parentScope, Map<String, String> variables) {
		this(command.getMonkeyId(), parentScope, command.getComponentType(), command.getMonkeyId(),
				command.getAction(), command.getArgs(), variables);
	}

	/**
	 * Instantiate a new scope with the current script filename, parent scope, built-in variables,
	 * and a map of named variables. The only reason to create a scope with variables is when the
	 * script is being data-driven.
	 * 
	 * @param filename
	 *            the MonkeyTalk script filename
	 * @param parentScope
	 *            the parent scope (above us on the scope stack)
	 * @param componentType
	 *            the parent MonkeyTalk command componentType
	 * @param monkeyId
	 *            the parent MonkeyTalk command monkeyId
	 * @param action
	 *            the parent MonkeyTalk command action
	 * @param args
	 *            the parent MonkeyTalk command args
	 * @param variables
	 *            the named variables (used when data-driving)
	 */
	public Scope(String filename, Scope parentScope, String componentType, String monkeyId,
			String action, List<String> args, Map<String, String> variables) {
		this.parentScope = parentScope;
		this.filename = filename;
		this.componentType = componentType;
		this.monkeyId = monkeyId;
		this.action = action;
		this.args = args;
		this.variables = variables;
		this.currentCommand = null;
		this.currentIndex = 0;
	}

	/**
	 * Get the parent scope. Returns {@code null} if this is the top of the scope stak (aka no
	 * parent exists).
	 * 
	 * @return the parent scope
	 */
	public Scope getParentScope() {
		return parentScope;
	}

	/**
	 * Get the current script filename.
	 * 
	 * @return the script filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Get the parent MonkeyTalk command componentType.
	 * 
	 * @return the componentType
	 */
	public String getComponentType() {
		return componentType;
	}

	/**
	 * Get the parent MonkeyTalk command monkeyId.
	 * 
	 * @return the monkeyId
	 */
	public String getMonkeyId() {
		return monkeyId;
	}

	/**
	 * Get the parent MonkeyTalk command action.
	 * 
	 * @return the action
	 */
	public String getAction() {
		return action;
	}

	/**
	 * Get the parent MonkeyTalk command args.
	 * 
	 * @return the args
	 */
	public List<String> getArgs() {
		if (args == null) {
			args = new ArrayList<String>();
		}
		return args;
	}

	/**
	 * Get the current named variables in the scope. This never returns {@code null}.
	 * 
	 * @return the named variables
	 */
	public Map<String, String> getVariables() {
		if (variables == null) {
			variables = new LinkedHashMap<String, String>();
		}
		return variables;
	}

	/**
	 * Helper to add a single named variable to the scope.
	 * 
	 * @param key
	 *            the variable key (aka it's name)
	 * @param val
	 *            the variable value
	 */
	public void addVariable(String key, String val) {
		getVariables().put(key, val);
	}

	/**
	 * Helper to add some named variables to the scope.
	 * 
	 * @param vars
	 *            the named variables
	 */
	public void addVariables(Map<String, String> vars) {
		if (vars != null) {
			getVariables().putAll(vars);
		}
	}

	/**
	 * Get the current MonkeyTalk command.
	 * 
	 * @return the command
	 */
	public Command getCurrentCommand() {
		return currentCommand;
	}

	/**
	 * Get the current MonkeyTalk command index. This is the 1-based index of commands run in the
	 * current scope, which usually corresponds to the line number in the current script file.
	 * 
	 * @see Scope#setCurrentCommand(Command, int)
	 * 
	 * @return the command index
	 */
	public int getCurrentIndex() {
		return currentIndex;
	}

	/**
	 * Set the given MonkeyTalk command as the current command, and also increment the command
	 * index.
	 * 
	 * @param command
	 *            the MonkeyTalk {@link Command}
	 */
	public void setCurrentCommand(Command command) {
		setCurrentCommand(command, currentIndex + 1);
	}

	/**
	 * Set the given MonkeyTalk command as the current command, and set the given index as the
	 * current command index.
	 * 
	 * @param command
	 *            the MonkeyTalk {@link Command}
	 * @param index
	 *            the command index
	 */
	public void setCurrentCommand(Command command, int index) {
		currentCommand = command;
		currentIndex = index;
	}

	/**
	 * Set the given index as the current command index.
	 * 
	 * @param index
	 *            the command index
	 */
	public void setCurrentIndex(int index) {
		currentIndex = index;
	}

	/**
	 * Substitute all variables found in the given command, and return a new fully-substituted
	 * {@link Command} object. If the given command is a comment, do nothing.
	 * 
	 * @param command
	 *            the MonkeyTalk {@link Command} object
	 * @return a new full-substituted {@link Command}
	 */
	public Command substituteCommand(Command command) {
		if (command.isComment()) {
			return command.clone();
		} else {
			return command.clone().substitute(componentType, monkeyId, action, args,
					merge(Globals.getGlobals(), variables));
		}
	}

	/** Helper to merge two maps (with m2 overriding m1) */
	private Map<String, String> merge(Map<String, String> m1, Map<String, String> m2) {
		Map<String, String> m = new LinkedHashMap<String, String>(m1);
		if (m2 != null) {
			for (Map.Entry<String, String> entry : m2.entrySet()) {
				m.put(entry.getKey(), entry.getValue());
			}
		}
		return Collections.unmodifiableMap(m);
	}

	/**
	 * Get the complete script hierarchy by recursing down the scope stack, and return it as a
	 * delimited string (delimiter = <code>" > "</code>).
	 * 
	 * @return the script stack as a delimited string
	 */
	public String getScopeHierarchy() {
		return getScopeHierarchy(this, " > ", false);
	}

	/**
	 * Get the complete script hierarchy by recursing down the given scope's scope stack, and return
	 * it as a delimited string.
	 * 
	 * @param scope
	 *            the scope
	 * @param delim
	 *            the script filename delimiter
	 * @return the script stack as a delimited string
	 */
	public String getScopeHierarchy(Scope scope, String delim) {
		return getScopeHierarchy(scope, delim, false);
	}

	/**
	 * Get the complete script hierarchy by recursing down the scope stack, and return it as a
	 * delimited string.
	 * 
	 * @param delim
	 *            the script filename delimiter
	 * @param withIndex
	 *            if true return filename plus index, otherwise just the filename
	 * @return the script stack, and index, as a delimited string
	 */
	public String getScopeHierarchy(String delim, boolean withIndex) {
		return getScopeHierarchy(this, delim, withIndex);
	}

	/**
	 * Get the complete script hierarchy by recursing down the given scope's scope stack, and return
	 * it as a delimited string.
	 * 
	 * @param scope
	 *            the scope
	 * @param delim
	 *            the script filename delimiter
	 * @param withIndex
	 *            if true return filename plus index, otherwise just the filename
	 * @return the script stack, and index, as a delimited string
	 */
	public String getScopeHierarchy(Scope scope, String delim, boolean withIndex) {
		if (scope == null) {
			return "";
		} else {
			return (scope.getParentScope() != null ? getScopeHierarchy(scope.getParentScope(),
					delim, withIndex) + delim : "")
					+ (scope.getFilename() == null ? "<commands>" : scope.getFilename())
					+ (withIndex ? ":" + scope.getCurrentIndex() : "");
		}
	}

	public String getScopeTrace() {
		String file = (filename == null ? "<commands>" : filename);
		String cmd = (currentCommand == null ? "<unknown command>" : currentCommand.toString());

		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : getVariables().entrySet()) {
			sb.append(entry.getKey()).append('=').append(entry.getValue()).append(" ");
		}

		String vars = (sb.length() != 0 ? " [" + sb.substring(0, sb.length() - 1) + "]" : "");

		return "  at " + cmd + vars + " (" + file + " : cmd #" + currentIndex + ")"
				+ (parentScope != null ? "\n" + parentScope.getScopeTrace() : "");
	}

	@Override
	public Scope clone() {
		List<String> argsClone = new ArrayList<String>();
		for (String arg : getArgs()) {
			argsClone.add(arg);
		}

		Map<String, String> variablesClone = new LinkedHashMap<String, String>();
		for (Map.Entry<String, String> var : getVariables().entrySet()) {
			variablesClone.put(var.getKey(), var.getValue());
		}

		Scope parentClone = (parentScope == null ? null : parentScope.clone());

		return new Scope(filename, parentClone, componentType, monkeyId, action, argsClone,
				variablesClone);
	}

	@Override
	public String toString() {
		return "Scope: hierarchy=" + getScopeHierarchy() + " curr=" + currentIndex + ":"
				+ currentCommand;
	}
}