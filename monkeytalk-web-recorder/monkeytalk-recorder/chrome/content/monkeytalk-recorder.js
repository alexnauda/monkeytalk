var log = function(text) {
	Components.classes["@mozilla.org/consoleservice;1"].getService(
			Components.interfaces.nsIConsoleService).logStringMessage(
			"MonkeyTalk: " + text);
};

var logElement = function(element) {
	log("Start Log " + element);
	for (prop in element) {
		try {
			log(prop + ": " + element[prop]);
		} catch (e) {

		}
	}
	log("End Log " + element);
};

/**
 * Http Sender
 */
var http = new XMLHttpRequest();
var url = "http://localhost:16861/fonemonkey";
var json = "{mtversion:1.0, mtcommand:RECORD, timestamp:0, componentType:button, monkeyId:x, action:tap}";

http.onreadystatechange = function() {// Call a function when the state
	// changes.
	if (http.readyState == 4 && http.status != 200) {
		// alert("HttpRequest Errors Occured " + http.readyState
		// + " and the status is " + http.status);
	}
};


var mt = {};

/**
 * MT command object
 * @param componentType
 * @param monkeyId
 * @param action
 * @param args
 * @returns {mt.command}
 */
mt.command = function(componentType, monkeyId, action, args) {
	this.componentType = componentType;
	this.monkeyId = monkeyId;
	this.action = action;
	this.args = args;
};

/**
 * Adapters adapt Se commands for a tag type to corresonding MT
 */
var adapters = {};

adapters.getAdapter = function(element) {

	var tagName = element.tagName.toLowerCase();
	
	log("tagName: " + tagName);
	if (adapters[tagName]) {
		log("has class");
		return new adapters[tagName](element);
	}
	return new adapters.tag(element);

};
/**
 * The default adapter
 * 
 * @param element
 * @returns {adapters.tag}
 */
adapters.tag = function(element) {
	if (!element) {
		return;
	}
	logElement(this);
	this.element = element;
	this.componentType = this.typeNames[element.tagName.toLowerCase()];
	
	this.monkeyId = this.mid(this.element, true);
	
};


adapters.tag.prototype.getMTCommand = function(command, target, value) {
	var componentType = this.componentType || target.tagName;
	var monkeyId = this.monkeyId;
	
	if (!monkeyId)
		monkeyId = this.ordinal(this.element);
	
	if (command == "click") {
		// Hack to see if this is a tag that's inside a link, and to return the
		// parent link
		parent = this.element.parentNode;
		while (parent != null) {
			if (parent.tagName == "A") {
				adapter = adapters.getAdapter(parent);
				return adapter.getMTCommand(command, target, value);
				break;
			}
			parent = parent.parentNode;
		}
	}

	var action = this.actions[command] || command;
	var args = value;
	return new mt.command(componentType, monkeyId, action, args);
};

adapters.tag.prototype.typeNames = {
	a : "Link",
	button : "Button",
	select : "ItemSelector",
	table : "Table",
	td : "Table",
	th : "Table",
	div : "Label",
	textarea : "TextArea",
	input : "Input",
	span : "Link",
	img : "Image",
	textbox : "Browser",
	toolbarbutton : "Browser"
};

adapters.tag.prototype.actions = {
	type : "enterText",
	click : "click"
};

adapters.tag.prototype.mid = function(e, findOrdinal) {
	var monkeyId;

	// Do not record value as monkeyId for
	// ItemSelector, CheckBox or RadioButtons
	if (this.componentType != undefined && 
		this.componentType.toLowerCase() == "table") {
		monkeyId = e.id || e.name || e.value
			|| e.title || e.styleClass;
	} 
	if (this.action == "click" && e.type.toLowerCase() == "radio") {
		monkeyId = e.name;
	} else if (e.tagName.toLowerCase() == "select" || 
		e.tagName.toLowerCase() == "textarea" || 
		e.tagName.toLowerCase() == "input") {
		
		if (e.type.toLowerCase() != "submit" ||
			e.type.toLowerCase() != "reset")
			monkeyId = e.id || e.name
				|| e.title || e.styleClass;
	} else {
		monkeyId = e.id || e.name || e.value
			|| e.textContent || e.title || e.styleClass;
	}
	
	if (monkeyId == undefined)
		return null;
	
	if (findOrdinal)
		monkeyId = this.ordinalMid(e, monkeyId);
	
	return monkeyId;
}

