function GetArgs() {}

sendToObjCLib = function (key,val)
{
	var iframe = document.createElement("IFRAME");
	iframe.setAttribute("src", key + ":monkeytalk" + val);
	document.documentElement.appendChild(iframe);
	iframe.parentNode.removeChild(iframe);
	iframe = null;
}


var tag;
var monkeyId;
var componentType;
var actionType;
var result;
var args = "";
var recordEvent = new GetArgs();

//We will pass the document mouseup event from here.
GetArgs.prototype.GetMonkeyEvent = function(event)
{
    //alert("File loaded and gettign event");
	getOrgEl = event.target || event.currentTarget || event.srcElement;

	if((!getOrgEl.id) && getOrgEl.tagName != "INPUT" && getOrgEl.tagName != "TEXTAREA" && getOrgEl.tagName != "SELECT" && getOrgEl.tagName != "OPTION")
	{
		console.log("Element TagName:" +getOrgEl.tagName);
		test = recordEvent.GetXPath(getOrgEl);
		monkeyXPathValue = "xpath="+test;
		args = "";
		actionType = actionTypes[getOrgEl.tagName.toString().toLowerCase()];
		componentType = componentName[getOrgEl.tagName.toString().toLowerCase()];
		xPathResult = "ComponentType*" + componentType + ";MonkeyId*" + monkeyXPathValue + ";Action*" + actionType + ";Args*" + args;
		alert(getOrgEl.tagName);
		sendToObjCLib(getOrgEl.tagName, xPathResult);
	}
	else
	{
		recordEvent.GetMonkeyId(getOrgEl);
	}
	
}

//We will traverse through the DOM to get the current element Tagname and it's relevant attributes
GetArgs.prototype.GetMonkeyId = function(getElement)
{
    args = " ";
    // alert("Inside monkey id");
	componentType = componentName[getElement.tagName.toString().toLowerCase()];
	actionType = actionTypes[getElement.tagName.toString().toLowerCase()];

	if(getElement.tagName == "INPUT")
	{
		recordEvent.GetInputTagType(getElement);
	}

	else if(getElement.tagName == "TEXTAREA")
	{
		//monkeyId = getElement.id || getElement.name || getElement.title || getElement.value;
		recordEvent.setMonkeyID(getElement);
		args = getElement.value;
		result = "ComponentType*" + componentType + ";MonkeyId*" + monkeyId + ";Action*" + actionType + ";Args*" + args;
		sendToObjCLib(getElement.tagName, result);
		getElement.onblur = function()
	    {
	        args = getElement.value;
            result = "ComponentType*" + componentType + ";MonkeyId*" + monkeyId + ";Action*EnterText" + ";Args*" + args;
	        sendToObjCLib(getElement.tagName, result);
	     }
	}

	else if(getElement.tagName == "BUTTON")
	{
		//monkeyId = getElement.id || getElement.name || getElement.title || getElement.value;
		recordEvent.setMonkeyID(getElement);
		result = "ComponentType*" + componentType + ";MonkeyId*" + monkeyId + ";Action*" + actionType + ";Args*" + args;
		sendToObjCLib(getElement.tagName, result);
	}

	else if(getElement.tagName == "A" || getElement.tagName == "SPAN")
	{
		if(recordEvent.GetParentElement(getElement))
		{
			//monkeyId = getElement.parentNode.getAttribute("href") || getElement.parentNode.name || getElement.parentNode.target || getElement.parentNode.innerHTML;
			recordEvent.setMonkeyID(getElement);
			console.log("Parent Node*" + monkeyId);
		}
		else
		{
			//monkeyId = getElement.getAttribute("href") ||  getElement.id || getElement.class || getElement.name || getElement.target || getElement.innerHTML;
			recordEvent.setMonkeyID(getElement);
			console.log("Inherit Node*" + monkeyId);
		}
		result = "ComponentType*" + componentType + ";MonkeyId*" + monkeyId + ";Action*" + actionType + ";Args*" + args;
		sendToObjCLib(getElement.tagName, result);
	}

	else if(getElement.tagName == "DIV")
	{
		console.log("Inside the DIV");
	}

	else if(getElement.tagName == "IMG" || getElement.tagName == "H1" || getElement.tagName == "H2" || getElement.tagName == "H3" || getElement.tagName == "H4" || getElement.tagName == "H5" || getElement.tagName == "H6")
	{
		if(recordEvent.GetParentElement(getElement))
		{
			//monkeyId = getElement.parentNode.href || getElement.parentNode.name || getElement.parentNode.target || getElement.parentNode.innerHTML;
			recordEvent.setMonkeyID(getElement);
			console.log("Parent Node*" + monkeyId);
		}
		else
		{
			//monkeyId = getElement.getAttribute("src") || getElement.alt || getElement.innerHTML;
			recordEvent.setMonkeyID(getElement);
			console.log("Inherit Node*" + monkeyId);
		}
		result = "ComponentType*" + componentType + ";MonkeyId*" + monkeyId + ";Action*" + actionType + ";Args*" + args;
		sendToObjCLib(getElement.tagName, result);
	}

	else if(getElement.tagName == "SELECT")
	{
		getElement.onchange = function()
		{
			args = getElement.options[getElement.selectedIndex].text;
			//monkeyId =  getElement.name || getElement.options || getElement.id;
			recordEvent.setMonkeyID(getElement);
			result = "ComponentType*" + componentType + ";MonkeyId*" + monkeyId + ";Action*" + actionType + ";Args*" + args;
			sendToObjCLib(getElement.tagName, result);
			console.log("Options result:" +result);
			args = "";
			console.log("Sending to OC+" +args);
			
		}
	}
	console.log(result);
	
}

