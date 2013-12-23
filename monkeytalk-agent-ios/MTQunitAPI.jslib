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

// MTQunitAPI.jslib
// MonkeyTalk
//
// Copyright 2011 Gorilla Logic, Inc. All rights reserved.
//
function CommandList() {
this.count  = 0;
this.dataCount  = 0;
this.array  = new Array();
this.add  = function()  {
var command  = "";
var component  = "";
var monkeyId  = "";
var args  = new Array();
for(var i = 0; i < this.add.arguments.length; i++) {
MTObjConnect.connect(this.add.arguments[i]);
if (i  == 0) command  = this.add.arguments[i];
else if (i  == 1) component  = this.add.arguments[i];
else if (i  == 2) monkeyId  = this.add.arguments[i];
else args[i - 3]  = this.add.arguments[i];
} if (command  == "DataDrive") {
this.driverCount();
} this.array[this.count]  = [command,component,monkeyId, "500", "0",args];
this.count++;
};
this.addRetry  = function()  {
var command  = "";
var component  = "";
var monkeyId  = "";
var playback  = "";
var timeout  = "";
var args  = new Array();
for(var i = 0; i < this.addRetry.arguments.length; i++) {
MTObjConnect.connect(this.addRetry.arguments[i]);
if (i  == 0) command  = this.addRetry.arguments[i];
else if (i  == 1) component  = this.addRetry.arguments[i];
else if (i  == 2) monkeyId  = this.addRetry.arguments[i];
else if (i  == 3) playback  = this.addRetry.arguments[i];
else if (i  == 4) timeout  = this.addRetry.arguments[i];
else args[i - 5]  = this.addRetry.arguments[i];
} if (command  == "DataDrive") {
this.driverCount();
} this.array[this.count]  = [command,component,monkeyId,playback,timeout,args];
this.count++;
};
this.addGet  = function(component,monkeyId,property,variable)  {
this.array[this.count]  = ["GetVariable",component,monkeyId, "0", "0",[property,variable]];
this.count++;
};
this.addDriver  = function(file)  {
this.driverCount();
this.array[this.count]  = ["DataDrive","","", "0", "0",[file]];
this.count++;
};
this.driverCount  = function()  {
MTObjConnect.connect("DataDrive","", function (response) {
MT.dataCount  = response;
});
};
this.play  = function(func)  {
MT.wasPlayCalled  = true;
MT.currentList  = this;
MTObjConnect.connect("MTPlayCommands",this.array, function (response) {
if (response  == 'success')  {
ok(true,"Test Successful ");
if (MT.currentCount  < MT.dataCount  - 1)  {
dataDrive  = MTDrive;
MT.wasPlayCalled  = false;
dataDrive();
MT.currentCount++;
} else  {
if (MT.dataCount  > 0) {
MT.currentCount  = 0;
MT.dataCount  = 0;
start();
}
} if (func)  {
MT.wasPlayCalled  = false;
func();
}
} else  {
equals( response, 'success', 'Test Failed ');
}
if (MT.dataCount  == 0  && ( ! func  || MT.wasPlayCalled  == false)) start();
});
};
}
function MTDrive() {
MT.currentList.play();
}
var MT  =  {
commandList: new CommandList()
, wasPlayCalled: new Boolean()
, currentList: new CommandList()
, currentCount: 0
, dataCount: 0
};
// MonkeyTalk API
function MTPlayCommand(command)  {
this.playNext  = false;
MTObjConnect.connect("MTPlayCommand",command, function (response) {
//this.playNext  = true;

MTObjConnect.connect(response,response, function (response) {});

if (MT.dataCount  == 0  && ( ! func  || MT.wasPlayCalled  == false)) start();
});
}
/**
* Definition of the applicaiton class
*/
function MonkeyApplication()  {}
MonkeyApplication.prototype.find  = function(componentType, monkeyId) {
switch(componentType) {
case "Button":return new MonkeyButton(monkeyId);
break;
case "TextField":return new MonkeyTextField(monkeyId);
break;
case "List":return new MonkeyList();
break;
case "DropDownList":return new MonkeyDropDownList();
break;
}
};
/**
* Definition of the root monkey component
*/function MonkeyComponent(monkeyId)  {
this.monkeyId  = monkeyId;
}
MonkeyComponent.prototype.getId  = function() {
alert ('GetId - Not Implemented');
};
MonkeyComponent.prototype.click  = function() {
var argsArray = [];
for(var i = 0; i < arguments.length; i++) {
argsArray[i] = arguments[i];
}

//MTPlayCommand(["Touch", this.monkeyId, delay, retrys, x, y]);
MTPlayCommand(["Touch", this.monkeyId, argsArray]);
//MT.commandList.addRetry("Touch", "", this.monkeyId, delay, retrys, x, y);
}
MonkeyComponent.prototype.enterText  = function() {
var argsArray = [];
for(var i = 0; i < arguments.length; i++) {
argsArray[i] = arguments[i];
}

MTPlayCommand(["InputText", this.monkeyId, argsArray]);
}
MonkeyComponent.prototype.clear  = function() {
var argsArray = [];
for(var i = 0; i < arguments.length; i++) {
argsArray[i] = arguments[i];
}

MTPlayCommand(["Clear", this.monkeyId, argsArray]);
}
MonkeyComponent.prototype.enter  = function() {
var argsArray = [];
for(var i = 0; i < arguments.length; i++) {
argsArray[i] = arguments[i];
}

MTPlayCommand(["Return", this.monkeyId, argsArray]);
}
MonkeyComponent.prototype.verify  = function(expectedValue, propertyPath) {
alert('Verify(' + expectedValue  + '='  + propertyPath  + ') - not implemented');
}
MonkeyComponent.prototype.verifyNot  = function(expectedValue, propertyPath) {
alert('VerifyNot(' + expectedValue  + ','  + propertyPath  + ') - not implemented');
}
MonkeyComponent.prototype.VerifyWildcard  = function(wildcard,propertyPath) {
alert('VerifyWildCard(' + wildcard  + ','  + propertyPath  + ') - not implemented');
}
MonkeyComponent.prototype.VerifyNotWildCard  = function(wildcard,propertyPath) {
alert('VerifyNotWildCard(' + wildcard  + ','  + propertyPath  + ') - not implemented');
}
MonkeyComponent.prototype.VerifyRegex  = function(regex,propertyPath) {
alert('VerifyRegex(' + regex  + ','  + propertyPath  + ') - not implemented');
}
MonkeyComponent.prototype.VerifyImage  = function() {
alert('VerifyImage() - not implemented');
}
MonkeyComponent.prototype.VerifyExists  = function() {
alert('VerifyExists() - not implemented');
}
MonkeyComponent.prototype.VerifyNotExists  = function() {
alert('VerifyNotExist() - not implemented');
}
//Store and Value field
/**
* Definition of components 
*/
function MonkeyButton(monkeyId)  {
// Call the parent constructor 
this.monkeyId  = monkeyId;
MonkeyComponent.call(this, monkeyId);
}
MonkeyButton.prototype  = new MonkeyComponent();
MonkeyButton.prototype.constructor  = MonkeyButton;
function MonkeyTextField(monkeyId)  {
// Call the parent constructor
this.monkeyId  = monkeyId; 
MonkeyComponent.call(this, monkeyId);
}
MonkeyTextField.prototype  = new MonkeyComponent();
MonkeyTextField.prototype.constructor  = MonkeyButton;
function MonkeyList()  {
// Call the parent constructor 
MonkeyComponent.call(this);
}
MonkeyList.prototype  = new MonkeyComponent();
MonkeyList.prototype.constructor  = MonkeyList;
MonkeyList.prototype.select  = function(value) {
alert('Select(' + value + ') - not implemented');
}
MonkeyList.prototype.selectRow  = function(index) {
alert('SelectRow(' + index + ') - not implemented');
}
function MonkeyDropDownList()  {
// Call the parent constructor 
MonkeyList.call(this);
}
MonkeyDropDownList.prototype  = new MonkeyList();
MonkeyDropDownList.prototype.constructor  = MonkeyDropDownList;