adapters.tag.prototype.ordinalMid = function(e, monkeyId) {
	// Find all elements in the DOM with element e tag
	var elements = window.content.document.getElementsByTagName(e.tagName);
	var ordinalMid = 0;
	
	for (var i = 0; i < elements.length; i++) {
		var element = elements[i];
		log("elementMid: " + element);
		var elementMid = this.mid(element, false);
		
		if (monkeyId == elementMid) {
			if (element == e)
				i = elements.length;
			else
				ordinalMid++;
		}
	}
	
	// Increment to make 1 based
	ordinalMid++;
	
	if (ordinalMid > 1)
		return monkeyId + "(" + ordinalMid + ")";
	
	return monkeyId;
}

adapters.tag.prototype.ordinal = function(e) {
	// Find all elements in the DOM
	var elements = window.content.document.getElementsByTagName("*");
	var ordinal = 0;
	
	for (var i = 0; i < elements.length; i++) {
		var element = elements[i];
		var isElementRadio = element.type == "radio";
		var isElementCheckBox = element.type == "checkbox";
		var isInput = (e.tagName == "input" && (!e.type || e.type == "text"));
		
		var isTagMatch = (element.tagName.toLowerCase() == e.tagName.toLowerCase());
		var isTypeMatch = (element.type == e.type);
		
		if (element == e) {
			i = elements.length;
		} else if (isTagMatch && isTypeMatch) {
			// Increment input only if it is not a checkbox or radio
			if (isInput && !isElementRadio && !isElementCheckBox) {
				ordinal++;
				log("ordinal: " + element.tagName + " type: " + element.type + " ordinal: " + ordinal + " element: " + e.type);
			} else if (!isInput || isElementRadio || isElementCheckBox) {
				ordinal++;
			}
		}
	}
	
	// Increment to make 1 based
	ordinal++;
	
	if (ordinal == 1)
		return "*";

	return "#" + ordinal.toString();
};


/**
 * Input adapter
 * @param element
 * @returns {adapters.input}
 */
adapters.input = function(element) {
	adapters.tag.call(this, element);
	this.componentType = this.typeNames[this.element.type.toLowerCase()];
};

adapters.input.prototype = new adapters.tag;


adapters.input.prototype.getMTCommand = function(command, target, value) {
	
	var mtcommand = adapters.tag.prototype.getMTCommand.call(this, command, target, value);
	if (command == "click" && target.type.toLowerCase() == "radio") {
		mtcommand.action = "select";
		mtcommand.componentType = "RadioButtons";
		mtcommand.monkeyId = adapters.tag.prototype.mid.call(this,target,true);
		//var value = target.nextSibling == "[object Text]" ? target.nextSibling.data.replace(/\s/g," ") : target.value;
		var value = target.value;
		mtcommand.args = [value];

	} else if (command == "click" && target.type.toLowerCase() == "checkbox") {
		var checkbox = this.element;
	
		if (checkbox.checked)
			mtcommand.action = 'on';
		else
			mtcommand.action = "off";
	}
	return mtcommand;
};



adapters.input.prototype.typeNames = {
	text : "Input",
	radio : "RadioButtons",
	checkbox : "CheckBox",
	submit : "Button",
	password : "Input",
	reset : "Button",
	img : "Image",
	button : "Button",
	file : "Input"
};

/**
 * Select adapter
 * @param element
 * @returns {adapters.select}
 */
adapters.select = function(element) {
	adapters.tag.call(this, element);
};

adapters.select.prototype = new adapters.tag;

adapters.select.prototype.getMTCommand = function(command, target, value) {

	var mtcommand = adapters.tag.prototype.getMTCommand.call(this, command, target, value);
	if (command == "select") {
		// Record text instead of value
		mtcommand.args = [target.options[target.selectedIndex].text];
	}
	
	if (target.multiple) {
		var args = new Array();
		var argCount = 0;
		mtcommand.args = "";
		mtcommand.action = "select";
		
		for (var i = 0; i < target.size; i++) {
			var option = target.options[i];
			
			if (option.selected) {
				log("selected: " + option.text);
				
				args[argCount] = option.text;
				argCount++;
			}
		}
		
		mtcommand.args = args.join("','");
	}
	
	if (mtcommand.args.length == 0)
			mtcommand.action = "clear";
	
	return mtcommand;
};

/**
 * BROWSER adapter
 * @param element
 * @returns {adapters.textbox} || {adapters.toolbarbutton}
 */
adapters.textbox = function(element) {
	adapters.tag.call(this, element);
};

adapters.textbox.prototype = new adapters.tag;

adapters.textbox.prototype.getMTCommand = function(command, target, value) {
	var mtcommand = adapters.tag.prototype.getMTCommand.call(this, command, target, value);
	
	mtcommand.monkeyId = "*";
	
	return mtcommand;
};