GetArgs.prototype.GetInputTagType = function(getElement)
{
    componentType = componentName[getElement.type.toString().toLowerCase()];
	if(getElement.type == "text")
    {
       // monkeyId = getElement.id || getElement.name || getElement.title || getElement.value;
       recordEvent.setMonkeyID(getElement);
        actionType = actionTypes[getElement.type.toString().toLowerCase()];
        result = "ComponentType*" + componentType + ";MonkeyId*" + monkeyId + ";Action*" + actionType + ";Args*" + args;
        sendToObjCLib(getElement.tagName, result);
        
    	getElement.onblur = function()
	    {
	        args = getElement.value;
	        console.log("Input ID*"+args);
            result = "ComponentType*" + componentType + ";MonkeyId*" + monkeyId + ";Action*EnterText" + ";Args*" + args;
	        sendToObjCLib(getElement.tagName, result);
	    }
        
    }
    else if(getElement.type == "radio")
    {
        //monkeyId = getElement.id || getElement.name || getElement.title || getElement.value;
        recordEvent.setMonkeyID(getElement);
        actionType = actionTypes[getElement.type.toString().toLowerCase()];
        args = getElement.value;
        console.log("Input ID*"+args);
        result = "ComponentType*" + componentType + ";MonkeyId*" + monkeyId + ";Action*" + actionType + ";Args*" + args;
        sendToObjCLib(getElement.tagName, result);
    }
    else if(getElement.type == "checkbox")
    {
    	getElement.onclick = function()
    	{
	    	if(getElement.checked == true)
	    	{
	    		actionType = "On";
	    	}
	    	else if(getElement.checked == false)
	    	{
	    		actionType = "Off";
	    	}
	    	//monkeyId = getElement.id || getElement.name || getElement.title || getElement.value;
	    	recordEvent.setMonkeyID(getElement);
	    	result = "ComponentType*" + componentType + ";MonkeyId*" + monkeyId + ";Action*" + actionType + ";Args*" + args;
	    	sendToObjCLib(getElement.tagName, result);
    	}
    }
    else if(getElement.type == "button")
	{
        actionType = actionTypes[getElement.type.toString().toLowerCase()];
        recordEvent.setMonkeyID(getElement);
		//monkeyId = getElement.id || getElement.name || getElement.title || getElement.value;
		result = "ComponentType*" + componentType + ";MonkeyId*" + monkeyId + ";Action*" + actionType + ";Args*" + args;
		sendToObjCLib(getElement.tagName, result);
	}

}

GetArgs.prototype.GetParentElement = function(getElement)
{
    if(!getElement.parentNode)
    {
        return;
    }
    else
    {
        if (getElement.parentNode.tagName == "A") 
        {
            return true;   
        }
        else
        {
            recordEvent.GetParentElement(getElement.parentNode);
        }
    }
}

GetArgs.prototype.GetXPath = function(element) {
    if (element.id!=='')
        return 'id("'+element.id+'")';
    if (element===document.body)
        return "html/"+element.tagName;

    var i= 0;
    var siblings= element.parentNode.childNodes;
    for (var y= 0; y<siblings.length; y++) {
        var sibling= siblings[y];
        if (sibling===element)
            return recordEvent.GetXPath(element.parentNode)+'/'+element.tagName+'['+(i+1)+']';
        if (sibling.nodeType===1 && sibling.tagName===element.tagName)
            i++;
    }
}

GetArgs.prototype.setMonkeyID = function(getElement)
{
   monkeyId  = getElement.id || getElement.name || getElement.title || getElement.value || getElement.getAttribute("href") || getElement.innerHTML ||  getElement.class || getElement.alt ||
   getElement.getAttribute("src") || getElement.options || getElement.parentNode.target ||  getElement.parentNode.getAttribute("href") || getElement.parentNode.innerHTML || getElement.parentNode.name;
}

//Component Type Array
componentName = {
    a : "Link",
    button : "Button",
    select : "ItemSelector",
    table : "Table",
    td : "Table",
    th : "Table",
    textarea : "TextArea",
    input : "Input",
    span : "Link",
    div : "Link",
    img : "Image",
    textbox : "Browser",
    toolbarbutton : "Browser",
    div : "Label",
    checkbox : "CheckBox",
    radio : "RadioButtons",
    text :"Input",
    h1 : "Link",
    h2 : "Link",
    h3 : "Link",
    h4 : "Link",
    h5 : "Link",
    h6 : "Link"
};

//Action Type Array
actionTypes = {
	a : "Click",
	button : "Click",
	span : "Tap",
	textarea : "Tap",
	text : "Tap",
	label : "Click",
	div : "Tap",
	img : "Tap",
	select : "Select",
	radio : "Select",
	h1 : "Click",
	h2 : "Click",
	h3 : "Click",
	h4 : "Click",
	h5 : "Click",
	h6 : "Click"

}

document.onmouseup = function (event) { recordEvent.GetMonkeyEvent(event)};
