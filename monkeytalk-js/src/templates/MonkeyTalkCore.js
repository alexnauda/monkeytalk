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

importClass(com.gorillalogic.monkeytalk.Command);
importClass(com.gorillalogic.monkeytalk.sender.CommandSender);
importClass(com.gorillalogic.monkeytalk.processor.PlaybackStatus);
importClass(com.gorillalogic.monkeytalk.processor.Scope);
importClass(com.gorillalogic.monkeytalk.processor.Step);

var MT = {};

MT.isNumber = function(v) {
	return !isNaN(v);
};

MT.isString = function(v) {
	var clazz = Object.prototype.toString.call(v).slice(8, -1);
	return v !== undefined && v !== null && clazz === 'String';
};

MT.asString = function(v) {
	return "" + v;
};

MT.getModifiers = function(args) {
	var lastArg = args[args.length - 1];
	var modifiers = new java.util.HashMap();
	if (!MT.isNumber(lastArg) && !MT.isString(lastArg)) {
		for (prop in lastArg) {
			modifiers.put(prop, MT.asString(lastArg[prop]));
		}
	}
	return modifiers;
};

MT.getArgList = function(args) {
	var argList = new java.util.ArrayList();
	for ( var i = 0; i < args.length; i++) {
		if (MT.isNumber(args[i]) || MT.isString(args[i])) {
			argList.add(MT.asString(args[i]));
		}
	}
	return argList;
};

/**
 * The base class for all MonkeyTalk objects
 * 
 * @class
 */
MT.MTObject = function(app, monkeyId) {
	this.monkeyId = monkeyId;
	this.app = app;
	this.componentType = "MTObject";
};

/**
 * An application at the specified host and port.
 * 
 * @class
 * @extends MT.MTObject
 * @param host
 *            the remote hostname or ip address where the application is
 *            running. Defaults to localhost.
 * @param port
 *            the port it's listening on. Defaults to 16862 (Android playback).
 * @param scriptDir
 *            the local directory in which to look for scripts
 */
MT.Application = function(host, port, scriptDir) {
	var h = host || "localhost";
	var p = port ? port : 16862;
	var dir = scriptDir ? scriptDir : ".";
	this.host = h;
	this.port = p;
	this.scriptDir = dir;
};

/**
 * Called by generated wrapper api's
 * 
 * @ignore
 */
MT.Application.prototype.play = function(mtobject, action, args, mods) {
	var cmd = new Command(mtobject.componentType, mtobject.monkeyId, action, args, mods);
	ScopeObj.setCurrentCommand(cmd);
	
	var step = new Step(cmd, ScopeObj, 0);
	StepsObj.add(step);
	var result = ProcessorObj.runScript(cmd, ScopeObj);
	step.setResult(result);

	if (result == null) {
		return result;
	} else if (result.getStatus() == PlaybackStatus.OK) {
		return "" + result.getMessage();
	} else {
		throw "" + result.getMessage();
	}
};

/**
 * A MonkeyTalk script. The monkeyID is the name of the script. If no extension
 * is specified, then the script runner will first search for a .js file, and if
 * none is found, the runner will search for an .mt file.
 * 
 * @param app
 * @param monkeyId
 * @returns {MT.Script}
 */
MT.Script = function(app, monkeyId) {
	MT.MTObject.call(this, app, monkeyId);
	this.componentType = "Script";
};

MT.Script.prototype = new MT.MTObject;

/**
 * Run the script with zero-or-more arguments
 * 
 * @returns the script results
 */
MT.Script.prototype.run = function() {
	var argArray = [].slice.apply(arguments);
	var mods = MT.getModifiers(argArray);

	if (mods.length > 0) {
		argArray.pop();
	}
	var argList = MT.getArgList(argArray);
	
	var cmd = new Command('Script', this.monkeyId, 'Run', argList, mods);
	ScopeObj.setCurrentCommand(cmd);

	var step = new Step(cmd, ScopeObj, 0);
	StepsObj.add(step);
	var result = ProcessorObj.runScript(cmd, ScopeObj);
	step.setResult(result);

	if (result == null) {
		return result;
	} else if (result.getStatus() == PlaybackStatus.OK) {
		return "" + result.getMessage();
	} else {
		throw "" + result.getMessage();
	}
};

/**
 * Run the script with the supplied csv file
 * 
 * @returns the script results
 */
MT.Script.prototype.runWith = function() {
	var argArray = [].slice.apply(arguments);
	var mods = MT.getModifiers(argArray);

	if (mods.length > 0) {
		argArray.pop();
	}
	var argList = MT.getArgList(argArray);

	var cmd = new Command('Script', this.monkeyId, 'RunWith', argList, mods);
	ScopeObj.setCurrentCommand(cmd);

	var step = new Step(cmd, ScopeObj, 0);
	StepsObj.add(step);
	var result = ProcessorObj.runScript(cmd, ScopeObj);
	step.setResult(result);

	if (result == null) {
		return result;
	} else if (result.getStatus() == PlaybackStatus.OK) {
		return "" + result.getMessage();
	} else {
		throw "" + result.getMessage();
	}
};

/**
 * Conditionally run the script if the given verify succeeds
 * 
 * @returns the script results
 */
MT.Script.prototype.runIf = function() {
	var argArray = [].slice.apply(arguments);
	var mods = MT.getModifiers(argArray);
	
	if (mods.length > 0) {
		argArray.pop();
	}
	var argList = MT.getArgList(argArray);
	
	var cmd = new Command('Script', this.monkeyId, 'RunIf', argList, mods);
	ScopeObj.setCurrentCommand(cmd);
	
	var step = new Step(cmd, ScopeObj, 0);
	StepsObj.add(step);
	var result = ProcessorObj.runScript(cmd, ScopeObj);
	step.setResult(result);
	
	if (result == null) {
		return result;
	} else if (result.getStatus() == PlaybackStatus.OK) {
		return "" + result.getMessage();
	} else {
		throw "" + result.getMessage();
	}
};

MT.Application.prototype.script = function(monkeyId) {
	return new MT.Script(this, monkeyId);
};

/**
 * A custom component type.
 * 
 * @extends MT.Script
 * @param app
 * @param monkeyId
 *            the monkeyId of this custom type instance
 * @param componentType
 *            the component type's name
 * @returns {MT.CustomType}
 */
MT.CustomType = function(app, monkeyId, componentType) {
	MT.Script.call(this, app, monkeyId);
	this.componentType = componentType;
};

MT.CustomType.prototype = new MT.Script;

/**
 * Run the given custom command as: Comp * Action
 */
MT.CustomType.prototype.run = function() {
	var argArray = [].slice.apply(arguments);
	var mods = MT.getModifiers(argArray);

	if (mods.length > 0) {
		argArray.pop();
	}
	var comp = argArray.shift();
	var act = argArray.shift();
	var argList = MT.getArgList(argArray);
	
	var cmd = new Command(comp, '*', act, argList, mods);
	ScopeObj.setCurrentCommand(cmd);

	var step = new Step(cmd, ScopeObj, 0);
	StepsObj.add(step);
	var result = ProcessorObj.runScript(cmd, ScopeObj);
	step.setResult(result);

	if (result == null) {
		return result;
	} else if (result.getStatus() == PlaybackStatus.OK) {
		return "" + result.getMessage();
	} else {
		throw "" + result.getMessage();
	}
};

/**
 * A custom typed for the named type
 * 
 * @param monkeyId
 * @param componentType
 * @returns {MT.CustomCommand}
 */
MT.Application.customType = function(monkeyId, componentType) {
	return new MT.CustomType(app, monkeyId, componentType);
};