adapters.toolbarbutton = function(element) {
	adapters.tag.call(this, element);
};

adapters.toolbarbutton.prototype = new adapters.tag;

adapters.toolbarbutton.prototype.getMTCommand = function(command, target, value) {
	var mtcommand = adapters.tag.prototype.getMTCommand.call(this, command, target, value);
	
	mtcommand.monkeyId = "*";
	
	if (target.id == "back-button")
		mtcommand.action = "back";
	else
		mtcommand.action = "forward";
	
	return mtcommand;
};

/**
 * TABLE adapter
 * @param element
 * @returns {adapters.table}
 */
adapters.table = function(element) {
	adapters.tag.call(this, element);
};

adapters.table.prototype = new adapters.tag;

adapters.table.prototype.getMTCommand = function(command, target, value) {
	var mtcommand = adapters.tag.prototype.getMTCommand.call(this, command, target, value);
	var child = this.element;
	var table = child;
	var tr = null;
	var cell = null;
	
	while (child.tagName.toLowerCase() != "table") {
		if (child.tagName.toLowerCase() == "tr")
			tr = child;
		else if (child.tagName.toLowerCase() == "td" ||
					child.tagName.toLowerCase() == "th")
			cell = child;
	
		child = child.parentNode;
		table = child;
	}
	
	if (!tr) {
		return mtcommand;
	}

	// Set monkeyId to that of table 
	// to avoid recording monkeyId of tr, td, etc. monkeyIds
	mtcommand.monkeyId = adapters.tag.prototype.mid.call(this,table,true);
	
	if (!mtcommand.monkeyId)
		mtcommand.monkeyId = adapters.tag.prototype.ordinal.call(this,table);
	
	if (this.element.textContent.length > 0) {
		mtcommand.action = 'select';
		mtcommand.args = this.element.textContent;
	} else {
		var row = tr.rowIndex + 1;
		mtcommand.action = 'selectIndex';
		
		if (cell) {
			var column = Array.prototype.indexOf.call(tr.childNodes, cell);
			
			if (column == 0)
				column++;
			
			mtcommand.args = row + "','" + column;
		} else {
			mtcommand.args = row;
		}
	}
	
	return mtcommand;
};

/**
 * TR adapter
 * @param element
 * @returns {adapters.tr}
 */
adapters.tr = function(element) {
	adapters.table.call(this, element);
};

adapters.tr.prototype = new adapters.table;

/**
 * TD adapter
 * @param element
 * @returns {adapters.td}
 */
adapters.td = function(element) {
	adapters.table.call(this, element);
};

adapters.td.prototype = new adapters.table;

/**
 * TH adapter
 * @param element
 * @returns {adapters.th}
 */
adapters.th = function(element) {
	adapters.table.call(this, element);
};

adapters.th.prototype = new adapters.table;

/**
 * The recorder. Registered as a listener on Se-recorder.
 * @returns
 */
function MonkeyTalkRecorder() {
	log("initializing");
	this.recordingEnabled = true;

	// var wm = Components.classes["@mozilla.org/appshell/window-mediator;1"]
	// .getService(Components.interfaces.nsIWindowMediator);
	// var browserWindow = wm.getMostRecentWindow("navigator:browser");

};

/**
 * Record Selenium command as corresponding MT command. Se-recorder calls this listener method.
 * @param command
 * @param target
 * @param value
 * @param win
 * @param insertBeforeLastCommand
 */
MonkeyTalkRecorder.prototype.addCommand = function(command, target, value, win,
		insertBeforeLastCommand) {
	log(command + " " + target + " " + value);
	if (target.ownerDocument == "[object XULDocument]" && 
		!((target.id == "urlbar" && command == "open") || 
			target.id == "back-button" || target.id == "forward-button")) {
		// In firefox's "chrome" (eg, address bar), not in actual webpage
		return;
	}
	var adapter = adapters.getAdapter(target);
	var mtc = adapter.getMTCommand(command, target, value);
	var args = mtc.args ? "'" + mtc.args + "'" : "";
	
	// record enter when typing
	if (command == "type") {
		args = args + ",enter";
	}
	
	var msg = "{mtversion:1.0, mtcommand:RECORD, timestamp:0, componentType:"
			+ mtc.componentType + ", monkeyId:'" + mtc.monkeyId + "', action:'"
			+ mtc.action + "', args:[" + args + "]}";
	log(msg);
	try {
		http.open("POST", url, true);
		http.setRequestHeader("Content-length", msg.length);
		http.setRequestHeader("Content-type", "application/json");
		http.send(msg);

	} catch (e) {
		alert("unable to send recorded command: " + e);
	}

};
