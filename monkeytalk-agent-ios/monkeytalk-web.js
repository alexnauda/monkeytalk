if (MonkeyTalk == undefined || adapters == undefined) {
	var MonkeyTalk = {};
	
	// ------------------------------------------------------ 
    // PLAYBACK 
    // ------------------------------------------------------ 
    MonkeyTalk.keys = ["monkeyId","id","name","value","title","className","textContent"];
    // MonkeyTalk.keys = ["monkeyId","id","name","value","title","className","alt","textContent","src","href"]; 
    
    MonkeyTalk.isIos = function() {
        var userAgent = window.navigator.userAgent;
        
        if (userAgent.match(/iPad/i) || userAgent.match(/iPhone/i)) {
            return true;
        }
        
        return false;
    };
    
    MonkeyTalk.monkeyIdMatchesElement = function(monkeyId, monkeyElement) {
    	var keys = MonkeyTalk.keys;
    	
    	for (var i = 0; i < keys.length; i++) {
    		var key = keys[i];
    		
    		if (monkeyId == monkeyElement[key])
    			return true;
    	}
    	
    	return false;
    };
    
	MonkeyTalk.getElement = function(elements, monkeyId) {
		var monkeyElements = new Array();
    	
    	for (var i = 0; i < elements.length; i++) {
    		var element = elements[i];
    		var monkeyElement = MonkeyTalk.monkeyElement(element);
    		
    		monkeyElements[i] = monkeyElement;
    		
    		console.log('find: ' + monkeyId + ' current: ' + monkeyElement["monkeyId"]);
    		
    		if (MonkeyTalk.monkeyIdMatchesElement(monkeyId, monkeyElement)) {
    			console.log('json: ' + JSON.stringify(monkeyElement));
    			return JSON.stringify(monkeyElement);
    		}
    	}
    	
    	return null;
	};
	
	MonkeyTalk.getHtmlElement = function(elements, monkeyId) {
		var monkeyElements = new Array();
    	
    	for (var i = 0; i < elements.length; i++) {
    		var element = elements[i];
    		var monkeyElement = MonkeyTalk.monkeyElement(element);
    		
    		monkeyElements[i] = monkeyElement;
    		
    		if (MonkeyTalk.monkeyIdMatchesElement(monkeyId, monkeyElement)) {
    			return element;
    		}
    	}
    	
    	return null;
	};
	
	MonkeyTalk.getCell = function(tableElements, tableId, cellId) {
		var table = MonkeyTalk.getHtmlElement(tableElements, tableId);
		var rows = table.getElementsByTagName('tr');
		
		for (var i = 0; i < rows.length; i++) {
			var element = rows[i];
			var monkeyElement = MonkeyTalk.monkeyElement(element);
			
			if (MonkeyTalk.monkeyIdMatchesElement(cellId, monkeyElement)) {
    			console.log('json: ' + JSON.stringify(monkeyElement));
    			return JSON.stringify(monkeyElement);
    		}
		}
		
		return null;
	};
	
	MonkeyTalk.getRadioButton = function(radiobuttons, value) {
		console.log('radio count: ' + radiobuttons.length);
		console.log('value: ' + value);
		for (var i = 0; i < radiobuttons.length; i++) {
			var element = radiobuttons[i];
			var monkeyElement = MonkeyTalk.monkeyElement(element);
			
			if (value == element.value) {
    			console.log('json: ' + JSON.stringify(monkeyElement));
    			return JSON.stringify(monkeyElement);
    		}
		}
		
		return null;
	};
	
	MonkeyTalk.getNthElement = function(elements, n, componentType) {
		var limitedElements = new Array();
		var offset = n - 1;
		
		for (var i = 0; i < elements.length; i++) {
			var e = elements[i];
			if (e.type != 'hidden')
				limitedElements[limitedElements.length] = e;
		}
		
		if (offset < 0)
			offset = 0;
        
		if (limitedElements.length > offset) {
			var element = limitedElements[offset];
			
			var monkeyElement = MonkeyTalk.monkeyElement(element);
			
			console.log('type: ' + monkeyElement["component"]);
			
			if (componentType == monkeyElement["component"]) {
				var json = JSON.stringify(monkeyElement);
				return json;
			}
		}
        
		return null;
	};
	
	MonkeyTalk.monkeyElement = function(element) {
		var monkeyElement = {};
    	var adapter = adapters.getAdapterForElement(element);
    	var mtc = adapter.getMTCommand(element, "tap");
    	//console.log('element: ' + mtc.monkeyId); 
    	monkeyElement["monkeyId"] = mtc.monkeyId;
    	monkeyElement["component"] = mtc.componentType;
    	
    	var rect = element.getBoundingClientRect();
    	var x = rect.left + element.clientWidth/2;
    	var y = rect.top + element.clientHeight/2;
    	
    	monkeyElement["id"] = element.id != undefined ? element.id : null;
    	monkeyElement["name"] = element.name != undefined ? element.name : null;
    	monkeyElement["tagName"] = element.tagName != undefined ? element.tagName : null;
    	monkeyElement["className"] = element.className != undefined ? element.className : null;
    	monkeyElement["value"] = element.value != undefined ? element.value : null;
    	monkeyElement["textContent"] = element.textContent != undefined ? element.textContent : null;
    	monkeyElement["type"] = element.type != undefined ? element.type : null;
    	monkeyElement["x"] = parseInt(x);
    	monkeyElement["y"] = parseInt(y);
    	monkeyElement["width"] = parseInt(element.clientWidth);
    	monkeyElement["height"] = parseInt(element.clientHeight);
    	monkeyElement["title"] = element.title != undefined ? element.title : null;
        
    	return monkeyElement;
	};
	
    // ------------------------------------------------------
	// COMPONENT TREE
	// ------------------------------------------------------
    
	MonkeyTalk.getComponentTreeJson = function() {
	//console.log("fetching component tree");
    	// wgxpath.install();
    	var xpath = document.evaluate( "/", document, null, XPathResult.ANY_TYPE, null );
		if (xpath != undefined && xpath != null) {
			var rootNode = xpath.iterateNext();
			if (rootNode != null && rootNode != undefined) {
                var componentTreeObj= {};
                componentTreeObj["ComponentType"]="HTMLDocument";
                componentTreeObj["monkeyId"]="";
                componentTreeObj["className"]="HTMLDocument";
                // gets a JavaScript array object
                componentTreeObj["children"] = MonkeyTalk.getComponentTreeJson2(rootNode);
				return JSON.stringify(componentTreeObj);
			}
		}
		return null;
	}
	
	MonkeyTalk.getComponentTreeJson2 = function(element) {
		// console.log("getComponentTreeJson2(element) ==> element=" + element + " with tagName=" + element.tagName);
		var nodeItems = new Array();
		if (element != null && element != undefined) {
			var childNodes = element.childNodes; // node list
			if (childNodes != undefined && childNodes != null) {
				for (var i=0; i<childNodes.length; i++) {
					var node = childNodes.item(i);
					if (node != undefined && node != null) {
						var nodeType = node.nodeType;
						if (nodeType==1) {
							// it's an element
							if (node == element) {
								// in case of self-child links
								continue;
							}
							if (node.tagName.toLowerCase() != "script" && node.tagName.toLowerCase() != "head") {
								// ignore script elements
								nodeItems[nodeItems.length]=MonkeyTalk.getComponentTreeEntry(node);
							}
						}
					}
				}
			}
		}
		return nodeItems;
	}
    
	MonkeyTalk.getComponentTreeEntry = function(element) {
		var comp = new Object();
		comp["ComponentType"] = "View";
        
		var identifyingValues = MonkeyTalk.getIdentifyingValues(element);
		var tagAdapter = adapters.getAdapterForElement(element);
		if (tagAdapter != undefined && tagAdapter != null) {
			var cmid = tagAdapter.mid(element,false);
			if (cmid != undefined && cmid!=null) {
				if (cmid.length > 25) {
					cmid = cmid.substring(0,25) + "...";
				}
			}
			comp["monkeyId"] = cmid ? cmid : tagAdapter.ordinal(element);
			comp["ComponentType"] = tagAdapter.componentType ? tagAdapter.componentType : "View";
			comp["ordinal"] = tagAdapter.ordinal(element);
		}
		
		if (identifyingValues != undefined && identifyingValues != null && identifyingValues.length>0) {
			comp["identifiers"] = identifyingValues;
			if (comp.monkeyId == undefined || comp.monkeyId == null) {
				comp["monkeyId"] = identifyingValues[0];
			}
		}
		
		comp["className"] = element.tagName;
		// comp["visible"] = element.isVisible;
		comp["children"] = MonkeyTalk.getComponentTreeJson2(element);
		
		return comp;
	}
	
	MonkeyTalk.getIdentifyingValues = function(element) {
		var keys = MonkeyTalk.keys;
		var identifyingValues = new Array();
		for ( var i = 0; i < keys.length; i++) {
			var key = keys[i];
			if (key == "textContent") {
				continue;
			}
			if (element.hasOwnProperty(key)) {
				var val = element[key];
				if (val != undefined && val != null && val.length>0) {
					identifyingValues[identifyingValues.length] = val;
				}
			}
		}
		return identifyingValues;
	}
	
    // ------------------------------------------------------
    // RECORDER 
    // ------------------------------------------------------ 
	
	MonkeyTalk.record = function (event, action) {
		console.log('MonkeyTalk Recording' + action);
        
        element = adapters.recordElement(event);
        
        var adapter = adapters.getAdapter(event);
        var mtc = adapter.getMTCommand(element, action);
        var mtaction = action;
        
        if (mtc.shouldIgnore == true) return;
        
        if (mtaction == null || mtaction == 'change' || mtc.action != null)
            mtaction = mtc.action;
        
        console.log('Element TagName:' + element.tagName);
        
        var json = {};
        json["component"] = mtc.componentType;
        json["monkeyId"] = mtc.monkeyId;
        json["action"] = mtaction;
        json["args"] = mtc.args;
        
        var jsonString = JSON.stringify(json);
        
        if (MonkeyTalk.isIos() == true) {
            // need to make this json 
            xPathResult = 'ComponentType*' + mtc.componentType + ';MonkeyId*' + mtc.monkeyId + ';Action*' + mtaction + ';Args*' + mtc.args;
            sendToObjCLib(element.tagName, xPathResult);
		} else {
			console.log('mtrecorder:' + jsonString);
            //window.mtrecorder.recordJson(jsonString); 
        }
	};
	
	MonkeyTalk.recordTap = function (x, y) {
		var action = 'Tap';
        console.log('MonkeyTalk Recording Tap');
        
        element = adapters.recordFromElement(document.elementFromPoint(x, y));
        
        if (element.tagName.toLowerCase() == 'iframe')
            element = adapters.elementInIframeAtPoint(element, x, y);
        
        var adapter = adapters.getAdapterForElement(element);
        var mtc = adapter.getMTCommand(element, action);
        var mtaction = action;
        
        if (mtc.shouldIgnore == true) return;
        
        if (mtaction == null || mtaction == 'change' || mtc.action != null)
            mtaction = mtc.action;
        
        console.log('Element TagName:' + element.tagName);
        
        if (mtc.componentType == undefined)
            mtc.componentType = 'View';
        
        var json = {};
        json["component"] = mtc.componentType;
        json["monkeyId"] = mtc.monkeyId;
        json["action"] = mtaction;
        json["args"] = mtc.args;
        
        var jsonString = JSON.stringify(json);
    	
        if (MonkeyTalk.isIos() == true) {
            return jsonString;
		} else {
            //window.mtrecorder.recordJson(jsonString); 
            console.log('mtrecorder:' + jsonString);
        }
    	
    	console.log('component: ' + mtc.componentType + ' monkeyId: ' + mtc.monkeyId + ' action: ' + mtaction + ' args: ' + mtc.args);
        //console.log ('{' + ''component':' + ''' + mtc.componentType + ''' + ',' + ''monkeyId':' + ''' + mtc.monkeyId + ''' + ',' + ''action':' + ''' + mtaction + ''' + ',' + ''args':' + ''' + mtc.args + ''' + '}'); 
	};
    
	// ------------------------------------------------------ 
    // SEND TO OBJC 
    // ------------------------------------------------------ 
    sendToObjCLib = function (key,val) {
        var iframe = document.createElement('IFRAME');
        iframe.setAttribute('src', key + ':monkeytalk' + val);
        document.documentElement.appendChild(iframe);
        iframe.parentNode.removeChild(iframe);
        iframe = null;
    };
    
    function MonkeyElement() {};
    var monkey = new MonkeyElement();
    
    // ------------------------------------------------------ 
    // MT COMMAND
    // ------------------------------------------------------ 
    var mt = {};
    mt.command = function(componentType, monkeyId, action, args) {
        this.componentType = componentType;
        this.monkeyId = monkeyId;
        this.action = action;
        this.args = args;
        this.shouldIgnore = false;
    };
    
    // ------------------------------------------------------ 
    // ADAPTERS 
    // ------------------------------------------------------ 
    var adapters = {};
    
    // get the adapter for each element 
    adapters.getAdapter = function(event) {
        element = adapters.recordElement(event);
        var tagName = element.tagName.toLowerCase();
        //console.log('element tag: ' + tagName); 
        if (adapters[tagName]) {
            return new adapters[tagName](element);
        }
        
        return new adapters.tag(element);
    };
    
    // get the adapter for specific element 
    adapters.getAdapterForElement = function(element) {
        var tagName = element.tagName.toLowerCase();
        //console.log('element tag: ' + tagName); 
        if (adapters[tagName]) {
            return new adapters[tagName](element);
        }
        
        return new adapters.tag(element);
    };
    
    // get the element we want to record 
    adapters.recordElement = function(event){
        element = event.target || event.currentTarget || event.srcElement;
        
        return adapters.recordFromElement(element);
    };
    
    // get the element we want to record 
    adapters.recordFromElement = function(element){
        if (element.parentNode.tagName == undefined)
            return element;
        
        if (element.parentNode.tagName.toString().toLowerCase() == 'button') {
            console.log('Using Parent Tag');
            return element.parentNode;
        }
        
        return element;
    };
    
    adapters.elementInIframeAtPoint = function(frame, x, y){
        var rect = frame.getBoundingClientRect();
        x -= rect.left;
        y -= rect.top;
        var doc = (frame.contentWindow || frame.contentDocument);
        if (doc.document)
            doc = doc.document;
        
        var element = adapters.recordFromElement(doc.elementFromPoint(x, y));
        
        return element;
    };
    
    // ------------------------------------------------------ 
    // DEFAULT ADAPTER 
    // ------------------------------------------------------ 
    adapters.tag = function(element) {
        if (!element) {
            return;
        }
        
        this.element = element;
        this.componentType = this.componentNames[element.tagName.toLowerCase()];
        this.monkeyId = this.mid(this.element, true);
    };
    
    // monkeyID 
    adapters.tag.prototype.mid = function(e, findOrdinal) {
        var monkeyId;
        
        // do not record value as monkeyId for 
        // ItemSelector, CheckBox or RadioButtons 
        if (this.componentType != undefined &&
            this.componentType.toLowerCase() == 'table') {
            monkeyId = e.id || e.name || e.value
            || e.title || e.styleClass;
        }
        if (e.type == undefined) {
            monkeyId = e.getAttribute('id') || e.getAttribute('name') || e.getAttribute('value') || e.getAttribute('title') || e.getAttribute('class') || e.getAttribute('alt') || e.textContent || e.getAttribute('src') || e.getAttribute('href');
        } else if (e.type.toLowerCase() == 'radio') {
            monkeyId = e.name;
        } else if (e.tagName.toLowerCase() == 'select' || e.tagName.toLowerCase() == 'textarea' || e.tagName.toLowerCase() == 'input') {
            if (e.type.toLowerCase() != 'submit' || e.type.toLowerCase() != 'reset')
                monkeyId = e.id || e.name || e.title || e.getAttribute('class') || e.styleClass;
            else {
                monkeyId = e.id || e.name || e.value || e.textContent || e.title || e.getAttribute('class') || e.styleClass;
            }
        } else {
            monkeyId = e.getAttribute('id') || e.getAttribute('name') || e.getAttribute('value') || e.getAttribute('title') || e.getAttribute('class') || e.getAttribute('alt') || e.textContent || e.getAttribute('src') || e.getAttribute('href');
        }
        
        if (monkeyId == e.textContent) {
            var isTextContentLegal =  monkeyId.substring(0,1) == ' ';
            isTextContentLegal = isTextContentLegal || monkeyId.substring(monkeyId.length-1,monkeyId.length) == ' ';
            isTextContentLegal = isTextContentLegal || (this.element.textContent == monkeyId && this.element.innerHTML.indexOf('<') !== -1);
            
            //if (!isTextContentLegal) 
        }
        
        if (monkeyId == undefined)
            return null;
        
        // do not use ordinal mid for radiobuttons 
        if (findOrdinal && this.componentType != undefined && this.componentType.toLowerCase() != 'radiobuttons')
            monkeyId = this.ordinalMid(e, monkeyId);
        
        return monkeyId;
    };
    
    // get ordinal monkey ID 
    adapters.tag.prototype.ordinalMid = function(e, monkeyId) {
        // find all elements in the DOM with element e tag 
        var elements = document.getElementsByTagName(e.tagName);
        var ordinalMid = 0;
        
        for (var i = 0; i < elements.length; i++) {
            var element = elements[i];
            var elementMid = this.mid(element, false);
            
            if (monkeyId == elementMid) {
                //console.log('elementMid: ' + e.textContent + ' current: ' + element.textContent + ' ordinal: ' + i); 
                if (element == e) {
                    //console.log('found element'); 
                    i = ordinalMid;
                    break;
                } else
                    ordinalMid++;
            }
            
        }
        
        // increment to make 1 based 
        ordinalMid++;
        
        if (ordinalMid > 1)
            return monkeyId + '(' + ordinalMid + ')';
        
        return monkeyId;
    };
    
    adapters.tag.prototype.ordinal = function(e) {
        // Find all elements in the DOM 
        var elements = document.getElementsByTagName('*');
        var ordinal = 0;
        
        for (var i = 0; i < elements.length; i++) {
            var element = elements[i];
            var isElementRadio = element.type == 'radio';
            var isElementCheckBox = element.type == 'checkbox';
            var isInput = (e.tagName == 'input' && (!e.type || e.type == 'text'));
            
            var isTagMatch = (element.tagName.toLowerCase() == e.tagName.toLowerCase());
            var isTypeMatch = (element.type == e.type);
            
            if (element == e) {
                i = elements.length;
            } else if (isTagMatch && isTypeMatch) {
                // Increment input only if it is not a checkbox or radio 
                if (isInput && !isElementRadio && !isElementCheckBox) {
                    ordinal++;
                    //log('ordinal: ' + element.tagName + ' type: ' + element.type + ' ordinal: ' + ordinal + ' element: ' + e.type); 
                } else if (!isInput || isElementRadio || isElementCheckBox) {
                    ordinal++;
                }
            }
        }
        
        // Increment to make 1 based 
        ordinal++;
        
        if (ordinal == 1)
            return '*';
        
        return '#' + ordinal.toString();
    };
    
    adapters.tag.prototype.getMTCommand = function(element, action) {
        var componentType = this.componentNames[element.tagName.toLowerCase()];
        var monkeyId = this.mid(element, true);
        var shouldFindOrdinal =  !monkeyId;
        shouldFindOrdinal = shouldFindOrdinal || monkeyId.substring(0,1) == ' ';
        shouldFindOrdinal = shouldFindOrdinal || monkeyId.substring(monkeyId.length-1,monkeyId.length) == ' ';
        shouldFindOrdinal = shouldFindOrdinal || (element.textContent == monkeyId && element.innerHTML.indexOf('<') !== -1);
        
        if (shouldFindOrdinal)
            monkeyId = this.ordinal(this.element);
        
        var action = this.actionTypes[element.tagName.toString().toLowerCase()];
        var args = '';
        return new mt.command(componentType, monkeyId, action, args);
    };
    
    // ------------------------------------------------------ 
    // COMPONENT TYPES 
    // ------------------------------------------------------ 
    adapters.tag.prototype.componentNames = {
        a : 'Link',
        button : 'Button',
        select : 'ItemSelector',
        table : 'Table',
        td : 'Table',
        th : 'Table',
        textarea : 'TextArea',
        input : 'Input',
        span : 'View',
        div : 'View',
        img : 'Image',
        textbox : 'TextArea',
        toolbarbutton : 'Browser',
        checkbox : 'CheckBox',
        radio : 'RadioButtons',
        text : 'Input',
        h1 : 'Label',
        h2 : 'Label',
        h3 : 'Label',
        h4 : 'Label',
        h5 : 'Label',
        h6 : 'Label',
        p : 'Label'
    };
    
    // ------------------------------------------------------ 
    // ACTION TYPES 
    // ------------------------------------------------------ 
    adapters.tag.prototype.actionTypes = {
        a : 'tap',
        button : 'tap',
        span : 'tap',
        text : 'tap',
        label : 'tap',
    div: 'tap',
        img : 'tap',
        select : 'select',
        radio : 'select',
        h1 : 'tap',
        h2 : 'tap',
        h3 : 'tap',
        h4 : 'tap',
        h5 : 'tap',
        h6 : 'tap'
    };
    
    // ------------------------------------------------------ 
    // INPUT ADAPTER 
    // ------------------------------------------------------ 
    adapters.input = function(element) {
        adapters.tag.call(this, element);
        this.componentType = this.componentNames[this.element.type.toLowerCase()];
    };
    
    adapters.input.prototype = new adapters.tag;
    
    adapters.input.prototype.getMTCommand = function(element, action) {
        var mtcommand = adapters.tag.prototype.getMTCommand.call(this, element, action);
        if (element.type.toLowerCase() == 'radio') {
            if (action.toLowerCase() != 'change') {
                mtcommand.shouldIgnore = true;
                return mtcommand;
            }
            mtcommand.action = 'select';
            mtcommand.componentType = 'RadioButtons';
            mtcommand.monkeyId = adapters.tag.prototype.mid.call(this,element,true);
            var value = element.value;
            mtcommand.args = value;
        } else if (element.type.toLowerCase() == 'checkbox') {
            if (action.toLowerCase() != 'change') {
                mtcommand.shouldIgnore = true;
                return mtcommand;
            }
            
            var checkbox = this.element;
            if (checkbox.checked)
                mtcommand.action = 'on';
            else
                mtcommand.action = 'off';
            mtcommand.componentType = 'CheckBox'
        } else if (element.type.toLowerCase() == 'button') {
            mtcommand.componentType = 'Button';
            mtcommand.monkeyId = element.value;
        } else {
            if (action != null && action.toLowerCase() == 'change') {
                // should not ignore this 
                // need to fix keyup record to take multiple args 
                mtcommand.shouldIgnore = true;
                mtcommand.action = 'enterText';
                mtcommand.args = element.value + ',enter';
                return mtcommand;
            }
            console.log('value: ' + element.value);
            mtcommand.args = element.value;
        }
        return mtcommand;
    };
    
    // ------------------------------------------------------ 
    // SELECT ADAPTER 
    // ------------------------------------------------------ 
    adapters.select = function(element) {
        adapters.tag.call(this, element);
        this.componentType = this.componentNames[this.element.type.toLowerCase()];
    };
    
    adapters.select.prototype = new adapters.tag;
    
    adapters.select.prototype.getMTCommand = function(element, action) {
        var mtcommand = adapters.tag.prototype.getMTCommand.call(this, element, action);
        
        if (action.toLowerCase() != 'change') {
            mtcommand.shouldIgnore = true;
            return mtcommand;
        }
        
        mtcommand.args = element.value;
        
        return mtcommand;
    };
    
    // ------------------------------------------------------ 
    // TEXTAREA ADAPTER 
    // ------------------------------------------------------ 
    adapters.textarea = function(element) {
    	adapters.tag.call(this, element);
    	this.componentType = this.componentNames[this.element.type.toLowerCase()];
    };
    
    adapters.textarea.prototype = new adapters.tag;
    
    adapters.textarea.prototype.getMTCommand = function(element, action) {
        var mtcommand = adapters.tag.prototype.getMTCommand.call(this, element, action);
        
        if (action != null && action.toLowerCase() == 'change') {
            // should not ignore this 
            // need to fix keyup record to take multiple args 
            mtcommand.shouldIgnore = true;
            mtcommand.action = 'enterText';
            mtcommand.args = element.value + ',enter';
            return mtcommand;
        }
        
        mtcommand.args = element.value;
        
        return mtcommand;
    };
    
    // ------------------------------------------------------ 
    // TABLE ADAPTER 
    // ------------------------------------------------------ 
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
			console.log('tr not found');
			return mtcommand;
		}
		
		console.log('tr found: ' + tr.textContent);
        
		// Set monkeyId to that of table 
		// to avoid recording monkeyId of tr, td, etc. monkeyIds 
		mtcommand.monkeyId = adapters.tag.prototype.mid.call(this,table,true);
        
		if (!mtcommand.monkeyId)
			mtcommand.monkeyId = adapters.tag.prototype.ordinal.call(this,table);
        
		if (tr.textContent.length > 0) {
			console.log('set action select');
			mtcommand.action = 'select';
			mtcommand.args = tr.textContent;
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
    
	// ------------------------------------------------------ 
    // TR ADAPTER 
    // ------------------------------------------------------ 
	adapters.tr = function(element) {
		adapters.table.call(this, element);
	};
	
	adapters.tr.prototype = new adapters.table;
	
	// ------------------------------------------------------ 
    // TD ADAPTER 
    // ------------------------------------------------------ 
	adapters.td = function(element) {
		adapters.table.call(this, element);
	};
	
	adapters.td.prototype = new adapters.table;
	
	// ------------------------------------------------------ 
    // TH ADAPTER 
    // ------------------------------------------------------ 
	adapters.th = function(element) {
		adapters.table.call(this, element);
	};
	
	adapters.th.prototype = new adapters.table;
    
    // ------------------------------------------------------ 
    // SETUP DOCUMENT 
    // ------------------------------------------------------ 
    // add listeners for elements in iframes 
    var mtframes = document.getElementsByTagName('iframe');
    
    for (var i = 0; i < mtframes.length; i++) {
        var mtframe = mtframes[i];
        //console.log('element: ' + mtframe.tagName + ':' + mtframe.getAttribute('id')); 
        
        var mtdoc = (mtframe.contentWindow || mtframe.contentDocument);
        if (mtdoc.document)
            mtdoc = mtdoc.document;
        
        mtdoc.onkeyup = function (event) { MonkeyTalk.record(event, 'enterText')};
        mtdoc.onchange = function (event) {MonkeyTalk.record(event, 'change')};
        mtdoc.onclick = function (event) {MonkeyTalk.record(event, 'tap')};
    }
    
    // handle keyup and change events via js 
    // taps handled in objc from gestures 
    document.onkeyup = function (event) { MonkeyTalk.record(event, 'enterText')};
    document.onchange = function (event) {MonkeyTalk.record(event, 'change')};
    
    if (MonkeyTalk.isIos() != true) {
        document.onclick = function (event) {MonkeyTalk.record(event, 'tap')};
    }
    
}
