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
package com.gorillalogic.monkeyconsole.tableview;

import java.util.UUID;

import com.gorillalogic.monkeytalk.Command;

public class TableRow extends Command {
   private String guid = UUID.randomUUID().toString();
public TableRow(){
	super();
}
public TableRow(Command c){
	super.setAction(c.getAction());
	super.setArgsAndModifiers(c.getArgsAsString() + " " + c.getModifiersAsString());
	String ctype = c.isComment() ? c.getCommand() : c.getComponentType();
	super.setComponentType(ctype);
	super.setComponentType(c.getComponentType());
	super.setMonkeyId(c.getMonkeyId());
	this.guid = UUID.randomUUID().toString();
}
public String getGuid() {
	return guid;
}

public void setGuid(String guid) {
	this.guid = guid;
}

@Override
public boolean equals(Object obj) {
	if(obj instanceof TableRow && ((TableRow)obj).getGuid().equalsIgnoreCase(guid)){
	return true;
	} else {
		return false;
	}
}

}